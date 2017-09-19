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


public class CommandThreadInfo implements ICommandThreadInfo {

	private String threadType;
	private long id = -1;
	private String jythonServerThreadId;
	private int priority = -1;
	private String name;
	private String state;
	private String date;
	private String time;
	private boolean isInterrupted = false;
	private String command;

	public CommandThreadInfo() { }

	public CommandThreadInfo(
			String commandThreadType,
			long   id,
			String jythonServerThreadId,
			int    priority,
			String name,
			String  state,
			String date,
			String time,
			boolean isInterrupted,
			String command) {
		this.threadType = commandThreadType;
		this.id = id;
		this.jythonServerThreadId = jythonServerThreadId;
		this.priority = priority;
		this.name = name;
		this.state = state;
		this.date = date;
		this.time = time;
		this.isInterrupted = isInterrupted;
		this.command = command;
	}

	@Override
	public String getCommand() {
		return null==command ? "" : command;
	}

	@Override
	public String getCommandThreadType() {
		return null == threadType ? "" : threadType;
	}

	@Override
	public String getDate() {
		return null==date ? "" : date;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getJythonServerThreadId() {
		return jythonServerThreadId;
	}

	@Override
	public String getName() {
		return null==name ? "" : name;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getState() {
		return null==state ? "unknown" : state;
	}

	@Override
	public String getTime() {
		return null==time ? "" : time;
	}

	@Override
	public boolean isInterrupted() {
		return isInterrupted;
	}

	@Override
	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public void setCommandThreadType(String threadType) {
		this.threadType = threadType;
	}

	@Override
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void setJythonServerThreadId(String jythonServerThreadId) {
		this.jythonServerThreadId = jythonServerThreadId;
	}

	@Override
	public void setInterrupted(boolean isInterrupted) {
		this.isInterrupted = isInterrupted;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}
}
