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

import com._42six.amino.api.job.AminoReducer;
import com._42six.amino.api.job.JobOutputEstimate;
import com._42six.amino.api.model.DatasetCollection;
import com._42six.amino.common.AminoWritable;
import ezbake.amino.dataloader.WarehausDataLoader;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class WarehauseAminoJobTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetJobEstimate() {
        WarehausAminoJob job = new TestClass();
        job.setConfig(new Configuration());
        Assert.assertEquals(JobOutputEstimate.MEDIUM, job.getJobEstimate());
    }

    @Test
    public void getDataLoaderClass_WarehausDataLoader() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DATA_LOADER, "ezbake.amino.dataloader.WarehausDataLoader");
        job.setConfig(configuration);
        Assert.assertEquals(WarehausDataLoader.class, job.getDataLoaderClass());
    }

    @Test
    @Ignore
    public void getDataLoaderClass_WarehausDataLoader_subclass() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DATA_LOADER, "ezbake.amino.job.WarehauseAminoJobTest.SubDataLoader");
        job.setConfig(configuration);
        Assert.assertEquals(WarehausDataLoader.class, job.getDataLoaderClass());
    }

    @Test(expected = IllegalStateException.class)
    public void getDataLoaderClass_config_not_set() {
        final WarehausAminoJob job = new TestClass();
        Assert.assertNull(job.getDataLoaderClass());
    }

    @Test(expected = IllegalStateException.class)
    public void getDataLoaderClass_missingFromConfig() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        job.setConfig(configuration);
        job.getDataLoaderClass();
    }

    @Test(expected = RuntimeException.class)
    public void getDataLoaderClass_not_sub_to_WarehauseLoader() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DATA_LOADER, "java.lang.Object");
        job.setConfig(configuration);
        Assert.assertEquals(WarehausDataLoader.class, job.getDataLoaderClass());
    }

    @Test
    public void getAminoReducerClasses_good() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_REDUCER_CLASSES,
                "ezbake.amino.job.WarehauseAminoJobTest$Reducer1 , ezbake.amino.job.WarehauseAminoJobTest$Reducer2");
        job.setConfig(configuration);
        job.getAminoReducerClasses();
        int count = 0;
        for (Class<? extends AminoReducer> c : job.getAminoReducerClasses()) {
            count++;
            if ((c != Reducer1.class) && (c != Reducer2.class)) {
                Assert.fail("Did not expect class: " + c);
            }
        }
        Assert.assertEquals(2, count);
    }

    @Test(expected = IllegalStateException.class)
    public void getAminoReducerClasses_missingFromConfig() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        job.setConfig(configuration);
        Assert.assertNull(job.getAminoReducerClasses());
    }

    @Test(expected = ClassCastException.class)
    public void getAminoReducerClasses_nonAminoReducer() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_REDUCER_CLASSES,
                "ezbake.amino.job.WarehauseAminoJobTest$Reducer1 , java.lang.Object");
        job.setConfig(configuration);
        job.getAminoReducerClasses();
    }

    @Test
    public void getAminoReducerClasses_missingClass() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("ezbake.amino.BogusClass");

        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_REDUCER_CLASSES,
                "ezbake.amino.job.WarehauseAminoJobTest$Reducer1 , ezbake.amino.BogusClass");
        job.setConfig(configuration);
        job.getAminoReducerClasses();
    }

    @Test
    public void getAminoDomainID_all_set() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_NAME, "Test Name");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_DESC, "Test Description");
        job.setConfig(configuration);
        Assert.assertEquals(new Integer(12345), job.getAminoDomainID());
    }

    @Test
    public void getAminoDomainID_domainName_missing() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_DESC, "Test Description");
        job.setConfig(configuration);
        Assert.assertEquals(12345, (long) job.getAminoDomainID());
    }

    @Test
    public void getAminoDomainID_domainDescription_missing() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_NAME, "Test Name");
        job.setConfig(configuration);
        Assert.assertEquals(12345, (long) job.getAminoDomainID());
    }

    @Test
    public void getAminoDomainName_set() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_NAME, "Test Name");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_DESC, "Test Description");
        job.setConfig(configuration);
        Assert.assertEquals("Test Name", job.getAminoDomainName());
    }

    @Test
    public void getAminoDomainName_not_set() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_DESC, "Test Description");
        job.setConfig(configuration);
        Assert.assertEquals("EzBake data source features", job.getAminoDomainName());
    }

    @Test
    public void getAminoDomainDescription_set() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_NAME, "Test Name");
        configuration.set(WarehausAminoJob.CFG_DOMAIN_DESC, "Test Description");
        job.setConfig(configuration);
        Assert.assertEquals("Test Description", job.getAminoDomainDescription());
    }

    @Test
    public void getAminoDomainDescription_not_set() {
        final WarehausAminoJob job = new TestClass();
        final Configuration configuration = new Configuration();
        configuration.set(WarehausAminoJob.CFG_DOMAIN_ID, "12345");
        job.setConfig(configuration);
        Assert.assertEquals("Features created across all EzBake data sources", job.getAminoDomainDescription());
    }

    public static class Reducer1 implements AminoReducer {
        public Iterable<AminoWritable> reduce(DatasetCollection foo) {
            return null;
        }

        public void setConfig(Configuration config) {
        }
    }

    public static class Reducer2 implements AminoReducer {
        public Iterable<AminoWritable> reduce(DatasetCollection foo) {
            return null;
        }

        public void setConfig(Configuration config) {
        }
    }

    public class TestClass extends WarehausAminoJob {

    }

}
