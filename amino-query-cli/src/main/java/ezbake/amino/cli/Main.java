package ezbake.amino.cli;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import ezbake.amino.cli.commands.Command;
import ezbake.base.thrift.*;
import ezbake.common.properties.EzProperties;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.security.client.EzSecurityTokenWrapper;
import ezbake.security.client.EzbakeSecurityClient;
import ezbake.services.amino.thrift.AminoService;
import ezbake.thrift.ThriftClientPool;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.thrift.TException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;

public class Main implements Callable<Integer>, AutoCloseable {
    private static final String DN = "CN=Smith, Joe, OU=People, OU=EzBake, OU=CSC, O=Foo, C=US";
    private static final Set<String> AUTHORIZATIONS = Sets.newHashSet("U");
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Option(name = "--use-mock-security", usage = "Use a mock security token", required = false)
    private boolean useMockSecurity = false;

    @Option(name = "--sid", usage = "Specify the user sid. Example: jsmith")
    private String sid = "";

    @Option(name = "--user-dn", usage = "Specify a user dn. Example: " + DN)
    private String userDn = "";

    private String commandName = "";

    private List<String> arguments = new ArrayList<>();


    private EzProperties properties;

    private final ThriftClientPool pool;

    private final AminoService.Client client;

    final CmdLineParser cmdLineParser;

    public int getArgumentsOfAt(String[] args) {
        Set<String> cmds = Sets.newHashSet(getCommands());
        for ( int i = 0; i < args.length; i++ ) {
            if ( cmds.contains(args[i]) ) {
                return i;
            }
        }
        return -1;
    }

    public EzProperties createEzConf() {
        try {
            EzConfiguration conf = new EzConfiguration();
            if ( useMockSecurity ) {
                conf.getProperties().setProperty(EzbakeSecurityClient.USE_MOCK_KEY, String.valueOf(true));
                conf.getProperties().setProperty(EzbakeSecurityClient.MOCK_USER_KEY, userDn);
            }
            String appName = conf.getProperties().getProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_NAME);
            if (Strings.isNullOrEmpty(appName) ) {
                conf.getProperties().setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_NAME, "AminoService");
            }
            return new EzProperties(conf.getProperties(), true);
        } catch (EzConfigurationLoaderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLoggerToErrorOnly() {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        // This is testing the string version just in case LogBack is not our logging api
        if (rootLogger.getClass().getName().equals("ch.qos.logback.classic.Logger")) {
            ch.qos.logback.classic.Logger lbRootLogger = ((ch.qos.logback.classic.Logger) rootLogger);
            lbRootLogger.detachAndStopAllAppenders();
            BasicConfigurator.configureDefaultContext();
            lbRootLogger.setLevel(Level.WARN);
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache")).setLevel(Level.WARN);
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.reflections")).setLevel(Level.WARN);
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.curator.framework.state.ConnectionStateManager")).setLevel(Level.ERROR);
        }




    }


    public static EzSecurityToken populateToken(String sid, String tid, Long exp, String principal, String name,
                                                String level, Set<String> authList) throws IOException {
        final ValidityCaveats validityCaveats = new ValidityCaveats();
        final EzSecurityToken su =
                new EzSecurityToken(validityCaveats, TokenType.USER, new EzSecurityPrincipal(principal,
                        validityCaveats));
        su.getTokenPrincipal().setName(name);
        su.getValidity().setIssuer("EzSecurity");
        su.getValidity().setIssuedTo(sid);
        su.getValidity().setIssuedFor(tid);
        su.getValidity().setNotAfter(System.currentTimeMillis() + exp);
        su.setAuthorizationLevel(level);
        su.setAuthorizations(new Authorizations());
        su.getAuthorizations().setFormalAuthorizations(authList);
        su.tokenPrincipal.validity = su.getValidity();

        su.getValidity().setSignature("This is not a signature");
        return su;
    }

    public EzSecurityToken createSecToken() {
        try {
            return populateToken(sid, "AminoService", 100000L, userDn, "Client Api", "U", AUTHORIZATIONS);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EzSecurityToken getSecurityToken() throws TException {
        if ( useMockSecurity ) {
            return createSecToken();
        } else {
            try(EzbakeSecurityClient client = new EzbakeSecurityClient(properties)) {
                EzSecurityTokenWrapper appToken = client.fetchAppToken();
                appToken.setUserId(sid);
                return appToken;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public Main(String[] args) {

        this.cmdLineParser = new CmdLineParser(this);
        if (args.length == 0) {
            // no point in loading the rest, to be faster for the user.
            this.properties = null;
            this.pool = null;
            this.client = null;
            return;
        }

        try {
            int splitAt = getArgumentsOfAt(args);
            if ( splitAt == -1 ) {
                throw new CmdLineException(cmdLineParser, "Command-name required");
            }
            commandName = args[splitAt];
            arguments = ImmutableList.copyOf(Arrays.copyOfRange(args, splitAt + 1, args.length));
            cmdLineParser.parseArgument(ImmutableList.copyOf(Arrays.copyOf(args, splitAt)));
        } catch (CmdLineException e) {
            System.out.println(e.getMessage());
            this.properties = null;
            this.pool = null;
            this.client = null;
            return;
        }

        this.properties = createEzConf();
        this.pool = new ThriftClientPool(properties);
        try {
            logger.info("Getting Thrift client from pool for amino:amino");
            this.client = pool.getClient("amino", "amino", AminoService.Client.class);
        } catch (final TException e) {
            throw new RuntimeException(e);
        }
    }

    public Callable<Integer> getCommand() {

        try {
            final Set<Class<? extends Command>> types = getAllCommands();
            Class<? extends Command> cmd = Iterables.find(types, new Predicate<Class<? extends Command>>() {
                @Override
                public boolean apply(@Nullable Class<? extends Command> aClass) {
                    return aClass != null && aClass.getName().endsWith(commandName);

                }
            });

            Command cmdInstance = cmd.newInstance();
            cmdInstance.initialize(this, arguments, client, getSecurityToken(), System.out);
            return cmdInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Class<? extends Command>>  getAllCommands() {
        final Reflections reflections = new Reflections(Command.class.getPackage().getName());
        return reflections.getSubTypesOf(Command.class);


    }

    public List<String> getCommands() {
        List<String> result = new ArrayList<>();
        Iterables.addAll(result, Iterables.transform(getAllCommands(), new Function<Class<? extends Command>, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Class<? extends Command> aClass) {
                assert aClass != null;
                return aClass.getName().substring(aClass.getName().lastIndexOf('.') + 1);
            }
        }));
        Collections.sort(result);
        return result;
    }


    @Override
    public Integer call() throws Exception {
        if ( this.properties == null ) {
            System.err.println(getJarName() + " command");
            cmdLineParser.setUsageWidth(getTerminalWidth());
            cmdLineParser.printUsage(System.err);
            System.err.println("Commands: ");
            for (String cmd : getCommands()) {
                System.err.println("\t" + cmd);
            }
            return 1;
        }
        return getCommand().call();
    }

    public String getJarName() {
        StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
        stringBuilderWriter.append("java -jar jarFilename ");
        cmdLineParser.printSingleLineUsage(stringBuilderWriter, null);
        return stringBuilderWriter.toString();
    }

    /**
     * A funny hack in order to get the proper terminal width so that we can word wrap properly
     * @return The number of characters wide the terminal is currently.
     */
    public static int getTerminalWidth() {
        try {
            Process p = Runtime.getRuntime().exec(new String[] {
                    "sh", "-c", "tput cols 2> /dev/tty" });
            p.waitFor();
            return Integer.parseInt(new BufferedReader(new InputStreamReader(p.getInputStream())).readLine());
        } catch (Exception e) {
            logger.error("Error getting terminal width", e);
            return 80;
        }

    }


    public static void main(String[] args) throws Exception {
        setLoggerToErrorOnly();
        Integer result;
        try (Main program = new Main(args)) {
            result = program.call();
        }
        if ( result == null ) result = 254;
        System.exit(result);
    }

    @Override
    public void close() {
        if ( pool != null && client != null)
            pool.returnToPool(client);
        if ( pool != null )
            pool.close();
    }
}
