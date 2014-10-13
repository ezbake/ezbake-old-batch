/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.amino.job.bitmap;

import com._42six.amino.api.framework.FrameworkDriver;
import com._42six.amino.bitmap.BitmapJob;
import com._42six.amino.common.AminoConfiguration;
import com.google.common.base.Preconditions;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * Base class for a Hadoop job that is run on EzBake's Azkaban
 */
public class EzAzkabanBitmapJobLoader {
    private Properties properties;
    protected String[] args;
    protected String bitmapClass;
    protected String execId;
    protected String startTimestamp;
    protected String projectId;

    private Logger logger = Logger.getLogger(EzAzkabanBitmapJobLoader.class);

    /**
     * This constructor is called by Azkaban to initialize the class.  The passed in properties are stored for later use
     * and the typical command line arguments are extracted.
     *
     * @param jobId The id of the job being run
     * @param props Any properties that were inherited by the job. Standard program arguments will be in the property
     *              value 'main.args'
     */
    public EzAzkabanBitmapJobLoader(String jobId, Properties props){
        this.properties = props;
        args = props.getProperty("main.args").split(" ");
        execId = props.getProperty("azkaban.flow.execid");
        try {
            startTimestamp = Long.toString((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(props.getProperty("azkaban.flow.start.timestamp")).getTime()));
        } catch (ParseException ex){
            logger.warn("Could not parse start time string. Using current time");
            startTimestamp = String.valueOf(Calendar.getInstance().getTime());
        }

        projectId = props.getProperty("azkaban.flow.projectid");

        bitmapClass = Preconditions.checkNotNull(properties.getProperty("BitmapJob.class"));
        logger.info("Initializing " + bitmapClass + " as part of JOB: " + jobId);
    }

    /**
     * Azkaban's runner will call the no parameter run method.  Typical command line arguments are passed in the
     * constructor and are stored in the 'args' variable.
     */
    @SuppressWarnings("unused")
    public void run() {
        final Configuration conf = new Configuration();
        final String jobDir = EzJobUtil.BITMAPS_DIR + projectId + "/" + startTimestamp + "/";
        Class<? extends BitmapJob> bitmapJobClass;
        int res;

        try {
            // Load up any EzBake specific configurations (Accumulo info, etc)
            EzJobUtil.loadEzConfigurations(conf);

            // Set the umask so that when we create the directories they will be readable by the group so they can be deleted later
            conf.set("fs.permissions.umask-mode", "002");

            // Set the base dir for the current job so we know where to make the working dir, etc
            conf.set(AminoConfiguration.BASE_DIR, jobDir);
            logger.info("Set job base dir to: "  + jobDir);

            // Find the most recent run of all the different jobs
            final String baseDirs = StringUtils.join(",", EzJobUtil.findMostRecentRuns(conf));
            conf.set(AminoConfiguration.OUTPUT_DIR, baseDirs);
            logger.info("Set most recent analytic runs: " + baseDirs);

            // Figure out which job to load and run it
            bitmapJobClass = Class.forName(bitmapClass).asSubclass(BitmapJob.class);
            conf.set("mapreduce.job.name", String.format("%s_%s_%s", conf.get("application.name", "Batch_Bitmaps"),
                    conf.get("service.name", "bitmapProcessing"), bitmapJobClass.getSimpleName()));
            res = ToolRunner.run(conf, bitmapJobClass.newInstance(), args);
        } catch (ClassNotFoundException e) {
            logger.error("Could not find the class: " + properties.getProperty("BitmapJob.class",
                    "<property BitmapJob.class not found in properties"));
            try{
                FrameworkDriver.updateStatus(conf, FrameworkDriver.JobStatus.FAILED, new Path(jobDir));
            } catch (IOException ex){
                // Swallow exception
            }
            res = -1;
        } catch (Exception e) {
            logger.error("Could not instantiate the class", e);
            try{
                FrameworkDriver.updateStatus(conf, FrameworkDriver.JobStatus.FAILED, new Path(jobDir));
            } catch (IOException ex){
                // Swallow exception
            }
            res = -1;
        }

        System.exit(res);
    }
}
