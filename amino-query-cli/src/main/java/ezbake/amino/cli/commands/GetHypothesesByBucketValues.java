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

package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.TByValuesRequest;
import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.spi.RestOfArgumentsHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetHypothesesByBucketValues extends Command {

    @Argument(index = 0, metaVar = "datasource-id", required = true)
    private String datasourceId = "";

    @Argument(index = 1, metaVar = "bucket-id", required = true)
    private String bucketId = "";

    @Argument(index = 2, metaVar = "feature-id", multiValued = true, handler = RestOfArgumentsHandler.class, required = true)
    private Set<String> bucketValues = new HashSet<>();

    @Override
    protected Integer execute() throws Exception {
        List<THypothesis> results = aminoClient.getHypothesesByBucketValues(getRequest(), securityToken);

        printListOfObjects(results,  new Function<THypothesis, String>() {
            @Override
            public String apply(THypothesis hypothesis) {
                return hypothesis.toString();
            }
        });

        return 0;
    }

    private TByValuesRequest getRequest() {
        return new TByValuesRequest(datasourceId, bucketId, bucketValues);
    }
}
