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
