package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

import static java.lang.String.format;

public class GetCountForHypothesisFeature extends Command {

    @Argument(index = 0, metaVar = "feature-id", required = true)
    private String featureId = "";

    @Argument(index = 1, metaVar = "bucket-name", required = true)
    private String bucketName = "";

    @Argument(index = 2, metaVar = "begin-range", required = true)
    private String beginRange = "";

    @Argument(index = 3, metaVar = "end-range", required = true)
    private String endRange = "";

    @Override
    protected Integer execute() throws Exception {
        int result = aminoClient.getCountForHypothesisFeature(featureId, bucketName, beginRange, endRange, securityToken);
        output.println(format("Count for feature %s on bucket %s for range \"%s\" to \"%s\" is %d.",
                featureId, bucketName, beginRange, endRange, result));
        return 0;
    }
}
