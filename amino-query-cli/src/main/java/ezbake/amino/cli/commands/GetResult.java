package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.TQueryResult;
import org.kohsuke.args4j.Argument;

public class GetResult extends Command {

    @Argument(index = 0, metaVar = "owner", required = true)
    String owner;

    @Argument(index = 1, metaVar = "id", required = true)
    String id;

    @Override
    protected Integer execute() throws Exception {
        TQueryResult result = aminoClient.getResult(owner, id, securityToken);
        output.println(result.toString());
        return 0;
    }
}
