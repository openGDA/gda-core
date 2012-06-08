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

package uk.ac.gda.epics.client.views.model;

import uk.ac.gda.epics.client.views.controllers.IMJpegViewController;

public interface FfMpegModel {

	String getNdArrayPort() throws Exception;

	void setNdArrayPort(String ndArrayPort) throws Exception;

	int getDim0Size() throws Exception;

	int getDim1Size() throws Exception;

	double getTimeStamp() throws Exception;

	/**
	 * This PV does not need to be monitored, so passing null as argument value for the monitor
	 * 
	 * @return URL for video streaming
	 */
	String getMjpegUrl() throws Exception;

	/**
	 * This PV does not need to be monitored, so passing null as argument value for the monitor
	 * 
	 * @return URL for video streaming
	 */
	String getJpegUrl() throws Exception;

	boolean registerMJpegViewController(IMJpegViewController viewController);

	boolean removeMJpegViewController(IMJpegViewController viewController);

}