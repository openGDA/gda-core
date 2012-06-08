/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.io.Serializable;

/**
 * QueuedCommandSummary is used to provide a summary of the Commands in a CommandQueue
 * For contains the unique CommandId that can be used in calls to manipulate the
 * Queue and a descriptive string to display to the user
 */
public class QueuedCommandSummary implements CommandSummary, Serializable{
	final public CommandId id;
	final public CommandSummary commandSummary;
	public QueuedCommandSummary(CommandId id, CommandSummary commandSummary) {
		super();
		this.id = id;
		this.commandSummary = commandSummary;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commandSummary == null) ? 0 : commandSummary.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public String getDescription() {
		return commandSummary.getDescription();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueuedCommandSummary other = (QueuedCommandSummary) obj;
		if (commandSummary == null) {
			if (other.commandSummary != null)
				return false;
		} else if (!commandSummary.equals(other.commandSummary))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "QueuedCommandSummary [description=" + commandSummary.getDescription() + ", id=" + id + "]";
	}
	
}
