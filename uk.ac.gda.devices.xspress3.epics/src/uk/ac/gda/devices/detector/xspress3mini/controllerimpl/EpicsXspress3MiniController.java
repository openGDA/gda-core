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

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
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

	private int loopWaitTimer = LocalProperties.getAsInt("gda.xsp3m.fluorescence.loopWaitTimer.ms", 500);

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
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
		// not needed in the XSP3M
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
	public void setRoiSumStartAndSize(int startX, int sizeX) throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			pvProvider.pvRoiSumStartX.putWait(startX);
			pvProvider.pvRoiSumSizeX.putWait(sizeX);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI SUM limits in AreaDetector plugin", e);
		}
	}

	@Override
	public void setRoiStartAndSize(int roiNo, int startX, int sizeX) throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			pvProvider.pvRoiStartX[roiNo-1].putWait(startX);
			pvProvider.pvRoiSizeX[roiNo-1].putWait(sizeX);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI limits in AreaDetector plugin", e);
		}
	}

	@Override
	public int[] getRoiStartAndSize(int roiNo) throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			int start = pvProvider.pvRoiStartX[roiNo-1].get();
			int size = pvProvider.pvRoiSizeX[roiNo-1].get();
			return new int[] {start,size};
		} catch (IOException e) {
			throw new DeviceException(String.format("IOException while getting ROI %d start and size in AreaDetector plugin", roiNo), e);
		}
	}


	/**
	 * Method for getting array data for a single channel multiple ROI device
	 * @return roiData
	 * @throws DeviceException
	 */
	@Override
	public double[][] readoutRoiArrayData(int[] recordRois) throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			pvProvider.updatePvsLatestMCAforXspress3MiniSingleChannel(recordRois);
			double[][] roiData = new double[recordRois.length][];
			int storageIndex = 0;
			for (int roiNumber : recordRois) {
				Double[] roi = pvProvider.pvsLatestMCA[roiNumber-1].get();
				roiData[storageIndex] = ArrayUtils.toPrimitive(roi,0.0);
				storageIndex++;
			}
			return roiData;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting ROI data for single channel device", e);
		}
	}




	@Override
	public void waitForDetector(boolean shouldBeBusy, long timeout) throws DeviceException {
		final long startTime = System.currentTimeMillis();

		while (!isDetectorInDesireState(shouldBeBusy)) {
			final long elapsedTime = (System.currentTimeMillis() - startTime);
			if (elapsedTime > timeout) {
				throw new DeviceException(String.format("Detector has not reached %s state after %d ms", shouldBeBusy ? "busy" : "unbusy", timeout));
			}
			try {
				Thread.sleep(loopWaitTimer);
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

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrays();

		EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();

		// there are seven types of scaler values to return
		Integer[][][] returnValuesWrongOrder = new Integer[7][][]; // scaler
																	// values,
																	// frame,
																	// channel
		returnValuesWrongOrder[0] = readIntegerArray(pvProvider.pvsTime, startChannel, finalChannel);
		returnValuesWrongOrder[1] = readIntegerArray(pvProvider.pvsResetTicks, startChannel, finalChannel);
		returnValuesWrongOrder[2] = readIntegerArray(pvProvider.pvsResetCount, startChannel, finalChannel);
		returnValuesWrongOrder[3] = readIntegerArray(pvProvider.pvsAllEvent, startChannel, finalChannel);
		returnValuesWrongOrder[4] = readIntegerArray(pvProvider.pvsAllGood, startChannel, finalChannel);
		returnValuesWrongOrder[5] = readIntegerArray(pvProvider.pvsPileup, startChannel, finalChannel);
		returnValuesWrongOrder[6] = readIntegerArray(pvProvider.pvsTotalTime, startChannel, finalChannel);

		return reorderScalerValues(returnValuesWrongOrder);
	}


	protected Integer[][] readIntegerArray(ReadOnlyPV<Integer[]>[] pvs, int startChannel, int finalChannel) throws DeviceException {
		Integer[][] returnValuesWrongOrder = new Integer[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				returnValuesWrongOrder[i] = pvs[i].get();
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching data", e);
			}
		}
		return invertIntegerArray(returnValuesWrongOrder);
	}


	@Override
	public void setAcquireTime(double time) throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			PV<Double> acquireTime = pvProvider.pvAcquireTime;
			acquireTime.putWait(time);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting Acquire Time", e);
		}
	}

	@Override
	public double getAcquireTime() throws DeviceException {
		try {
			EpicsXspress3MiniControllerPvProvider pvProvider = (EpicsXspress3MiniControllerPvProvider)getPvProvider();
			PV<Double> acquireTime = pvProvider.pvAcquireTime;
			return acquireTime.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting Acquire Time", e);
		}
	}
}
