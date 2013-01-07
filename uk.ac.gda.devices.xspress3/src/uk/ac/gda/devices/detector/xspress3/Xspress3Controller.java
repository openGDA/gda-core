package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.factory.FactoryException;

/**
 * Xspress3 is a generic electronic readout chain for up to 8 MCAs. Data is held
 * in memory in 'frames' and can be randomly accessed.
 * <p>
 * It does not provide timing control and this must be provided externally and a
 * gate signal given to the Xspress3 box. So this class must always be used in
 * conjunction with something which provides that timing e.g. TFG.
 * <p>
 * Direct readout is provided by an EPICS layer. The EPICS writes HDF5 files
 * which hold scaler, ROI, MCA and deadtime data. A maximum of 4 ROI are
 * available (sofwtare ROI in the EPICS layer), and 2 windows are available in
 * the scalers (Xspress3 defined).
 * <p>
 * The EPICS layer provides access to the scaler, ROI, and progress information.
 * It also provides the 'latest' MCA data for online visualisation, although
 * access to this data would not be quick enough for full data reduction /
 * analysis purposes.
 * <p>
 * This class is the GDA interface to the EPICS Xspress3 template.
 * <p>
 * In GDA, arrays are zero based. In EPICS, arrays are 1 based. So input
 * parameters to this interface, and results from this interface are all zero
 * based.
 * 
 * @author rjw82
 * 
 */
public interface Xspress3Controller {

	// from acquisition control and status panel
	public void doStart() throws DeviceException;

	public void doStop() throws DeviceException;

	public void doErase() throws DeviceException;

	public void doReset() throws DeviceException;
	
	public boolean isSavingFiles() throws DeviceException;
	
	public void setSavingFiles(Boolean saveFiles) throws DeviceException;

	public Integer getNumFramesToAcquire() throws DeviceException;

	public void setNumFramesToAcquire(Integer numFrames) throws DeviceException;

	public void setTriggerMode(TRIGGER_MODE mode) throws DeviceException;

	public TRIGGER_MODE getTriggerMode() throws DeviceException;

	public Boolean isBusy() throws DeviceException;

	public Boolean isConnected() throws DeviceException;

	public String getStatusMessage() throws DeviceException;

	/**
	 * @return - matches values in the Detector interface getStatus().
	 *         {@link #Detector.getStatus()}
	 * @throws DeviceException
	 */
	public int getStatus() throws DeviceException;

	/**
	 * @return - the total number of frames of data available in memory for
	 *         reading out.
	 * @throws DeviceException
	 */
	public int getTotalFramesAvailable() throws DeviceException; // ?

	// from System Configuration panel

	public Integer getMaxNumberFrames() throws DeviceException;

	// readout methods

	// /**
	// * Uncorrected in-window scaler counts.
	// * <p>
	// * Use the value from {@link #getNumFramesAvailable()} to know what frames
	// * will have valid data in
	// *
	// * @param startFrame
	// * @param finalFrame
	// * @param startChannel
	// * @param finalChannel
	// * @return int[frame][channel]
	// */
	// public int[][] readoutRawSCA(int startFrame, int finalFrame,
	// int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Dead-time corrected in-window scaler counts, for window 1
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames
	 * will have valid data in
	 * 
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][channel]
	 */
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame,
			int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Dead-time corrected in-window scaler counts, for window 2
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames
	 * will have valid data in
	 * 
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][channel]
	 */
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame,
			int startChannel, int finalChannel) throws DeviceException;

	/**
	 * Array of scaler stats.
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames
	 * will have valid data in
	 * 
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return int[frame][channel][time,reset ticks, reset counts,all events,
	 *         all goodEvents, pileup counts]
	 */
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame,
			int startChannel, int finalChannel) throws DeviceException;

	/**
	 * The dead-time parameters
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames
	 * will have valid data in
	 * 
	 * @param startChannel
	 * @param finalChannel
	 * @return int[channel][allGoodGradient,allGoodOffset,inWindowGradient,
	 *         inWindowOffset]
	 */
	public Integer[][] readoutDTCParameters(int startChannel, int finalChannel)
			throws DeviceException;

	/**
	 * The deadtime corrected number of counts in each ROI.
	 * <p>
	 * Use the value from {@link #getNumFramesAvailable()} to know what frames
	 * will have valid data in
	 * 
	 * @param startFrame
	 * @param finalFrame
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[frame][detector channel][ROIs]
	 */
	public Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame,
			int startChannel, int finalChannel) throws DeviceException;

	/**
	 * @return the number of roi which will be read out by the
	 *         readoutDTCorrectedROI method.
	 */
	public int getNumberROIToRead();

	/**
	 * @param numRoiToRead
	 * @return
	 * @throws IllegalArgumentException
	 *             - if the value given is too high for the implementation
	 */
	public void setNumberROIToRead(int numRoiToRead)
			throws IllegalArgumentException;

	/**
	 * The latest available MCA in the record. When running a series of time
	 * frames, there is no guarentee how up to date this is.
	 * <p>
	 * This is only for indicating the quality of the MCA rather than returning
	 * a specific MCA spectrum.
	 * <p>
	 * The MCA will be written to file by the underlying EPICS.
	 * 
	 * @param startChannel
	 * @param finalChannel
	 * @return Double[detector channel][mca channel]
	 */
	public Double[][] readoutDTCorrectedLatestMCA(int startChannel,
			int finalChannel) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @param lowHighMCAChannels
	 *            [lowChannel,highChannel]
	 */
	public void setROILimits(int channel, int roiNumber,
			int[] lowHighMCAChannels) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @return int[] [lowChannel,highChannel]
	 */
	public Integer[] getROILimits(int channel, int roiNumber)
			throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @param lowHighScalerWindowChannels
	 *            [lowChannel,highChannel]
	 */
	public void setWindows(int channel, int roiNumber,
			int[] lowHighScalerWindowChannels) throws DeviceException;

	/**
	 * @param channel
	 * @param roiNumber
	 * @return int[] [lowChannel,highChannel]
	 */
	public Integer[] getWindows(int channel, int roiNumber)
			throws DeviceException;

	// file saving options

	public void setFilePath(String path) throws DeviceException;

	public void setFilePrefix(String template) throws DeviceException;

	public void setNextFileNumber(int nextNumber) throws DeviceException;

	public String getFilePath() throws DeviceException;

	public String getFilePrefix() throws DeviceException;
	
	public int getNextFileNumber() throws DeviceException;

	public void configure() throws FactoryException;

	public int getNumFramesPerReadout() throws DeviceException;

}
