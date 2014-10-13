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

import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Argument;

public class GetHypothesis extends Command {

    @Argument(index = 0, metaVar = "owner", required = true)
    String owner;

    @Argument(index = 1, metaVar = "id", required = true)
    String id;
    @Override
    public Integer execute() throws Exception {
        THypothesis buckets = aminoClient.getHypothesis(owner, id, securityToken);
        output.println(buckets.toString());

        return 0;
    }
}
