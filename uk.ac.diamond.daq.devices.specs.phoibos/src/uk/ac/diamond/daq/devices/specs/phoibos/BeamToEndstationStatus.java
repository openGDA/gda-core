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
import gda.device.enumpositioner.EnumPositionerBase;
import gda.device.enumpositioner.EpicsPneumaticCallback;
import gda.device.enumpositioner.EpicsPositionerCallback;
import uk.ac.diamond.daq.devices.specs.phoibos.api.IBeamToEndstationStatus;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IBeamToEndstationStatus.class)
public class BeamToEndstationStatus implements IBeamToEndstationStatus{

	private static final Logger logger = LoggerFactory.getLogger(BeamToEndstationStatus.class);

	protected String name;

	protected ArrayList<EnumPositionerBase> devicesToCheck = new ArrayList<>();
	protected ArrayList<EnumPositionerBase> devicesBlockingTheBeam = new ArrayList<>();
	protected String VALVE_OR_SHUTTER_OPEN = "Open";
	protected String CAMERA_OUT_OF_BEAM = "Out of Beam";
	protected String errorMessageLineTemplate = "Device '%s' status is '%s' \n";


	public BeamToEndstationStatus(ArrayList<EnumPositionerBase> valvesShuttersAndCameraToCheck) {
		this.devicesToCheck = valvesShuttersAndCameraToCheck;
	}

	@Override
	public boolean beamInEndstation() {

		for (EnumPositionerBase  device : devicesToCheck) {
			try {
				if (device instanceof EpicsPneumaticCallback) {
					EpicsPneumaticCallback valveOrShutterDevice = (EpicsPneumaticCallback)device;
					if (!valveOrShutterDevice.getPosition().equals(VALVE_OR_SHUTTER_OPEN)) {
						devicesBlockingTheBeam.add(device);
						logger.debug("Adding valve or shutter to {} block list", device.getName());
					}
				} else if(device instanceof EpicsPositionerCallback) {
					EpicsPositionerCallback cameraDevice = (EpicsPositionerCallback)device;
					if (!cameraDevice.getPosition().equals(CAMERA_OUT_OF_BEAM)) {
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
			if (device instanceof EpicsPneumaticCallback) {
				try {
					status = ((EpicsPneumaticCallback)device).getPosition();
				} catch (DeviceException e) {
					logger.error("Could not get status of valve or shutter", e);
				}
			} else if(device instanceof EpicsPositionerCallback) {
				try {
					status = ((EpicsPositionerCallback)device).getPosition();
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

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	//debugging
	public ArrayList<EnumPositionerBase>  getDevices() {
		return devicesToCheck;
	}

	public ArrayList<EnumPositionerBase>  getBeamBlockingDevices() {
		return devicesBlockingTheBeam;
	}
}
