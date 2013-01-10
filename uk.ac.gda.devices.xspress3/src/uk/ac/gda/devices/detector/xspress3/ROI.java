/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class ROI implements Serializable {

	private int start;
	private int end;
	private String name = "<need to set a name>";

	public ROI() {
	}

	public ROI(String roiName, int roiStart, int roiEnd) {
		this.name=roiName;
		this.end = roiEnd;
		this.start = roiStart;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ROI other = (ROI) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String roiName) {
		this.name = roiName;
	}

	public void setStart(int roiStart) {
		this.start = roiStart;
	}

	public void setEnd(int roiEnd) {
		this.end = roiEnd;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}
}
