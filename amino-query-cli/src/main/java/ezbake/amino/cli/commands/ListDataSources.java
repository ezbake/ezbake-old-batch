package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.TDatasourceMetadata;

import javax.annotation.Nullable;
import java.util.List;

public class ListDataSources extends Command {
    @Override
    public Integer execute() throws Exception {


        List<TDatasourceMetadata> buckets = aminoClient.listDataSources(securityToken);

        printListOfObjects(buckets,  new Function<TDatasourceMetadata, String>() {
            @Nullable
            @Override
            public String apply(@Nullable TDatasourceMetadata o) {
                assert o != null;
                return o.toString();
            }
        });

        return 0;
    }
}
