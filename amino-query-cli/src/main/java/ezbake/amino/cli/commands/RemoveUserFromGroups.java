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
