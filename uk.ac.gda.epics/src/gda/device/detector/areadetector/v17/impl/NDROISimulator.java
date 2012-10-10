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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDROI;

public class NDROISimulator implements NDROI {

	NDPluginBase pluginBase;

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}	
	
	@Override
	public String getLabel() throws Exception {

		return null;
	}

	@Override
	public void setLabel(String label) throws Exception {


	}

	@Override
	public String getLabel_RBV() throws Exception {

		return null;
	}

	@Override
	public int getBinX() throws Exception {

		return 0;
	}

	@Override
	public void setBinX(int binx) throws Exception {


	}

	@Override
	public int getBinX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getBinY() throws Exception {

		return 0;
	}

	@Override
	public void setBinY(int biny) throws Exception {


	}

	@Override
	public int getBinY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getBinZ() throws Exception {

		return 0;
	}

	@Override
	public void setBinZ(int binz) throws Exception {


	}

	@Override
	public int getBinZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMinX() throws Exception {

		return 0;
	}

	@Override
	public void setMinX(int minx) throws Exception {


	}

	@Override
	public int getMinX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMinY() throws Exception {

		return 0;
	}

	@Override
	public void setMinY(int miny) throws Exception {


	}

	@Override
	public int getMinY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMinZ() throws Exception {

		return 0;
	}

	@Override
	public void setMinZ(int minz) throws Exception {


	}

	@Override
	public int getMinZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getSizeX() throws Exception {

		return 0;
	}

	@Override
	public void setSizeX(int sizex) throws Exception {


	}

	@Override
	public int getSizeX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getSizeY() throws Exception {

		return 0;
	}

	@Override
	public void setSizeY(int sizey) throws Exception {


	}

	@Override
	public int getSizeY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getSizeZ() throws Exception {

		return 0;
	}

	@Override
	public void setSizeZ(int sizez) throws Exception {


	}

	@Override
	public int getSizeZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMaxSizeX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMaxSizeY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMaxSizeZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getReverseX() throws Exception {

		return 0;
	}

	@Override
	public void setReverseX(int reversex) throws Exception {


	}

	@Override
	public short getReverseX_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getReverseY() throws Exception {

		return 0;
	}

	@Override
	public void setReverseY(int reversey) throws Exception {


	}

	@Override
	public short getReverseY_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getReverseZ() throws Exception {

		return 0;
	}

	@Override
	public void setReverseZ(int reversez) throws Exception {


	}

	@Override
	public short getReverseZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySizeX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySizeY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySizeZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public boolean isScalingEnabled() throws Exception {

		return false;
	}

	@Override
	public void enableScaling() throws Exception {


	}

	@Override
	public void disableScaling() throws Exception {


	}

	@Override
	public short isScalingEnabled_RBV() throws Exception {

		return 0;
	}

	@Override
	public double getScale() throws Exception {

		return 0;
	}

	@Override
	public void setScale(double scale) throws Exception {


	}

	@Override
	public double getScale_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getDataTypeOut() throws Exception {

		return 0;
	}

	@Override
	public void setDataTypeOut(int datatypeout) throws Exception {


	}

	@Override
	public short getDataTypeOut_RBV() throws Exception {

		return 0;
	}

	@Override
	public void setAreaDetectorROI(AreaDetectorROI areaDetectorROI) throws Exception {


	}

	@Override
	public AreaDetectorROI getAreaDetectorROI() throws Exception {

		return null;
	}

	@Override
	public void reset() throws Exception {


	}

	@Override
	public boolean isEnableX() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnableY() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnableZ() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enableX() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableX() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableY() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableY() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableZ() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableZ() {
		// TODO Auto-generated method stub
		
	}

}
