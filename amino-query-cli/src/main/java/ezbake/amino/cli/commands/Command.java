/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.amino.cli.commands;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import ezbake.amino.cli.Main;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.services.amino.thrift.AminoService;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * The Command interface for creating Amino Cli Commands.  All sub classes of this object is automatically seen by the
 * CLI program.  Please see the README.md in this package for more information on how to develop a Command.
 */
public abstract class Command implements Callable<Integer> {
    protected AminoService.Client aminoClient;
    protected CmdLineParser cmdLineParser;
    protected PrintStream output;
    protected EzSecurityToken securityToken;
    protected List<String> rawArgs;

    protected CmdLineException cliParseException = null;

    private Main main;

    /**
     * Default constructor, this constructor should not be overridden and it should be left mostly blank.
     */
    protected Command() {

    }

    /**
     * This should be called directly after the command was created.  This is required instead of being in the constructor
     * due to the fact that the Command's CLI arguments annotations haven't been parsed at construction time.
     *
     * @param main - the Main program that initialized this object
     * @param args - Arguments into the command
     * @param client - the AminoService Client that should be used in communications
     * @param securityToken - Security Token of the user who wish to perform the operation
     * @param output - The output stream to print to
     */
    public final void initialize(Main main, List<String> args, AminoService.Client client, EzSecurityToken securityToken, PrintStream output) {
        aminoClient = client;
        this.securityToken = securityToken;
        this.output = output;
        rawArgs = args;
        this.main = main;
        parseArgs();
        init();
    }

    /**
     * Since we can't allow the Command to create a constructor due to when the arguments are getting parsed, this allows the Command to
     * specified initialization procedures directly after the Command has been constructed.
     */
    protected void init() {

    }


    /**
     * The public method that the Consumer should call to execute this command
     * @return Unix Exit code to which the command desire to be returned
     * @throws Exception - On any errors while executing the command
     */
    @Override
    public final Integer call() throws Exception {
        if (checkIfNeedToShowUsage()) {
            if ( cliParseException != null ) {
                System.err.println("Invalid arguments: " + cliParseException.getMessage());
            }
            return showUsage();
        }
        return execute();
    }

    /**
     * This is where the guts of the Command goes to perform the action of the command
     * @return Unix Exit code to which the command desire to be returned
     * @throws Exception - On any errors while executing the command
     */
    protected abstract Integer execute() throws Exception;

    /**
     * This is a hook for Command's to override if they need additional logic to show the usage.
     * @return true if the Command desire the usage method to be used instead of calling execute.
     */
    protected boolean shouldShowUsage() {
        return false;
    }

    /**
     * @return true if we should print the uage and not actually call the Command's execute method.
     */
    private boolean checkIfNeedToShowUsage() {
        return cliParseException != null || shouldShowUsage();
    }

    /**
     * The method to parse the arguments
     */
    protected final void parseArgs() {
        try {
            this.cmdLineParser = new CmdLineParser(this);
            cmdLineParser.parseArgument(rawArgs);
        } catch (CmdLineException e) {
            cliParseException = e;
        }
    }

    /**
     * Print the list of strings to the output stream, where each item is indented with a tab.
     * @param list - the list of string values to print.
     */
    protected void printList(Iterable<String> list) {
        output.print("\t");
        output.println(Joiner.on("\n\t").join(list) + "\n");
    }


    /**
     * Print a list of objects to the output stream, where each item is indented in by a tab.
     * @param list - The list of objects to print
     * @param toStr - the method to format the object into a string
     * @param <F> - The object type
     */
    protected <F> void printListOfObjects(java.lang.Iterable<F> list, com.google.common.base.Function<? super F,? extends String> toStr) {
        output.print("\t");
        output.println(Joiner.on("\n\t").join(Iterables.transform(list, toStr)) + "\n");
    }

    /**
     * @return The name of the object's implementing class without the package name
     */
    public final String getCmdName() {
        String strName = this.getClass().getName();
        return strName.substring(strName.lastIndexOf('.') + 1);
    }

    /**
     * Show the usage message and return the value to exit the program with
     * @return -1
     */
    protected Integer showUsage() {
        cmdLineParser.setUsageWidth(Main.getTerminalWidth());
        System.err.print(main.getJarName() + " " + getCmdName() + " ");
        cmdLineParser.printSingleLineUsage(System.err);
        System.err.println();
        cmdLineParser.printUsage(System.err);
        String str =  extraUsageHelp();
        if ( !Strings.isNullOrEmpty(str) )
            System.err.println(str);
        return -1;
    }

    /**
     * This function is meant for the Commands implementations to override if they need to give the user extra information
     * on how to use their command.
     *
     * @return - extra information on how to use this command
     */
    protected String extraUsageHelp() {
        return "";
    }

    /**
     * This function is useful for Commands that take in a bit of arguments that wish to pull them from a file and from the CLI as a bunch of key-value pairs<br/>
     * <br/>
     * Any options specified on the cmdLine will replace any coming in from the file.
     *
     * @param cmdLine - The parsed out CmdLine Map.  Use "handler=MapOptionHandler.class" in the Args4j @Option annotation to generate this mapping
     * @param propFileName - A filename to of the file to load and merge with the cmdLine map
     * @param loadInto - The properties file to place all of the loaded properties into.
     *
     * @throws java.lang.RuntimeException on any IOException from reading from the property file
     */
    protected void loadIntoProperties(Map<String, String> cmdLine, String propFileName, Properties loadInto) {
        if (!Strings.isNullOrEmpty(propFileName)) {
            try (FileInputStream fos = new FileInputStream(propFileName)) {
                loadInto.load(fos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        loadInto.putAll(cmdLine);
    }

}
