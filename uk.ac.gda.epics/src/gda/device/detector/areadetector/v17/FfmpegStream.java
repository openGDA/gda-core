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


/**
 * This maps to the fmpeg plugin on the EDM screen
 */
public interface FfmpegStream {
	static final String QUALITY = "QUALITY";

	static final String QUALITY_RBV = "QUALITY_RBV";

	static final String FALSE_COL = "FALSE_COL";

	static final String FALSE_COL_RBV = "FALSE_COL_RBV";

	static final String ALWAYS_ON = "ALWAYS_ON";

	static final String ALWAYS_ON_RBV = "ALWAYS_ON_RBV";

	static final String HTTP_PORT_RBV = "HTTP_PORT_RBV";

	static final String HOST_RBV = "HOST_RBV";

	static final String CLIENTS_RBV = "CLIENTS_RBV";

	static final String JPG_URL_RBV = "JPG_URL_RBV";

	static final String MJPG_URL_RBV = "MJPG_URL_RBV";

	NDPluginBase getPluginBase();

	/**
	 *
	 */
	double getQUALITY() throws Exception;

	/**
	 *
	 */
	void setQUALITY(double quality) throws Exception;

	/**
	 *
	 */
	double getQUALITY_RBV() throws Exception;

	/**
	 *
	 */
	short getFALSE_COL() throws Exception;

	/**
	 *
	 */
	void setFALSE_COL(int false_col) throws Exception;

	/**
	 *
	 */
	short getFALSE_COL_RBV() throws Exception;

	/**
	 *
	 */
	short getALWAYS_ON() throws Exception;

	/**
	 *
	 */
	void setALWAYS_ON(int always_on) throws Exception;

	/**
	 *
	 */
	short getALWAYS_ON_RBV() throws Exception;

	/**
	 *
	 */
	double getHTTP_PORT_RBV() throws Exception;

	/**
	 *
	 */
	String getHOST_RBV() throws Exception;

	/**
	 *
	 */
	int getCLIENTS_RBV() throws Exception;

	/**
	 *
	 */
	String getJPG_URL_RBV() throws Exception;

	/**
	 *
	 */
	String getMJPG_URL_RBV() throws Exception;

	/**
	 * 
	 */
	void reset() throws Exception;

}