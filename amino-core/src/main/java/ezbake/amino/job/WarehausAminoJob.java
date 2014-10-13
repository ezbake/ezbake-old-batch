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

package ezbake.amino.job;

import com._42six.amino.api.job.AminoJob;
import com._42six.amino.api.job.AminoReducer;
import com._42six.amino.api.job.JobOutputEstimate;
import com._42six.amino.data.DataLoader;
import com.google.common.base.Preconditions;
import ezbake.amino.dataloader.WarehausDataLoader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.StringUtils;

import java.util.ArrayList;

/**
 * Mapreduce Job for accessing data from the data warehaus
 */
public class WarehausAminoJob implements AminoJob {

    public static final String CFG_JOB_ESTIMATE = "job.estimate";
    protected Configuration config;

    @Override
    public JobOutputEstimate getJobEstimate() {
        Preconditions.checkState(config != null, "Configuration has not been set");
        return config.getEnum(CFG_JOB_ESTIMATE, JobOutputEstimate.MEDIUM);
    }

    @Override
    final public String getJobName() {
        Preconditions.checkState(config != null, "Configuration has not been set");
        return String.format("%s_%s", config.get("application.name",""), config.get("service.name",""));
    }

    @Override
    public Class<? extends DataLoader> getDataLoaderClass() {
        if (config == null) {
            throw new IllegalStateException("WarehausAminoJob was not configured");
        }
        Class<? extends DataLoader> cls = config.getClass(CFG_DATA_LOADER, null, WarehausDataLoader.class);
        if (cls == null) {
            throw new IllegalStateException("Dataloader was not set in the configuration");
        }
        return cls;
    }

    @Override
    public Iterable<Class<? extends AminoReducer>> getAminoReducerClasses() {
        if (config == null) {
            throw new IllegalStateException("WarehausAminoJob was not configured");
        }

        final ArrayList<Class<? extends AminoReducer>> classes = new ArrayList<>();
        try {
            String[] classNames = StringUtils.getStrings(config.get(CFG_REDUCER_CLASSES));
            if (classNames == null) {
                throw new IllegalStateException(CFG_REDUCER_CLASSES + " missing from config");
            }
            for (String classname : classNames) {
                classes.add(config.getClassByName(classname.trim()).asSubclass(AminoReducer.class));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    /**
     * If you want to group multiple jobs/data sources into the same domain of Amino, use this ID to set that domain.
     * If you return an Integer (not null) you must return values for getAminoDomainName and getAminoDomainDescription.
     *
     * @return the domain id to group this job in, if you don't want to group, return null.
     */
    @Override
    public Integer getAminoDomainID() {
        if (config == null) {
            throw new IllegalStateException("WarehausAminoJob was not configured");
        }
        return Integer.parseInt(this.config.get(CFG_DOMAIN_ID, "0"));
    }

    /**
     * The name that describes the AminoDomain (returned from getAminoDomain)
     *
     * @return the domain name that represents the AminoDomain (returned from getAminoDomain)
     */
    @Override
    public String getAminoDomainName() {
        if (config == null) {
            throw new IllegalStateException("WarehausAminoJob was not configured");
        }
        return config.get(CFG_DOMAIN_NAME, "EzBake data source features");
    }

    /**
     * A description of the AminoDomain (returned from getAminoDomain)
     *
     * @return the domain description that represents the AminoDomain (returned from getAminoDomain)
     */
    @Override
    public String getAminoDomainDescription() {
        if (config == null) {
            throw new IllegalStateException("WarehausAminoJob was not configured");
        }
        return config.get(CFG_DOMAIN_DESC, "Features created across all EzBake data sources");
    }

    /**
     * Set the configuration for this AminoJob
     *
     * @param config The Job configuration
     */
    @Override
    public void setConfig(Configuration config) {
        this.config = config;
    }
}
