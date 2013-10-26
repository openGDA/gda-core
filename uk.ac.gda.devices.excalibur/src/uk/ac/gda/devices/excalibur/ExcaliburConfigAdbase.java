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

package uk.ac.gda.devices.excalibur;

import gda.device.detector.areadetector.v17.ADBase;

/**
 * 
 */
public interface ExcaliburConfigAdbase extends ADBase{

	/**
	 * 
	 */
	public int getCounterDepth() throws Exception;

	/**
	 * 
	 */
	public void setCounterDepth(int counterDepth) throws Exception;

	/**
	 * 
	 */
	public ArrayCounts getArrayCounts() throws Exception;

	short[] getPixelMask() throws Exception;

	void setPixelMask(short[] pixelMask) throws Exception;

	short[] getPixelTest() throws Exception;

	void setPixelTest(short[] pixelMask) throws Exception;

	void setPixelGainMode(short[] pixelGainMode) throws Exception;

	short[] getPixelGainMode() throws Exception;

	void setPixelThresholdA(short[] pixelThresholdA) throws Exception;

	short[] getPixelThresholdA() throws Exception;

	short[] getPixelThresholdB() throws Exception;

	void setPixelThresholdB(short[] pixelThresholdB) throws Exception;
	
	void setChipEnable(short [] enableBits) throws Exception;
	
	short[] getChipEnable() throws Exception;

}
