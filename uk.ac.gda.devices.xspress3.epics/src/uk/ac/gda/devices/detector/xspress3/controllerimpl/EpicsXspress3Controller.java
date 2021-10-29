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

package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.gda.devices.detector.xspress3.CAPTURE_MODE;
import uk.ac.gda.devices.detector.xspress3.ReadyForNextRow;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.UPDATE_CTRL;
import uk.ac.gda.devices.detector.xspress3.XSPRESS3_TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

/**
 * There is more functionality in the EPICS Xspress3 template than made
 * available here.
 * <p>
 * Functionality outside of the this class which is relevant: file saving,
 *
 * @author rjw82
 *
 */
public class EpicsXspress3Controller extends FindableConfigurableBase implements Xspress3Controller {

	private static final Logger logger = LoggerFactory.getLogger(EpicsXspress3Controller.class);

	private static final int NUMBER_ROIs = 10; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation
	private static final int MCA_SIZE = 4096; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation

	private static final double TIMEOUTS_MONITORING = 60;

	protected String epicsTemplate;

	private EpicsXspress3ControllerPvProvider pvProviderCached;

	private int numRoiToRead = 1;

	protected int numberOfDetectorChannels = 4;

	private boolean iocVersion3 = false;

	private boolean useErasePv = true;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (epicsTemplate == null || epicsTemplate.isEmpty()) {
			throw new FactoryException("Epics template has not been set!");
		}

		try {
			Boolean epicsConnectionToHardware = getPvProvider().pvIsConnected.get() == CONNECTION_STATE.Connected;
			if (!epicsConnectionToHardware) {
				logger.error("EPICS is not connected to underlying Xspress3 hardware.\\nConnect EPICS to Xspreess3 before doing any more in GDA.");
			}
		} catch (DeviceException | IOException e) {
			throw new FactoryException("Excpetion trying to connect to Xspress3 EPICS template", e);
		}
		setConfigured(true);
	}

	protected EpicsXspress3ControllerPvProvider getPvProvider() throws DeviceException {
		if (pvProviderCached == null) {
			pvProviderCached = createPvProvider(epicsTemplate, numberOfDetectorChannels);
			pvProviderCached.createPVs();
		}
		return pvProviderCached;
	}

	protected EpicsXspress3ControllerPvProvider createPvProvider(String epicsTemplate, int numberOfDetectorChannels) {
		return new EpicsXspress3ControllerPvProvider(epicsTemplate, numberOfDetectorChannels);
	}

	@Override
	public int getNumberROIToRead() {
		return numRoiToRead;
	}

	@Override
	public void setNumberROIToRead(int numRoiToRead) throws IllegalArgumentException {
		this.numRoiToRead = numRoiToRead;
	}

	/**
	 * Used to derive the available PVs.
	 */
	@Override
	public int getNumberOfChannels() {
		// should be returned by EPICs or it will slow down scan?
		return numberOfDetectorChannels;
	}

	public void setNumberOfChannels(int numberOfDetectorChannels) {
		this.numberOfDetectorChannels = numberOfDetectorChannels;
	}

	@Override
	public void doStart() throws DeviceException {
		try {
			doErase();
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			pvProvider.startTimeSeries();
			if (iocVersion3) {
				pvProvider.pvSquashAuxDim.putWait(UPDATE_RBV.Enabled);
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
	public boolean isSavingFiles() throws DeviceException {
		try {
			return getPvProvider().pvIsFileWriting.get() == CAPTURE_CTRL_RBV.Capture;
		} catch (IOException e) {
			throw new DeviceException("IOException while reading save files flag", e);
		}
	}

	@Override
	public void setSavingFiles(Boolean saveFiles) throws DeviceException {
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			if (saveFiles) {
				pvProvider.pvStartStopFileWriting.putNoWait(CAPTURE_CTRL_RBV.Capture);
				pvProvider.pvIsFileWriting.waitForValue(value-> value==CAPTURE_CTRL_RBV.Capture, TIMEOUTS_MONITORING);
			} else {
				pvProvider.pvIsFileWriting.waitForValue(value-> value==CAPTURE_CTRL_RBV.Done, TIMEOUTS_MONITORING);
				pvProvider.pvStartStopFileWriting.putNoWait(CAPTURE_CTRL_RBV.Done);
			}
		} catch (IOException e) {
			throw new DeviceException("IOException while setting save files flag", e);
		} catch (InterruptedException|TimeoutException e) {
			throw new DeviceException("Problem waiting for capture readback to update", e.getMessage(), e);
		}
	}

	@Override
	public void doStopSavingFiles() throws DeviceException {
		try {
			getPvProvider().pvStartStopFileWriting.putNoWait(CAPTURE_CTRL_RBV.Done);
		}catch (IOException e) {
			throw new DeviceException("IOException while stopping hdf file writer", e);
		}
	}

	@Override
	public void doStop() throws DeviceException {
		try {
			getPvProvider().pvAcquire.putNoWait(ACQUIRE_STATE.Done);
			Thread.sleep(100);
			getPvProvider().stopTimeSeries();
		} catch (IOException e) {
			throw new DeviceException("IOException while stopping acquisition", e);
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while stopping acquisition", e);
		}
	}

	@Override
	public void doErase() throws DeviceException {
		if (!useErasePv) {
			logger.debug("doErase called but not sending to Epics");
			return;
		}
		try {
			getPvProvider().pvErase.putWait(ERASE_STATE.Erase);
		} catch (IOException e) {
			throw new DeviceException("IOException while erasing memory", e);
		}
	}

	@Override
	public void doReset() throws DeviceException {
		try {
			getPvProvider().pvReset.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("IOException while resetting", e);
		}
	}

	@Override
	public void setArrayCounter(int n) throws DeviceException {
		try {
			getPvProvider().pvSetArrayCounter.putWait(n);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ArrayCounter", e);
		}
	}

	@Override
	public Integer getNumFramesToAcquire() throws DeviceException {
		try {
			return getPvProvider().pvGetNumImages.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while fetching number of frames to acquire", e);
		}
	}

	@Override
	public void setNumFramesToAcquire(Integer numFrames) throws DeviceException {
		try {
			getPvProvider().pvSetNumImages.putWait(numFrames);
		} catch (IOException e) {
			throw new DeviceException("IOException while resetting", e);
		}
	}

	@Override
	public void setPointsPerRow(Integer pointsPerRow) throws DeviceException {
		try {
			getPvProvider().pvPointsPerRow.putWait(pointsPerRow);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting points perRow", e);
		}
	}

	@Override
	public void setPerformROICalculations(Boolean doCalcs) throws DeviceException {
		try {
			UPDATE_CTRL setValue = UPDATE_CTRL.Disable;
			if (doCalcs) {
				setValue = UPDATE_CTRL.Enable;
			}
			getPvProvider().pvSetRoiCalc.putWait(setValue);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI calculations on/off", e);
		}
	}

	public Boolean getPerformROICalculations() throws DeviceException {
		try {
			UPDATE_RBV getValue = getPvProvider().pvGetRoiCalc.get();
			if (getValue == UPDATE_RBV.Enabled) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting ROI calculations", e);
		}
	}

	@Override
	public void setTriggerMode(TRIGGER_MODE mode) throws DeviceException {
		try {
			XSPRESS3_TRIGGER_MODE x3TM = convertToXspress3TriggerMode(mode);

			if (x3TM != null) {
				getPvProvider().pvSetTrigMode.putWait(x3TM);
			} else {
				throw new DeviceException("Attempt to set trigger mode to an invalid value");
			}

		} catch (IOException e) {
			throw new DeviceException("IOException while setting trigger mode", e);
		}

	}

	private XSPRESS3_TRIGGER_MODE convertToXspress3TriggerMode(TRIGGER_MODE triggerMode) {
		XSPRESS3_TRIGGER_MODE convertedX3TM = null;
		for (XSPRESS3_TRIGGER_MODE x3tm : XSPRESS3_TRIGGER_MODE.values()) {
			if (triggerMode.name().equals(x3tm.name())){
				convertedX3TM = x3tm;
				break;
			}
		}
		return convertedX3TM;
	}

	@Override
	public TRIGGER_MODE getTriggerMode() throws DeviceException {
		try {
			XSPRESS3_TRIGGER_MODE x3tm = getPvProvider().pvGetTrigMode.get();
			TRIGGER_MODE tm = convertToTriggerMode(x3tm);

			if (tm != null) {
				throw new DeviceException(String.format("Error converting %s to TRIGGER_MODE", x3tm.name()));
			}
			return tm;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting trigger mode", e);
		}
	}

	private TRIGGER_MODE convertToTriggerMode(XSPRESS3_TRIGGER_MODE x3TriggerMode) {
		TRIGGER_MODE convertedTriggerMode = null;
		for (TRIGGER_MODE tm : TRIGGER_MODE.values()) {
			if (x3TriggerMode.name().equals(tm.name())){
				convertedTriggerMode = tm;
				break;
			}
		}
		return convertedTriggerMode;
	}

	@Override
	public void setFileEnableCallBacks(UPDATE_CTRL callback) throws DeviceException {
		try {
			getPvProvider().pvSetFileEnableCallbacks.putWait(callback);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting File EnableCallbacks", e);
		}

	}

	@Override
	public void setFileCaptureMode(CAPTURE_MODE captureMode) throws DeviceException {
		try {
			getPvProvider().pvSetFileCaptureMode.putWait(captureMode);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting File Capture Mode", e);
		}

	}

	@Override
	public void setFileArrayCounter(int arrayCounter) throws DeviceException {
		try {
			getPvProvider().pvSetFileArrayCounter.putWait(arrayCounter);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting File array counter", e);
		}
	}

	@Override
	public Boolean isBusy() throws DeviceException {
		try {
			return getPvProvider().pvIsBusy.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting isBusy value", e);
		}
	}

	@Override
	public Boolean isConnected() throws DeviceException {
		try {
			return getPvProvider().pvIsConnected.get() == CONNECTION_STATE.Connected;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting isConnected value", e);
		}
	}

	@Override
	public String getStatusMessage() throws DeviceException {
		try {
			return getPvProvider().pvGetStatusMsg.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting status message", e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		try {
			XSPRESS3_EPICS_STATUS currentStatus = getPvProvider().pvGetState.get();
			if (currentStatus == XSPRESS3_EPICS_STATUS.Idle || currentStatus == XSPRESS3_EPICS_STATUS.Aborted) {
				return Detector.IDLE;
			}
			if (currentStatus == XSPRESS3_EPICS_STATUS.Error) {
				return Detector.FAULT;
			}
			if (currentStatus == XSPRESS3_EPICS_STATUS.Acquire || currentStatus == XSPRESS3_EPICS_STATUS.Readout
					|| currentStatus == XSPRESS3_EPICS_STATUS.Correct || currentStatus == XSPRESS3_EPICS_STATUS.Saving
					|| currentStatus == XSPRESS3_EPICS_STATUS.Aborting) {
				return Detector.BUSY;
			}
			if (currentStatus == XSPRESS3_EPICS_STATUS.Waiting) {
				return Detector.PAUSED;
			}
			if (currentStatus == XSPRESS3_EPICS_STATUS.Initializing
					|| currentStatus == XSPRESS3_EPICS_STATUS.Disconnected) {
				return Detector.STANDBY;
			}
			// unknown
			return Detector.FAULT;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting state", e);
		}
	}

	/**
	 * @return - the number of frames EPICS reads per readout
	 * @throws DeviceException
	 */
	@Override
	public int getNumFramesPerReadout() throws DeviceException {
		try {
			return getPvProvider().pvGetNumFramesPerReadout.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while number of frames per readout", e);
		}
	}

	@Override
	public int getTotalFramesAvailable() throws DeviceException {
		try {
			return getPvProvider().pvGetNumFramesAvailableToReadout.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while number of frames available", e);
		}
	}

	@Override
	public int getTotalHDFFramesAvailable() throws DeviceException {
		try {
			return getPvProvider().pvHDFNumCaptureRBV.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while number of frames available", e);
		}
	}

	@Override
	public Integer getMaxNumberFrames() throws DeviceException {
		try {
			return getPvProvider().pvGetMaxFrames.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while maximum number of frames", e);
		}
	}

	public void setPerformROIUpdates(Boolean doUpdates) throws DeviceException {
		try {
			UPDATE_CTRL setValue = UPDATE_CTRL.Disable;
			if (doUpdates) {
				setValue = UPDATE_CTRL.Enable;
			}
			getPvProvider().pvSetRoiCalc.putWait(setValue);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting roi updates on/off", e);
		}
	}

	public Boolean getPerformROIUpdates() throws DeviceException {
		try {
			UPDATE_RBV getValue = getPvProvider().pvGetRoiCalc.get();
			if (getValue == UPDATE_RBV.Enabled) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting roi updates setting", e);
		}
	}

	@Override
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrays();

		return readDoubleWaveform(getPvProvider().pvsScalerWindow1, startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrays();

		return readDoubleWaveform(getPvProvider().pvsScalerWindow2, startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrays();

		EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();

		// there are six types of scaler values to return
		Integer[][][] returnValuesWrongOrder = new Integer[6][][]; // scaler
																	// values,
																	// frame,
																	// channel
		returnValuesWrongOrder[0] = readIntegerWaveform(pvProvider.pvsTime, startFrame, finalFrame, startChannel,
				finalChannel);
		returnValuesWrongOrder[1] = readIntegerWaveform(pvProvider.pvsResetTicks, startFrame, finalFrame, startChannel,
				finalChannel);
		returnValuesWrongOrder[2] = readIntegerWaveform(pvProvider.pvsResetCount, startFrame, finalFrame, startChannel,
				finalChannel);
		returnValuesWrongOrder[3] = readIntegerWaveform(pvProvider.pvsAllEvent, startFrame, finalFrame, startChannel,
				finalChannel);
		returnValuesWrongOrder[4] = readIntegerWaveform(pvProvider.pvsAllGood, startFrame, finalFrame, startChannel,
				finalChannel);
		returnValuesWrongOrder[5] = readIntegerWaveform(pvProvider.pvsPileup, startFrame, finalFrame, startChannel,
				finalChannel);
		return reorderScalerValues(returnValuesWrongOrder);
	}

	protected Integer[][][] reorderScalerValues(Integer[][][] returnValuesWrongOrder) {

		int numScalers = returnValuesWrongOrder.length;
		int numFrames = returnValuesWrongOrder[0].length;
		int numChannels = returnValuesWrongOrder[0][0].length;

		Integer[][][] correctedArray = new Integer[numFrames][numChannels][numScalers];
		for (int scaler = 0; scaler < numScalers; scaler++) {
			for (int frame = 0; frame < numFrames; frame++) {
				for (int channel = 0; channel < numChannels; channel++) {
					correctedArray[frame][channel][scaler] = returnValuesWrongOrder[scaler][frame][channel];
				}
			}
		}
		return correctedArray;
	}

	@Override
	public Integer[][] readoutDTCParameters(int startChannel, int finalChannel) throws DeviceException {
		EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
		Integer[][] valuesWrongOrder = new Integer[4][]; // 4 values per channel
		valuesWrongOrder[0] = readIntegerArray(pvProvider.pvsGoodEventGradient, startChannel, finalChannel);
		valuesWrongOrder[1] = readIntegerArray(pvProvider.pvsGoodEventOffset, startChannel, finalChannel);
		valuesWrongOrder[2] = readIntegerArray(pvProvider.pvsInWinEventGradient, startChannel, finalChannel);
		valuesWrongOrder[3] = readIntegerArray(pvProvider.pvsInWinEventOffset, startChannel, finalChannel);
		return invertIntegerArray(valuesWrongOrder);
	}

	@Override
	public Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {

		updateArrays();

		try {
			int numROIs = getNumberROIToRead();

			if (numROIs == 0) {
				throw new DeviceException("The number of ROI to readout has not been defined!");
			}

			Double[][][] valuesWrongOrder = new Double[numROIs][][];
			for (int roi = 0; roi < numROIs; roi++) {
				// [frame][channel]
				valuesWrongOrder[roi] = readDoubleWaveform(getPvProvider().pvsROIs[roi], startFrame, finalFrame,
						startChannel, finalChannel);
			}
			return reorderROIValues(valuesWrongOrder);
		} catch (Exception e) {
			throw new DeviceException("Exception while fetching regions of interest", e);
		}
	}

	private Double[][][] reorderROIValues(Double[][][] valuesWrongOrder) {

		int numRoi = valuesWrongOrder.length;
		int numFrames = valuesWrongOrder[0].length;
		int numChannels = valuesWrongOrder[0][0].length;

		Double[][][] correctedArray = new Double[numFrames][numChannels][numRoi];
		for (int roi = 0; roi < numRoi; roi++) {
			for (int frame = 0; frame < numFrames; frame++) {
				for (int channel = 0; channel < numChannels; channel++) {
					correctedArray[frame][channel][roi] = valuesWrongOrder[roi][frame][channel];
				}
			}
		}
		return correctedArray;
	}

	@Override
	public double[][] readoutDTCorrectedLatestMCA(int startChannel, int finalChannel) throws DeviceException {
		// With the PV frame_available that checked if all arrays have been updated and returned the current frame updated
		// no need to call updateArrays()
		// updateArrays();

		if (!iocVersion3)
			updateArrays();

		double[][] mcas = new double[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
			    Double[] array = getPvProvider().pvsLatestMCA[i].get();
				mcas[i] = ArrayUtils.toPrimitive(array,0.0);
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching mca array data", e);
			}
		}
		return mcas;
	}

	@Override
	public void setDeadTimeCorrectionInputArrayPort(String port) throws DeviceException {
		try {
			getPvProvider().pvDtcInputArrayPort.putWait(port);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting DTC port", e);
		}
	}

	protected void updateArrays() throws DeviceException {
		updateArraysImpl();
	}

	private synchronized void updateArraysImpl() throws DeviceException {
		int maxNumChannels;
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			pvProvider.pvUpdate.putWait(1);
			maxNumChannels = pvProvider.pvGetMaxNumChannels.get();
			// With the EPICs upgrade, it seems that the update arrays does not work as
			// before and we miss some points. The work around here is to update
			// SCA5 array individually for each channel

			pvProvider.updateTimeSeries();

//			for (int i = 0; i < maxNumChannels; i++) {
//				if (pvProvider.getUseNewEpicsInterface()) {
//					pvProvider.pvsSCA5UpdateArrays[i].putWait(UPDATE_CTRL.Read);
//				} else {
//					pvProvider.pvsSCA5UpdateArrays[i].putWait(UPDATE_CTRL.Enable);
//				}
//			}
		} catch (IOException e) {
			throw new DeviceException("IOException while updating Xspress3 arrays", e);
		}
	}

	@Override
	public double[][] readoutDTCorrectedLatestSummedMCA(int startChannel, int finalChannel) throws DeviceException {

		updateArrays();

		double[][] mcas = new double[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				mcas[i] = ArrayUtils.toPrimitive(getPvProvider().pvsLatestMCASummed[i].get());
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching mca array data", e);
			}
		}
		return mcas;
	}

	@Override
	public void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) throws DeviceException {
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			pvProvider.pvsROIHLM[roiNumber][channel].putWait(lowHighMCAChannels[1]);
			pvProvider.pvsROILLM[roiNumber][channel].putWait(lowHighMCAChannels[0]);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI limits", e);
		}
	}

	@Override
	public Integer[] getROILimits(int channel, int roiNumber) throws DeviceException {
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			Integer[] limits = new Integer[2];
			limits[0] = pvProvider.pvsROILLM[roiNumber][channel].get();
			limits[1] = pvProvider.pvsROIHLM[roiNumber][channel].get();
			return limits;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting ROI limits", e);
		}
	}

	@Override
	public void setWindows(int channel, int windowNumber, int[] lowHighScalerWindowChannels) throws DeviceException {
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			switch (windowNumber) {
			case 0:
				pvProvider.pvsScaWin1Low[channel].putNoWait(0);
				pvProvider.pvsScaWin1High[channel].putNoWait(lowHighScalerWindowChannels[1]);
				pvProvider.pvsScaWin1Low[channel].putWait(lowHighScalerWindowChannels[0]);
				break;
			case 1:
				pvProvider.pvsScaWin2Low[channel].putNoWait(0);
				pvProvider.pvsScaWin2High[channel].putNoWait(lowHighScalerWindowChannels[1]);
				pvProvider.pvsScaWin2Low[channel].putWait(lowHighScalerWindowChannels[0]);
				break;
			default:
				throw new DeviceException("Cannot set scaler window: value for window unacceptable");
			}
		} catch (IOException e) {
			throw new DeviceException("IOException while setting scaler window limits", e);
		}
	}

	@Override
	public Integer[] getWindows(int channel, int windowNumber) throws DeviceException {
		try {
			EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
			Integer[] limits = new Integer[2];
			switch (windowNumber) {
			case 1:
				limits[0] = pvProvider.pvsScaWin1LowRBV[channel].get();
				limits[1] = pvProvider.pvsScaWin1HighRBV[channel].get();
				break;
			case 2:
				limits[0] = pvProvider.pvsScaWin2LowRBV[channel].get();
				limits[1] = pvProvider.pvsScaWin2HighRBV[channel].get();
				break;
			default:
				throw new DeviceException("Cannot get scaler window: value for window unacceptable");
			}
			return limits;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting scaler window limits", e);
		}
	}

	public String getEpicsTemplate() {
		return epicsTemplate;
	}

	public void setEpicsTemplate(String epicsTemplate) {
		this.epicsTemplate = epicsTemplate;
	}

	protected Double[][] readDoubleWaveform(ReadOnlyPV<Double[]>[] pvs, int startFrame, int finalFrame, int startChannel,
			int finalChannel) throws DeviceException {
		// this is [channel][frame]
		Double[][] returnValuesWrongOrder = new Double[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				Double[] allFrames = pvs[i].get();
				returnValuesWrongOrder[i] = (Double[]) ArrayUtils.subarray(allFrames, startFrame, finalFrame + 1);
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching double array data", e);
			}
		}
		// return [frame][channel]
		return invertDoubleArray(returnValuesWrongOrder);
	}

	/*
	 * Arrays are stored in EPICS as a waveform over all the frames per channel.
	 * But GDA wants to use an array per frame, so need to convert from a
	 * Double[channel][frame] to Double[frame][channel]
	 */
	private Double[][] invertDoubleArray(Double[][] returnValuesWrongOrder) {
		int numChannels = returnValuesWrongOrder.length;
		int numFrames = returnValuesWrongOrder[0].length;

		Double[][] correctedArray = new Double[numFrames][numChannels];
		for (int frame = 0; frame < numFrames; frame++) {
			for (int channel = 0; channel < numChannels; channel++) {
				correctedArray[frame][channel] = returnValuesWrongOrder[channel][frame];
			}
		}
		return correctedArray;
	}

	protected Integer[][] readIntegerWaveform(ReadOnlyPV<Integer[]>[] pvs, int startFrame, int finalFrame,
			int startChannel, int finalChannel) throws DeviceException {
		Integer[][] returnValuesWrongOrder = new Integer[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				Integer[] allFrames = pvs[i].get();
				returnValuesWrongOrder[i] = (Integer[]) ArrayUtils.subarray(allFrames, startFrame, finalFrame + 1);
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching integer array data", e);
			}
		}
		return invertIntegerArray(returnValuesWrongOrder);
	}

	protected Integer[][] invertIntegerArray(Integer[][] returnValuesWrongOrder) {
		int numChannels = returnValuesWrongOrder.length;
		int numFrames = returnValuesWrongOrder[0].length;

		Integer[][] correctedArray = new Integer[numFrames][numChannels];
		for (int frame = 0; frame < numFrames; frame++) {
			for (int channel = 0; channel < numChannels; channel++) {
				correctedArray[frame][channel] = returnValuesWrongOrder[channel][frame];
			}
		}
		return correctedArray;
	}

	private Integer[] readIntegerArray(ReadOnlyPV<Integer>[] pvs, int startChannel, int finalChannel)
			throws DeviceException {
		Integer[] returnValues = new Integer[finalChannel - startChannel + 1];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				returnValues[i] = pvs[i].get();
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching integer data", e);
			}
		}
		return returnValues;
	}

	@Override
	public void setFilePath(String path) throws DeviceException {
		try {
			getPvProvider().pvSetFilePath.putWait(path);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting filepath", e);
		}

	}

	@Override
	public void setFilePrefix(String template) throws DeviceException {
		try {
			getPvProvider().pvSetFilePrefix.putWait(template);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting file prefix", e);
		}

	}

	@Override
	public void setNextFileNumber(int nextNumber) throws DeviceException {
		try {
			getPvProvider().pvNextFileNumber.putWait(nextNumber);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting file number", e);
		}

	}

	@Override
	public String getFilePath() throws DeviceException {
		try {
			return getPvProvider().pvGetFilePath.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting filepath", e);
		}
	}

	@Override
	public String getFilePrefix() throws DeviceException {
		try {
			return getPvProvider().pvGetFilePrefix.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting file prefix", e);
		}
	}

	@Override
	public int getNextFileNumber() throws DeviceException {
		try {
			return getPvProvider().pvNextFileNumber.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting file number", e);
		}
	}

	@Override
	public String getFullFileName() throws DeviceException {
		try {
			return getPvProvider().pvHDFFullFileName.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting file name", e);
		}
	}


	@Override
	public void setHDFFileAutoIncrement(boolean b) throws DeviceException {
		try {
			getPvProvider().pvHDFAutoIncrement.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFAttributes(boolean b) throws DeviceException {
		try {
			getPvProvider().pvHDFAttributes.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFPerformance(boolean b) throws DeviceException {
		try {
			getPvProvider().pvHDFPerformance.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFLazyOpen(boolean b) throws DeviceException {
		try {
			getPvProvider().pvHDFLazyOpen.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFNumFramesChunks(int i) throws DeviceException {
		try {
			getPvProvider().pvHDFNumFramesChunks.putNoWait(i);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting num HDF frames to acquire", e);
		}
	}

	@Override
	public void setHDFNumFramesToAcquire(int i) throws DeviceException {
		try {
			getPvProvider().pvHDFNumCapture.putNoWait(i);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting num HDF frames to acquire", e);
		}
	}

	@Override
	public boolean isChannelEnabled(int channel) throws DeviceException {
		try {
			return getPvProvider().pvsChannelEnable[channel].get() == UPDATE_RBV.Enabled;
		} catch (IOException e) {
			throw new DeviceException("IOException while checking channel enabled", e);
		}
	}

	@Override
	public void enableChannel(int channel, boolean doEnable) throws DeviceException {

		EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
		PV<UPDATE_RBV> enablePv = iocVersion3 ? pvProvider.pvsChannelEnableIocV3[channel]
											  : pvProvider.pvsChannelEnable[channel];

		UPDATE_RBV value = doEnable ? UPDATE_RBV.Enabled : UPDATE_RBV.Disabled;

		try {
			enablePv.putWait(value);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting channel enabled", e);
		}
	}

	@Override
	public int getNumberOfRois() {
		return NUMBER_ROIs;
	}

	@Override
	public int getMcaSize() {
		// maybe should returned what is given by EPICS?
		return MCA_SIZE;
	}

	@Override
	public int waitUntilFrameAvailable(int desiredPoint) throws DeviceException {
		try {
			return getPvProvider().pvGetNumFramesAvailableToReadout.waitForValue(frames -> frames == desiredPoint, TIMEOUTS_MONITORING);
		} catch (Exception e) {
			throw new DeviceException("Problem while waiting for point: " + desiredPoint, e);
		}
	}


	@Override
	public ReadyForNextRow monitorReadyForNextRow(ReadyForNextRow readyForNextRow) throws DeviceException {
		ReadyForNextRow isReadyForNextRow;
		try {
			isReadyForNextRow = getPvProvider().pvReadyForNextRow.waitForValue(new ReadyForNextRowPredicate(readyForNextRow), TIMEOUTS_MONITORING);
		} catch (Exception e) {
			throw new DeviceException("Problem while waiting for ready for next row: " + readyForNextRow, e);
		}

		return isReadyForNextRow;
	}

	public void setIocVersion3(boolean version3) {
		this.iocVersion3 = version3;
	}

	public String getSCAAttrName(int channel, int scaler) throws IOException, DeviceException {
		return getPvProvider().pvsSCAttrName[channel][scaler].get();
	}

	public void setUseNewEpicsInterface(boolean useNewEpicsInterface) {
		logger.warn("'useNewEpicsInterface' is no longer needed and should be removed from Spring config for {}", getName());
	}

	@Override
	public void configureHDFDimensions(int[] scanDimensions) throws DeviceException {
		try {
			if (scanDimensions!=null) {
				setHDFExtraDimensions(scanDimensions.length-1);

				//For 2d, 3d scans, set the extra dimensions to match the scan shape
				if (scanDimensions.length>1 && scanDimensions.length<4) {
					EpicsXspress3ControllerPvProvider pvProvider = getPvProvider();
					pvProvider.pvExtraDimN.putWait(scanDimensions[0]);
					pvProvider.pvExtraDimX.putWait(scanDimensions[1]);
					if (scanDimensions.length==3) {
						pvProvider.pvExtraDimY.putWait(scanDimensions[2]);
					}
				} else {
					logger.warn("Attempting to set HDF extra dimensions using scan dimensions with shape {}. Only up to 3 dimensional shapes allowed", Arrays.toString(scanDimensions));
				}
			}
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setHDFExtraDimensions(int extraDimensions) throws DeviceException {
		try {
			getPvProvider().pvExtraDimensions.putWait(extraDimensions);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting HDF extra dimensions", e);
		}
	}

	@Override
	public void setStoreAttributesUsingExraDims(boolean useExtraDims) throws DeviceException {
		try {
			getPvProvider().pvDimAttDatasets.putWait(useExtraDims ? 1 : 0);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting the 'store attributes using exra dimensions' flag", e);
		}
	}

	@Override
	public void setHDFNDArrayPort(String port) throws DeviceException {
		try {
			getPvProvider().pvHDFNDArrayPort.putWait(port);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting HDF NDArray port", e);
		}

	}

	@Override
	public void setFileTemplate(String fileTemplate) throws DeviceException {
		try {
			getPvProvider().pvSetFileTemplate.putWait(fileTemplate);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting file template", e);
		}
	}

	@Override
	public void setHDFXML(String xml) throws DeviceException {
		try {
			getPvProvider().pvHDFXML.putWait(xml);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting HDF XML", e);
		}
	}

	@Override
	public void setHDFNDAttributeChunk(int chunk) throws DeviceException {
		try {
			getPvProvider().pvHDFNDAttributeChunk.putWait(chunk);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while setting HDF NDAttribute chunk", e);
		}
	}

	@Override
	public void setHDFPositionMode(boolean positionMode) throws DeviceException {
		try {
			getPvProvider().pvHDFPositionMode.putWait(positionMode);
		} catch (IOException e) {
			throw new DeviceException("Error encountered while toggling HDF position mode", e);
		}
	}

	public boolean isUseErasePv() {
		return useErasePv;
	}

	public void setUseErasePv(boolean useErasePv) {
		this.useErasePv = useErasePv;
	}
}
