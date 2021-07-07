/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.rest.device.positioner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.ui.tool.rest.device.DeviceRestServiceClientBase;

/**
 * Client to control a remote EnumPositioner.
 *
 *
 * @author Maurizio Nagni
 */
@Service
public class PositionerClient {

	@Autowired
	private DeviceRestServiceClientBase service;

	public static final String REMOTE_SERVICE = "positioner";

	/**
	 * Retrieves a positioner position
	 * @param motorName a string identifying the motor, usually the server-side bean name
	 * @return the motor position
	 * @throws GDAClientRestException
	 */
	public String getPosition(String motorName) throws GDAClientRestException {
		return (String) service.getDeviceValue(motorName, REMOTE_SERVICE, "getPosition").getValue();
	}

	/**
	 * Set a motor position
	 * @param motorName a string identifying the motor, usually the server-side bean name
	 * @param position the required new position
	 * @throws GDAClientRestException
	 */
	public void setPosition(String motorName, String position) throws GDAClientRestException {
		service.setDeviceValue(motorName, REMOTE_SERVICE, "moveTo", position);
	}
}
