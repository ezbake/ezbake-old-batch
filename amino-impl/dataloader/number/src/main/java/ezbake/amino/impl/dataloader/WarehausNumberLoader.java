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

package ezbake.amino.impl.dataloader;

import ezbake.amino.dataloader.WarehausDataLoader;
import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataLoader for loading the Numbers dataset from the data warehaus
 */
public class WarehausNumberLoader extends WarehausDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(WarehausNumberLoader.class);

    private static final Text BUCKET1 = new Text("number");
    private static final Text BUCKET2 = new Text("number2");
    private int number2Max = 10; // TODO - for testing

    static {
        bucketsAndDisplayNames.put(BUCKET1, new Text("number"));
        bucketsAndDisplayNames.put(BUCKET2, new Text("number2"));
    }

    @Override
    public void setConfig(Configuration config) {
        super.setConfig(config);
        number2Max = config.getInt("number2-max", 500);
        logger.info("Number2-max set to " + number2Max);
    }

    /**
     * Method to extract the data from the RAW info.
     *
     * @param rawData   The raw data to process
     * @param outputMap The MapWritable for storing the bucketed values
     */
    @Override
    protected void extractFromRaw(final byte[] rawData, final MapWritable outputMap) {
        final Text data = new Text(rawData);
        outputMap.put(BUCKET1, data);
        int twoVal = Integer.parseInt(data.toString());
        if (twoVal <= number2Max) {
            outputMap.put(BUCKET2, data);
        }
    }

    /**
     * Method to extract the data from the Thrift info.
     *
     * @param rawThrift The raw bytes of the Thrift class to deserialize
     * @param outputMap The MapWritable for storing the bucketed values
     */
    @Override
    protected void extractFromThrift(final byte[] rawThrift, final MapWritable outputMap) {
        logger.error("Thrift extractor is not implemented");
        throw new NotImplementedException();
    }

    /**
     * Get the name of the data source
     *
     * @return the name of the data source
     */
    @Override
    public String getDataSourceName() {
        return "numbers";
    }

    /**
     * Get the name of the data set based on the contents of the MapWritable (you could have more than one type of
     * dataset in the datasource)
     *
     * @return the name of the data set, typically just an explicit string, but if there is more than one dataset in the
     *         datasource, you may want to examine the MapWritable to determine the dataset name.
     */
    @Override
    public String getDataSetName(MapWritable mw) {
        return "numbersDS";
    }

    /**
     * Visibility assigned to this datasource.  This will be in the Accumulo visibility cell.
     */
    @Override
    public String getVisibility() {
        return "U";
    }

    /**
     * Visibility assigned to this datasource.  This will be a human readable string which represents the
     * getVisibility() value, e.g. the Accumulo visibility string.
     */
    @Override
    public String getHumanReadableVisibility() {
        return "UNCLASSIFIED";
    }
}
