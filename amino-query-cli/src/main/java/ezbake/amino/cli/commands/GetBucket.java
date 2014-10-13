package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.TBucketMetadata;
import org.kohsuke.args4j.Argument;

public class GetBucket extends Command {

    @Argument(index = 0, metaVar = "id", required = true)
    String id;

    @Override
    public Integer execute() throws Exception {


        TBucketMetadata result = aminoClient.getBucket(id, securityToken);

        output.println(result.toString());
        return 0;
    }
}
