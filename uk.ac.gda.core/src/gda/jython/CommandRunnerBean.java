/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

/*
 * Call InterfaceProvider.getCommandRunner() only when needed rather than in constructor when Factories may not be created
 */
public class CommandRunnerBean implements ICommandRunner{

	private ICommandRunner runner;

	private ICommandRunner getRunner() {
		if( runner == null){
			runner = InterfaceProvider.getCommandRunner();
		}
		return runner;
	}
	CommandRunnerBean(){
	}


	@Override
	public void runCommand(String command) {
		getRunner().runCommand(command);
	}
	@Override
	public void runCommand(String command, String scanObserver) {
		getRunner().runCommand(command, scanObserver);
	}
	@Override
	public String evaluateCommand(String command) {
		return getRunner().evaluateCommand(command);
	}
	@Override
	public CommandThreadEvent runScript(File script, String sourceName) {
		return getRunner().runScript(script, sourceName);
	}
	@Override
	public boolean runsource(String command, String source) {
		return getRunner().runsource(command, source);
	}
	@Override
	public String locateScript(String scriptToRun) {
		return getRunner().locateScript(scriptToRun);
	}


}
