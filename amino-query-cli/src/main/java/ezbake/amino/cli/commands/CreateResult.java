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

import ezbake.services.amino.thrift.TQueryResult;
import org.kohsuke.args4j.Argument;

public class CreateResult extends Command {

    @Argument(index = 0, metaVar = "owner", usage = "owner", required = true)
    String owner = "";

    @Argument(index = 1, metaVar = "hypothesis-id", usage = "hypothesis-id", required = true)
    String hypothesisId = "";

    @Argument(index = 2, metaVar = "max-results", usage = "max-results", required = true)
    int maxResults = 200;

    @Argument(index = 3, metaVar = "justification", usage = "justification", required = true)
    String justification = "";

    @Override
    public Integer execute() throws Exception {
        TQueryResult result = aminoClient.createResult(owner, hypothesisId, maxResults, justification, securityToken);
        output.println(result.toString());
        return 0;
    }
}
