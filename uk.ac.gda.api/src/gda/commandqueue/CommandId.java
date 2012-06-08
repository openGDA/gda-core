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
 * A class that is used to uniquely identify each entry in a CommandQueue
 * 
 * The static method <code>generateCommandId</code> is used to create 
 * an instance of CommandId
 * 
 */
public class CommandId implements Serializable{

	static Integer globalId=0;
	static final public CommandId noneCommand = generateCommandId();

	int id;

	private CommandId(int id) {
		this.id = id;
	}

	/**
	 * method to use to create a unique CommandId
	 */
	static public CommandId generateCommandId() {
		int _id;
		synchronized (globalId){
			_id = globalId;
			globalId++;
		}
		return new CommandId(_id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandId other = (CommandId) obj;
		if (id != other.id)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CommandId [id=" + id + "]";
	}
}
