package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.epics.ReadOnlyPV;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
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
public class EpicsController implements Xspress3Controller, Configurable, Findable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsController.class);

	private String epicsTemplate;

	private EpicsControllerPvProvider pvProvider;

	private String name;

	private int numRoiToRead;

	@Override
	public void configure() throws FactoryException {
		if (epicsTemplate == null || epicsTemplate.isEmpty()) { 
			throw new FactoryException("Epics template has not been set!");
		}
		pvProvider = new EpicsControllerPvProvider(epicsTemplate);

		try {
			int epicsConnectionToHardware = pvProvider.pvConnectionStatus.get();
			if (epicsConnectionToHardware != 1) {
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

	@Override
	public void doStart() throws DeviceException {
		try {
			pvProvider.pvAcquire.putCallback(1);
		} catch (IOException e) {
			throw new DeviceException("IOException while starting acquisition", e);
		}
	}

	public boolean isSavingFiles() throws DeviceException {
		try {
			return pvProvider.pvIsFileWriting.get() == CAPTURE_CTRL_RBV.Capture;
		} catch (IOException e) {
			throw new DeviceException("IOException while reading save files flag", e);
		}
	}

	public void setSavingFiles(Boolean saveFiles) throws DeviceException {
		try {
			if (saveFiles) {
				pvProvider.pvStartStopFileWriting.put(CAPTURE_CTRL_RBV.Capture);
			} else {
				pvProvider.pvStartStopFileWriting.put(CAPTURE_CTRL_RBV.Done);
			}
		} catch (IOException e) {
			throw new DeviceException("IOException while setting save files flag", e);
		}
	}

	@Override
	public void doStop() throws DeviceException {
		try {
			pvProvider.pvAcquire.put(0);
		} catch (IOException e) {
			throw new DeviceException("IOException while stopping acquisition", e);
		}
	}

	@Override
	public void doErase() throws DeviceException {
		try {
			pvProvider.pvErase.put(1);
		} catch (IOException e) {
			throw new DeviceException("IOException while erasing memory", e);
		}
	}

	@Override
	public void doReset() throws DeviceException {
		try {
			pvProvider.pvReset.put(1);
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
			pvProvider.pvSetNumImages.put(numFrames);
		} catch (IOException e) {
			throw new DeviceException("IOException while resetting", e);
		}
	}

	public void setPerformROICalculations(Boolean doCalcs) throws DeviceException {
		try {
			UPDATE_CTRL setValue = UPDATE_CTRL.Disable;
			if (doCalcs) {
				setValue = UPDATE_CTRL.Enable;
			}
			pvProvider.pvSetRoiCalc.put(setValue);
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
			pvProvider.pvSetTrigMode.put(mode);
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
			return pvProvider.pvIsConnected.get();
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
			if (currentStatus == XSPRESS3_EPICS_STATUS.Idle) {
				return Detector.IDLE;
			}
			if (currentStatus == XSPRESS3_EPICS_STATUS.Acquire) {
				return Detector.BUSY;
			}
			return Detector.STANDBY;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting state", e);
		}
	}

	/**
	 * @return - the number of frames EPICS reads per readout
	 * @throws DeviceException
	 */
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
	public Integer getMaxNumberFrames() throws DeviceException {
		try {
			return pvProvider.pvGetMaxFrames.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while maximum number of frames", e);
		}
	}

	public void setPerformScalerUpdates(Boolean doUpdates) throws DeviceException {
		try {
			UPDATE_CTRL setValue = UPDATE_CTRL.Disable;
			if (doUpdates) {
				setValue = UPDATE_CTRL.Enable;
			}
			pvProvider.pvSetScalerUpdate.put(setValue);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting scaler updates on/off", e);
		}
	}

	public Boolean getPerformScalerUpdates() throws DeviceException {
		try {
			UPDATE_RBV getValue = pvProvider.pvGetScalerUpdate.get();
			if (getValue == UPDATE_RBV.Enabled) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting scaler updates setting", e);
		}
	}

	public void setScalerRefreshPeriod(int numFrames) throws DeviceException {
		try {
			pvProvider.pvScalerUpdatePeriod.put(numFrames);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting scaler refresh period", e);
		}
	}

	public int getScalerRefreshPeriod() throws DeviceException {
		try {
			return pvProvider.pvScalerUpdatePeriod.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while fetching scaler refresh period", e);
		}
	}

	public void setPerformMCAUpdates(Boolean doUpdates) throws DeviceException {
		try {
			UPDATE_CTRL setValue = UPDATE_CTRL.Disable;
			if (doUpdates) {
				setValue = UPDATE_CTRL.Enable;
			}
			pvProvider.pvSetMCAUpdate.put(setValue);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting mca updates on/off", e);
		}
	}

	public Boolean getPerformMCAUpdates() throws DeviceException {
		try {
			UPDATE_RBV getValue = pvProvider.pvGetMCAUpdate.get();
			if (getValue == UPDATE_RBV.Enabled) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new DeviceException("IOException while getting mca updates setting", e);
		}
	}

	public void setMCARefreshPeriod(int numFrames) throws DeviceException {
		try {
			pvProvider.pvMCAUpdatePeriod.put(numFrames);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting mca refresh period", e);
		}
	}

	public int getMCARefreshPeriod() throws DeviceException {
		try {
			return pvProvider.pvMCAUpdatePeriod.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while fetching mca refresh period", e);
		}
	}

	public void setPerformROIUpdates(Boolean doUpdates) throws DeviceException {
		try {
			UPDATE_CTRL setValue = UPDATE_CTRL.Disable;
			if (doUpdates) {
				setValue = UPDATE_CTRL.Enable;
			}
			pvProvider.pvSetRoiCalc.put(setValue);
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

	public void setROIRefreshPeriod(int numFrames) throws DeviceException {
		try {
			pvProvider.pvSCAROIUpdatePeriod.put(numFrames);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting ROI refresh period", e);
		}
	}

	public int getROIRefreshPeriod() throws DeviceException {
		try {
			return pvProvider.pvSCAROIUpdatePeriod.get();
		} catch (IOException e) {
			throw new DeviceException("IOException while fetching ROI refresh period", e);
		}
	}

	@Override
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		return readDoubleWaveform(pvProvider.pvsScalerWindow1, startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		return readDoubleWaveform(pvProvider.pvsScalerWindow2, startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
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

		try {
			int numROIs = getNumberROIToRead();

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
	public Double[][] readoutDTCorrectedLatestMCA(int startChannel, int finalChannel) throws DeviceException {

		Double[][] mcas = new Double[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				mcas[i] = pvProvider.pvsLatestMCA[i].get();
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching mca array data", e);
			}
		}
		return mcas;
	}
	
	@Override
	public Double[][] readoutDTCorrectedLatestSummedMCA(int startChannel, int finalChannel) throws DeviceException {

		Double[][] mcas = new Double[finalChannel - startChannel + 1][];
		for (int i = startChannel; i <= finalChannel; i++) {
			try {
				mcas[i] = pvProvider.pvsLatestMCASummed[i].get();
			} catch (IOException e) {
				throw new DeviceException("IOException while fetching mca array data", e);
			}
		}
		return mcas;
	}


	@Override
	public void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) throws DeviceException {
		try {
			pvProvider.pvsROILLM[roiNumber][channel].put(lowHighMCAChannels[0]);
			pvProvider.pvsROIHLM[roiNumber][channel].put(lowHighMCAChannels[1]);
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
			case 1:
				pvProvider.pvsScaWin1Low[channel].put(lowHighScalerWindowChannels[0]);
				pvProvider.pvsScaWin1High[channel].put(lowHighScalerWindowChannels[1]);
				break;
			case 2:
				pvProvider.pvsScaWin2Low[channel].put(lowHighScalerWindowChannels[0]);
				pvProvider.pvsScaWin2High[channel].put(lowHighScalerWindowChannels[1]);
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

	// private Double[] readDoubleArray(ReadOnlyPV<Double>[] pvs,
	// int startChannel, int finalChannel) throws DeviceException {
	// Double[] returnValues = new Double[finalChannel - startChannel];
	// for (int i = startChannel; i < finalChannel; i++) {
	// try {
	// returnValues[i] = pvs[i].get();
	// } catch (IOException e) {
	// throw new DeviceException(
	// "IOException while fetching integera data", e);
	// }
	// }
	// return returnValues;
	// }

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
			pvProvider.pvSetFilePath.putCallback(path);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting filepath", e);
		}

	}

	@Override
	public void setFilePrefix(String template) throws DeviceException {
		try {
			pvProvider.pvSetFilePrefix.putCallback(template);
		} catch (IOException e) {
			throw new DeviceException("IOException while setting file prefix", e);
		}

	}

	@Override
	public void setNextFileNumber(int nextNumber) throws DeviceException {
		try {
			pvProvider.pvNextFileNumber.putCallback(nextNumber);
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
}
