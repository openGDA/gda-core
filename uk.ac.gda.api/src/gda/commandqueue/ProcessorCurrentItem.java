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
 * class returned from Processor getCurrentItem method
 * Used to get information to display to users  
 */
public class ProcessorCurrentItem implements Serializable {
	final String description;
	private final CommandId commandID;

//	public ProcessorCurrentItem(String description) {
//		super();
//		this.description = description;
//		this.id=null;
//	}

	public ProcessorCurrentItem(String description, CommandId removedHeadID) {
		super();
		this.description=description;
		this.commandID=removedHeadID;
	}

	@Override
	public String toString() {
		return "ProcessorCurrentItem [description=" + description + "]";
	}

	public String getDescription() {
		return description;
	}

	public CommandId getCommandID() {
		return commandID;
	}
	
}
