package ezbake.amino.cli.commands;

import java.util.Set;

public class ListGroups extends Command {

    @Override
    public Integer execute() throws Exception {
        final Set<String> groups = aminoClient.listGroups(securityToken);
        output.println("Groups:");
        printList(groups);
        return 0;
    }
}
