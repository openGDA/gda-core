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

import gda.device.detector.areadetector.v17.NDPluginBase;

/**
 * 
 */
public interface Gap {

	/**
	 * @return {@link NDPluginBase}
	 */
	NDPluginBase getPluginBase();

	/**
	 * 
	 */
	String getAdjacentAddress() throws Exception;

	/**
	 * 
	 */
	int getAdjacentPort() throws Exception;

	/**
	 * 
	 */
	boolean isAdjacentConnected() throws Exception;

	/**
	 * 
	 */
	int getAdjacentRowsReceived() throws Exception;

	/**
	 * 
	 */
	int getDroppedAdjacentOlder() throws Exception;

	/**
	 * 
	 */
	int getDroppedFrameOlder() throws Exception;

	/**
	 * 
	 */
	int getAdjacentOverrun() throws Exception;

	/**
	 * 
	 */
	int getFrameOverrun() throws Exception;

	/**
	 * 
	 */
	void clearRowsReceived() throws Exception;

	/**
	 * 
	 */
	void enableGapFilling() throws Exception;

	/**
	 * 
	 */
	void disableGapFilling() throws Exception;

	/**
	 * 
	 */
	void setGapFillMode(int mode) throws Exception;

	/**
	 * 
	 */
	void setGapFillConstant(int gapFillConstant) throws Exception;

	/**
	 * 
	 */
	int getGapFillConstant() throws Exception;

	/**
	 * 
	 */
	int getGapFillConstant_RBV() throws Exception;

	/**
	 * 
	 */
	boolean isGapFillingEnabled() throws Exception;

	/**
	 * 
	 */
	int getGapFillMode() throws Exception;
}
