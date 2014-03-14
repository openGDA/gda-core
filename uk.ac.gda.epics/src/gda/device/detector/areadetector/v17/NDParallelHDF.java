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

package gda.device.detector.areadetector.v17;

public interface NDParallelHDF extends NDFile{
	
	double getTransferRate() throws Exception;
	
	double getTransferRateAverage() throws Exception;
	
	int getConnectionState() throws Exception;
	
	int getStoreAttr() throws Exception;

	void setStoreAttr(int storeAttr) throws Exception;

	int getStorePerform() throws Exception;

	void setStorePerform(int storePerform) throws Exception;
	
	double getRuntime() throws Exception;

	double getIOSpeed() throws Exception;
	
	public enum ChunkSizeMode {
		AUTO,
		MANUAL,
		NDATTR
	}
	

	int getChunkSizeMode() throws Exception;
	
	void setChunkSizeMode(int chunkSizeMode) throws Exception;
	
	
	int getChunkSize0() throws Exception;

	void setChunkSize0(int value) throws Exception;

	int getChunkSize1() throws Exception;

	void setChunkSize1(int value) throws Exception;

	int getChunkSize2() throws Exception;

	void setChunkSize2(int value) throws Exception;

	public enum DsetSizeMode {
		AUTO,
		MANUAL,
		NDATTR
	}
	

	int getDsetSizeMode() throws Exception;
	
	void setDsetSizeMode(int dsetSizeMode) throws Exception;
	
	int getDsetSize0() throws Exception;

	void setDsetSize0(int value) throws Exception;

	int getDsetSize1() throws Exception;

	void setDsetSize1(int value) throws Exception;

	int getDsetSize2() throws Exception;

	void setDsetSize2(int value) throws Exception;
	

	public enum RoiPosMode {
		AUTO,
		MANUAL,
		NDATTR
	}
	

	int getRoiPosMode() throws Exception;
	
	void setRoiPosMode(int dsetSizeMode) throws Exception;
	
	int getRoiPos0() throws Exception;

	void setRoiPos0(int value) throws Exception;

	int getRoiPos1() throws Exception;

	void setRoiPos1(int value) throws Exception;

	int getRoiPos2() throws Exception;

	void setRoiPos2(int value) throws Exception;
	
}
