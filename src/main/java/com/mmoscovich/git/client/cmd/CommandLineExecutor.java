package com.mmoscovich.git.client.cmd;

import lombok.extern.slf4j.Slf4j;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

@Slf4j
public class CommandLineExecutor {
	/** Success exit code. */
    public static final int SUCCESS_EXIT_CODE = 0;
	
	private final Commandline cmd = new Commandline();
	private String executable;
	
	public CommandLineExecutor(String executable) {
		this.executable = executable;
		this.init();
	}
	
	private void init() {
		cmd.setExecutable(executable);
	}
	
	/**
     * Executes command line.
     * 
     * @param cmd
     *            Command line.
     * @param failOnError
     *            Whether to throw exception on NOT success exit code.
     * @param args
     *            Command line arguments.
     * @return {@link CommandResult} instance holding command exit code, output
     *         and error if any.
     * @throws CommandLineException
     * @throws MojoFailureException
     *             If <code>failOnError</code> is <code>true</code> and command
     *             exit code is NOT equals to 0.
     */
    public CommandResult executeCommand(final boolean failOnError, final String... args)
            throws CommandLineException {

    	this.init();;

        if (log.isDebugEnabled()) {
        	log.debug(cmd.getExecutable() + " " + StringUtils.join(args, " "));
        }

        cmd.clearArgs();
        cmd.addArguments(args);

        final StreamConsumer out;
//        if (verbose) {
//            out = new DefaultConsumer();
//        } else {
        out = new CommandLineUtils.StringStreamConsumer();
//        }

        final CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        // execute
        final int exitCode = CommandLineUtils.executeCommandLine(cmd, out, err);

        String errorStr = err.getOutput();
        String outStr = "";
        if (out instanceof StringStreamConsumer) {
            outStr = ((StringStreamConsumer) out).getOutput();
        }

        if (failOnError && exitCode != SUCCESS_EXIT_CODE) {
            // not all commands print errors to error stream
            if (StringUtils.isBlank(errorStr) && StringUtils.isNotBlank(outStr)) {
                errorStr = outStr;
            }

            throw new CommandLineException("Process exited with error: " + errorStr);
        }

        return new CommandResult(exitCode, outStr, errorStr);
    }

    public static class CommandResult {
        private final int exitCode;
        private final String out;
        private final String error;

        private CommandResult(final int exitCode, final String out,
                final String error) {
            this.exitCode = exitCode;
            this.out = out;
            this.error = error;
        }

        /**
         * @return the exitCode
         */
        public int getExitCode() {
            return exitCode;
        }

        /**
         * @return the out
         */
        public String getOut() {
            return out;
        }

        /**
         * @return the error
         */
        public String getError() {
            return error;
        }
    }
}
