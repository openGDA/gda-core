/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.enumpositioner.DummyValve;
import gda.device.enumpositioner.EnumPositionerBase;


public class DummyBeamToEndstationStatus extends BeamToEndstationStatus {


	private static final Logger logger = LoggerFactory.getLogger(DummyBeamToEndstationStatus.class);


	public DummyBeamToEndstationStatus(ArrayList<EnumPositionerBase> valvesShuttersAndCameraToCheck) {
		super(valvesShuttersAndCameraToCheck);
	}


	@Override
	public boolean beamInEndstation() {

		for (EnumPositionerBase  device : devicesToCheck) {
			try {
				if (device instanceof DummyValve) {
					DummyValve dummyValve = (DummyValve)device;
					if (!dummyValve.getPosition().equals(VALVE_OR_SHUTTER_OPEN)) {
						devicesBlockingTheBeam.add(dummyValve);
						logger.debug("Adding valve to {} block list", device.getName());
					}
				} else if(device instanceof DummyEnumPositioner) {
					DummyEnumPositioner dummyShutterOrCamera = (DummyEnumPositioner)device;
					if (!dummyShutterOrCamera.getPosition().equals(CAMERA_OUT_OF_BEAM)) {
						devicesBlockingTheBeam.add(device);
						logger.debug("Adding camera to {} block list", device.getName());
					}
				}
			} catch (DeviceException e) {
				logger.debug("Failed to get device position");
			}
		}

		if (devicesBlockingTheBeam.isEmpty()) {
			return true;
		}
		return false;
	}


	@Override
	public String getErrorMessage() {
		String errorMessage="";
		String status="";
		String name="";
		for (EnumPositionerBase  device : devicesBlockingTheBeam) {
			name = device.getName();
			if (device instanceof DummyValve) {
				try {
					status = ((DummyValve)device).getPosition();
				} catch (DeviceException e) {
					logger.error("Could not get status of valve", e);
				}
			} else if(device instanceof DummyEnumPositioner) {
				try {
					status = ((DummyEnumPositioner)device).getPosition();
				} catch (DeviceException e) {
					logger.error("Could not get status of camera", e);
				}
			}
			errorMessage = errorMessage.concat(String.format(errorMessageLineTemplate, name, status));
		}
		// Empty list before returning error message
		devicesBlockingTheBeam.clear();
		return errorMessage;
	}
}
