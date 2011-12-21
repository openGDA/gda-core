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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DimsDataList implements Serializable {

	
	private List<DimsData> dimsData;
		
	public DimsDataList() {
	}
	
	public List<DimsData> getDimsData() {
		return dimsData;
	}

	public void setDimsData(List<DimsData> slices) {
		this.dimsData = slices;
	}
	
	public void add(DimsData dimension) {
		if (dimsData==null) dimsData = new ArrayList<DimsData>(3);
		if (dimsData.size()>dimension.getDimension() && dimension.getDimension()>-1) {
			dimsData.set(dimension.getDimension(), dimension);
		} else {
			dimsData.add(dimension);
		}
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
		DimsDataList other = (DimsDataList) obj;
		if (dimsData == null) {
			if (other.dimsData != null)
				return false;
		} else if (!dimsData.equals(other.dimsData))
			return false;
		return true;
	}

	public static Object[] getDefault() {
		return new DimsData[]{new DimsData(0)};
	}
	
	public Object[] getElements() {
		if (dimsData==null) return null;
		return dimsData.toArray(new DimsData[dimsData.size()]);
	}

	public int size() {
		if (dimsData==null) return 0;
		return dimsData.size();
	}

	public DimsData getDimsData(int i) {
		if (dimsData==null) return null;
		return dimsData.get(i);
	}

	public Iterator<DimsData> iterator() {
		if (dimsData==null) return null;
		return dimsData.iterator();
	}
	
	public void clear() {
		if (dimsData!=null) dimsData.clear();
	}
}
