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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestDimsDataList implements Serializable {

	
	private List<TestDimsData> dimsData;
		
	public TestDimsDataList() {
	}
	
	public List<TestDimsData> getDimsData() {
		return dimsData;
	}

	public void setDimsData(List<TestDimsData> slices) {
		this.dimsData = slices;
	}
	
	public void add(TestDimsData dimension) {
		if (dimsData==null) dimsData = new ArrayList<TestDimsData>(3);
//		if (sliceData.size()>dimension.getDimension() && dimension.getDimension()>-1) {
//		    sliceData.set(dimension.getDimension(), dimension);
//		} else {
			dimsData.add(dimension);
//		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimsData == null) ? 0 : dimsData.hashCode());
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
		TestDimsDataList other = (TestDimsDataList) obj;
		if (dimsData == null) {
			if (other.dimsData != null)
				return false;
		} else if (!dimsData.equals(other.dimsData))
			return false;
		return true;
	}

	public Object[] getElements() {
		if (dimsData==null) return null;
		return dimsData.toArray(new TestDimsData[dimsData.size()]);
	}

	public int size() {
		if (dimsData==null) return 0;
		return dimsData.size();
	}

	public TestDimsData getSliceData(int i) {
		if (dimsData==null) return null;
		return dimsData.get(i);
	}

	public Iterator<TestDimsData> iterator() {
		if (dimsData==null) return null;
		return dimsData.iterator();
	}
}
