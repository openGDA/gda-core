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
package gda.commandqueue;
import gda.jython.InterfaceProvider;
/**
 * JythonScriptFileRunnerCommand is an implementation of Command whose run method runs the script file set by the
 * setScriptFile method in the CommandRunner
 */
public class JythonCommandStringRunnerCommand extends JythonScriptFileRunnerCommand {
	protected String commandString;

	@Override
	public void runCommand() {
		// Run the command
		InterfaceProvider.getCommandRunner().runScript(commandString);
		// Update the Command.STATE to RUNNING using the beginRun method
		beginRun();
	}
	@Override
	public String toString() {
		return "JythonCommand [commandString=" + commandString + ", description=" + getDescription() + ":" + state + "]";
	}
	/**
	 * @return command string to run
	 */
	public String getCommandString() {
		return commandString;
	}
	/**
	 * @param commandString to run
	 */
	public void setCommandString(String commandString) {
		this.commandString = commandString;
		setDescription(commandString);
	}
}
