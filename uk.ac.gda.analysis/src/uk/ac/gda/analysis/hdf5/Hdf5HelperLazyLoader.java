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

import gda.analysis.io.ScanFileHolderException;

import java.util.Arrays;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

/*
 * Class to create a lazy dataset from an address in a hdf file
 */
public class Hdf5HelperLazyLoader implements ILazyLoader {

	private String fileName;
	private String groupName;
	private String dataSetName;
	private int dtype;
	private Hdf5HelperData helperData=null;
	
	
	
	public int getDtype() throws Exception {
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

	private void checkConfigured() throws Exception{
		if(helperData == null ){
			helperData = Hdf5Helper.getInstance().readDataSetAll(fileName, groupName, dataSetName, false);
			dtype = HDF5Loader.getDtype(helperData.h5Datatype.getDatatypeClass(), helperData.h5Datatype.getDatatypeSize());
		}
	}
	@Override
	public AbstractDataset getDataset(IMonitor mon, int[] shape, int[] start, int[] stop, int[] step)
			throws ScanFileHolderException {
		try {
			checkConfigured();
			if (step == null) {
				step = new int[shape.length];
				Arrays.fill(step, 1);
			}
//			AbstractDataset.checkSlice(shape, start, stop, start, stop, step);		
			long[] sstart = new long[start.length];
			long[] sstride = new long[start.length];
			long[] dsize = new long[start.length];
			int[] int_dsize = new int[start.length];
			for( int i=0; i< start.length; i++){
				sstart[i] = start[i];
				sstride[i] = step[i];
				dsize[i] = (stop[i]-start[i])/step[i];
				int_dsize[i] = (int) dsize[i];
			}
				Object data2 = Hdf5Helper.getInstance().readDataSet(fileName, groupName, dataSetName, sstart, sstride, dsize, null, null, dsize, helperData.native_type, null, true).data;
				return HDF5Loader.createDataset(data2, int_dsize, dtype, extend);
		} catch (Exception e) {
			throw new ScanFileHolderException("Error reading from " + fileName,e);
		}
	}
	
	public ILazyDataset getLazyDataSet() throws Exception{
		checkConfigured();
		
		int [] dims = new int[helperData.dims.length];
		for( int i=0; i< dims.length;i++){
			dims[i] = (int) helperData.dims[i];
		}
		return new LazyDataset(dataSetName, dtype,dims, this);
	}
	

}
