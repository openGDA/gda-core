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

import gda.device.DeviceBase;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Findable implementer of Runnable whose run method calls InterfaceProvider.getCommandRunner().runCommand() 
 * with the command property set by setCommand
 * Simple set the command to null or empty for the pollDone method to do nothing
 */
public class CommandRunnerRunnable extends DeviceBase implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(CommandRunnerRunnable.class);
	
	String command;
	/**
	 * 
	 */
	public CommandRunnerRunnable(){
		setLocal(true);
	}
	
	@Override
	public void configure() throws FactoryException {
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
		if(!configured){
			logger.error(getName() + "- pollDone called before configure");
		} else {
			if( command != null && !command.isEmpty())
			{
				try{
					//use evaluate otherwise a new thread is started each time
					InterfaceProvider.getCommandRunner().evaluateCommand(command);
				} catch (Throwable ex){
					logger.error(getName() + " - error executing command "+command, ex);
				}
			}
		}
	}
	
}
