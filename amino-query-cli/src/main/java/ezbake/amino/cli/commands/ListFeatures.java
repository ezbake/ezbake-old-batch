package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.TFeatureMetadata;
import org.kohsuke.args4j.Argument;

import javax.annotation.Nullable;
import java.util.List;

public class ListFeatures extends Command {

    @Argument(index = 0, metaVar = "datasource-id", required = true)
    String datasourceId;
    @Override
    public Integer execute() throws Exception {


        List<TFeatureMetadata> buckets = aminoClient.listFeatures(datasourceId, securityToken);

        printListOfObjects(buckets,  new Function<TFeatureMetadata, String>() {
            @Nullable
            @Override
            public String apply(@Nullable TFeatureMetadata o) {
                assert o != null;
                return o.toString();
            }
        });

        return 0;
    }
}
