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
public interface Fix {

	NDPluginBase getPluginBase() throws Exception;

	/**
	 * 
	 */
	void enableStatistics() throws Exception;

	/**
	 * 
	 */
	void disableStatistics() throws Exception;

	/**
	 * 
	 */
	void enableScaleEdgePixels() throws Exception;

	/**
	 * 
	 */
	void disableScaleEdgePixels() throws Exception;

	/**
	 * 
	 */
	double getStripeSum_RBV() throws Exception;

	/**
	 * 
	 */
	double getStripeMin_RBV() throws Exception;

	/**
	 * 
	 */
	double getStripeMax_RBV() throws Exception;

	/**
	 * 
	 */
	boolean isStatisticsEnabled() throws Exception;

	/**
	 * 
	 */
	boolean isScaleEdgePixelsEnabled() throws Exception;
}
