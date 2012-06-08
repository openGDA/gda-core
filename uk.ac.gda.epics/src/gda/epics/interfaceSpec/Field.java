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
 * Field interface
 */
public interface Field {
	/**
	 * @return name
	 */
	public String getName();

	/**
	 * @param attributeName
	 * @return Attribute
	 */
	public Attribute getAttribute(String attributeName);

	/**
	 * @return PV
	 */
	public String getPV();

	/**
	 * @return boolean
	 */
	public boolean isReadOnly();
	/**
	 * @return The type
	 */
	public String getType();

	/**
	 * @return attribute names
	 */
	public Iterator<String> getAttributeNames();
}
