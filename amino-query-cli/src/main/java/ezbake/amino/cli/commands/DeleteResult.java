package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

public class DeleteResult extends Command {

    @Argument(index = 0, metaVar = "id", required = true)
    String id;

    @Override
    protected Integer execute() throws Exception {
        aminoClient.deleteResult(id, securityToken);
        return 0;
    }
}
