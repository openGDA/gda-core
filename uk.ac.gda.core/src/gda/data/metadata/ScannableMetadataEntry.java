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

import gda.device.Scannable;
import gda.jython.JythonServerFacade;

/**
 * A {@link MetadataEntry} that returns the position of a {@link Scannable} as
 * its value.
 */
public class ScannableMetadataEntry extends MetadataEntry {
	
	private String scannableName;
	
	/**
	 * Creates a scannable metadata entry.
	 */
	public ScannableMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates a scannable metadata entry that will read the position of the
	 * specified scannable.
	 * 
	 * @param name the metadata entry name
	 * @param scannableName the name of the scannable
	 */
	public ScannableMetadataEntry(String name, String scannableName) {
		setName(name);
		setScannableName(scannableName);
	}
	
	/**
	 * Sets the name of the scannable that this metadata entry will read.
	 * 
	 * @param scannableName the name of the scannable
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}
	
	@Override
	public String readActualValue() {
		String command = scannableName + ".getPosition()";
		return JythonServerFacade.getInstance().evaluateCommand(command).toString();
	}
}
