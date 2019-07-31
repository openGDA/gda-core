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

package gda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableConfigurableBase;
import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import gda.jython.commands.GeneralCommands;

/**
 * Findable implementation of Runnable whose run method calls {@link ICommandRunner#runCommand}
 * with the command property set by setCommand.
 * <p>
 * Any initial setup (importing modules etc) can be done by setting the initialSetup field.
 * This setup will be run when this runner is configured and each time the namespace is reset.
 * <p>
 * Set the command to null or empty for the run method to do nothing
 */
public class CommandRunnerRunnable extends FindableConfigurableBase implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(CommandRunnerRunnable.class);

	private String command;
	private String initialSetup;

	@Override
	public void configure() {
		if (initialSetup != null) {
			InterfaceProvider.getCommandRunner().evaluateCommand(initialSetup);
			GeneralCommands.add_reset_hook(() -> setConfigured(false));
		}
		setConfigured(true);
	}

	/**
	 * @return command that is sent to InterfaceProvider.getCommandRunner().runCommand()
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command that is sent to InterfaceProvider.getCommandRunner().runCommand()
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public void run() {
		if (!isConfigured()) {
			configure();
		}
		if (isConfigured() && command != null && !command.isEmpty()) {
			try{
				InterfaceProvider.getCommandRunner().runCommand(command);
			} catch (Exception ex){
				logger.error("{} - error executing command '{}'", getName(), command, ex);
			}
		}
	}

	public void setInitialSetup(String initialSetup) {
		this.initialSetup = initialSetup;
	}
}
