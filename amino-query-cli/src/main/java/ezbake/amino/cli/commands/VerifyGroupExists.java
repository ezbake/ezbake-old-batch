package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

public class VerifyGroupExists extends Command {

    @Argument(index = 0, metaVar = "group", usage = "Group name to verify", required = true)
    String group;
    @Override
    public Integer execute() throws Exception {
        boolean result = aminoClient.verifyGroupExists(group, securityToken);
        output.append("group '").append(group).append("' ");
        output.append(result ? "exist" : "does not exist").append('.');
        return result ? 0 : 1;
    }
}
