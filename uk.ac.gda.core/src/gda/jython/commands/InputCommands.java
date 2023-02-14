/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.jython.commands;

import gda.factory.Finder;
import gda.jython.JythonServer;
import gda.jython.JythonServer.JythonServerThread;
import gda.jython.JythonServerFacade;

/**
 * InputCommands
 */
public class InputCommands {

	private static JythonServer server;

	private InputCommands() {}

	/**
	 * Request input from the Jython terminal with a prompt.
	 * For Jython code this should not be required directly as the builtin input
	 * and raw_input functions redirect to here.
	 *
	 * @see #requestInput() requestInput() if a prompt is not required (equivalent to passing
	 *     null or an empty string.
	 * @param promptString to display to user asking for input
	 * @return String entered by user
	 * @throws InterruptedException if the thread is interrupted or the user cancels the input
	 */
	public static String requestInput(String promptString) throws InterruptedException {
		if (Thread.currentThread() instanceof JythonServerThread jst) {
			return jst.requestInput(promptString);
		}
		if (promptString != null && !promptString.isEmpty()) {
			// prevents empty new line being printed on the console
			JythonServerFacade.getInstance().print(promptString);
		}
		return getServer().requestRawInput();
	}


	/**
	 * Request input from the Jython terminal.
	 * For Jython code this should not be required directly as the builtin input
	 * and raw_input functions redirect to here.
	 *
	 * @see #requestInput(String) requestInput(String) if a prompt is required
	 * @return String entered by user
	 * @throws InterruptedException if the thread is interrupted or the user cancels the input
	 */
	public static String requestInput() throws InterruptedException {
		return requestInput(null);
	}


	private static JythonServer getServer() {
		if (server == null) {
			server = Finder.findSingleton(JythonServer.class);
		}
		return server;
	}
}
