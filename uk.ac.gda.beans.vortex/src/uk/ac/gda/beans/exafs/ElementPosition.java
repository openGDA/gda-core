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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

/**
 *
 */
public class ElementPosition implements Serializable {

	private String  name;
	private String  principleElement;
	private Integer wheelPosition;
	
	/**
	 * 
	 */
	public ElementPosition() {
		
	}
	/**
	 * @param name
	 * @param pos
	 */
	public ElementPosition(String name, int pos) {
		this.name            = name;
		this.principleElement= name;
		this.wheelPosition   = pos;
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	/**
	 * @return Returns the elementName.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param elementName The elementName to set.
	 */
	public void setName(String elementName) {
		this.name = elementName;
	}

	/**
	 * @return Returns the wheelPosition.
	 */
	public Integer getWheelPosition() {
		return wheelPosition;
	}

	/**
	 * @param wheelPosition The wheelPosition to set.
	 */
	public void setWheelPosition(Integer wheelPosition) {
		this.wheelPosition = wheelPosition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime
				* result
				+ ((principleElement == null) ? 0 : principleElement.hashCode());
		result = prime * result
				+ ((wheelPosition == null) ? 0 : wheelPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ElementPosition other = (ElementPosition) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (principleElement == null) {
			if (other.principleElement != null) {
				return false;
			}
		} else if (!principleElement.equals(other.principleElement)) {
			return false;
		}
		if (wheelPosition == null) {
			if (other.wheelPosition != null) {
				return false;
			}
		} else if (!wheelPosition.equals(other.wheelPosition)) {
			return false;
		}
		return true;
	}
	/**
	 * @return Returns the principleElement.
	 */
	public String getPrincipleElement() {
		return principleElement;
	}
	/**
	 * @param principleElement The principleElement to set.
	 */
	public void setPrincipleElement(String principleElement) {
		this.principleElement = principleElement;
	}

}
