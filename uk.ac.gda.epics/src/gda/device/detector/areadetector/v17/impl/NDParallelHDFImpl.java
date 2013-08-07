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

package gda.device.detector.areadetector.v17.impl;

import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDParallelHDF;
import gda.epics.CachedLazyPVFactory;

/**
 * class to implement the Area Detector Parallel-HDF plugin 
 */
public class NDParallelHDFImpl extends NDFileImpl implements NDParallelHDF, InitializingBean {

	private static final String ROI_POS2 = "RoiPos2";
	private static final String ROI_POS2_RBV = "RoiPos2_RBV";
	private static final String ROI_POS1 = "RoiPos1";
	private static final String ROI_POS1_RBV = "RoiPos1_RBV";
	private static final String ROI_POS0 = "RoiPos0";
	private static final String ROI_POS0_RBV = "RoiPos0_RBV";
	private static final String ROI_POS_MODE = "RoiPosMode";
	private static final String ROI_POS_MODE_RBV = "RoiPosMode_RBV";
	private static final String DSET_SIZE2 = "DsetSize2";
	private static final String DSET_SIZE2_RBV = "DsetSize2_RBV";
	private static final String DSET_SIZE1 = "DsetSize1";
	private static final String DSET_SIZE1_RBV = "DsetSize1_RBV";
	private static final String DSET_SIZE0 = "DsetSize0";
	private static final String DSET_SIZE0_RBV = "DsetSize0_RBV";
	private static final String DSET_SIZE_MODE = "DsetSizeMode";
	private static final String DSET_SIZE_MODE_RBV = "DsetSizeMode_RBV";
	private static final String CHUNK_SIZE2 = "ChunkSize2";
	private static final String CHUNK_SIZE2_RBV = "ChunkSize2_RBV";
	private static final String CHUNK_SIZE1 = "ChunkSize1";
	private static final String CHUNK_SIZE1_RBV = "ChunkSize1_RBV";
	private static final String CHUNK_SIZE0 = "ChunkSize0";
	private static final String CHUNK_SIZE0_RBV = "ChunkSize0_RBV";
	private static final String CHUNK_SIZE_MODE = "ChunkSizeMode";
	private static final String CHUNK_MODE_SIZE_RBV = "ChunkModeSize_RBV";
	private static final String IO_SPEED = "IOSpeed";
	private static final String RUNTIME = "Runtime";
	private static final String STORE_PERFORM = "StorePerform";
	private static final String STORE_ATTR = "StoreAttr";
	private static final String CONN_STAT = "ConnStat";
	private static final String TRANSFER_RATE_AVG = "TransferRate_AVG";
	private static final String TRANSFER_RATE_RBV = "TransferRate_RBV";
	CachedLazyPVFactory dev;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPvProvider() == null )
			throw new Exception("pvProvider is not set");
		String prefix = getPvProvider().getPV("");
		if( prefix == null || prefix.isEmpty())
			throw new Exception("pvProvider.getPv('') returns an null or empty string");
		dev = new CachedLazyPVFactory(prefix);

	}
	
	
	@Override
	public double getTransferRate() throws Exception {
		return dev.getReadOnlyPVDouble(TRANSFER_RATE_RBV).get();
	}

	@Override
	public double getTransferRateAverage() throws Exception {
		return dev.getReadOnlyPVDouble(TRANSFER_RATE_AVG).get();
	}

	@Override
	public int getConnectionState() throws Exception {
		return dev.getReadOnlyPVInteger(CONN_STAT).get();
	}

	@Override
	public int getStoreAttr() throws Exception {
		return 1 ; //return dev.getReadOnlyPVInteger(STORE_ATTR).get();
	}

	@Override
	public void setStoreAttr(int storeAttr) throws Exception {
		// storeAttr cannot be changed - yet. dev.getPVInteger(STORE_ATTR).putWait(storeAttr);
	}

	@Override
	public int getStorePerform() throws Exception {
		return 1; //return dev.getReadOnlyPVInteger(STORE_PERFORM).get();
	}

	@Override
	public void setStorePerform(int storePerform) throws Exception {
		// storePerform cannot be changed - yet. dev.getPVInteger(STORE_PERFORM).putWait(storePerform);
	}

	@Override
	public double getRuntime() throws Exception {
		return dev.getReadOnlyPVDouble(RUNTIME).get();
	}

	@Override
	public double getIOSpeed() throws Exception {
		return dev.getReadOnlyPVDouble(IO_SPEED).get();
	}

	@Override
	public int getChunkSizeMode() throws Exception {
		return dev.getReadOnlyPVInteger(CHUNK_MODE_SIZE_RBV).get();
	}

	@Override
	public void setChunkSizeMode(int chunkSizeMode) throws Exception {
		dev.getPVInteger(CHUNK_SIZE_MODE).putWait(chunkSizeMode);
	}

	@Override
	public int getChunkSize0() throws Exception {
		return dev.getReadOnlyPVInteger(CHUNK_SIZE0_RBV).get();
	}

	@Override
	public void setChunkSize0(int value) throws Exception {
		dev.getPVInteger(CHUNK_SIZE0).putWait(value);
	}

	@Override
	public int getChunkSize1() throws Exception {
		return dev.getReadOnlyPVInteger(CHUNK_SIZE1_RBV).get();
	}

	@Override
	public void setChunkSize1(int value) throws Exception {
		dev.getPVInteger(CHUNK_SIZE1).putWait(value);
	}

	@Override
	public int getChunkSize2() throws Exception {
		return dev.getReadOnlyPVInteger(CHUNK_SIZE2_RBV).get();
	}

	@Override
	public void setChunkSize2(int value) throws Exception {
		dev.getPVInteger(CHUNK_SIZE2).putWait(value);
	}

	@Override
	public int getDsetSizeMode() throws Exception {
		return dev.getReadOnlyPVInteger(DSET_SIZE_MODE_RBV).get();
	}

	@Override
	public void setDsetSizeMode(int dsetSizeMode) throws Exception {
		dev.getPVInteger(DSET_SIZE_MODE).putWait(dsetSizeMode);
	}

	@Override
	public int getDsetSize0() throws Exception {
		return dev.getReadOnlyPVInteger(DSET_SIZE0_RBV).get();
	}

	@Override
	public void setDsetSize0(int value) throws Exception {
		dev.getPVInteger(DSET_SIZE0).putWait(value);
	}

	@Override
	public int getDsetSize1() throws Exception {
		return dev.getReadOnlyPVInteger(DSET_SIZE1_RBV).get();
	}

	@Override
	public void setDsetSize1(int value) throws Exception {
		dev.getPVInteger(DSET_SIZE1).putWait(value);
	}

	@Override
	public int getDsetSize2() throws Exception {
		return dev.getReadOnlyPVInteger(DSET_SIZE2_RBV).get();
	}

	@Override
	public void setDsetSize2(int value) throws Exception {
		dev.getPVInteger(DSET_SIZE2).putWait(value);
	}

	@Override
	public int getRoiPosMode() throws Exception {
		return dev.getReadOnlyPVInteger(ROI_POS_MODE_RBV).get();
	}

	@Override
	public void setRoiPosMode(int dsetSizeMode) throws Exception {
		dev.getPVInteger(ROI_POS_MODE).putWait(dsetSizeMode);
	}

	@Override
	public int getRoiPos0() throws Exception {
		return dev.getReadOnlyPVInteger(ROI_POS0_RBV).get();
	}

	@Override
	public void setRoiPos0(int value) throws Exception {
		dev.getPVInteger(ROI_POS0).putWait(value);
	}

	@Override
	public int getRoiPos1() throws Exception {
		return dev.getReadOnlyPVInteger(ROI_POS1_RBV).get();
	}

	@Override
	public void setRoiPos1(int value) throws Exception {
		dev.getPVInteger(ROI_POS1).putWait(value);
	}

	@Override
	public int getRoiPos2() throws Exception {
		return dev.getReadOnlyPVInteger(ROI_POS2_RBV).get();
	}

	@Override
	public void setRoiPos2(int value) throws Exception {
		dev.getPVInteger(ROI_POS2).putWait(value);
	}

}
