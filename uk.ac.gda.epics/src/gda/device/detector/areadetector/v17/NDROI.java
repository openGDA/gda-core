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

package gda.device.detector.areadetector.v17;

import gda.device.detector.areadetector.AreaDetectorROI;
import gda.observable.Observable;

/**
 * This maps to the roi plugin on the edm screen.
 */
public interface NDROI {
	/**
	 * List all the PVs
	 */

	static final String Label = "Name";

	static final String Label_RBV = "Name_RBV";

	static final String BinX = "BinX";

	static final String BinX_RBV = "BinX_RBV";

	static final String BinY = "BinY";

	static final String BinY_RBV = "BinY_RBV";

	static final String BinZ = "BinZ";

	static final String BinZ_RBV = "BinZ_RBV";

	static final String MinX = "MinX";

	static final String MinX_RBV = "MinX_RBV";

	static final String MinY = "MinY";

	static final String MinY_RBV = "MinY_RBV";

	static final String MinZ = "MinZ";

	static final String MinZ_RBV = "MinZ_RBV";

	static final String SizeX = "SizeX";

	static final String SizeX_RBV = "SizeX_RBV";

	static final String SizeY = "SizeY";

	static final String SizeY_RBV = "SizeY_RBV";

	static final String SizeZ = "SizeZ";

	static final String SizeZ_RBV = "SizeZ_RBV";

	static final String MaxSizeX_RBV = "MaxSizeX_RBV";

	static final String MaxSizeY_RBV = "MaxSizeY_RBV";

	static final String MaxSizeZ_RBV = "MaxSizeZ_RBV";

	static final String ReverseX = "ReverseX";

	static final String ReverseX_RBV = "ReverseX_RBV";

	static final String ReverseY = "ReverseY";

	static final String ReverseY_RBV = "ReverseY_RBV";

	static final String ReverseZ = "ReverseZ";

	static final String ReverseZ_RBV = "ReverseZ_RBV";

	static final String ArraySizeX_RBV = "ArraySizeX_RBV";

	static final String ArraySizeY_RBV = "ArraySizeY_RBV";

	static final String ArraySizeZ_RBV = "ArraySizeZ_RBV";

	static final String EnableScale = "EnableScale";

	static final String EnableScale_RBV = "EnableScale_RBV";

	static final String Scale = "Scale";

	static final String Scale_RBV = "Scale_RBV";

	static final String DataTypeOut = "DataTypeOut";

	static final String DataTypeOut_RBV = "DataTypeOut_RBV";

	static final String EnableX = "EnableX";

	static final String EnableY = "EnableY";

	static final String EnableZ = "EnableZ";

	/**
	 *
	 */
	String getLabel() throws Exception;

	/**
	 *
	 */
	void setLabel(String label) throws Exception;

	/**
	 *
	 */
	String getLabel_RBV() throws Exception;

	/**
	 *
	 */
	int getBinX() throws Exception;

	/**
	 *
	 */
	void setBinX(int binx) throws Exception;

	/**
	 *
	 */
	int getBinX_RBV() throws Exception;

	/**
	 *
	 */
	int getBinY() throws Exception;

	/**
	 *
	 */
	void setBinY(int biny) throws Exception;

	/**
	 *
	 */
	int getBinY_RBV() throws Exception;

	/**
	 *
	 */
	int getBinZ() throws Exception;

	/**
	 *
	 */
	void setBinZ(int binz) throws Exception;

	/**
	 *
	 */
	int getBinZ_RBV() throws Exception;

	/**
	 *
	 */
	int getMinX() throws Exception;

	/**
	 *
	 */
	void setMinX(int minx) throws Exception;

	/**
	 *
	 */
	int getMinX_RBV() throws Exception;

	/**
	 *
	 */
	int getMinY() throws Exception;

	/**
	 *
	 */
	void setMinY(int miny) throws Exception;

	/**
	 *
	 */
	int getMinY_RBV() throws Exception;

	/**
	 *
	 */
	int getMinZ() throws Exception;

	/**
	 *
	 */
	void setMinZ(int minz) throws Exception;

	/**
	 *
	 */
	int getMinZ_RBV() throws Exception;

	/**
	 *
	 */
	int getSizeX() throws Exception;

	/**
	 *
	 */
	void setSizeX(int sizex) throws Exception;

	/**
	 *
	 */
	int getSizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getSizeY() throws Exception;

	/**
	 *
	 */
	void setSizeY(int sizey) throws Exception;

	/**
	 *
	 */
	int getSizeY_RBV() throws Exception;

	/**
	 *
	 */
	int getSizeZ() throws Exception;

	/**
	 *
	 */
	void setSizeZ(int sizez) throws Exception;

	/**
	 *
	 */
	int getSizeZ_RBV() throws Exception;

	/**
	 *
	 */
	int getMaxSizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getMaxSizeY_RBV() throws Exception;

	/**
	 *
	 */
	int getMaxSizeZ_RBV() throws Exception;

	/**
	 *
	 */
	short getReverseX() throws Exception;

	/**
	 *
	 */
	void setReverseX(int reversex) throws Exception;

	/**
	 *
	 */
	short getReverseX_RBV() throws Exception;

	/**
	 *
	 */
	short getReverseY() throws Exception;

	/**
	 *
	 */
	void setReverseY(int reversey) throws Exception;

	/**
	 *
	 */
	short getReverseY_RBV() throws Exception;

	/**
	 *
	 */
	short getReverseZ() throws Exception;

	/**
	 *
	 */
	void setReverseZ(int reversez) throws Exception;

	/**
	 *
	 */
	short getReverseZ_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySizeY_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySizeZ_RBV() throws Exception;

	/**
	 *
	 */
	boolean isScalingEnabled() throws Exception;

	/**
	 *
	 */
	void enableScaling() throws Exception;

	/**
	 *
	 */
	void disableScaling() throws Exception;

	/**
	 *
	 */
	short isScalingEnabled_RBV() throws Exception;

	/**
	 *
	 */
	double getScale() throws Exception;

	/**
	 *
	 */
	void setScale(double scale) throws Exception;

	/**
	 *
	 */
	double getScale_RBV() throws Exception;

	/**
	 *
	 */
	short getDataTypeOut() throws Exception;

	/**
	 *
	 */
	void setDataTypeOut(int datatypeout) throws Exception;

	/**
	 *
	 */
	short getDataTypeOut_RBV() throws Exception;

	/**
	 * @return true if enabled
	 * @throws Exception 
	 */
	boolean isEnableX() throws Exception;

	/**
	 * @return true if enabled
	 * @throws Exception 
	 */
	boolean isEnableY() throws Exception;

	/**
	 * @return true if enabled
	 * @throws Exception 
	 */
	boolean isEnableZ() throws Exception;

	/**
	 * @throws Exception 
	 * 
	 */
	void enableX() throws Exception;

	/**
	 * @throws Exception 
	 * 
	 */
	void disableX() throws Exception;

	/**
	 * @throws Exception 
	 * 
	 */
	void enableY() throws Exception;

	/**
	 * @throws Exception 
	 * 
	 */
	void disableY() throws Exception;

	/**
	 * @throws Exception 
	 * 
	 */
	void enableZ() throws Exception;

	/**
	 * @throws Exception 
	 * 
	 */
	void disableZ() throws Exception;

	/**
	 * @param areaDetectorROI
	 * @throws Exception
	 */
	void setAreaDetectorROI(AreaDetectorROI areaDetectorROI) throws Exception;

	/**
	 * @return {@link NDPluginBase}
	 */
	NDPluginBase getPluginBase();

	/**
	 * @return {@link AreaDetectorROI}
	 * @throws Exception
	 */
	AreaDetectorROI getAreaDetectorROI() throws Exception;

	/**
	 * 
	 */
	void reset() throws Exception;

	Observable<Integer> createMinXObservable() throws Exception;

	Observable<Integer> createMinYObservable() throws Exception;

	Observable<Integer> createSizeXObservable() throws Exception;

	Observable<Integer> createSizeYObservable() throws Exception;

	Observable<String> createEnableXObservable() throws Exception;

	Observable<String> createEnableYObservable() throws Exception;
}