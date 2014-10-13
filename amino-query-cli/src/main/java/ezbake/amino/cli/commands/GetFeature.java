package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.TFeatureMetadata;
import org.kohsuke.args4j.Argument;

public class GetFeature extends Command {

    @Argument(index = 0, metaVar = "id", required = true)
    String id;
    @Override
    public Integer execute() throws Exception {


        TFeatureMetadata result = aminoClient.getFeature(id, securityToken);

        output.println(result.toString());
        return 0;
    }
}
