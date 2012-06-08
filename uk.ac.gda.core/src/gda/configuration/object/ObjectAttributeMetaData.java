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

package gda.configuration.object;

/**
 * A class storing the metadata for the attribute (field) of an object
 */
public class ObjectAttributeMetaData {
	// README - add units/enumeration, min/max value, usage/context
	private String name;

	private String type;

	// non-null if type="enum"
	// private String [] enumeration;

	private String defaultValue;

	// private String minValue;
	// private String maxValue;

	private int minOccurs;

	private int maxOccurs;

	// usage/context
	// private String description;

	/**
	 * @param name
	 *            the name of the object attribute
	 * @param type
	 *            the type of the attribute
	 * @param defaultValue
	 *            the default value of the attribute
	 * @param minOccurs
	 *            the minimum number of occurrences allowed
	 * @param maxOccurs
	 *            the maximum number of occurrences allowed
	 */
	public ObjectAttributeMetaData(String name, String type, String defaultValue, int minOccurs, int maxOccurs) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 *            The defaultValue to set.
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return Returns the maxOccurs.
	 */
	public int getMaxOccurs() {
		return maxOccurs;
	}

	/**
	 * @param maxOccurs
	 *            The maxOccurs to set.
	 */
	public void setMaxOccurs(int maxOccurs) {
		this.maxOccurs = maxOccurs;
	}

	/**
	 * @return Returns the minOccurs.
	 */
	public int getMinOccurs() {
		return minOccurs;
	}

	/**
	 * @param minOccurs
	 *            The minOccurs to set.
	 */
	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
}
