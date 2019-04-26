/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.jython.server;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.sshd.server.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.IScanDataPointObserver;
import gda.jython.JythonServerFacade;
import gda.scan.IScanDataPoint;

/**
 * Handler for running a shell when an SSH connection is made with a command.
 * <pre>
 * $ ssh gda-control -p2222 'pos base_x 42'
 * </pre>
 * Passes the given command to {@link JythonServerFacade#runsource(String)}
 */
public class SshExecCommand extends GdaCommand {
	private static final Logger logger = LoggerFactory.getLogger(SshExecCommand.class);

	/** The command passed on the command line */
	private final String command;

	/** Create SshExecCommand to run a given command */
	public SshExecCommand(String command) {
		this.command = command;
	}

	/**
	 * Run a single command from SSH client
	 * @param env client environment
	 * @return exit code - 0 on success, 1 on error
	 */
	@Override
	protected int run(Environment env) throws IOException {
		logger.debug("Exec command '{}' from {}", command, getClientAddress());
		JythonServerFacade jsf = JythonServerFacade.getInstance();
		try (StdOut out = new StdOut(jsf)) {
			boolean incomplete = jsf.runsource(command, getStdin());
			if (incomplete) {
				getStderr().write("Incomplete Command\n\r".getBytes());
			}
			return incomplete ? EXIT_ERROR : EXIT_SUCCESS;
		} finally {
			logger.debug("Exec complete");
		}
	}


	/**
	 * Wrapper around this command's {@link OutputStream} to write output from the JythonServer.
	 * <p>
	 * This is {@link AutoCloseable} so can be used in try-with-resources blocks without any
	 * configuration to connect/disconnect to the JythonServer
	 * <br>
	 * <pre>
	 * JythonServerFacade jsf = JythonServerFacade.getInstance();
	 * try (StdOut out = new StdOut(jsf)) {
	 *     jsf.runsource(command, params.getIn());
	 * }
	 * </pre>
	 */
	protected class StdOut implements IScanDataPointObserver, gda.jython.Terminal, AutoCloseable {
		private JythonServerFacade jsf;
		public StdOut(JythonServerFacade jsf) {
			this.jsf = jsf;
			jsf.addOutputTerminal(this);
			jsf.addIScanDataPointObserver(this);
		}
		@Override
		public void update(Object source, Object arg) {
			if (arg instanceof IScanDataPoint) {
				IScanDataPoint sdp = (IScanDataPoint) arg;
				if (sdp.getCurrentPointNumber() == 0) {
					write(sdp.getHeaderString());
					write("\n");
				}
				write(sdp.toFormattedString());
				write("\n");
			}
		}
		@Override
		public void write(String output) {
			try {
				getStdout().write(output.getBytes());
				getStdout().flush();
			} catch (IOException e) {
				logger.error("Could not write {} to terminal", output);
			}
		}
		@Override
		public void close() {
			jsf.deleteIScanDataPointObserver(this);
			jsf.deleteOutputTerminal(this);
		}
	}
}
