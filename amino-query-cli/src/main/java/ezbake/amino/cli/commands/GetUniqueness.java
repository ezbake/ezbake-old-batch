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

import org.kohsuke.args4j.Argument;

import static java.lang.String.format;

public class GetUniqueness extends Command {

    @Argument(index = 0, metaVar = "feature-id", required = true)
    private String featureId = "";

    @Argument(index = 1, metaVar = "bucket-name", required = true)
    private String bucketName = "";

    @Argument(index = 2, metaVar = "count", required = true)
    private int count = Integer.MAX_VALUE;


    @Override
    protected Integer execute() throws Exception {
        double result = aminoClient.getUniqueness(featureId, bucketName, count, securityToken);

        output.println(format("Uniqueness for feature %s of bucket %s with count %d is %0.4f", featureId, bucketName, count, result));

        return 0;
    }
}
