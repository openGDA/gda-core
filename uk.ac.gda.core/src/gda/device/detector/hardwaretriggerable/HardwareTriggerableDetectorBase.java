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

package gda.device.detector.hardwaretriggerable;

import gda.device.DeviceException;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.continuouscontroller.SimulatedTriggerObserver;
import gda.device.detector.DetectorBase;

public abstract class HardwareTriggerableDetectorBase extends DetectorBase implements HardwareTriggerableDetector, SimulatedTriggerObserver {

	private HardwareTriggerProvider triggerProvider;
	
	private boolean hardwareTriggering;

	private int numberImagesToCollect = 1;

	public void setHardwareTriggerProvider(HardwareTriggerProvider triggerProvider) {
		this.triggerProvider = triggerProvider;
	}

	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		return triggerProvider;
	}

	@Override
	public void setHardwareTriggering(boolean b) throws DeviceException {
		hardwareTriggering = b;		
	}

	@Override
	public boolean isHardwareTriggering() {
		return hardwareTriggering;
	}
	
	@Override
	public void setNumberImagesToCollect(int numberImagesToCollect) {
		this.numberImagesToCollect = numberImagesToCollect;
	}

	public int getNumberImagesToCollect() {
		return this.numberImagesToCollect;
	}

}
