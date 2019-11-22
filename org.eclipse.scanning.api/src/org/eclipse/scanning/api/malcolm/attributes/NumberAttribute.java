/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.malcolm.attributes;

/**
 *
 * Encapsulates a number attribute as read from a malcolm device
 *
 * @author Matt Taylor
 *
 */
public class NumberAttribute extends MalcolmAttribute<Number> {

	private String dtype;
	private Number value;

	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}

	@Override
	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dtype == null) ? 0 : dtype.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumberAttribute other = (NumberAttribute) obj;
		if (dtype == null) {
			if (other.dtype != null)
				return false;
		} else if (!dtype.equals(other.dtype))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NumberAttribute [value=" + value + ", getName()=" + getName() + "]";
	}

}
