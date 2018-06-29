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

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_TIME;
import static java.util.Objects.requireNonNull;

import java.lang.Thread.State;
import java.time.LocalDateTime;

public class CommandThreadInfo implements ICommandThreadInfo {

	private final long id;
	private final String command;
	private final String threadType;
	private final String jythonServerThreadId;
	private final String name;
	private final String state;
	private final String date;
	private final String time;
	private final boolean interrupted;
	private final int priority;

	private String toStringCache;

	private CommandThreadInfo(Builder info) {
		info.validate();
		id = info.id;
		command = info.command;
		threadType = info.threadType;
		jythonServerThreadId = info.jythonServerThreadId;
		name = info.name;
		state = info.state;
		date = info.date;
		time = info.time;
		interrupted = info.interrupted;
		priority = info.priority;
	}

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public String getCommandThreadType() {
		return threadType;
	}

	@Override
	public String getDate() {
		return date;
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
		return name;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public String getTime() {
		return time;
	}

	@Override
	public boolean isInterrupted() {
		return interrupted;
	}

	@Override
	public String toString() {
		if (toStringCache == null) {
			toStringCache = "CommandThreadInfo [threadType=" + threadType + ", id=" + id + ", jythonServerThreadId="
					+ jythonServerThreadId + ", priority=" + priority + ", name=" + name + ", state=" + state + ", date="
					+ date + ", time=" + time + ", isInterrupted=" + interrupted + ", command=" + command + "]";
		}
		return toStringCache;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private long id = -1;
		private String command;
		private String threadType;
		private String jythonServerThreadId;
		private String name;
		private String state;
		private String date;
		private String time;
		private boolean interrupted;
		private int priority = -1;

		public Builder id(long id) {
			if (id < 0) throw new IllegalArgumentException("ID must be non-negative");
			this.id = id;
			return this;
		}

		public Builder threadType(CommandThreadType type) {
			requireNonNull(type, "Type must not be null");
			this.threadType = type.toString();
			return this;
		}

		public Builder command(String command) {
			requireNonNull(command, "Command must not be null");
			this.command = command;
			return this;
		}

		public Builder jythonServerThreadId(String id) {
			requireNonNull(id, "Thread ID must not be null");
			this.jythonServerThreadId = id;
			return this;
		}

		public Builder name(String name) {
			requireNonNull(name, "Name must not be null");
			this.name = name;
			return this;
		}

		public Builder state(State state) {
			requireNonNull(state, "State must not be null");
			this.state = state.toString();
			return this;
		}

		public Builder datetime(LocalDateTime datetime) {
			requireNonNull(datetime, "Datetime must not be null");
			date = ISO_DATE.format(datetime);
			time = ISO_TIME.format(datetime);
			return this;
		}

		public Builder interrupted(boolean interrupted) {
			this.interrupted = interrupted;
			return this;
		}

		public Builder priority(int priority) {
			if (priority < 0) throw new IllegalArgumentException("Priority must be non-negative");
			this.priority = priority;
			return this;
		}

		private void validate() {
			requireNonNull(command, "Command must be set");
			requireNonNull(threadType, "ThreadType must be set");
			requireNonNull(jythonServerThreadId, "serverThreadId must be set");
			requireNonNull(name, "Name must be set");
			requireNonNull(state, "State must be set");
			requireNonNull(date, "Date must be set");
			requireNonNull(time, "Time must be set");
			if (id < 0) throw new IllegalArgumentException("ID must be set");
			if (priority < 0) throw new IllegalArgumentException("Priority must be set");
		}

		public CommandThreadInfo build() {
			return new CommandThreadInfo(this);
		}
	}
}
