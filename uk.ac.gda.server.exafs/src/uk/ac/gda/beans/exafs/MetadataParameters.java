/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

public class MetadataParameters implements Serializable{
	private String scannableName;
	
	/**
	 * 
	 */
	public MetadataParameters() {
		
	}
	
	public MetadataParameters(String scannableName) {
		this.scannableName = scannableName;
	}
	
	/**
	 * @return Returns the scannableName.
	 */
	public String getScannableName() {
		return scannableName;
	}

	/**
	 * @param scannableName The scannableName to set.
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public int hashCode() {
		return 1;
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
		MetadataParameters other = (MetadataParameters) obj;
		if (scannableName == null) {
			if (other.scannableName != null) {
				return false;
			}
		} else if (!scannableName.equals(other.scannableName)) {
			return false;
		}
		return true;
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
}
