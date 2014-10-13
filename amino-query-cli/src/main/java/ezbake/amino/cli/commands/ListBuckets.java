package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.TBucketMetadata;
import org.kohsuke.args4j.Argument;

import javax.annotation.Nullable;

public class ListBuckets extends Command {

    @Argument(index = 0, metaVar = "datasource-id", required = true)
    String datasourceId;

    @Override
    public Integer execute() throws Exception {


        Iterable<TBucketMetadata> buckets = aminoClient.listBuckets(datasourceId, securityToken);

        printListOfObjects(buckets,  new Function<TBucketMetadata, String>() {
            @Nullable
            @Override
            public String apply(@Nullable TBucketMetadata o) {
                assert o != null;
                return o.toString();
            }
        });

        return 0;
    }
}
