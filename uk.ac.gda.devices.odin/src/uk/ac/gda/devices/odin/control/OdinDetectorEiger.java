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
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;

public class OdinDetectorEiger extends OdinDetectorDecorator {

	private static final Logger logger = LoggerFactory.getLogger(OdinDetectorEiger.class);

	private static final String ACQUIRE = "Acquire";
	private static final int ODIN_TIMEOUT = 10;

	private String basePv;

	private PV<String> manualTrigger;
	private PV<Integer> trigger;
	private ReadOnlyPV<Integer> fanReady;
	private PV<Integer> numTriggers;
	private PV<String> acquiring;

	public OdinDetectorEiger(OdinDetectorController delegate) {
		super(delegate);
	}

	// TODO how to check for stale parameters
	// TODO Meta listener check ready/working?
	// TODO do we need to change odin datatype to match the detector?

	@Override
	public void configure() throws FactoryException {
		if (basePv == null) {
			throw new IllegalStateException("Cannot configure Odin detector without base PV");
		}
		if (!isConfigured()) {
			manualTrigger = LazyPVFactory.newEnumPV(basePv + "CAM:ManualTrigger", String.class);
			trigger = LazyPVFactory.newIntegerPV(basePv + "CAM:Trigger");
			fanReady = LazyPVFactory.newReadOnlyIntegerPV(basePv + "OD:FAN:StateReady_RBV");
			numTriggers = LazyPVFactory.newIntegerPV(basePv + "CAM:NumTriggers");
			acquiring = LazyPVFactory.newEnumPV(basePv + "CAM:Acquire", String.class);
			setConfigured(true);
		}
	}

	@Override
	public void startCollection() throws DeviceException {
		try {
			trigger.putNoWait(1);
			logger.debug("Soft trigger sent");
		} catch (IOException e) {
			logger.error("Error sending trigger", e);
		}
	}

	@Override
	public void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode,
			String triggerMode) throws DeviceException {
		try {
			manualTrigger.putWait("Yes");
			numTriggers.putWait(frames);
			super.prepareCamera(frames, requestedLiveTime, requestedDeadTime, imageMode, triggerMode);
			acquiring.putNoWait(ACQUIRE);
			acquiring.waitForValue(ACQUIRE::equals, ODIN_TIMEOUT);
			fanReady.waitForValue(v -> v == 1, ODIN_TIMEOUT);
		} catch (IOException | IllegalStateException | TimeoutException e) {
			logger.error("Error preparing detecctor", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
			Thread.currentThread().interrupt();
		}
		logger.debug("Detector armed");
	}

	@Override
	public void prepareDataWriter(int frames) throws DeviceException {
		super.setOffsetAndUid(0, 1);
		super.prepareDataWriter(frames);
	}


	@Override
	public void setOffsetAndUid(int offset, int uid) throws DeviceException {
		// do nothing for the Eiger in this mode
	}

	@Override
	public void waitWhileAcquiring() {
		// acquire remains high for whole acquisition
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

}
