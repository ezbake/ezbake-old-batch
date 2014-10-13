package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

public class DeleteHypothesis extends Command {

    @Argument(index = 0, metaVar = "hypothesis-id", usage = "id of the hypothesis to be deleted", required = true)
    String hypothesisId;

    @Override
    protected Integer execute() throws Exception {
        aminoClient.deleteHypothesis(hypothesisId, securityToken);
        return 0;
    }
}
