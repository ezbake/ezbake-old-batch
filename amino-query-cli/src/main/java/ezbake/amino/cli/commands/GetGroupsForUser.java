package ezbake.amino.cli.commands;

import java.util.Set;

public class GetGroupsForUser extends Command {

    @Override
    public Integer execute() throws Exception {


        Set<String> groups = aminoClient.getGroupsForUser(securityToken);

        printList(groups);

        return 0;
    }
}
