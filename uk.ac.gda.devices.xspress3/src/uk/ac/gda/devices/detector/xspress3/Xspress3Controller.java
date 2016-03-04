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

package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Findable;

/**
 * Xspress3 is a generic electronic readout chain for up to 8 MCAs. Data is held in memory in 'frames' and can be randomly accessed.
 * <p>
 * It does not provide timing control and this must be provided externally and a gate signal given to the Xspress3 box. So this class must always be used in
 * conjunction with something which provides that timing e.g. TFG.
 * <p>
 * Direct readout is provided by an EPICS layer. The EPICS writes HDF5 files which hold scaler, ROI, MCA and deadtime data. A maximum of 4 ROI are available
 * (sofwtare ROI in the EPICS layer), and 2 windows are available in the scalers (Xspress3 defined).
 * <p>
 * The EPICS layer provides access to the scaler, ROI, and progress information. It also provides the 'latest' MCA data for online visualisation, although
 * access to this data would not be quick enough for full data reduction / analysis purposes.
 * <p>
 * This class is the GDA interface to the EPICS Xspress3 template.
 * <p>
 * In GDA, arrays are zero based. In EPICS, arrays are 1 based. So input parameters to this interface, and results from this interface are all zero based.
 */
public interface Xspress3Controller extends Findable {

	// from acquisition control and status panel
	public void doStart() throws DeviceException;

	public void doStop() throws DeviceException;

	public void doErase() throws DeviceException;

	public void doReset() throws DeviceException;

	public boolean isSavingFiles() throws DeviceException;

	public void setSavingFiles(Boolean saveFiles) throws DeviceException;

	public void setHDFFileAutoIncrement(boolean b) throws DeviceException;

	public void setHDFNumFramesToAcquire(int i) throws DeviceException;

	public Integer getNumFramesToAcquire() throws DeviceException;

	public void setNumFramesToAcquire(Integer numFrames) throws DeviceException;

	// to switch on/off EPICS calculations. When off EPICS will perform much quicker, at the moment.
	public void setPerformROICalculations(Boolean doCalcs) throws DeviceException;

	public void setTriggerMode(TRIGGER_MODE mode) throws DeviceException;

	public TRIGGER_MODE getTriggerMode() throws DeviceException;

	public Boolean isBusy() throws DeviceException;

	public Boolean isConnected() throws DeviceException;

	public String getStatusMessage() throws DeviceException;

	/**
	 * @return - matches values in the Detector interface getStatus(). {@link #Detector.getStatus()}
	 * @throws DeviceException
	 */
	public int getStatus() throws DeviceException;

	/**
	 * @return - the total number of frames of data available in memory for reading out.
	 * @throws DeviceException
	 */
	public int getTotalFramesAvailable() throws DeviceException; // ?

	// from System Configuration panel

	public Integer getMaxNumberFrames() throws DeviceException;

	// readout methods

	/**
	 * Dead-time corrected in-window scaler counts, for window 1
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][channel]
	 */
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Dead-time corrected in-window scaler counts, for window 2
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][channel]
	 */
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Array of scaler stats.
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return int[frame][channel][time,reset ticks, reset counts,all events, all goodEvents, pileup counts]
	 */
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * The dead-time parameters
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startChannel
	 * @param finalChannel
	 * @return int[channel][allGoodGradient,allGoodOffset,inWindowGradient, inWindowOffset]
	 */
	public Integer[][] readoutDTCParameters(int startChannel, int finalChannel) throws DeviceException;

	/**
	 * The deadtime corrected number of counts in each ROI.
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][detector channel][ROIs]
	 */
	public Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @return the number of roi which will be read out by the readoutDTCorrectedROI method.
	 */
	public int getNumberROIToRead();

	/**
	 * @param numRoiToRead
	 * @return
	 * @throws IllegalArgumentException
	 *             - if the value given is too high for the implementation
	 */
	public void setNumberROIToRead(int numRoiToRead) throws IllegalArgumentException;

	/**
	 * The latest available MCA in the record. When running a series of time frames, there is no guarentee how up to date this is.
	 * <p>
	 * This is only for indicating the quality of the MCA rather than returning a specific MCA spectrum.
	 * <p>
	 * The MCA will be written to file by the underlying EPICS.
	 *
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[detector channel][mca channel]
	 */
	public double[][] readoutDTCorrectedLatestMCA(int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[detector channel][mca channel]
	 * @throws DeviceException
	 */
	public double[][] readoutDTCorrectedLatestSummedMCA(int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @param lowHighMCAChannels
	 *            [lowChannel,highChannel]
	 */
	public void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @return int[] [lowChannel,highChannel]
	 */
	public Integer[] getROILimits(int channel, int roiNumber) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @param lowHighScalerWindowChannels
	 *            [lowChannel,highChannel]
	 */
	public void setWindows(int channel, int roiNumber, int[] lowHighScalerWindowChannels) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @return int[] [lowChannel,highChannel]
	 */
	public Integer[] getWindows(int channel, int roiNumber) throws DeviceException;

	// file saving options

	public void setFilePath(String path) throws DeviceException;

	public void setFilePrefix(String template) throws DeviceException;

	public void setNextFileNumber(int nextNumber) throws DeviceException;

	public String getFilePath() throws DeviceException;

	public String getFilePrefix() throws DeviceException;

	public int getNextFileNumber() throws DeviceException;

	/**
	 * @return String - the full file path of the HDF file being written, or the last one written
	 * @throws DeviceException
	 */
	public String getFullFileName() throws DeviceException;

	public void configure() throws FactoryException;

	public int getNumFramesPerReadout() throws DeviceException;

	public int getNumberOfChannels();

	/*
	 * Enabled in the Epics sense of EPICS, not in the GDA sense of contributing to the FF.
	 */
	public boolean isChannelEnabled(int channel) throws DeviceException;

	/*
	 * Enabled in the Epics sense of EPICS, not in the GDA sense of contributing to the FF.
	 */
	public void enableChannel(int channel, boolean doEnable) throws DeviceException;

	public int getNumberOfRois();

	public int getMcaSize();

	public int getTotalHDFFramesAvailable() throws DeviceException;

	public void setHDFAttributes(boolean b) throws DeviceException;

	public void setHDFPerformance(boolean b) throws DeviceException;

	public void setHDFNumFramesChunks(int i) throws DeviceException;

	public void setPointsPerRow(Integer pointsPerRow) throws DeviceException;

	public int monitorUpdateArraysAvailableFrame(int desiredPoint) throws DeviceException;

	public ReadyForNextRow monitorReadyForNextRow(ReadyForNextRow readyForNextRow) throws DeviceException;

	public void setFileEnableCallBacks(UPDATE_CTRL callback) throws DeviceException;

	public void setFileCaptureMode(CAPTURE_MODE captureMode) throws DeviceException;

	public void setFileArrayCounter(int arrayCounter) throws DeviceException;

}

