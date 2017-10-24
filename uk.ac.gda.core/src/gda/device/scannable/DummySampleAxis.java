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
import gda.factory.FactoryException;

/**
 * Simulated axis for testing
 */
public class DummySampleAxis extends SampleXYZAxis {

	double positionInMicrons = 0.0;

	@Override
	public void configure() throws FactoryException{
		try {
			unitsComponent.setHardwareUnitString("micron");
			unitsComponent.setUserUnits("mm");
		} catch (DeviceException e) {
			throw new FactoryException("Error configuring units", e);
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		this.notifyIObservers(this, ScannableStatus.BUSY);
		Double dblPosition = ScannableUtils.objectToArray(position)[0];
		dblPosition = this.unitsComponent.convertObjectToHardwareUnitsAssumeUserUnits(dblPosition);
		positionInMicrons = dblPosition;
		this.notifyIObservers(this, ScannableStatus.IDLE);
	}

	@Override
	public Object getPosition() throws DeviceException {
		return this.unitsComponent.convertObjectToUserUnitsAssumeHardwareUnits(positionInMicrons);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		// not a problem to overlap commands
		return false;
	}

}
