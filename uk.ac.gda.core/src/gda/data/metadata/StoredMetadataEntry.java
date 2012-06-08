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

/**
 * Simple metadata entry that holds a string value.
 */
public class StoredMetadataEntry extends MetadataEntry {

	protected String value;
	
	/**
	 * Creates a stored metadata entry.
	 */
	public StoredMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates a stored metadata entry with the specified name.
	 * 
	 * @param name the name
	 */
	public StoredMetadataEntry(String name) {
		setName(name);
	}
	
	/**
	 * Creates a stored metadata entry with the specified name and value.
	 * 
	 * @param name the metadata entry name
	 * @param value the value
	 */
	public StoredMetadataEntry(String name, String value) {
		setName(name);
		setValue(value);
	}
	
	/**
	 * Sets the value stored by this metadata entry.
	 * 
	 * @param value the value
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
		notifyIObservers(this, value);
	}
	
	@Override
	public String readActualValue() {
		return value;
	}
	
	@Override
	public boolean canStoreValue() {
		return true;
	}	

}