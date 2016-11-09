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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.python.jline.console.ConsoleReader;
import org.python.jline.console.history.FileHistory;

import gda.configuration.properties.LocalProperties;

public class JlineServerListenThread extends ServerListenThreadBase {

	private final ConsoleReader cr;

	public JlineServerListenThread(InputStream in, OutputStream out, SessionClosedCallback sessionClosedCallback) throws IOException {

		super(sessionClosedCallback);

		this.cr = new ConsoleReader(in, out);

		final String gdaVar = LocalProperties.getVarDir();
		final File historyFile = new File(gdaVar, "server.history");
		cr.setHistory(new FileHistory(historyFile));
	}

	@Override
	protected String readLine(String prompt) throws IOException {
		return cr.readLine(prompt);
	}

}
