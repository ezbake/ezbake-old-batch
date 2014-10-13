package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.TByValuesRequest;
import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.spi.RestOfArgumentsHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetHypothesesByBucketValues extends Command {

    @Argument(index = 0, metaVar = "datasource-id", required = true)
    private String datasourceId = "";

    @Argument(index = 1, metaVar = "bucket-id", required = true)
    private String bucketId = "";

    @Argument(index = 2, metaVar = "feature-id", multiValued = true, handler = RestOfArgumentsHandler.class, required = true)
    private Set<String> bucketValues = new HashSet<>();

    @Override
    protected Integer execute() throws Exception {
        List<THypothesis> results = aminoClient.getHypothesesByBucketValues(getRequest(), securityToken);

        printListOfObjects(results,  new Function<THypothesis, String>() {
            @Override
            public String apply(THypothesis hypothesis) {
                return hypothesis.toString();
            }
        });

        return 0;
    }

    private TByValuesRequest getRequest() {
        return new TByValuesRequest(datasourceId, bucketId, bucketValues);
    }
}
