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

public class OdinDetectorEigerThresholdScan extends OdinDetectorDecorator implements OdinEigerDetectorController {

	private static final Logger logger = LoggerFactory.getLogger(OdinDetectorEigerThresholdScan.class);

	private static final String ACQUIRE = "Acquire";
	private static final int ODIN_TIMEOUT = 10;
	private static final String PARAMETERS_VALID = "Valid";
	private static final String PARMETERS_STALE = "Stale";

	private String basePv;

	private PV<String> manualTrigger;
	private PV<Integer> trigger;
	private ReadOnlyPV<Integer> fanReady;
	private PV<Integer> numTriggers;
	private PV<String> acquiring;
	private ReadOnlyPV<String> staleParameters;


	public OdinDetectorEigerThresholdScan(OdinDetectorController delegate) {
		super(delegate);
	}

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
			staleParameters = LazyPVFactory.newReadOnlyEnumPV(basePv + "CAM:StaleParameters_RBV", String.class);
			setConfigured(true);
		}
	}

	@Override
	public void startCollection() throws DeviceException {
		try {
			waitUntilParametersNotStale();
			acquiring.putNoWait(ACQUIRE);
			acquiring.waitForValue(ACQUIRE::equals, ODIN_TIMEOUT);
			fanReady.waitForValue(v -> v == 1, ODIN_TIMEOUT);
			trigger.putNoWait(1);
			logger.debug("Soft trigger sent");
		} catch (IOException | TimeoutException e) {
			logger.error("Error sending trigger", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode,
			String triggerMode) throws DeviceException {
		try {
			manualTrigger.putWait("Yes");
			super.prepareCamera(frames, requestedLiveTime, requestedDeadTime, imageMode, triggerMode);
			numTriggers.putWait(1);
		} catch (IOException | IllegalStateException e) {
			logger.error("Error preparing detecctor", e);
		}
		logger.debug("Detector armed");
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

	private void waitUntilParametersNotStale() throws DeviceException {
		try {
			staleParameters.waitForValue(PARAMETERS_VALID::equals, ODIN_TIMEOUT);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(e);
		} catch (IllegalStateException | TimeoutException | IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setManualTrigger(boolean enable) {
		try {
			if (enable) {
				manualTrigger.putWait("Yes");
			} else {
				manualTrigger.putWait("No");
			}
		} catch (IOException e) {
			logger.error("Could not set manual triggers", e);
		}
	}

	@Override
	public void setNumTriggers(int triggers) {
		try {
			numTriggers.putWait(triggers);
		} catch (IOException e) {
			logger.error("Could not set triggers to {}", triggers, e);
		}

	}

}
