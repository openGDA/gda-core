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

import gda.configuration.properties.LocalProperties;

/**
 * A {@link MetadataEntry} that looks up the value of a property.
 */
public class PropertyMetadataEntry extends MetadataEntry {

	protected String propertyName;
	
	/**
	 * Creates a property metadata entry.
	 */
	public PropertyMetadataEntry() {
		// do nothing
	}
	
	/**
	 * Creates a property metadata entry with the specified name and property
	 * name.
	 * 
	 * @param name the metadata entry name
	 * @param propertyName the property name
	 */
	public PropertyMetadataEntry(String name, String propertyName) {
		setName(name);
		setPropertyName(propertyName);
	}
	
	/**
	 * Sets the property name that will be read.
	 * 
	 * @param propertyName the property name
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	@Override
	public String readActualValue() throws Exception {
		return LocalProperties.get(propertyName);
	}
}
