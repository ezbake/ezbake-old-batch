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

package ezbake.amino.dataloader;

import com._42six.amino.common.bigtable.impl.AccumuloDataLoader;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.configuration.PropertiesConfigurationLoader;
import ezbake.ins.thrift.gen.InternalNameService;
import ezbake.ins.thrift.gen.InternalNameServiceConstants;
import ezbake.ins.thrift.gen.JobRegistration;
import ezbake.security.client.EzSecurityTokenWrapper;
import ezbake.security.client.EzbakeSecurityClient;
import ezbake.security.serialize.VisibilitySerialization;
import ezbake.security.serialize.thrift.VisibilityWrapper;
import ezbake.thrift.ThriftClientPool;
import ezbake.thrift.ThriftUtils;
import ezbake.warehaus.VersionControl;
import ezbakehelpers.accumulo.AccumuloHelper;
import ezbakehelpers.hadooputils.HadoopConfigurationUtils;
import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.client.mapreduce.lib.impl.InputConfigurator;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.TimestampFilter;
import org.apache.accumulo.core.iterators.user.WholeRowIterator;
import org.apache.accumulo.core.util.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.thrift.TException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public abstract class WarehausDataLoader extends AccumuloDataLoader {

    private static final String WAREHAUS_TABLE = "warehaus.oneaa_warehaus";
    public static final String CFG_DATA_TYPE = "warehausDataLoader.dataType";
    public static final String CFG_URI_PREFIX = "warehausDataLoader.datasource.uri";
    private static final Logger logger = LoggerFactory.getLogger(WarehausDataLoader.class);
    private ProcessData processor;
    private DATA_TYPE dataType;
    private String dataSourceURI;

    private ThriftClientPool clientPool = null;

    // time range
    public static final String CFG_TIME_RANGE_TYPE = "warehausDataLoader.timeRange.Type";

    // for EXPLICIT_RANGE and START_DATE
    public static final String CFG_TIME_RANGE_START_DATE = "warehausDataLoader.timeRange.startDate";
    public static final String CFG_TIME_RANGE_START_DATE_INCLUSIVE = "warehausDataLoader.timeRange.startDateInclusive";
    public static final String CFG_TIME_RANGE_END_DATE = "warehausDataLoader.timeRange.endDate";
    public static final String CFG_TIME_RANGE_END_DATE_INCLUSIVE = "warehausDataLoader.timeRange.endDateInclusive";

    // for RECENT_PERIOD
    public static final String CFG_TIME_RANGE_RECENT_MAGNITUDE = "warehausDataLoader.timeRange.recentPeriodMagnitude";
    public static final String CFG_TIME_RANGE_RECENT_UNIT = "warehausDataLoader.timeRange.recentPeriodUnit";

    private long timestampStart = -1;
    private boolean timestampStartInclusive;
    private long timestampEnd;
    private boolean timestampEndInclusive;

    private String formalAuths = null;

    @Override
    public void setConfig(Configuration config) {
        super.setConfig(config);
        config.set(AccumuloDataLoader.CFG_TABLE, WAREHAUS_TABLE);

        // Merge the application.properties into the Configuration
        final Properties ezProperties;
        try {
            ezProperties = new EzConfiguration(
                new PropertiesConfigurationLoader(HadoopConfigurationUtils.propertiesFromConfiguration(config)),
                new ClasspathConfigurationLoader("/application.properties")).getProperties();
        } catch (EzConfigurationLoaderException e) {
            logger.error("Could not load the Ezconfigurations", e);
            throw new IllegalStateException(e);
        }

        // Convert the EzConfig Accumulo connection information to what the dataloader understands
        final AccumuloHelper accumuloHelper = new AccumuloHelper(ezProperties);
        config.set(AccumuloDataLoader.CFG_INSTANCE,
                Preconditions.checkNotNull(accumuloHelper.getAccumuloInstance(), "Instance missing from EZ config"));
        config.set(AccumuloDataLoader.CFG_ZOOKEEPERS,
                Preconditions.checkNotNull(accumuloHelper.getAccumuloZookeepers(), "Zookeepers missing from EZ config"));
        config.set(AccumuloDataLoader.CFG_USERNAME,
                Preconditions.checkNotNull(accumuloHelper.getAccumuloUsername(), "Username missing from EZ config"));
        config.set(AccumuloDataLoader.CFG_PASSWORD,
                Preconditions.checkNotNull(accumuloHelper.getAccumuloPassword(), "Password missing from EZ config"));

        EzbakeSecurityClient securityClient = null;
        InternalNameService.Client insClient = null;
        try {
            if(clientPool == null){
                clientPool = new ThriftClientPool(ezProperties);
            }

            securityClient = new EzbakeSecurityClient(ezProperties, clientPool);
            final EzSecurityTokenWrapper tokenWrapper = securityClient.fetchAppToken(clientPool.getSecurityId(InternalNameServiceConstants.SERVICE_NAME));

            // Use EzbakeSecurity to determine what the application's Accumulo Authorizations are
            config.set(AccumuloDataLoader.CFG_AUTHS,
                    Joiner.on(",").join(tokenWrapper.getAuthorizations().getFormalAuthorizations()));
            logger.info("Setting auths to: " + config.get(AccumuloDataLoader.CFG_AUTHS));
            final EzbakeSecurityClient client = new EzbakeSecurityClient(ezProperties);
            formalAuths = Joiner.on(",").join(client.fetchAppToken().getAuthorizations().getFormalAuthorizations());
            config.set(AccumuloDataLoader.CFG_AUTHS, formalAuths);

            // Grab the URI prefixes for the warehaus.   If one is provided in the config, use that, otherwise call out
            // to INS and figure out what the prefix is for each job
            dataSourceURI = config.get(CFG_URI_PREFIX);
            if(dataSourceURI == null){
                logger.info("Calling out to INS to get URI prefixes");
                insClient = clientPool.getClient("ins", InternalNameService.Client.class);
                final Set<JobRegistration> jobRegistrations = insClient.getJobRegistrations(tokenWrapper.getApplicationSecurityId(), tokenWrapper);
                if(jobRegistrations.isEmpty()){
                    throw new IllegalStateException("There are no Amino Job registrations for the application");
                }

                final List<String> uris = new ArrayList<>(jobRegistrations.size());
                for(JobRegistration job : jobRegistrations){
                    String uriPrefix = job.getUriPrefix().trim();
                    if(uriPrefix.endsWith("/")){
                        uriPrefix = uriPrefix.substring(0, uriPrefix.length()-1);
                    }
                    uris.add(uriPrefix);
                    logger.info("Adding datasource URI {} for job {}", job.getUriPrefix(), job.getJobName());
                }

                if(!uris.isEmpty()){
                    dataSourceURI = Joiner.on(",").join(uris);
                } else {
                    logger.error("Could not find a valid URI prefix!  Aborting");
                    throw new IllegalStateException("URI prefix not found");
                }
            } else {
                logger.info("Loading URI prefix from config");
                // Strip off any extra characters that might be in the prefix
                dataSourceURI = dataSourceURI.trim();
                if(dataSourceURI.endsWith("/")){
                    dataSourceURI = dataSourceURI.substring(0, dataSourceURI.length()-1);
                }
                logger.info("Setting datasource URI to {}", dataSourceURI);
            }
        } catch (TException e) {
            throw new IllegalStateException(e);
        } finally {
            if(securityClient != null){
                try {
                    securityClient.close();
                } catch (IOException e) {
                    logger.warn("Could not close security client", e);
                }
            }
            if(clientPool != null){
                clientPool.returnToPool(insClient);
                clientPool.close();
            }
        }

        // Get the data type and make sure it's legit
        final String dataTypeCfg = config.get(CFG_DATA_TYPE, DATA_TYPE.PARSED.toString());
        try {
            dataType = DATA_TYPE.valueOf(dataTypeCfg.trim().toUpperCase(Locale.ENGLISH)); // This can throw an IllegalArgumentException
        } catch (IllegalArgumentException ex) {
            logger.error("Data type of {} is invalid.  Must be one of {}", dataTypeCfg, DATA_TYPE.values());
            throw ex;
        }

        ////////////////////
        // UGH.  This is an ugly "hack" as Java 7 and below doesn't have closures/function pointers.  If we ever get to use
        // Java 8, update this logic
        // Set the processor for appropriate data type.  Normally this would be done with a closure/function pointer,
        // but Java 7 and below doesn't have those, so we need to do this hack in the mean time.  This is to avoid the
        // unnecessary overhead of a switch statement in the for loop of the processWholeRow method (which is silly to
        // use since the method will be the same one every pass through the loop.  If there's a better way to do this,
        // please feel free to update this ugly code
        ////////////////////

        switch (dataType) {
            case RAW:
                processor = new ProcessData() {
                    @Override
                    public void process(byte[] rawData, MapWritable outputMap) throws IOException {
                        extractFromRaw(rawData, outputMap);
                    }
                };
                logger.info("Created processor for RAW data");
                break;
            case PARSED:
                processor = new ProcessData() {
                    @Override
                    public void process(byte[] rawData, MapWritable outputMap) throws IOException {
                        final VisibilityWrapper wrapper;
                        final VersionControl vc;

                        try {
                            wrapper = VisibilitySerialization.deserializeVisibilityWrappedBytes(rawData);
                            vc = ThriftUtils.deserialize(VersionControl.class, wrapper.getValue());
                        } catch (TException e) {
                            logger.error("Problems deserializing thrift", e);
                            throw new IOException(e);
                        }
                        extractFromThrift(vc.getPacket(), outputMap);
                    }
                };
                logger.info("Created processor for Thrift data");
                break;
            default:
                throw new IllegalStateException("Processor not set up for type " + dataType.toString());
        }

        // Check to see if there is any temporal configurations and config as needed
        final String timeRangeCfg = config.get(CFG_TIME_RANGE_TYPE);
        if(timeRangeCfg != null && !timeRangeCfg.isEmpty()) {
            final TIME_RANGE_TYPE timeRangeType = TIME_RANGE_TYPE.valueOf(timeRangeCfg.trim().toUpperCase(Locale.ENGLISH));
            switch (timeRangeType) {
                case EXPLICIT_RANGE:
                    timestampStart = Long.parseLong(config.get(CFG_TIME_RANGE_START_DATE));
                    timestampStartInclusive = Boolean.parseBoolean(config.get(CFG_TIME_RANGE_START_DATE_INCLUSIVE, "true"));
                    timestampEnd = Long.parseLong(config.get(CFG_TIME_RANGE_END_DATE));
                    timestampEndInclusive = Boolean.parseBoolean(config.get(CFG_TIME_RANGE_END_DATE_INCLUSIVE, "true"));
                    break;
                case START_DATE:
                    timestampStart = Long.parseLong(config.get(CFG_TIME_RANGE_START_DATE));
                    timestampStartInclusive = Boolean.parseBoolean(config.get(CFG_TIME_RANGE_START_DATE_INCLUSIVE, "true"));
                    timestampEnd = DateTime.now().getMillis();
                    timestampEndInclusive = true;
                    break;
                case RECENT_PERIOD:
                    final int magnitude = Integer.parseInt(config.get(CFG_TIME_RANGE_RECENT_MAGNITUDE));
                    final TIME_RANGE_UNITS units = TIME_RANGE_UNITS.valueOf(config.get(CFG_TIME_RANGE_RECENT_UNIT).trim().toUpperCase(Locale.ENGLISH));
                    switch (units) {
                        case MINUTES:
                            timestampStart = DateTime.now().minusMinutes(magnitude).getMillis();
                            break;
                        case HOURS:
                            timestampStart = DateTime.now().minusHours(magnitude).getMillis();
                            break;
                        case DAYS:
                            timestampStart = DateTime.now().minusDays(magnitude).getMillis();
                            break;
                        case MONTHS:
                            timestampStart = DateTime.now().minusMonths(magnitude).getMillis();
                            break;
                        case YEARS:
                            timestampStart = DateTime.now().minusYears(magnitude).getMillis();
                            break;
                    }
                    timestampStartInclusive = Boolean.parseBoolean(config.get(CFG_TIME_RANGE_START_DATE_INCLUSIVE, "true"));
                    timestampEnd = DateTime.now().getMillis();
                    timestampEndInclusive = true;
                    break;
            }
        }

    }

    @Override
    /**
     * Initialize the AccumuloInputFormat so that it fetches the rows that we are interested in.  Also sets up the
     * appropriate processing methods based on the type of data to process
     */
    public void initializeFormat(Job job) throws IOException {
        // Set the columns on the AccumuloInputFormat to only fetch what we need
        final List<Pair<Text, Text>> columns = new ArrayList<>(1);
        columns.add(new Pair<>(new Text(dataSourceURI), new Text(dataType.toString())));
        logger.info("Fetching columns => {} : {}", columns.get(0).getFirst().toString(),
                columns.get(0).getSecond().toString());
        AccumuloInputFormat.fetchColumns(job, columns);

        for(IteratorSetting is : InputConfigurator.getIterators(org.apache.accumulo.core.client.mapred.AccumuloInputFormat.class, job.getConfiguration())){
            logger.warn(is.toString());
            if(is.getName().equals("TemporalQueryTimestampFilter")){
                logger.error("Format already initialized!!");
                return; // TODO - Fix this
            }
        }

        // If searching temporally, configure the iterator
        if(timestampStart != -1){

            final IteratorSetting is = new IteratorSetting(1, "TemporalQueryTimestampFilter", TimestampFilter.class);
            TimestampFilter.setRange(is, timestampStart, timestampStartInclusive, timestampEnd, timestampEndInclusive);
            logger.info("Setting timestamp range: {}({}) to {}({})", timestampStart, timestampStartInclusive,
                    timestampEnd, timestampEndInclusive);
            AccumuloInputFormat.addIterator(job, is);
        }

        super.initializeFormat(job);
    }

    /**
     * Process a whole row from the WholeRowIterator.
     *
     * @param key   The Key returned from the WholeRowIterator
     * @param value The Value returned from the WholeRowIterator
     * @return MapWritable with all of the bucketed values
     */
    @Override
    protected MapWritable processWholeRow(Key key, Value value) throws IOException {
        final MapWritable outputMap = new MapWritable();

        for (Value v : WholeRowIterator.decodeRow(key, value).values()) {
            if (v == null) {
                logger.warn("Value was NULL for key: " + key.toString());
                continue;
            }
            processor.process(v.get(), outputMap);
        }
        return outputMap;
    }

    /**
     * Visibility assigned to this datasource.  This will be in the Accumulo visibility cell.
     */
    @Override
    public String getVisibility() {
        Preconditions.checkNotNull(formalAuths, "The visibilities have not been set yet");
        return formalAuths;
    }

    /**
     * Visibility assigned to this datasource.  This will be a human readable string which represents the
     * getVisibility() value, e.g. the Accumulo visibility string.
     */
    @Override
    public String getHumanReadableVisibility() {
        Preconditions.checkNotNull(formalAuths, "The visibilities have not been set yet");
        return formalAuths; // TODO - this isn't really human readable, but not sure how to get the HR form
    }


    /**
     * Method to extract the data from the RAW info.
     *
     * @param rawData   The raw data to process
     * @param outputMap The MapWritable for storing the bucketed values
     * @throws java.io.IOException
     */
    protected abstract void extractFromRaw(final byte[] rawData, final MapWritable outputMap) throws IOException;

    /**
     * Method to extract the data from the Thrift info.
     *
     * @param rawThrift The raw bytes of the Thrift class to deserialize
     * @param outputMap The MapWritable for storing the bucketed values
     * @throws java.io.IOException
     */
    protected abstract void extractFromThrift(final byte[] rawThrift, final MapWritable outputMap) throws IOException;

    public enum DATA_TYPE {
        RAW,
        PARSED
    }

    public enum TIME_RANGE_TYPE {
        EXPLICIT_RANGE,
        START_DATE,
        RECENT_PERIOD
    }

    public enum TIME_RANGE_UNITS {
        MINUTES,
        HOURS,
        DAYS,
        MONTHS,
        YEARS
    }

    private interface ProcessData {
        void process(final byte[] rawData, final MapWritable outputMap) throws IOException;
    }

}
