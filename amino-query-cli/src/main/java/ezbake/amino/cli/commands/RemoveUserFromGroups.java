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

import java.util.HashSet;
import java.util.Set;

public class RemoveUserFromGroups extends Command {

    @Argument(index = 0, metaVar = "user", required = true)
    String user;

    @Argument(index = 1, metaVar = "groups", multiValued = true)
    Set<String> groups = new HashSet<>();

    @Override
    protected Integer execute() throws Exception {
        aminoClient.removeUserFromGroups(user, groups, securityToken);
        return 0;
    }
}
