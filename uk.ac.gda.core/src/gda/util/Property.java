/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import java.util.Vector;

/**
 * Property Class
 */
public class Property {
	String name = null;

	String purpose = null;

	String type = null;

	String defaultValue = null;

	/**
	 * @param name
	 * @param purpose
	 * @param type
	 * @param defaultValue
	 */
	public Property(String name, String purpose, String type, String defaultValue) {
		setBasics(name, purpose);
		this.type = type;
		this.defaultValue = defaultValue;
	}

	/**
	 * @param name
	 * @param purpose
	 */
	public Property(String name, String purpose) {
		setBasics(name, purpose);
	}

	private void setBasics(String name, String purpose) {
		this.name = name;
		this.purpose = purpose;
	}

	/**
	 * @param sProps
	 * @return Vector of Property
	 */
	public static Vector<Property> toVector(Property[] sProps) {
		Vector<Property> vProps = new Vector<Property>();
		for (int i = 0; i < sProps.length; i++)
			vProps.add(sProps[i]);
		return vProps;
	}

	@Override
	public String toString() {
		String output = "NAME: " + name + "\n";
		output += "PURPOSE: " + purpose + "\n";
		if (type != null)
			output += "TYPE: " + type + "\n";
		if (defaultValue != null)
			output += "DEFAULT VALUE: " + defaultValue + "\n";
		return (output);
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return purposes
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * @param purpose
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
}