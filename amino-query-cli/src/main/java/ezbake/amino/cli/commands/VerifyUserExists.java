package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

public class VerifyUserExists extends Command {

    @Argument(index = 0, metaVar = "user", required = true)
    String user;
    @Override
    public Integer execute() throws Exception {
        boolean result = aminoClient.verifyUserExists(user, securityToken);
        output.append("User '").append(user).append("' ");
        output.append(result ? "exist" : "does not exist").append('.');
        return result ? 0 : 1;
    }
}
