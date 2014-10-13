package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

import java.util.HashSet;
import java.util.Set;

public class RemoveUsersFromGroup extends Command {

    @Argument(index = 0, metaVar = "group", required = true)
    String group;

    @Argument(index = 1, metaVar = "users", multiValued = true)
    Set<String> users = new HashSet<>();

    @Override
    protected Integer execute() throws Exception {
        aminoClient.removeUsersFromGroup(group, users, securityToken);
        return 0;
    }
}
