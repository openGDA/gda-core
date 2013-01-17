/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector;

import gda.device.DeviceException;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;

public class HardwareTriggeredNXDetector extends NXDetector implements HardwareTriggeredDetector {

	private HardwareTriggerProvider triggerProvider;

	public void setHardwareTriggerProvider(HardwareTriggerProvider hardwareTriggerProvider) {
		this.triggerProvider = hardwareTriggerProvider;
	}
	
	@Override
	protected void prepareCollectionStrategyAtScanStart(int numberImagesPerCollection) throws Exception,
			DeviceException {
		super.prepareCollectionStrategyAtScanStart(numberImagesPerCollection);
	}
	
	
	@Override
	public void collectData() throws DeviceException {
		lastReadoutValue = null;
	}
	
	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		return triggerProvider;
	}

	@Override
	public void arm() throws DeviceException {
		try {
			// Set number of images: the last trigger to end the exposure is superfluous
			getCollectionStrategy().prepareForCollection(getCollectionTime(), getHardwareTriggerProvider().getNumberTriggers() - 1);
			getCollectionStrategy().collectData();
		} catch (Exception e) {
			throw new DeviceException(e);
		}

	}

	@Override
	public boolean integratesBetweenPoints() {
		return true;
	}

}
