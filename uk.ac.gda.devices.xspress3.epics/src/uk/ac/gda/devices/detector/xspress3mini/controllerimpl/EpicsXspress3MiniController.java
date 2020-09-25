/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3mini.controllerimpl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.epics.PV;
import gda.factory.FactoryException;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.XSPRESS3_MINI_TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.Xspress3MiniController;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.ACQUIRE_STATE;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.EpicsXspress3Controller;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.EpicsXspress3ControllerPvProvider;

public class EpicsXspress3MiniController extends EpicsXspress3Controller implements Xspress3MiniController {

	private static final Logger logger = LoggerFactory.getLogger(EpicsXspress3MiniController.class);

	private boolean useErasePv = true;

	private int waitForBusyTimeout = LocalProperties.getAsInt("gda.xsp3m.fluorescence.waitForBusyTimeout", 5) * 1000;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setUseNewEpicsInterface(true);
		super.configure();
	}

	@Override
	protected EpicsXspress3ControllerPvProvider createPvProvider(String epicsTemplate, int numberOfDetectorChannels) {
		return new EpicsXspress3MiniControllerPvProvider(epicsTemplate, numberOfDetectorChannels);
	}

	@Override
	public void doStart() throws DeviceException {
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			doErase();
			// call 'Erase/Start' on SCAS time series data for each channel/detector element
			int numChannels = pvProvider.pvGetMaxNumChannels.get();
			for(int i=0; i<numChannels; i++) {
				((EpicsXspress3MiniControllerPvProvider)pvProvider).pvsSCA5UpdateArraysMini[i].putWait(ACQUIRE_STATE.Done);
			}
			// nowait as the IOC does not send a callback (until all data
			// collection finished I suppose, which is not what we want here)
			pvProvider.pvAcquire.putNoWait(ACQUIRE_STATE.Acquire);
			Thread.sleep(100);
		} catch (IOException e) {
			throw new DeviceException("IOException while starting acquisition", e);
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while starting acquisition", e);
		}
	}

	@Override
	public void doErase() throws DeviceException {
		if (!useErasePv) {
			logger.debug("doErase called but not sending to Epics");
			return;
		}
		try {
			((EpicsXspress3MiniControllerPvProvider)getPvProvider()).pvEraseMini.putWait(1); //Assumed 1 means erase
		} catch (IOException e) {
			throw new DeviceException("IOException while erasing memory", e);
		}
	}

	@Override
	public Boolean getPerformROICalculations() throws DeviceException {
		try {
			Double getValue = ((EpicsXspress3MiniControllerPvProvider)getPvProvider()).pvGetRoiCalcMini.get();
			return (getValue == 1);
		} catch (IOException e) {
			throw new DeviceException("IOException while getting ROI calculations", e);
		}
	}

	@Override
	public Boolean getPerformROIUpdates() throws DeviceException {
		try {
			Double getValue = ((EpicsXspress3MiniControllerPvProvider)getPvProvider()).pvGetRoiCalcMini.get();
			return (getValue == 1);
		} catch (IOException e) {
			throw new DeviceException("IOException while getting roi updates setting", e);
		}
	}

	@Override
	public Integer[] getROILimits(int channel, int roiNumber) throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			Integer[] limits = new Integer[2];
			limits[0] = pvProvider.pvsROILLM[roiNumber][channel].get();
			limits[1] = limits[0] + pvProvider.pvsROISize[roiNumber][channel].get();
			return limits;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting ROI limits", e);
		}
	}

	@Override
	public void setTriggerMode(TRIGGER_MODE mode) throws DeviceException {
		try {
			XSPRESS3_MINI_TRIGGER_MODE x3MTM = convertToXspress3MiniTriggerMode(mode);

			if (x3MTM != null) {
				((EpicsXspress3MiniControllerPvProvider)getPvProvider()).pvSetTrigModeMini.putWait(x3MTM);
			} else {
				throw new DeviceException("Attempt to set trigger mode to an invalid value");
			}

		} catch (IOException e) {
			throw new DeviceException("IOException while setting trigger mode", e);
		}

	}

	private XSPRESS3_MINI_TRIGGER_MODE convertToXspress3MiniTriggerMode(TRIGGER_MODE triggerMode) {
		XSPRESS3_MINI_TRIGGER_MODE convertedX3TM = null;
		for (XSPRESS3_MINI_TRIGGER_MODE x3mtm : XSPRESS3_MINI_TRIGGER_MODE.values()) {
			if (triggerMode.name().equals(x3mtm.name())){
				convertedX3TM = x3mtm;
				break;
			}
		}
		return convertedX3TM;
	}

	@Override
	public TRIGGER_MODE getTriggerMode() throws DeviceException {
		try {
			XSPRESS3_MINI_TRIGGER_MODE x3mtm = ((EpicsXspress3MiniControllerPvProvider)getPvProvider()).pvGetTrigModeMini.get();
			TRIGGER_MODE tm = convertToTriggerMode(x3mtm);

			if (tm != null) {
				throw new DeviceException(String.format("Error converting %s to TRIGGER_MODE", x3mtm.name()));
			}
			return tm;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting trigger mode", e);
		}
	}

	private TRIGGER_MODE convertToTriggerMode(XSPRESS3_MINI_TRIGGER_MODE x3MiniTriggerMode) {
		TRIGGER_MODE convertedTriggerMode = null;
		for (TRIGGER_MODE tm : TRIGGER_MODE.values()) {
			if (x3MiniTriggerMode.name().equals(tm.name())){
				convertedTriggerMode = tm;
				break;
			}
		}
		return convertedTriggerMode;
	}

	@Override
	protected void updateArrays() throws DeviceException {
		updateArrayState(ACQUIRE_STATE.Acquire);
	}

	@Override
	public void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) throws DeviceException {

		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();

			PV<Integer> roiSize = pvProvider.pvsROISize[roiNumber][channel];
			int size = lowHighMCAChannels[1] - lowHighMCAChannels[0];

			PV<Integer> roiLLM = pvProvider.pvsROILLM[roiNumber][channel];
			int llm = lowHighMCAChannels[0];

			String msg = String.format("setting ROI limits - %s:%s - %s:%s", roiSize.getPvName(), size, roiLLM.getPvName(), llm);
			logger.info(msg);

			roiSize.putWait(size);
			roiLLM.putWait(llm);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI limits", e);
		}
	}

	@Override
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrayState(ACQUIRE_STATE.Acquire);

		try {
			getPvProvider().pvAcquire.putNoWait(ACQUIRE_STATE.Acquire);
		} catch (IOException e) {
			throw new DeviceException("IOException whilst setting acquire PV", e);
		}

		waitForDetector(true, waitForBusyTimeout);
		waitForDetector(false, waitForBusyTimeout);

		Double[][] value = readDoubleWaveform(getPvProvider().pvsScalerWindow1, startFrame, finalFrame, startChannel, finalChannel);

		updateArrayState(ACQUIRE_STATE.Done);

		return value;
	}

	@Override
	public void waitForDetector(boolean shouldBeBusy, long timeout) throws DeviceException {
		final long startTime = System.currentTimeMillis();

		while (!isDetectorInDesireState(shouldBeBusy)) {
			final long elapsedTime = (System.currentTimeMillis() - startTime);
			if (elapsedTime > timeout) {
				throw new DeviceException(String.format("Detector has not reached desired state after %d ms", timeout));
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new DeviceException("Interrupted while waiting for detector to be busy", e);
			}
		}
		logger.debug("Detector is in desired state");
	}

	protected boolean isDetectorInDesireState(boolean shouldBeBusy) throws DeviceException {
		if (shouldBeBusy) {
			if (getStatus() == Detector.BUSY) {
				return true;
			}
		} else {
			if (getStatus() != Detector.BUSY) {
				return true;
			}
		}

		return false;
	}

	protected void updateArrayState(ACQUIRE_STATE newState) throws DeviceException {
		int maxNumChannels;
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			maxNumChannels = pvProvider.pvGetMaxNumChannels.get();

			for (int i = 0; i < maxNumChannels; i++) {
				((EpicsXspress3MiniControllerPvProvider)pvProvider).pvsSCA5UpdateArraysMini[i].putNoWait(newState);
			}
		} catch (IOException e) {
			throw new DeviceException("IOException while reseting Xspress3 Mini arrays", e);
		}
	}
}
