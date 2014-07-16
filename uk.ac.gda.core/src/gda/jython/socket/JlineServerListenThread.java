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

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import jline.ConsoleReader;
import jline.Terminal;

public class JlineServerListenThread extends ServerListenThreadBase {

	private ConsoleReader cr;

	public JlineServerListenThread(InputStream in, PrintWriter out, SessionClosedCallback sessionClosedCallback) throws IOException {
		
		super(sessionClosedCallback);
		
		Terminal.setupTerminal();
		
		this.cr = new ConsoleReader(in, out);
		
		final String gdaVar = LocalProperties.getVarDir();
		final File historyFile = new File(gdaVar, "server.history");
		cr.getHistory().setHistoryFile(historyFile);
	}

	@Override
	protected String readLine(String prompt) throws IOException {
		return cr.readLine(prompt);
	}

}
