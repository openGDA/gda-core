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
 * Interface that corresponds to $excalibur_ioc/excaliburApp/Db/mpxiiChipReg.template
 */
public interface MpxiiiChipReg {
	/**
	 * 
	 */
	int getDacSense() throws Exception;

	/**
	 * 
	 */
	public void setDacSense(int dacSense) throws Exception;

	/**
	 * 
	 */
	int getDacSenseDecode() throws Exception;

	/**
	 * 
	 */
	public void setDacSenseDecode(int dacSenseDecode) throws Exception;

	/**
	 * 
	 */
	String getDacSenseName() throws Exception;

	/**
	 * 
	 */
	public void setDacSenseName(String dacSenseName) throws Exception;

	/**
	 * 
	 */
	int getDacExternal() throws Exception;

	/**
	 * 
	 */
	public void setDacExternal(int dacExternal) throws Exception;

	/**
	 * 
	 */
	int getDacExternalDecode() throws Exception;

	/**
	 * 
	 */
	public void setDacExternalDecode(int dacExternalDecode) throws Exception;

	/**
	 * 
	 */
	String getDacExternalName() throws Exception;

	/**
	 * 
	 */
	public void setDacExternalName(String dacExternalName) throws Exception;

	/**
	 */
	ChipAnper getAnper() throws Exception;

	/**
	 * 
	 */
	public void setAnper(ChipAnper anper) throws Exception;

	/**
	 * 
	 */
	ChipPixel getPixel() throws Exception;

	/**
	 * 
	 */
	public void setPixel(ChipPixel pixel) throws Exception;

}
