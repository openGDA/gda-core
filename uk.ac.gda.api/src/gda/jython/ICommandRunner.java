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

package gda.jython;

import java.io.File;

/**
 * Interface used by some classes to run a simple Jython command
 * Provided to ensure loose coupling between callers and command runner implementation
 */
public interface ICommandRunner {
	/**
	 * 
	 * @param command
	 */
	public void runCommand(String command);
	
	/**
	 * @param command
	 *            String
	 * @param scanObserver
	 *            String
	 * @see Jython#runCommand(String, String)
	 */
	public void runCommand(String command, String scanObserver);
	
	/**
	 * Runs a single line Jython command through the interpreter and returns the result in the form of a string. Note:
	 * this method waits until the command has finished so it can return the result. If the command takes a long time it
	 * will hang the thread which calls this method. So this method must be called in a separate thread from the main
	 * GUI thread, else the GUI will seize up until the command given to this method has returned. For an example of
	 * the, see the gda.jython.JythonTerminal class.
	 * @param command 
	 * @return the result of the command
	 */	
	public String evaluateCommand(String command);	
	
	public void runScript(File script, String sourceName);
	
	/**
	 * @param command
	 *            String
	 * @param source
	 *            String
	 * @return boolean
	 * @see Jython#runsource
	 */
	public boolean runsource(String command, String source);

	/**
	 * Find a script with given name in the GDA's script project folders.
	 * @param scriptToRun The name of a Jython script file.
	 * @return A path to the script in one of the project folders. The first
	 *         file with matching name will be returned, or null if no file
	 *         could be located.
	 */
	public String locateScript(String scriptToRun);
}
