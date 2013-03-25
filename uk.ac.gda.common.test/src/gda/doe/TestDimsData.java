/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.doe;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.doe.DOEField;

/**
 * Bean to hold slice data
 */
public class TestDimsData implements Serializable {

	
	@DOEField(value=1, type=java.lang.Integer.class)
	private String    sliceRange;

	private int       dimension;
	private int       axis;
	private int       slice;

	public TestDimsData() {
		
	}
	
	public TestDimsData(final int dim) {
		this.dimension = dim;
	}
		


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + axis;
		result = prime * result + dimension;
		result = prime * result + slice;
		result = prime * result + ((sliceRange == null) ? 0 : sliceRange.hashCode());
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
		TestDimsData other = (TestDimsData) obj;
		if (axis != other.axis)
			return false;
		if (dimension != other.dimension)
			return false;
		if (slice != other.slice)
			return false;
		if (sliceRange == null) {
			if (other.sliceRange != null)
				return false;
		} else if (!sliceRange.equals(other.sliceRange))
			return false;
		return true;
	}

	public String getSliceRange() {
		return sliceRange;
	}

	public void setSliceRange(String sliceRange) {
		this.sliceRange = sliceRange;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public int getAxis() {
		return axis;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}

	public int getSlice() {
		return slice;
	}

	public void setSlice(int slice) {
		this.slice = slice;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}
