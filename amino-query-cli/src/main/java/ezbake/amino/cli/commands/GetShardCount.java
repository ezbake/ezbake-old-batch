package ezbake.amino.cli.commands;

public class GetShardCount extends Command {
    @Override
    protected Integer execute() throws Exception {
        output.append("Shard Count: ").append(Integer.toString(aminoClient.getHashCount()));
        return 0;
    }
}
