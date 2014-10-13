package ezbake.azkaban.job;

import com._42six.amino.api.framework.FrameworkDriver;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Cleans up HDFS by removing directories of a certain age
 */
public class HdfsCleaner {

    private Logger logger = LoggerFactory.getLogger(HdfsCleaner.class);

    protected boolean cleanoutAnalytics = true;
    protected boolean cleanoutBitmaps = true;
    protected long ageOffTimestamp = Long.MAX_VALUE;
    protected int lastN = 1;

    // The delta in time from now to age off directories
    public static final String AGEOFF_PERIOD = "ageoff.period";

    // The absolute timestamp to use for aging off directories
    public static final String AGEOFF_ABSOLUTE = "ageoff.absolute";

    // Keep the last N dirs of a project
    public static final String AGEOFF_KEEP_LAST_N = "ageoff.keep_last_N";

    // Boolean on whether or not to clean out the analytics directory
    public static final String CLEANOUT_ANALYTICS = "cleanout.analytics";

    // Boolean on whether or not to clean out the bitmaps directory
    public static final String CLEANOUT_BITMAPS = "cleanout.bitmaps";



    /**
     * This constructor is called by Azkaban to initialize the class.  The passed in properties are stored for later use
     * and the typical command line arguments are extracted.
     *
     * @param jobId The id of the job being run
     * @param props Any properties that were inherited by the job. Standard program arguments will be in the property
     *              value 'main.args'
     */
    public HdfsCleaner(@SuppressWarnings("UnusedParameters") String jobId, Properties props){
        long startTime;

        // Grab the timestamp in epoch seconds for when this job was started
        try {
            startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(props.getProperty("azkaban.flow.start.timestamp")).getTime();
        } catch (ParseException ex){
            logger.warn("Could not parse start time string. Using current time");
            startTime = System.currentTimeMillis();
        }

        // Compute what age to age off
        final String ageOffPeriod = props.getProperty(AGEOFF_PERIOD);
        final String ageOffAbsolute = props.getProperty(AGEOFF_ABSOLUTE);

        // Figure out how many we need to keep of each
        try{
            lastN = Integer.parseInt(props.getProperty(AGEOFF_KEEP_LAST_N, "1"));
            // Can't have negative keepers
            if(lastN < 0) {
                logger.warn("Can not have negative number of runs to keep.  Keeping 0");
                lastN = 0;
            }
        } catch (NumberFormatException ex){
            logger.warn("Property '{}' is not a valid integer.  Using default of {}", AGEOFF_KEEP_LAST_N, lastN);
        }

        if(lastN > 0){
            logger.info("Keeping the last '{}' runs from each project", lastN);
        }

        // Choose absolute over delta
        if(ageOffAbsolute != null){
            ageOffTimestamp = Long.valueOf(ageOffAbsolute);
        } else if(ageOffPeriod != null){
            ageOffTimestamp = startTime - Long.valueOf(ageOffPeriod);
        }

        logger.info("Pruning directories older than {}", new DateTime(ageOffTimestamp));

        // Check to see which dirs we should be cleaning out
        cleanoutAnalytics = props.getProperty(CLEANOUT_ANALYTICS, "true").compareToIgnoreCase("false") != 0;
        cleanoutBitmaps = props.getProperty(CLEANOUT_BITMAPS, "true").compareToIgnoreCase("false") != 0;
    }

    private void pruneDirectories(final FileSystem fs, Path path) throws IOException {
        // For each of the projects, filter through each of its runs
        for (FileStatus projectNumber : fs.listStatus(path)) {

            // There SHOULD only be directories, but filter just to make sure
            final FileStatus[] runDirs = fs.listStatus(projectNumber.getPath(), new PathFilter() {
                @Override
                public boolean accept(Path path) {
                    try {
                        return fs.isDirectory(path);
                    } catch (IOException e) {
                        logger.error("Error trying to filter directories", e);
                        return false;
                    }
                }
            });

            // Directories are returned canonically. Though this should be good enough, let's sort the right way
            final SortedSet<Long> runSet = new TreeSet<>();
            for(FileStatus status : runDirs){
                try {
                    runSet.add(Long.valueOf(status.getPath().getName()));
                } catch (NumberFormatException ex){
                    logger.error("Directory {} is not a long and probably not a runtime dir. Skipping", status.getPath().getName() );
                }
            }

            // Keep the last N number of runs
            int i = 0;
            final int stopAt = runSet.size() - lastN;
            for(Long runtime : runSet){
                if(i++ >= stopAt){
                    break;
                }

                final Path runtimeDir = new Path(projectNumber.getPath(), runtime.toString());
                logger.info("Checking dir <{}> vs ageoff of <{}>", runtimeDir, ageOffTimestamp);

                // Check if it meets the threshold for pruning and if so delete the dir
                if (runtime <= ageOffTimestamp) {
                    // Check to make sure this isn't the dir of a currently running job
                    try (FSDataInputStream pidStream = fs.open(new Path(runtimeDir, FrameworkDriver.STATUS_FILE))) {
                        if (FrameworkDriver.JobStatus.valueOf(pidStream.readUTF()) == FrameworkDriver.JobStatus.RUNNING) {
                            logger.warn("Directory <{}> is for a currently running job.  Skipping", runtimeDir);
                            continue;
                        }
                    } catch (IOException e){
                        logger.warn("directory {} missing PID file or could not be read.  Skipping", runtimeDir.getName(), e.getMessage());
                        continue;
                    }

                    logger.info("Removing dir: {}", runtimeDir);
                    fs.delete(runtimeDir, true);
                }
            }
        }
    }

    /**
     * Called by the Azkaban hadoopJava type
     */
    @SuppressWarnings("unused")
    public void run(){
        logger.info("Cleaning up old jobs");

        final Configuration conf = new Configuration();
        try(FileSystem fs = FileSystem.get(conf)) {
            if(cleanoutAnalytics){
                pruneDirectories(fs, new Path(EzJobUtil.ANALYTICS_DIR));
            }

            if(cleanoutBitmaps){
                pruneDirectories(fs, new Path(EzJobUtil.BITMAPS_DIR));
            }
        } catch (IOException ex){
            logger.error("Problems pruning directories", ex);
            System.exit(-1);
        }
    }
}
