package ezbake.amino.cli.commands;

public class GetHashCount extends Command {
    @Override
    protected Integer execute() throws Exception {
        output.append("Hash Count: ").append(Integer.toString(aminoClient.getHashCount()));
        return 0;
    }
}
