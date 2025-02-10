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
	StringBuilder errorMessage;

	public BeamToEndstationStatus(ArrayList<EnumPositionerBase> valvesShuttersAndCameraToCheck) {
		this.devicesToCheck = valvesShuttersAndCameraToCheck;
	}

	@Override
	public boolean beamInEndstation() {
		devicesToCheck.stream().filter(this::isDeviceBlocking).forEach(devicesBlockingTheBeam::add);
		return devicesBlockingTheBeam.isEmpty();
	}

	private boolean isDeviceBlocking(EnumPositionerBase device) {
		final String position = getPosition(device);
		if (position!=null) {
			final boolean result = !(position.equals(CAMERA_OUT_OF_BEAM) || position.equals(VALVE_OR_SHUTTER_OPEN));
			if (result) logger.debug("Adding device {} to block list as it has position: {}", device.getName(), position);
			return result;
		}
		logger.debug("Adding device {} to block list as it has position: {}", device.getName(), position);
		return true;
	}

	private String getPosition(EnumPositionerBase device) {
		try {
			return (String) device.getPosition();
		} catch (DeviceException e) {
			logger.error("Failed to get device position", e);
		}
		return null;
	}

	@Override
	public String getErrorMessage() {
		errorMessage = new StringBuilder();
		devicesBlockingTheBeam.stream().forEach(device -> errorMessage.append(String.format(errorMessageLineTemplate, device.getName(), getPosition(device))));
		// Empty list before returning error message
		devicesBlockingTheBeam.clear();
		return errorMessage.toString();
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
