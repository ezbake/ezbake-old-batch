# Amino Service Cli

This project is designed to allow CLI access to the amino service

## To build

    mvn clean package
    
## To Install for use

  * Unless you are in mock-security-mode, this cli will have to be installed along side that of the amino-service.  This way it can get the same crypto-keys to talk to it.
  * Mock-Security-mode will not work with a properly configured AminoService running SSL as the certs will not validate.
  
## To run

  * Ensure that ezconfiguration is installed, and the env var `EZCONFIGURATION_DIR` is set
  * Ensure that the amino service is running, and properly registered itself in ezDiscovery
  * Easier way to run this is probably to set an alias:

    cli='java -jar target/amino-query-cli-2.0-SNAPSHOT.jar --sid '\''juser'\'' --user-dn '\''CN=User, Joe, OU=People, OU=EzBake, OU=CSC, O=Foo, C=US'\''  --use-mock-security'

  * Obliviously replace `juser` and `User, Joe` with your username and DN.  Remove --use-mock-security to use for real
  * Run without any other arguments for a list of commands.
  
    cli
    
  * Run with a single command to see the usage for the command, for example:
  
    cli GetCountForHypothesisFeature
  
  
  * For the CreateHypothesis command see the file `sample_hypothesis` for a sample hypothesis file.  Its just a java properties file of each of the properties for a hypothesis.  You may also just specified the values on the cli.
  
  
## To add an additional command to the cli

  * Create a new java class under the package `ezbake.amino.cli.commands` named by the name you want the user to call your command.
  * Do not create any constructors for the class.  It will break the command.  Instead override `init`, this is called right after construction.
  * Make the class extend from `ezbake.amino.cli.commands.Command` and implement the method `execute`
    * The Integer return value will end up being the unix exit code.
   
        @Override
        public Integer execute() throws Exception {
            return 0;
        }

  * Optionally override any of the following methods
    * `extraUsageHelp` - Print additional help for this command when the user didn't specify all required arguments
    * `shouldShowUsage` - If you need a custom reason to show the usage message
    * `init` - If you need to do something in the initialization phase.
      * You may for example call `loadIntoProperties` if you desire to load properties from a properties file and cli
  * Useful Members from `ezbake.amino.cli.commands.Command`
    * `AminoService.Client aminoClient` - client for the AminoService to talk to.
    * `PrintStream output` - the output stream you should print to instead of `System.out`, since we may want to redirect output.
    * `EzSecurityToken securityToken` - The security token for the user calling the cli
    * `CmdLineParser cmdLineParser` - the cli parser. May be useful for showing help.  Probably won't need to touch this.
    * `List<String> rawArgs` - Raw unparsed arguments that was passed into the command.  Probably won't need to touch this.
    * `void printList(List<String>)` - Prints the list of strings to the output stream
    * `<F> void printListOfObjects(java.lang.Iterable<F> list, com.google.common.base.Function<? super F,? extends String> toStr)` - Prints a list of objects to the screen using the passed in function as a toString method
    * `void loadIntoProperties(Map<String, String> cmdLine, String propFileName, Properties loadInto)` - Load the Map from the CLI parsing, using the filename passed in to merge into the Properties passed in last.
  * Adding arguments / Options for your command
    * For a positional argument into your command just add a new member and annotate it with `@Argument(index = 0, metaVar = "group", usage = "Group name to verify", required = true)`
      * change metaVar to the name you want and the usage to your usage.
      * Index is position in your positional argument list you want this member variable to source its data
    * For command line switches/named arguments
      * `@Option(name = "-s", aliases = "--sample-arg", usage = "A sample argument")`
    * See args4j for more information.  Or look at some of the existing commands for more examples.
    * These arguments and options will be automatically parsed, nothing else your command will need to do to get them to be populated.
  * Throwing an exception will result in the exception stack trace to be printed and the process ending with a non-zero exit code.