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

package gda.device.detector.addetector.triggering;

import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADDriverPilatus;
import gda.device.detector.areadetector.v17.ADDriverPilatus.PilatusTriggerMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareTriggeredPilatus extends HardwareTriggeredStandard {

	private final ADDriverPilatus adDriverPilatus;

	private final PilatusTriggerMode triggerMode;

	public HardwareTriggeredPilatus(ADBase adBase, ADDriverPilatus adDriverPilatus, double readoutTime,
			PilatusTriggerMode triggerMode) {
		super(adBase, readoutTime);
		this.adDriverPilatus = adDriverPilatus;
		this.triggerMode = triggerMode;
	}

	public ADDriverPilatus getAdDriverPilatus() {
		return adDriverPilatus;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages) throws Exception {
		super.prepareForCollection(collectionTime, numImages);
		getAdDriverPilatus().setDelayTime(0);
	}

	@Override
	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(triggerMode.ordinal());
	}

	@Override
	public void collectData() throws Exception {
		getAdBase().startAcquiring();
		getAdDriverPilatus().waitForArmed(30);

	}

}
