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

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.zebra.GreaterThanOrEqualTo;
import gda.epics.ReadOnlyPV;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import uk.ac.gda.devices.detector.xspress3.CAPTURE_MODE;
import uk.ac.gda.devices.detector.xspress3.ReadyForNextRow;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.UPDATE_CTRL;
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
public class EpicsXspress3Controller implements Xspress3Controller, Configurable, Findable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsXspress3Controller.class);

	private static final int NUMBER_ROIs = 10; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation
	private static final int MCA_SIZE = 4096; // fixed for the moment, but will could be changed in the future as this is an EPICS-level calculation

	private static final double TIMEOUTS_MONITORING = 60;

	private String epicsTemplate;

	private EpicsXspress3ControllerPvProvider pvProvider;

	private String name;

	private int numRoiToRead = 1;

//	private int[] dimensionsOfLastFile;

	private int numberOfDetectorChannels = 4;

	private boolean epicsVersion2 = false;

	private boolean useNewEpicsInterface = false;

	@Override
	public void configure() throws FactoryException {
		if (epicsTemplate == null || epicsTemplate.isEmpty()) {
			throw new FactoryException("Epics template has not been set!");
		}
		pvProvider = new EpicsXspress3ControllerPvProvider(epicsTemplate, numberOfDetectorChannels);
		pvProvider.setUseNewEpicsInterface(useNewEpicsInterface);
		pvProvider.createPVs();

		try {
			Boolean epicsConnectionToHardware = pvProvider.pvIsConnected.get() == CONNECTION_STATE.Connected;
			if (!epicsConnectionToHardware) {
				logger.error("EPICS is not connected to underlying Xspress3 hardware.\\nConnect EPICS to Xspreess3 before doing any more in GDA.");
			}
		} catch (IOException e) {
			throw new FactoryException("Excpetion trying to connect to Xspress3 EPICS template", e);
		}
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
			pvProvider.pvErase.putWait(ERASE_STATE.Erase);

			if (pvProvider.getUseNewEpicsInterface()) {
				// call 'Erase/Start' on SCAS time series data for each channel/detector element
				int numChannels = pvProvider.pvGetMaxNumChannels.get();
				for(int i=0; i<numChannels; i++) {
					pvProvider.pvsSCA5UpdateArrays[i].putWait(UPDATE_CTRL.Disable);
				}
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
			return pvProvider.pvIsFileWriting.get() == CAPTURE_CTRL_RBV.Capture;
		} catch (IOException e) {
			throw new DeviceException("IOException while reading save files flag", e);
		}
	}

	@Override
	public void setSavingFiles(Boolean saveFiles) throws DeviceException {
		try {
			if (saveFiles) {
				pvProvider.pvStartStopFileWriting.putNoWait(CAPTURE_CTRL_RBV.Capture);
			} else {
				pvProvider.pvStartStopFileWriting.putNoWait(CAPTURE_CTRL_RBV.Done);
			}
		} catch (IOException e) {
			throw new DeviceException("IOException while setting save files flag", e);
		}
	}

	@Override
	public void doStop() throws DeviceException {
		try {
			pvProvider.pvAcquire.putNoWait(ACQUIRE_STATE.Done);
			Thread.sleep(100);
		} catch (IOException e) {
			throw new DeviceException("IOException while stopping acquisition", e);
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while stopping acquisition", e);
		}
	}

	@Override
	public void doErase() throws DeviceException {
		try {
			pvProvider.pvErase.putWait(ERASE_STATE.Erase);
		} catch (IOException e) {
			throw new DeviceException("IOException while erasing memory", e);
		}
	}

	@Override
	public void doReset() throws DeviceException {
		try {
			pvProvider.pvReset.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("IOException while resetting", e);
		}
	}

	@Override
	public Integer getNumFramesToAcquire() throws DeviceException {
		try {
			return pvProvider.pvGetNumImages.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while fetching number of frames to acquire", e);
		}
	}

	@Override
	public void setNumFramesToAcquire(Integer numFrames) throws DeviceException {
		try {
			pvProvider.pvSetNumImages.putWait(numFrames);
		} catch (IOException e) {
			throw new DeviceException("IOException while resetting", e);
		}
	}

	@Override
	public void setPointsPerRow(Integer pointsPerRow) throws DeviceException {
		try {
			pvProvider.pvPointsPerRow.putWait(pointsPerRow);
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
			pvProvider.pvSetRoiCalc.putWait(setValue);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI calculations on/off", e);
		}
	}

	public Boolean getPerformROICalculations() throws DeviceException {
		try {
			UPDATE_RBV getValue = pvProvider.pvGetRoiCalc.get();
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
			pvProvider.pvSetTrigMode.putWait(mode);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting trigger mode", e);
		}

	}

	@Override
	public TRIGGER_MODE getTriggerMode() throws DeviceException {
		try {
			return pvProvider.pvGetTrigMode.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting trigger mode", e);
		}
	}

	@Override
	public void setFileEnableCallBacks(UPDATE_CTRL callback) throws DeviceException {
		try {
			pvProvider.pvSetFileEnableCallbacks.putWait(callback);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting File EnableCallbacks", e);
		}

	}

	@Override
	public void setFileCaptureMode(CAPTURE_MODE captureMode) throws DeviceException {
		try {
			pvProvider.pvSetFileCaptureMode.putWait(captureMode);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting File Capture Mode", e);
		}

	}

	@Override
	public void setFileArrayCounter(int arrayCounter) throws DeviceException {
		try {
			pvProvider.pvSetFileArrayCounter.putWait(arrayCounter);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting File array counter", e);
		}
	}

	@Override
	public Boolean isBusy() throws DeviceException {
		try {
			return pvProvider.pvIsBusy.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting isBusy value", e);
		}
	}

	@Override
	public Boolean isConnected() throws DeviceException {
		try {
			return pvProvider.pvIsConnected.get() == CONNECTION_STATE.Connected;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting isConnected value", e);
		}
	}

	@Override
	public String getStatusMessage() throws DeviceException {
		try {
			return pvProvider.pvGetStatusMsg.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting status message", e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		try {
			XSPRESS3_EPICS_STATUS currentStatus = pvProvider.pvGetState.get();
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
			return pvProvider.pvGetNumFramesPerReadout.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while number of frames per readout", e);
		}
	}

	@Override
	public int getTotalFramesAvailable() throws DeviceException {
		try {
			return pvProvider.pvGetNumFramesAvailableToReadout.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while number of frames available", e);
		}
	}

	@Override
	public int getTotalHDFFramesAvailable() throws DeviceException {
		try {
			return pvProvider.pvHDFNumCaptureRBV.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while number of frames available", e);
		}
	}

	@Override
	public Integer getMaxNumberFrames() throws DeviceException {
		try {
			return pvProvider.pvGetMaxFrames.get();
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
			pvProvider.pvSetRoiCalc.putWait(setValue);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting roi updates on/off", e);
		}
	}

	public Boolean getPerformROIUpdates() throws DeviceException {
		try {
			UPDATE_RBV getValue = pvProvider.pvGetRoiCalc.get();
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

		return readDoubleWaveform(pvProvider.pvsScalerWindow1, startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrays();

		return readDoubleWaveform(pvProvider.pvsScalerWindow2, startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		updateArrays();

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

	private Integer[][][] reorderScalerValues(Integer[][][] returnValuesWrongOrder) {

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
				valuesWrongOrder[roi] = readDoubleWaveform(pvProvider.pvsROIs[roi], startFrame, finalFrame,
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

		if (!epicsVersion2)
			updateArrays();

		double[][] mcas = new double[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
			    Double[] array = pvProvider.pvsLatestMCA[i].get();
				mcas[i] = ArrayUtils.toPrimitive(array,0.0);
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching mca array data", e);
			}
		}
		return mcas;
	}

	private synchronized void updateArrays() throws DeviceException {
		int maxNumChannels;
		try {
			pvProvider.pvUpdate.putWait(1);
			maxNumChannels = pvProvider.pvGetMaxNumChannels.get();
			// With the EPICs upgrade, it seems that the update arrays does not work as
			// before and we miss some points. The work around here is to update
			// SCA5 array individually for each channel
			for (int i = 0; i < maxNumChannels; i++) {
				if (pvProvider.getUseNewEpicsInterface()) {
					pvProvider.pvsSCA5UpdateArrays[i].putWait(UPDATE_CTRL.Read);
				} else {
					pvProvider.pvsSCA5UpdateArrays[i].putWait(UPDATE_CTRL.Enable);
				}
			}
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
				mcas[i] = ArrayUtils.toPrimitive(pvProvider.pvsLatestMCASummed[i].get());
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching mca array data", e);
			}
		}
		return mcas;
	}

	@Override
	public void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) throws DeviceException {
		try {
			pvProvider.pvsROIHLM[roiNumber][channel].putWait(lowHighMCAChannels[1]);
			pvProvider.pvsROILLM[roiNumber][channel].putWait(lowHighMCAChannels[0]);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI limits", e);
		}
	}

	@Override
	public Integer[] getROILimits(int channel, int roiNumber) throws DeviceException {
		try {
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

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private Double[][] readDoubleWaveform(ReadOnlyPV<Double[]>[] pvs, int startFrame, int finalFrame, int startChannel,
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

	private Integer[][] readIntegerWaveform(ReadOnlyPV<Integer[]>[] pvs, int startFrame, int finalFrame,
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

	private Integer[][] invertIntegerArray(Integer[][] returnValuesWrongOrder) {
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
			pvProvider.pvSetFilePath.putWait(path);
//			pvProvider.pvAllElementSumSetFilePath.putWait(path);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting filepath", e);
		}

	}

	@Override
	public void setFilePrefix(String template) throws DeviceException {
		try {
			pvProvider.pvSetFilePrefix.putWait(template);
//			pvProvider.pvAllElementSumSetFilePrefix.putWait(Xspress3Detector.ALL_ELEMENT_SUM_LABEL +template);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting file prefix", e);
		}

	}

	@Override
	public void setNextFileNumber(int nextNumber) throws DeviceException {
		try {
			pvProvider.pvNextFileNumber.putWait(nextNumber);
//			pvProvider.pvAllElementSumNextFileNumber.putWait(nextNumber);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting file number", e);
		}

	}

	@Override
	public String getFilePath() throws DeviceException {
		try {
			return pvProvider.pvGetFilePath.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting filepath", e);
		}
	}

	@Override
	public String getFilePrefix() throws DeviceException {
		try {
			return pvProvider.pvGetFilePrefix.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting file prefix", e);
		}
	}

	@Override
	public int getNextFileNumber() throws DeviceException {
		try {
			return pvProvider.pvNextFileNumber.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting file number", e);
		}
	}

	@Override
	public String getFullFileName() throws DeviceException {
		try {
			return pvProvider.pvHDFFullFileName.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while getting file name", e);
		}
	}


	@Override
	public void setHDFFileAutoIncrement(boolean b) throws DeviceException {
		try {
			pvProvider.pvHDFAutoIncrement.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFAttributes(boolean b) throws DeviceException {
		try {
			pvProvider.pvHDFAttributes.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFPerformance(boolean b) throws DeviceException {
		try {
			pvProvider.pvHDFPerformance.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFLazyOpen(boolean b) throws DeviceException {
		try {
			pvProvider.pvHDFLazyOpen.putNoWait(b);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting auto increment", e);
		}
	}

	@Override
	public void setHDFNumFramesChunks(int i) throws DeviceException {
		try {
			pvProvider.pvHDFNumFramesChunks.putNoWait(i);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting num HDF frames to acquire", e);
		}
	}

	@Override
	public void setHDFNumFramesToAcquire(int i) throws DeviceException {
		try {
			pvProvider.pvHDFNumCapture.putNoWait(i);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting num HDF frames to acquire", e);
		}
	}

	@Override
	public boolean isChannelEnabled(int channel) throws DeviceException {
		try {
			return pvProvider.pvsChannelEnable[channel].get() == UPDATE_RBV.Enabled;
		} catch (IOException e) {
			throw new DeviceException("IOException while checking channel enabled", e);
		}
	}

	@Override
	public void enableChannel(int channel, boolean doEnable)
			throws DeviceException {
		try {
			if (doEnable) {
				pvProvider.pvsChannelEnable[channel].putWait(UPDATE_RBV.Enabled);
			} else {
				pvProvider.pvsChannelEnable[channel].putWait(UPDATE_RBV.Disabled);
			}
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
	public int monitorUpdateArraysAvailableFrame(int desiredPoint) throws DeviceException {
		int numPointsAvailable = 0;
		try {
			numPointsAvailable = pvProvider.pvUpdateArraysAvailableFrame.waitForValue(new GreaterThanOrEqualTo(desiredPoint), TIMEOUTS_MONITORING);
		} catch (Exception e) {
			throw new DeviceException("Problem while waiting for point: " + desiredPoint, e);
		}

		return numPointsAvailable;
	}


	@Override
	public ReadyForNextRow monitorReadyForNextRow(ReadyForNextRow readyForNextRow) throws DeviceException {
		ReadyForNextRow isReadyForNextRow;
		try {
			isReadyForNextRow = pvProvider.pvReadyForNextRow.waitForValue(new ReadyForNextRowPredicate(readyForNextRow), TIMEOUTS_MONITORING);
		} catch (Exception e) {
			throw new DeviceException("Problem while waiting for ready for next row: " + readyForNextRow, e);
		}

		return isReadyForNextRow;
	}

	public boolean isEpicsVersion2() {
		return epicsVersion2;
	}

	public void setEpicsVersion2(boolean epicsVersion2) {
		this.epicsVersion2 = epicsVersion2;
	}

	public String getSCAAttrName(int channel, int scaler) throws IOException {
		return pvProvider.pvsSCAttrName[channel][scaler].get();
	}

	public boolean getUseNewEpicsInterface() {
		return useNewEpicsInterface;
	}

	public void setUseNewEpicsInterface(boolean useNewEpicsInterface) {
		this.useNewEpicsInterface = useNewEpicsInterface;
	}
}
