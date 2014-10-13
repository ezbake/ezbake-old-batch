package ezbake.amino.cli.commands;

import org.kohsuke.args4j.Argument;

import static java.lang.String.format;

public class GetUniqueness extends Command {

    @Argument(index = 0, metaVar = "feature-id", required = true)
    private String featureId = "";

    @Argument(index = 1, metaVar = "bucket-name", required = true)
    private String bucketName = "";

    @Argument(index = 2, metaVar = "count", required = true)
    private int count = Integer.MAX_VALUE;


    @Override
    protected Integer execute() throws Exception {
        double result = aminoClient.getUniqueness(featureId, bucketName, count, securityToken);

        output.println(format("Uniqueness for feature %s of bucket %s with count %d is %0.4f", featureId, bucketName, count, result));

        return 0;
    }
}
