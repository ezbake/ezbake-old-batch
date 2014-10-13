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

import ezbake.services.amino.thrift.TAddUsersRequest;
import ezbake.services.amino.thrift.TGroupMember;
import ezbake.services.amino.thrift.TGroupRole;
import org.kohsuke.args4j.Argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddToGroup extends Command {

    @Argument(index = 0, metaVar = "group-name", required = true)
    String groupName;

    @Argument(index = 1, metaVar = "group-member:group-role1,group-role2", multiValued = true)
    List<String> groupMember = new ArrayList<>();

    @Override
    public Integer execute() throws Exception {

        TAddUsersRequest request = new TAddUsersRequest(groupName, getGroupMembers());

        aminoClient.addToGroup(request, securityToken);
        return 0;
    }


    public Set<TGroupMember> getGroupMembers() {
        return CreateGroup.parseGroupMembers(groupMember);
    }

    @Override
    protected String extraUsageHelp() {
        StringBuilder result = new StringBuilder("Member Roles:\n");
        for (TGroupRole role : TGroupRole.values()) {
            result.append("\t").append(role.toString()).append("\n");
        }
        return result.toString();
    }
}
