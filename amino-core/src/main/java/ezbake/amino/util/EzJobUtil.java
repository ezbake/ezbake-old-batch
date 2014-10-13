package ezbake.amino.util;

import com._42six.amino.api.framework.FrameworkDriver;
import com._42six.amino.common.bigtable.TableConstants;
import com.google.common.base.Preconditions;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.DirectoryConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbakehelpers.accumulo.AccumuloHelper;
import ezbakehelpers.hadooputils.HadoopConfigurationUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Base class for all of the Amino bitmap related jobs.  This takes care of loading up the parameters via EzConfiguration
 * before calling the non EzBake centric code.
 */
public class EzJobUtil {

    private static Logger logger = LoggerFactory.getLogger(EzJobUtil.class);

    public static final String ANALYTICS_DIR = "/ezbatch/amino/analytics/";
    public static final String BITMAPS_DIR = "/ezbatch/amino/bitmaps/";

    /**
     * Traverses through the AminoConfiguration.BASE_DIR and finds the most recent successful run for each project
     *
     * @param conf Hadoop Configuration
     * @return Set of the most recent successful run for each project
     * @throws IOException
     */
    public static Set<Path> findMostRecentRuns(Configuration conf) throws IOException {
        return findMostRecentRuns(conf, ANALYTICS_DIR);
    }

    /**
     * Traverses through the AminoConfiguration.BASE_DIR and finds the most recent successful run for each project
     *
     * @param conf Hadoop Configuration
     * @param baseDir The base directory of all the jobs
     * @return Set of the most recent successful run for each project
     * @throws IOException
     */
    public static Set<Path> findMostRecentRuns(Configuration conf, String baseDir) throws IOException {
        Preconditions.checkNotNull(conf);
        Preconditions.checkNotNull(baseDir, "The basedir is missing");
        final Set<Path> mostRecentRuns = new HashSet<>();

        try(FileSystem fs = FileSystem.get(conf)) {
            // Iterate through all of the project job dirs
            for (FileStatus project : fs.listStatus(new Path(baseDir))) {
                long newestRun = Long.MIN_VALUE;
                Path mostRecentRun = null;

                // For each project check to see if the run is complete and a success, and if so grab the most recent one
                for (FileStatus jobRunDir : fs.listStatus(project.getPath())) {
                    try {
                        final Path pidFile = new Path(jobRunDir.getPath(), FrameworkDriver.STATUS_FILE);
                        FrameworkDriver.JobStatus jobStatus;

                        try (FSDataInputStream pidStream = fs.open(pidFile)) {
                            jobStatus = FrameworkDriver.JobStatus.valueOf(pidStream.readUTF());
                        }

                        if (jobStatus == FrameworkDriver.JobStatus.COMPLETE) {
                            long currentPidTime = Long.valueOf(jobRunDir.getPath().getName());
                            if (currentPidTime > newestRun) {
                                newestRun = currentPidTime;
                                mostRecentRun = jobRunDir.getPath();
                            }
                        }
                    } catch (IOException ex) {
                        logger.warn("Could not load status PID for '" + jobRunDir.getPath().toString() + "'");
                    }
                }
                if (mostRecentRun != null) {
                    logger.info("Adding data path: " + mostRecentRun.getName());
                    mostRecentRuns.add(mostRecentRun);
                }
            }
        }

        return mostRecentRuns;
    }

    /**
     * Loads the configurations specific to EzBake into the Amino configuration.  Will also pull the application.properties
     * from the jar and put it in the Configuration
     *
     * @param configToAddTo The Configuration to add the config values to
     */
    public static void loadEzConfigurations(Configuration configToAddTo) throws EzConfigurationLoaderException {

            final EzConfiguration ezConfiguration = new EzConfiguration(new DirectoryConfigurationLoader(),
                    new ClasspathConfigurationLoader("/application.properties"));
            loadEzConfigurations(ezConfiguration.getProperties(), configToAddTo);
    }

    /**
     * Loads the configurations specific to EzBake into the Amino configuration
     *
     * @param ezConfiguration The EZConfiguration to pull values from
     * @param configToAddTo The Configuration to add the config values to
     */
    public static void loadEzConfigurations(Properties ezConfiguration, Configuration configToAddTo)  {
        Preconditions.checkState(!ezConfiguration.isEmpty(), "EzConfiguration was empty!");
        Preconditions.checkNotNull(configToAddTo);

        // Overwrite all of the Accumulo connection information as it was provided by EzConfig
        final AccumuloHelper accumuloHelper = new AccumuloHelper(ezConfiguration);
        configToAddTo.set(TableConstants.CFG_INSTANCE, accumuloHelper.getAccumuloInstance());
        configToAddTo.set(TableConstants.CFG_ZOOKEEPERS, accumuloHelper.getAccumuloZookeepers());
        configToAddTo.set(TableConstants.CFG_USER, accumuloHelper.getAccumuloUsername());
        configToAddTo.set(TableConstants.CFG_PASSWORD, accumuloHelper.getAccumuloPassword());

        com._42six.amino.common.HadoopConfigurationUtils.mergeConfs(configToAddTo,
                HadoopConfigurationUtils.configurationFromProperties(ezConfiguration));
        logger.info("Loaded EzConfiguration information into the Amino configs");
    }
}
