/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.ShellFactory.Environment;
import org.apache.sshd.server.ShellFactory.ExitCallback;
import org.apache.sshd.server.ShellFactory.Shell;

/**
 * Represents a GDA SSH {@link Shell}.
 */
public class SshShell implements Shell {

	private InputStream inputStream;

	private OutputStream outputStream;

	private ExitCallback exitCallback;

	private ServerThread serverThread;

	@Override
	public void setInputStream(InputStream in) {
		this.inputStream = in;
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.outputStream = out;
	}

	@Override
	public void setErrorStream(OutputStream err) {
		// TODO SshShell: deal with the error stream
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.exitCallback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		serverThread = new SshServerThread(inputStream, outputStream, exitCallback);
		serverThread.start();
	}

	@Override
	public void destroy() {
		// TODO SshShell: implement destroy()
	}

}
