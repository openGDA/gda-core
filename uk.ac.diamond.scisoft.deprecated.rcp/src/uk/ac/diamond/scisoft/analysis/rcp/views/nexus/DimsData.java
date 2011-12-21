/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.views.nexus;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.doe.DOEField;

/**
 * Bean to hold slice data
 */
public class DimsData implements Serializable {

	
	@DOEField(value=1, type=java.lang.Integer.class)
	private String    sliceRange;

	private int       dimension=-1;
	private int       axis=-1;
	private int       slice=0;

	public DimsData() {
		
	}
	
	public DimsData(final int dim) {
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
		DimsData other = (DimsData) obj;
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
		if (axis>-1) return null;
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
		if (axis>-1) return -1;
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
