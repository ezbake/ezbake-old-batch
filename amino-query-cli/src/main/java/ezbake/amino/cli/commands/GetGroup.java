package ezbake.amino.cli.commands;

import ezbake.services.amino.thrift.TGroup;
import org.kohsuke.args4j.Argument;

public class GetGroup extends Command {

    @Argument(index = 0, metaVar = "group-name", required = true)
    private String groupName = "";


    @Override
    protected Integer execute() throws Exception {
        TGroup group = aminoClient.getGroup(groupName, securityToken);

        output.println(group.toString());

        return 0;
    }
}
