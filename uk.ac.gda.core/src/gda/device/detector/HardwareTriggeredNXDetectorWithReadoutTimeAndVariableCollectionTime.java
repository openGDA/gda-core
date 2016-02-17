/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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
import gda.device.continuouscontroller.ConstantVelocityMoveController2;
import gda.device.scannable.VariableCollectionTimeDetector;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareTriggeredNXDetectorWithReadoutTimeAndVariableCollectionTime extends HardwareTriggeredNXDetectorWithReadoutTime
		implements VariableCollectionTimeDetector {

	private static final Logger logger = LoggerFactory.getLogger(HardwareTriggeredNXDetectorWithReadoutTimeAndVariableCollectionTime.class);
	private double[] times;
	private boolean timeProfileUsed = false;

	// VariableCollectionTimeDetector methods

	@Override
	public void setCollectionTimeProfile(double[] times) throws DeviceException {
		logger.trace("setCollectionTimeProfile({})",  Arrays.toString(times));
		this.times = times;
		timeProfileUsed = false;
	}

	@Override
	public double[] getCollectionTimeProfile() throws DeviceException {
		logger.trace("getCollectionTimeProfile()={}",  Arrays.toString(times));
		timeProfileUsed = true;
		return times;
	}

	// Detector methods

	@Override
	public double getCollectionTime() throws DeviceException {
		timeProfileUsed = true;
		if (getHardwareTriggerProvider() instanceof ConstantVelocityMoveController2) {
			int pointBeingPrepared = ((ConstantVelocityMoveController2)getHardwareTriggerProvider()).getPointBeingPrepared();
			logger.trace("getCollectionTime() pointBeingPrepared={} returning {}", pointBeingPrepared, times[pointBeingPrepared]);
			return times[pointBeingPrepared];
		}
		throw new DeviceException("{} HardwareTriggerProvider must implement ConstantVelocityMoveController2", this.getName());
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		logger.trace("setCollectionTime({}) times={} stack trace {}", collectionTime,  Arrays.toString(times), Arrays.toString(Thread.currentThread().getStackTrace()));
		resetTimeProfileIfUsed();
		times = ArrayUtils.add(times, collectionTime);
		super.setCollectionTime(-1);
	}

	// Class functions

	private void resetTimeProfileIfUsed() {
		/* Note that we cannot reset the time profile atScanStart as setting up the profile happens before atScanStart
		 * is called. The only way we can tell if a profile needs to be reset is if we are setting new collection times
		 * after we have already used points from the current profile (in the last scan).
		 */
		if (timeProfileUsed) {
			logger.debug("resetTimeProfileIfUsed() time profile already used, reseting...");
			timeProfileUsed = false;
			times = ArrayUtils.EMPTY_DOUBLE_ARRAY;
			
			if (getHardwareTriggerProvider() instanceof ConstantVelocityMoveController2) {
				ConstantVelocityMoveController2 cvmc = ((ConstantVelocityMoveController2)getHardwareTriggerProvider());
				logger.debug("resetTimeProfileIfUsed() also reseting point being prepared... was {} ({} is a ConstantVelocityMoveController2)", cvmc.getPointBeingPrepared(), cvmc.getName());
				cvmc.resetPointBeingPrepared();
			}
		}
	}
}
