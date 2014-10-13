package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.TQueryResult;
import org.kohsuke.args4j.Argument;

public class CreateResult extends Command {

    @Argument(index = 0, metaVar = "owner", usage = "owner", required = true)
    String owner = "";

    @Argument(index = 1, metaVar = "hypothesis-id", usage = "hypothesis-id", required = true)
    String hypothesisId = "";

    @Argument(index = 2, metaVar = "max-results", usage = "max-results", required = true)
    int maxResults = 200;

    @Argument(index = 3, metaVar = "justification", usage = "justification", required = true)
    String justification = "";

    @Override
    public Integer execute() throws Exception {
        TQueryResult result = aminoClient.createResult(owner, hypothesisId, maxResults, justification, securityToken);
        output.println(result.toString());
        return 0;
    }
}
