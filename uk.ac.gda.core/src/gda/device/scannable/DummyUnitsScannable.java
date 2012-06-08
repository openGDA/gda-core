/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

import gda.device.DeviceException;

/**
 * A dummy scannable for testing / simulations which uses units.
 */
public class DummyUnitsScannable extends ScannableMotionUnitsBase {
	
	private double currentPosition = 0;
	
	/**
	 * Castor constructor
	 */
	public DummyUnitsScannable(){
	}

	
	/**
	 * Constructor with minimal required settings.
	 * 
	 * @param name
	 * @param initialPosition
	 * @param hardwareUnits
	 * @param userUnits
	 * @throws DeviceException 
	 */
	public DummyUnitsScannable(String name, double initialPosition, String hardwareUnits, String userUnits) throws DeviceException{
		setName(name);
		setInputNames(new String[]{name});
		currentPosition = initialPosition;
		setHardwareUnitString(hardwareUnits);
		setInitialUserUnits(userUnits);
		configured = true;
	}
	
	@Override
	public void configure(){
		//everything done in setters
		configured = true;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		Double targetInHardwareUnits = unitsComponent.convertObjectToHardwareUnitsAssumeUserUnits(externalPosition);
		String report = checkPositionValid(targetInHardwareUnits);
		if (report != null) {
			throw new DeviceException(report);
		}
		currentPosition = targetInHardwareUnits;
	}


	@Override
	public Object getPosition() throws DeviceException {
		Double positionInUserUnits = unitsComponent.convertObjectToUserUnitsAssumeHardwareUnits(currentPosition);
		return positionInUserUnits;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

}
