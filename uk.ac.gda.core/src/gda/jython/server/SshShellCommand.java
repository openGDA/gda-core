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
import java.util.Map;

import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.Signal;
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

	/**
	 * Run the Jython shell - blocks while shell is running
	 * @param env client environment
	 * @return exit code - 0 on success, 1 on error
	 */
	@Override
	protected int run(Environment env) {
		try {
			logger.info("Creating Jython shell for {}", getClientAddress());
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
		Terminal terminal = TerminalBuilder.builder().name("JLine SSH").type(env.getEnv().get("TERM")).system(false)
				.streams(getStdin(), getStdout()).build();
		terminal.setSize(
				new Size(Integer.parseInt(env.getEnv().get("COLUMNS")), Integer.parseInt(env.getEnv().get("LINES"))));
		Attributes attr = terminal.getAttributes();
		for (Map.Entry<PtyMode, Integer> e : env.getPtyModes().entrySet()) {
			switch (e.getKey()) {
			case VINTR:
				attr.setControlChar(ControlChar.VINTR, e.getValue());
				break;
			case VQUIT:
				attr.setControlChar(ControlChar.VQUIT, e.getValue());
				break;
			case VERASE:
				attr.setControlChar(ControlChar.VERASE, e.getValue());
				break;
			case VKILL:
				attr.setControlChar(ControlChar.VKILL, e.getValue());
				break;
			case VEOF:
				attr.setControlChar(ControlChar.VEOF, e.getValue());
				break;
			case VEOL:
				attr.setControlChar(ControlChar.VEOL, e.getValue());
				break;
			case VEOL2:
				attr.setControlChar(ControlChar.VEOL2, e.getValue());
				break;
			case VSTART:
				attr.setControlChar(ControlChar.VSTART, e.getValue());
				break;
			case VSTOP:
				attr.setControlChar(ControlChar.VSTOP, e.getValue());
				break;
			case VSUSP:
				attr.setControlChar(ControlChar.VSUSP, e.getValue());
				break;
			case VDSUSP:
				attr.setControlChar(ControlChar.VDSUSP, e.getValue());
				break;
			case VREPRINT:
				attr.setControlChar(ControlChar.VREPRINT, e.getValue());
				break;
			case VWERASE:
				attr.setControlChar(ControlChar.VWERASE, e.getValue());
				break;
			case VLNEXT:
				attr.setControlChar(ControlChar.VLNEXT, e.getValue());
				break;
			/*
			case VFLUSH:
				attr.setControlChar(ControlChar.VMIN, e.getValue());
				break;
			case VSWTCH:
				attr.setControlChar(ControlChar.VTIME, e.getValue());
				break;
			 */
			case VSTATUS:
				attr.setControlChar(ControlChar.VSTATUS, e.getValue());
				break;
			case VDISCARD:
				attr.setControlChar(ControlChar.VDISCARD, e.getValue());
				break;
			case ECHO:
				attr.setLocalFlag(LocalFlag.ECHO, e.getValue() != 0);
				break;
			case ICANON:
				attr.setLocalFlag(LocalFlag.ICANON, e.getValue() != 0);
				break;
			case ISIG:
				attr.setLocalFlag(LocalFlag.ISIG, e.getValue() != 0);
				break;
			case ICRNL:
				attr.setInputFlag(InputFlag.ICRNL, e.getValue() != 0);
				break;
			case INLCR:
				attr.setInputFlag(InputFlag.INLCR, e.getValue() != 0);
				break;
			case IGNCR:
				attr.setInputFlag(InputFlag.IGNCR, e.getValue() != 0);
				break;
			case OCRNL:
				attr.setOutputFlag(OutputFlag.OCRNL, e.getValue() != 0);
				break;
			case ONLCR:
				attr.setOutputFlag(OutputFlag.ONLCR, e.getValue() != 0);
				break;
			case ONLRET:
				attr.setOutputFlag(OutputFlag.ONLRET, e.getValue() != 0);
				break;
			case OPOST:
				attr.setOutputFlag(OutputFlag.OPOST, e.getValue() != 0);
				break;
			default:
				break;
			}
		}
		terminal.setAttributes(attr);
		env.addSignalListener(signals -> {
			terminal.setSize(new Size(Integer.parseInt(env.getEnv().get("COLUMNS")),
					Integer.parseInt(env.getEnv().get("LINES"))));
			terminal.raise(Terminal.Signal.WINCH);
		}, Signal.WINCH);
		return terminal;
	}
}
