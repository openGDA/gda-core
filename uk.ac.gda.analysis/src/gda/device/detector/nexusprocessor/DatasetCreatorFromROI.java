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

package gda.device.detector.nexusprocessor;

import gda.factory.Findable;

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Slice;

public class DatasetCreatorFromROI implements DatasetCreator, Findable {

	private Slice[] sliceArray;

	public void setROI( int startX, int stopX, int startY, int stopY){
		setSliceArray( new Slice[]{ new Slice(startX, stopX, 1),new Slice(startY, stopY, 1)});
		setEnable(true);
	}
	@Override
	public Dataset createDataSet(Dataset ds) {
		Dataset result = ds;
		if( enable && ( sliceArray != null)){
			if( sliceArray.length != ds.getRank())
				throw new IllegalArgumentException("sliceArray.length :" +  sliceArray.length + " != ds.getRank() : " + ds.getRank());
			result = ds.getSlice(sliceArray);

		}
		return result;
	}

	public Slice[] getSliceArray() {
		return sliceArray;
	}

	public void setSliceArray(Slice[] sliceArray) {
		this.sliceArray = sliceArray;
	}

	private String name;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private boolean enable;

	public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	@Override
	public String toString() {
		return "DatasetCreatorFromROI [sliceArray=" + Arrays.toString(sliceArray) + ", enable="
				+ enable + "]";
	}

}
