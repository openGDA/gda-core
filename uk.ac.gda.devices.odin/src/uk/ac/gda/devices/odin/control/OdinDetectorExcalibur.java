/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin.control;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.factory.FactoryException;

public class OdinDetectorExcalibur extends OdinDetectorDecorator {

	private static final Logger logger = LoggerFactory.getLogger(OdinDetectorExcalibur.class);


	private PV<String> counterDepth;
	private PV<Integer> pausePolling;
	private PV<Integer> continuePolling;

	private String basePv;


	public OdinDetectorExcalibur(OdinDetectorController delegate) {
		super(delegate);
	}

	@Override
	public void configure() throws FactoryException {
		if (basePv == null) {
			throw new IllegalStateException("Cannot configure Odin detector without base PV");
		}
		if (!isConfigured()) {
			counterDepth = LazyPVFactory.newEnumPV(basePv + "CAM:CounterDepth", String.class);
			pausePolling = LazyPVFactory.newIntegerPV(basePv + "CAM:PausePolling");
			continuePolling = LazyPVFactory.newIntegerPV(basePv + "CAM:ContinuePolling");
			setConfigured(true);
		}
	}



	@Override
	public void stopCollection() throws DeviceException {
		super.stopCollection();
		try {
			continuePolling.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("Error continuing polling", e);
		}
	}



	@Override
	public void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode,
			String triggerMode) throws DeviceException {
		try {
			pausePolling.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("Error pausing polling", e);
		}
		super.prepareCamera(frames, requestedLiveTime, requestedDeadTime, imageMode, triggerMode);

	}


	public String getCounterDepth() {
		try {
			return counterDepth.get();
		} catch (IOException e) {
			logger.error("Could not get Counter Depth from detector {}", counterDepth.getPvName(), e);
			return null;
		}
	}

	public void setCounterDepth(String depth) {
		try {
			counterDepth.putWait(depth);
		} catch (IOException e) {
			logger.error("Could not set Counter Depth to {}", depth);
		}
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

}
