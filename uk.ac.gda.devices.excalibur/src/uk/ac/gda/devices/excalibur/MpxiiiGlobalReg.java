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

/**
 * Interface that corresponds to $excalibur_ioc/excaliburApp/Db/mpxiiiGlobalReg.template
 * 
 * 
 */
public interface MpxiiiGlobalReg {
	/**
	 * 
	 */
	int getColourMode() throws Exception;

	/**
	 * 
	 */
	String getColourModeAsString() throws Exception;

	/**
	 * 
	 */
	public void setColourMode(int index) throws Exception;

	/**
	 * 
	 */
	String[] getColourModeLabels() throws Exception;

	/**
	 * 
	 */
	double getDacNumber() throws Exception;

	/**
	 * 
	 */
	public void setDacNumber(double dacNumber) throws Exception;

	/**
	 * 
	 */
	double getDacNameCalc1() throws Exception;

	/**
	 * 
	 */
	public void setDacNameCalc1(double dacNameCalc1) throws Exception;

	/**
	 * 
	 */
	double getDacNameCalc2() throws Exception;

	/**
	 * 
	 */
	public void setDacNameCalc2(double dacNameCalc2) throws Exception;

	/**
	 * 
	 */
	double getDacNameCalc3() throws Exception;

	/**
	 * 
	 */
	public void setDacNameCalc3(double dacNameCalc3) throws Exception;

	/**
	 * 
	 */
	int getDacNameSel1() throws Exception;

	/**
	 * 
	 */
	public void setDacNameSel1(int dacNameSel1) throws Exception;

	/**
	 * 
	 */
	int getDacNameSel2() throws Exception;

	/**
	 * 
	 */
	public void setDacNameSel2(int dacNameSel2) throws Exception;

	/**
	 * 
	 */
	int getDacNameSel3() throws Exception;

	/**
	 * 
	 */
	public void setDacNameSel3(int dacNameSel3) throws Exception;

	/**
	 * 
	 */
	String getDacName() throws Exception;

	/**
	 * 
	 */
	public void setDacName(String dacName) throws Exception;

	/**
	 * 
	 */
	public void setColourModeAsString(String colourMode) throws Exception;

	/**
	 * 
	 */
	String[] getCounterDepthLabels() throws Exception;

	/**
	 * 
	 */
	int getCounterDepth() throws Exception;

	/**
	 * 
	 */
	String getCounterDepthAsString() throws Exception;

	/**
	 * 
	 */
	public void setCounterDepth(int counterDepth) throws Exception;

	/**
	 * 
	 */
	public void setCounterDepthAsString(String counterDepth) throws Exception;

}
