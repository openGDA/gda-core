/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.hdf5;

import java.io.IOException;

import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazyLoader;

/*
 * Class to create a lazy dataset from an address in a hdf file
 */
public class Hdf5HelperLazyLoader implements ILazyLoader {

	private String fileName;
	private String groupName;
	private String dataSetName;
	private int dtype;
	private Hdf5HelperData helperData = null;

	public int getDType() throws Exception {
		checkConfigured();
		return dtype;
	}

	public Hdf5HelperLazyLoader(String fileName, String groupName, String dataSetName, boolean extend) {
		super();
		this.fileName = fileName;
		this.groupName = groupName;
		this.dataSetName = dataSetName;
		this.extend = extend;
	}

	private boolean extend;

	@Override
	public boolean isFileReadable() {
		return true;
	}

	private void checkConfigured() throws Exception {
		if (helperData == null) {
			helperData = Hdf5Helper.getInstance().readDataSetAll(fileName, groupName, dataSetName, false);
			dtype = HDF5Utils.getDType(helperData.h5Datatype.getDatatypeClass(),
					(int) helperData.h5Datatype.getDatatypeSize());
		}
	}

	@Override
	public Dataset getDataset(IMonitor mon, SliceND slice) throws IOException {
		try {
			checkConfigured();
			int rank = slice.getSourceShape().length;
			long[] sstart = new long[rank];
			long[] sstride = new long[rank];
			long[] dsize = new long[rank];
			int[] start = slice.getStart();
			int[] step  = slice.getStep();
			int[] shape = slice.getShape();
			for (int i = 0; i < rank; i++) {
				sstart[i]  = start[i];
				sstride[i] = step[i];
				dsize[i]   = shape[i];
			}
			Object data2 = Hdf5Helper.getInstance().readDataSet(fileName, groupName, dataSetName, sstart, sstride, dsize, null, null, dsize, helperData.native_type, null, true).data;
			return HDF5Utils.createDataset(data2, shape, dtype, extend);
		} catch (Exception e) {
			throw new IOException("Error reading from " + fileName,e);
		}
	}

	public ILazyDataset getLazyDataSet() throws Exception{
		checkConfigured();

		int [] dims = new int[helperData.dims.length];
		for( int i=0; i< dims.length;i++){
			dims[i] = (int) helperData.dims[i];
		}
		return new LazyDataset(dataSetName, dtype, dims, this);
	}
}
