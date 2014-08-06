/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.ShellFactory.ExitCallback;

public class SshServerThread extends ServerThread implements SessionClosedCallback {

	private ExitCallback exitCallback;

	protected SshServerThread(InputStream in, OutputStream out, ExitCallback exitCallback) {
		super(in, out);
		this.exitCallback = exitCallback;
	}

	@Override
	public void sessionClosed() {
		exitCallback.onExit(0);
	}

}
