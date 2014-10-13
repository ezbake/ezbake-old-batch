package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Argument;

import javax.annotation.Nullable;
import java.util.List;

public class ListHypotheses extends Command {
    @Argument(index = 0, metaVar = "user-id", usage = "user-id", required = true)
    String userId;

    @Override
    public Integer execute() throws Exception {
        List<THypothesis> results = aminoClient.listHypotheses(userId, securityToken);

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
