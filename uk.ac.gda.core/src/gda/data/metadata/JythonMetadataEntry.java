/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.metadata;

import gda.jython.JythonServerFacade;

/**
 * A {@link MetadataEntry} that evaluates a Jython command.
 */
public class JythonMetadataEntry extends MetadataEntry {
	
	private String command;
	
	/**
	 * Creates a Jython metadata entry.
	 */
	public JythonMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates a Jython metadata entry that will evaluate the specified Jython
	 * command.
	 * 
	 * @param name the metadata entry name
	 * @param command the Jython command
	 */
	public JythonMetadataEntry(String name, String command) {
		setName(name);
		setCommand(command);
	}
	
	/**
	 * Sets the Jython command that this entry will evaluate.
	 * 
	 * @param command the Jython command
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String readActualValue() {
		return JythonServerFacade.getInstance().evaluateCommand(command);
	}

}
