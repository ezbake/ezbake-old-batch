package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Argument;

public class GetHypothesis extends Command {

    @Argument(index = 0, metaVar = "owner", required = true)
    String owner;

    @Argument(index = 1, metaVar = "id", required = true)
    String id;
    @Override
    public Integer execute() throws Exception {
        THypothesis buckets = aminoClient.getHypothesis(owner, id, securityToken);
        output.println(buckets.toString());

        return 0;
    }
}
