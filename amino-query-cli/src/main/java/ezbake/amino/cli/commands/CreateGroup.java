package ezbake.amino.cli.commands;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import ezbake.services.amino.thrift.*;
import org.kohsuke.args4j.Argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CreateGroup extends Command {

    @Argument(index = 0, metaVar = "group-name", required = true)
    private String groupName = "";

    @Argument(index = 1, metaVar = "group-member:group-role1,group-role2", multiValued = true, required = true)
    private List<String> groupMember = new ArrayList<>();


    @Override
    public Integer execute() throws Exception {
        long dateCreated = System.currentTimeMillis();
        String createdBy = securityToken.getValidity().getIssuedTo();

        TCreateGroupRequest request = new TCreateGroupRequest(new TGroup(groupName, createdBy, dateCreated, getGroupMembers()));

        aminoClient.createGroup(request, securityToken);

        return 0;
    }

    public static Set<TGroupMember> parseGroupMembers(Collection<String> groupMembers) {
        return ImmutableSet.copyOf(Collections2.transform(groupMembers, new Function<String, TGroupMember>() {
            @Override
            public TGroupMember apply(String member) {
                String[] memberAndRole = member.split(":");
                String name = memberAndRole[0];

                Set<TGroupRole> roles = ImmutableSet.copyOf(
                        Iterables.transform(Splitter.on(',').split(memberAndRole[1]), new Function<String, TGroupRole>() {
                            @Override
                            public TGroupRole apply(String name) {
                                return TGroupRole.valueOf(name);
                            }
                        }));
                return new TGroupMember(name, roles);
            }
        }));
    }

    public Set<TGroupMember> getGroupMembers() {
        return parseGroupMembers(groupMember);
    }

}
