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

package ezbake.amino.api;

import com._42six.amino.api.framework.FrameworkDriver;
import com._42six.amino.common.AminoConfiguration;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.DirectoryConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbakehelpers.hadooputils.HadoopConfigurationUtils;
import ezbake.amino.util.EzJobUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * EzBake wrapper around the Amino FrameworkDriver
 */
public class EzFrameworkDriver extends Configured implements Tool {

    protected Configuration conf;
    protected Properties properties;
    protected String[] args;
    protected String startTimestamp;
    protected String projectId;

    private static Logger logger = LoggerFactory.getLogger(EzFrameworkDriver.class);

    /**
     * This constructor is called by Azkaban to initialize the class.  The passed in properties are stored for later use
     * and the typical command line arguments are extracted.
     *
     * @param jobId The id of the job being run
     * @param props Any properties that were inherited by the job. Standard program arguments will be in the property
     *              value 'main.args'
     */
    public EzFrameworkDriver(String jobId, Properties props){
        logger.info("Azkaban initalizing EzFrameworkDriver");
        try {
            startTimestamp = Long.toString((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(props.getProperty("azkaban.flow.start.timestamp")).getTime()));
        } catch (ParseException ex){
            logger.warn("Could not parse start time string. Using current time");
            startTimestamp = String.valueOf(Calendar.getInstance().getTime());
        }
        this.projectId = props.getProperty("azkaban.flow.projectid");
        this.args = props.getProperty("main.args").split(" ");
        this.properties = props;

        Properties ezProperties;
        try {
            ezProperties = new EzConfiguration(new DirectoryConfigurationLoader(),
                    new ClasspathConfigurationLoader("/application.properties")).getProperties();
        } catch(EzConfigurationLoaderException e) {
            logger.error("Could not load ezconfiguration", e);
            throw new RuntimeException(e);
        }

        this.conf = HadoopConfigurationUtils.configurationFromProperties(ezProperties);
        try {
            // TODO - Fix this.  Used in the EzFrameworkDriver and EzAzkabanBitmapJobLoader
            conf.set(AminoConfiguration.OUTPUT_DIR, EzJobUtil.ANALYTICS_DIR + projectId + "/" + startTimestamp + "/");
            conf.set(AminoConfiguration.BASE_DIR,  EzJobUtil.ANALYTICS_DIR + projectId + "/" + startTimestamp + "/");
            logger.info("Set HDFS base dir to: " + EzJobUtil.ANALYTICS_DIR + projectId + "/" + startTimestamp + "/");
            FrameworkDriver.initalizeConf(this.conf, this.args);
            AminoConfiguration.createDirConfs(conf);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Azkaban's runner will call the no parameter run method.  Typical command line arguments are passed in the
     * constructor and are stored in the 'args' variable.
     */
    public void run() {
        int res;
        try {
            // Set the umask so that when we create the directories they will be readable by the group so they can be deleted later
            conf.set("fs.permissions.umask-mode", "002");

            res = ToolRunner.run(this.conf, new FrameworkDriver(), this.args);
        } catch (Exception e) {
            logger.error("Problems running the job: ", e);
            res = -1;
        }
        System.exit(res);
    }

    public static void main(String[] args) throws Exception{
        final Configuration conf = new Configuration();

        FrameworkDriver.initalizeConf(conf, args);

        int res = ToolRunner.run(conf, new FrameworkDriver(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] strings) throws Exception {
        logger.error("Not implemented");
        return -1;
    }
}
