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
 * 
 */
public interface ChipPixel {
	/**
	 * 
	 */
	short[] getMask() throws Exception;

	/**
	 * 
	 */
	public void setMask(short[] mask) throws Exception;

	/**
	 * 
	 */
	short[] getTest() throws Exception;

	/**
	 * 
	 */
	public void setTest(short[] test) throws Exception;

	/**
	 * 
	 */
	short[] getGainMode() throws Exception;

	/**
	 * 
	 */
	public void setGainMode(short[] gainMode) throws Exception;

	/**
	 * 
	 */
	short[] getThresholdA() throws Exception;

	/**
	 * 
	 */
	public void setThresholdA(short[] thresholdA) throws Exception;

	/**
	 * 
	 */
	short[] getThresholdB() throws Exception;

	/**
	 * 
	 */
	public void setThresholdB(short[] thresholdB) throws Exception;
}