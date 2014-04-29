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

package gda.jython.commandinfo;

import gda.jython.commandinfo.ICommandThreadInfo;

public class CommandThreadInfo implements ICommandThreadInfo {
	
	private String commandThreadType;
	private String date;
	private String time;
	private String queue;
	private String interrupt;
	private String command;

	public CommandThreadInfo(
			String commandThreadType, 
			String date, 
			String time, 
			String queue, 
			String interrupt,
			String command) {
		this.commandThreadType = commandThreadType;
		this.date = date;
		this.time = time;
		this.queue = queue;
		this.interrupt = interrupt;
		this.command = command;
	}

	public CommandThreadInfo() { }

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public String getCommandThreadType() {
		return commandThreadType;
	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public String getInterrupt() {
		return interrupt;
	}

	@Override
	public String getQueue() {
		return queue;
	}

	@Override
	public String getTime() {
		return time;
	}

	@Override
	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public void setCommandThreadType(String commandThreadType) {
		this.commandThreadType = commandThreadType;
	}

	@Override
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public void setInterrupt(String interrupt) {
		this.interrupt = interrupt;
	}

	@Override
	public void setQueue(String queue) {
		this.queue = queue;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}
}
