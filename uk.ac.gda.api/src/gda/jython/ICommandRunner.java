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

import gda.jython.commandinfo.CommandThreadEvent;

/**
 * Interface used by some classes to run a simple Jython command
 * Provided to ensure loose coupling between callers and command runner implementation<pre>
 *********************************************************
 * Runner           Blocking  Interruptible  Script lock
 * evaluateCommand    Yes           No            No
 * runCommand          No          Yes            No
 * runScript           No          Yes           Yes
 * runsource          Yes          Yes            No
 *********************************************************</pre>
 * Blocking:      This will not return until the command or script has finished
 *                running. If it takes a long time to run, it will hang the
 *                thread which calls this method, so this it must be called in
 *                a separate thread from the main GUI thread, else the GUI will
 *                seize up until the command has returned.
 * Interruptible: Thread can be interrupted. If the command takes a long time
 *                to run, stopping a scan, script or doing a 'stop all' will
 *                interrupt the thread.
 * Script lock:   Acquire the script lock and run the script, or return BUSY
 *                without running the script, if the lock already has already
 *                been acquired. This allows script locked scripts to ensure
 *                only one will ever be running at once.
 */
public interface ICommandRunner {

	/**
	 * Executes the Jython command in a new thread.
	 *
	 * <BR><BR>Non-blocking, Interruptible, Not script locked.
	 * <BR> See {@link ICommandRunner} for the other options.
	 *
	 * @param command to run
	 */
	public void runCommand(String command);

	/**
	 * Runs a single line Jython command through the interpreter and returns the result in the form of a string. Note:
	 * this method waits until the command has finished so it can return the result. If the command takes a long time it
	 * will hang the thread which calls this method. So this method must be called in a separate thread from the main
	 * GUI thread, else the GUI will seize up until the command given to this method has returned. For an example of
	 * the, see the gda.jython.JythonTerminal class.
	 *
	 * <BR><BR>Blocking, Not interruptible, Not script locked.
	 * <BR> See {@link ICommandRunner} for the other options.
	 *
	 * @param command to run
	 * @return the string representation of the result
	 */
	public String evaluateCommand(String command);

	/**
	 * Runs the jython command string, and changes the ScriptStatus as is goes.
	 *
	 * <BR><BR>Non-blocking, Interruptible, Script locked.
	 * <BR> See {@link ICommandRunner} for the other options.
	 *
	 * @param scriptContents to run
	 * @return status
	 */
	CommandThreadEvent runScript(String scriptContents);

	/**
	 * Runs the Jython script, and changes the ScriptStatus as is goes.
	 *
	 * <BR><BR>Non-blocking, Interruptible, Script locked.
	 * <BR> See {@link ICommandRunner} for the other options.
	 *
	 * @param script to run
	 * @return status
	 */
	public CommandThreadEvent runScript(File script);

	/**
	 * Similar to {@link #runCommand}, except that a boolean is returned if the command was complete or if additional lines of a
	 * multi-line command are required. Used only by the JythonTerminal to determine which prompt to display. Note: this
	 * method waits until the command has finished so it can return the result. If the command takes a long time it will
	 * hang the thread which calls this method. So this method must be called in a separate thread from the main GUI
	 * thread, else the GUI will seize up until the command given to this method has returned.
	 *
	 * <BR><BR>Blocking, Interruptible, Not script locked.
	 * <BR> See {@link ICommandRunner} for the other options.
	 *
	 * @param command to run
	 * @return true if command was incomplete and more is required (eg "if True:"), false otherwise (including on error)
	 *
	 * @see Jython#runsource
	 */
	public boolean runsource(String command);

	/**
	 * Find a script with given name in the GDA's script project folders.
	 *
	 * @param scriptToRun The name of a Jython script file.
	 * @return A path to the script in one of the project folders. The first
	 *         file with matching name will be returned, or null if no file
	 *         could be located.
	 */
	public String locateScript(String scriptToRun);
}
