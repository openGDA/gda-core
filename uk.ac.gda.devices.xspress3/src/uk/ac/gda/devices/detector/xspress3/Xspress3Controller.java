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
	void doStart() throws DeviceException;

	void doStop() throws DeviceException;

	void doErase() throws DeviceException;

	void doReset() throws DeviceException;

	void setArrayCounter(int n) throws DeviceException;

	boolean isSavingFiles() throws DeviceException;

	void setSavingFiles(Boolean saveFiles) throws DeviceException;

	/** Stop the hdf file writer */
	void doStopSavingFiles() throws DeviceException;

	void setHDFFileAutoIncrement(boolean b) throws DeviceException;

	void setHDFNumFramesToAcquire(int i) throws DeviceException;

	Integer getNumFramesToAcquire() throws DeviceException;

	void setNumFramesToAcquire(Integer numFrames) throws DeviceException;

	// to switch on/off EPICS calculations. When off EPICS will perform much quicker, at the moment.
	void setPerformROICalculations(Boolean doCalcs) throws DeviceException;

	void setTriggerMode(TRIGGER_MODE mode) throws DeviceException;

	TRIGGER_MODE getTriggerMode() throws DeviceException;

	Boolean isBusy() throws DeviceException;

	Boolean isConnected() throws DeviceException;

	String getStatusMessage() throws DeviceException;

	/**
	 * @return - matches values in the Detector interface getStatus(). {@link #Detector.getStatus()}
	 * @throws DeviceException
	 */
	int getStatus() throws DeviceException;

	/**
	 * @return - the total number of frames of data available in memory for reading out.
	 * @throws DeviceException
	 */
	int getTotalFramesAvailable() throws DeviceException; // ?

	// from System Configuration panel

	Integer getMaxNumberFrames() throws DeviceException;

	// readout methods

	/**
	 * Dead-time corrected in-window scaler counts, for window 1
	 * <p>
	 * Use the value from {@link #getTotalFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][channel]
	 */
	Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Dead-time corrected in-window scaler counts, for window 2
	 * <p>
	 * Use the value from {@link #getTotalFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][channel]
	 */
	Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Array of scaler stats.
	 * <p>
	 * Use the value from {@link #getTotalFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return int[frame][channel][time,reset ticks, reset counts,all events, all goodEvents, pileup counts]
	 */
	Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * The dead-time parameters
	 * <p>
	 * Use the value from {@link #getTotalFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startChannel
	 * @param finalChannel
	 * @return int[channel][allGoodGradient,allGoodOffset,inWindowGradient, inWindowOffset]
	 */
	Integer[][] readoutDTCParameters(int startChannel, int finalChannel) throws DeviceException;

	/**
	 * The deadtime corrected number of counts in each ROI.
	 * <p>
	 * Use the value from {@link #getTotalFramesAvailable()} to know what frames will have valid data in
	 *
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][detector channel][ROIs]
	 */
	Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame, int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @return the number of roi which will be read out by the readoutDTCorrectedROI method.
	 */
	int getNumberROIToRead();

	/**
	 * @throws IllegalArgumentException
	 *             - if the value given is too high for the implementation
	 */
	void setNumberROIToRead(int numRoiToRead) throws IllegalArgumentException;

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
	double[][] readoutDTCorrectedLatestMCA(int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[detector channel][mca channel]
	 * @throws DeviceException
	 */
	double[][] readoutDTCorrectedLatestSummedMCA(int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @param lowHighMCAChannels
	 *            [lowChannel,highChannel]
	 */
	void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @return int[] [lowChannel,highChannel]
	 */
	Integer[] getROILimits(int channel, int roiNumber) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @param lowHighScalerWindowChannels
	 *            [lowChannel,highChannel]
	 */
	void setWindows(int channel, int roiNumber, int[] lowHighScalerWindowChannels) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @return int[] [lowChannel,highChannel]
	 */
	Integer[] getWindows(int channel, int roiNumber) throws DeviceException;

	// file saving options

	void setFilePath(String path) throws DeviceException;

	void setFilePrefix(String template) throws DeviceException;

	void setNextFileNumber(int nextNumber) throws DeviceException;

	String getFilePath() throws DeviceException;

	String getFilePrefix() throws DeviceException;

	int getNextFileNumber() throws DeviceException;

	/**
	 * @return String - the full file path of the HDF file being written, or the last one written
	 * @throws DeviceException
	 */
	String getFullFileName() throws DeviceException;

	void configure() throws FactoryException;

	int getNumFramesPerReadout() throws DeviceException;

	int getNumberOfChannels();

	/*
	 * Enabled in the Epics sense of EPICS, not in the GDA sense of contributing to the FF.
	 */
	boolean isChannelEnabled(int channel) throws DeviceException;

	/*
	 * Enabled in the Epics sense of EPICS, not in the GDA sense of contributing to the FF.
	 */
	void enableChannel(int channel, boolean doEnable) throws DeviceException;

	int getNumberOfRois();

	int getMcaSize();

	int getTotalHDFFramesAvailable() throws DeviceException;

	void setHDFAttributes(boolean b) throws DeviceException;

	void setHDFPerformance(boolean b) throws DeviceException;

	void setHDFNumFramesChunks(int i) throws DeviceException;

	void setHDFLazyOpen(boolean b) throws DeviceException;

	void setPointsPerRow(Integer pointsPerRow) throws DeviceException;

	int waitUntilFrameAvailable(int scanPoint) throws DeviceException;

	ReadyForNextRow monitorReadyForNextRow(ReadyForNextRow readyForNextRow) throws DeviceException;

	void setFileEnableCallBacks(UPDATE_CTRL callback) throws DeviceException;

	void setFileCaptureMode(CAPTURE_MODE captureMode) throws DeviceException;

	void setFileArrayCounter(int arrayCounter) throws DeviceException;

	/**
	 * Set HDF writer 'extra dimensions' PVs from the scan dimensions (up to 3-dimensional shape).
	 * The number of extra dimensions is set to 0 for a 1-dimensional scan.
	 * @param scanDimensions - number of points in each dimension of the scan
	 */
	void configureHDFDimensions(int[] scanDimensions) throws DeviceException;

	void setHDFExtraDimensions(int extraDimensions) throws DeviceException;

	void setStoreAttributesUsingExraDims(boolean useExtraDims) throws DeviceException;

	void setHDFNDArrayPort(String port) throws DeviceException;

	void setFileTemplate(String fileTemplate) throws DeviceException;

	void setHDFXML(String xml) throws DeviceException;

	void setHDFNDAttributeChunk(int chunk) throws DeviceException;

	void setHDFPositionMode(boolean positionMode) throws DeviceException;

	void setDeadTimeCorrectionInputArrayPort(String port) throws DeviceException;
}