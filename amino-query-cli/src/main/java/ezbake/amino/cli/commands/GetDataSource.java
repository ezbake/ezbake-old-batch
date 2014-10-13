package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.TDatasourceMetadata;
import org.kohsuke.args4j.Argument;

public class GetDataSource extends Command {

    @Argument(index = 0, metaVar = "datasource-id", required = true)
    String datasourceId;
    @Override
    public Integer execute() throws Exception {
        TDatasourceMetadata buckets = aminoClient.getDataSource(datasourceId, securityToken);
        output.println(buckets.toString());

        return 0;
    }
}
