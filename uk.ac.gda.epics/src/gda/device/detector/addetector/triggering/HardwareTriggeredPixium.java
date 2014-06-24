/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase;

/**
 * When hardware triggering the acquire period ( which normally would have no effect) has to be set to 
 * acquireTime + a 'readout' time.
 * 
 * Also the time between subsequent triggers pulses must be within 10% time of the acquirePeriod
 */
public class HardwareTriggeredPixium extends HardwareTriggeredStandard {

	public HardwareTriggeredPixium(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		getAdBase().setAcquirePeriod(collectionTime+getReadoutTime());
		getAdBase().setAcquireTime(collectionTime);
	}

}
