package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import ezbake.services.amino.thrift.THypothesis;
import org.kohsuke.args4j.Option;

import javax.annotation.Nullable;
import java.util.List;

public class GetGroupHypothesesForUser extends Command {

    @Option(name = "--user-owned", usage = "Flag to display only user owned groups")
    boolean userOwned = false;

    @Override
    protected Integer execute() throws Exception {

        List<THypothesis> results = aminoClient.getGroupHypothesesForUser(userOwned, securityToken);

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
