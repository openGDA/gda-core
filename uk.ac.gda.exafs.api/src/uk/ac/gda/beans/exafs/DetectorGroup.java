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
import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;

/**
 *
 */
public class DetectorGroup implements Serializable {
	private static final long serialVersionUID = -7901297089683824142L;
	private String name;
	private String [] detector;

	/**
	 * One used by castor.
	 */
	public DetectorGroup() {
		super();
	}

	/**
	 * Testing only.
	 * @param name
	 * @param detector
	 */
	public DetectorGroup(String name, String[] detector) {
		this.name     = name;
		this.detector = detector;
	}
	/**
	 *
	 */
	public void clear() {
		detector = null;
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
	 * @return Returns the detectors.
	 */
	public String[] getDetector() {
		return detector;
	}
	/**
	 * @param detector The detector to set.
	 */
	public void setDetector(String[] detector) {
		this.detector = detector;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(detector);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DetectorGroup other = (DetectorGroup) obj;
		if (!Arrays.equals(detector, other.detector)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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

