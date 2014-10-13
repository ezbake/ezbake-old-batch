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
import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Option;

import javax.annotation.Nullable;
import java.util.List;

public class GetGroupHypothesesForUser extends Command {

    @Option(name = "--user-owned", usage = "Flag to display only user owned groups")
    boolean userOwned = false;

    @Override
    protected Integer execute() throws Exception {

        List<THypothesis> results = aminoClient.getGroupHypothesesForUser(userOwned, securityToken);

        printListOfObjects(results,  new Function<THypothesis, String>() {
            @Nullable
            @Override
            public String apply(@Nullable THypothesis o) {
                assert o != null;
                return o.toString();
            }
        });

        return 0;
    }
}
