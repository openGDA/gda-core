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

package uk.ac.gda.doe;

import java.io.Serializable;

public class FieldValue implements Serializable {

	private String name,value;
    private Object originalObject;
	public FieldValue() {
		
	}
	
	public FieldValue(final Object originalObject, String name, String value) {
		super();
		setOriginalObject(originalObject);
		setName(name);
		setValue(value);
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * NOTE: Intentionally not using value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((originalObject == null) ? 0 : originalObject.hashCode());
		return result;
	}

	/**
	 * NOTE: Intentionally not using value
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldValue other = (FieldValue) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (originalObject == null) {
			if (other.originalObject != null)
				return false;
		} else if (!originalObject.equals(other.originalObject))
			return false;
		return true;
	}

	/**
	 * @return Returns the originalObject.
	 */
	public Object getOriginalObject() {
		return originalObject;
	}

	/**
	 * @param originalObject The originalObject to set.
	 */
	public void setOriginalObject(Object originalObject) {
		this.originalObject = originalObject;
	}
}
