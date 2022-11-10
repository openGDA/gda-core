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

import java.io.IOError;
import java.io.IOException;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;
import org.apache.sshd.server.channel.ChannelSession;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Attributes.OutputFlag;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.server.shell.JythonShell;

/**
 * Handler for running a shell when an SSH connection is made.
 * <pre>
 * $ ssh gda-control -p2222
 * </pre>
 * Delegates to {@link JythonShell}
 */
public class SshShellCommand extends GdaCommand {
	private static final Logger logger = LoggerFactory.getLogger(SshShellCommand.class);

	@SuppressWarnings("unused")
	public SshShellCommand(ChannelSession session) {
		// required by ShellFactory interface. Not sure why the session is passed in here
		// as well as in the run/destroy methods
	}

	/**
	 * Run the Jython shell - blocks while shell is running
	 * @param env client environment
	 * @return exit code - 0 on success, 1 on error
	 */
	@Override
	protected int run(ChannelSession session, Environment env) {
		try {
			logger.info("Creating Jython shell for {}", getClientAddress(session.getServerSession()));
			Terminal term = getTerminal(env);
			try (JythonShell shell = new JythonShell(term, env.getEnv())) {
				shell.run();
				return EXIT_SUCCESS;
			} catch (IOError ioe) {
				// Thrown by jline if the SSH connection is closed unexpectedly
				logger.error("SSH connection lost", ioe);
			} catch (Exception e) {
				term.writer().format("Error connecting to GDA: '%s'", e.getMessage());
				logger.error("Jython shell failed", e);
			}
		} catch (Exception e) {
			logger.error("Unable to start shell", e);
		}
		return EXIT_ERROR;
	}

	/**
	 * Build a terminal and set attributes based on client's environment
	 * <br>
	 * This is copied almost verbatim from jline's ShellImplFactory but allows the shell better
	 * control of threads and improved logging.
	 * @param env The environment of the client connection.
	 * @return A Terminal using the clients stdin and stdout. Reacts to terminal resizing.
	 * @throws IOException if terminal can't be created
	 */
	private Terminal getTerminal(Environment env) throws IOException {
		var columns = Integer.parseInt(env.getEnv().get("COLUMNS"));
		var rows = Integer.parseInt(env.getEnv().get("LINES"));
		Terminal terminal = TerminalBuilder.builder()
				.name("JLine SSH")
				.type(env.getEnv().get("TERM"))
				.system(false)
				.streams(getStdin(), getStdout())
				.size(new Size(columns, rows))
				.build();
		Attributes attr = terminal.getAttributes();
		for (var e : env.getPtyModes().entrySet()) {
			Object flag = switch (e.getKey()) {
			case VINTR -> ControlChar.VINTR;
			case VQUIT -> ControlChar.VQUIT;
			case VERASE -> ControlChar.VERASE;
			case VKILL -> ControlChar.VKILL;
			case VEOF -> ControlChar.VEOF;
			case VEOL -> ControlChar.VEOL;
			case VEOL2 -> ControlChar.VEOL2;
			case VSTART -> ControlChar.VSTART;
			case VSTOP -> ControlChar.VSTOP;
			case VSUSP -> ControlChar.VSUSP;
			case VDSUSP -> ControlChar.VDSUSP;
			case VREPRINT -> ControlChar.VREPRINT;
			case VWERASE -> ControlChar.VWERASE;
			case VLNEXT -> ControlChar.VLNEXT;
//			case VFLUSH -> ControlChar.VMIN;
//			case VSWTCH -> ControlChar.VTIME;
			case VSTATUS -> ControlChar.VSTATUS;
			case VDISCARD -> ControlChar.VDISCARD;
			case ECHO -> LocalFlag.ECHO;
			case ICANON -> LocalFlag.ICANON;
			case ISIG -> LocalFlag.ISIG;
			case ICRNL -> InputFlag.ICRNL;
			case INLCR -> InputFlag.INLCR;
			case IGNCR -> InputFlag.IGNCR;
			case OCRNL -> OutputFlag.OCRNL;
			case ONLCR -> OutputFlag.ONLCR;
			case ONLRET -> OutputFlag.ONLRET;
			case OPOST -> OutputFlag.OPOST;
			default -> null;
			};
			if (flag instanceof ControlChar cc) {
				attr.setControlChar(cc, e.getValue());
			} else if (flag instanceof LocalFlag lf) {
				attr.setLocalFlag(lf, e.getValue() != 0);
			} else if (flag instanceof InputFlag inf) {
				attr.setInputFlag(inf, e.getValue() != 0);
			} else if (flag instanceof OutputFlag of) {
				attr.setOutputFlag(of, e.getValue() != 0);
			}

		}
		terminal.setAttributes(attr);
		env.addSignalListener((channel, signals) -> {
			terminal.setSize(new Size(Integer.parseInt(env.getEnv().get("COLUMNS")),
					Integer.parseInt(env.getEnv().get("LINES"))));
			terminal.raise(Terminal.Signal.WINCH);
		}, Signal.WINCH);
		return terminal;
	}
}
