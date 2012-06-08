/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.epics.interfaceSpec;

import java.util.Iterator;

/**
 * Device Interface
 */
public interface Device {

	/**
	 * @return name
	 */
	public String getName();

	/**
	 * @return type
	 */
	public String getType();

	/**
	 * @param fieldName
	 * @return field
	 */
	public Field getField(String fieldName);

	/**
	 * @return Iterator field names
	 */
	public Iterator<String> getFieldNames();
	
	/**
	 * @param attributeName
	 * @return String - value of attribute or null if not found
	 */
	public String getAttributeValue(String attributeName);
	
	/**
	 * @return The value of the desc attribute if given else an empty string
	 */
	public String getDescription();

}
