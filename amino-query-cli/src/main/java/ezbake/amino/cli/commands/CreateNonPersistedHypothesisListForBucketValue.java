package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Argument;

import javax.annotation.Nullable;
import java.util.List;

public class CreateNonPersistedHypothesisListForBucketValue extends Command {
    @Argument(index = 0, metaVar = "datasource-id", usage = "datasource-id", required = true)
    private String datasourceId;

    @Argument(index = 1, metaVar = "bucket-id", usage = "bucket-id", required = true)
    private String bucketId;

    @Argument(index = 2, metaVar = "bucket-values", usage = "bucket-values", required = true)
    private List<String> bucketValues;

    @Argument(index = 3, metaVar = "justification", usage = "justification", required = true)
    private String justification;

    @Argument(index = 4, metaVar = "feature-ids", usage = "feature-ids", required = true)
    private List<String> featureIds;

    @Argument(index = 5, metaVar = "timeout", usage = "timeout", required = true)
    private long timeout;

    @Override
    protected Integer execute() throws Exception {
        List<THypothesis> results = aminoClient.createNonPersistedHypothesisListForBucketValue(
                datasourceId, bucketId, bucketValues, justification, featureIds, timeout, securityToken);

        printListOfObjects(results,  new Function<THypothesis, String>() {
            @Nullable
            @Override
            public String apply(@Nullable THypothesis o) {
                assert o != null;
                return o.toString();
            }
        });
        return 0;
    }
}
