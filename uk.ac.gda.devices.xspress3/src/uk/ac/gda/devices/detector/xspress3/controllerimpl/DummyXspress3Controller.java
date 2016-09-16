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

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DummyDAServer;
import gda.device.timer.Tfg;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.devices.detector.xspress3.CAPTURE_MODE;
import uk.ac.gda.devices.detector.xspress3.ReadyForNextRow;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.UPDATE_CTRL;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

/**
 * For unit testing / simulation when EPICS sim not available.
 * <p>
 * Use existing Tfg and DummyDAServer simulations to replace the functionality
 * provided by the Epics layer. This is why is runs as an Xspress2 simulation.
 * <p>
 * With the Tfg object this should be able to provide timing so this simulation
 * could be run with the Xspress3System class as well as the Xspress3Detector
 * class.
 *
 * @author rjw82
 *
 */
public class DummyXspress3Controller implements Xspress3Controller, Findable, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(DummyXspress3Controller.class);

	private int NUMBER_ROIs = 10;
	private int MCA_SIZE = 4096;

	// Tfg is only for simulation purposes to get current state (in real system
	// Xspress3 hardware would know what is going on)
	// Tfg should not be driven by this simulation as Xspress3Controllers only
	// have access to Xspress3 hardware which has no frame times
	private Tfg tfg;
	private DummyDAServer daServer;
	private String name = "not set";
	private String xspressSystemName = "'xs3'";
	private String mcaOpenCommand = "xspress2 open-mca " + xspressSystemName;
	private String scalerOpenCommand = "xspress2 open-scalers " + xspressSystemName;
	private String startupScript = "xspress2 format-run " + xspressSystemName + " res-none";
	private int mcaHandle = -1;
	private int scalerHandle = -1;
	private Integer numFramesToAcquire;
	private TRIGGER_MODE mode;
	private DetectorROI[] roi;
	private DetectorROI[] windows;
	private String path;
	private int numRoiToRead;
	private String template;
	private int nextNumber;
//	private int[] fileDimensions;
	private int numberOfChannels;
	private boolean[] enabledChannels;
	private String simulationFileName;

	public DummyXspress3Controller(Tfg tfg, DummyDAServer daServer) {
		super();
		this.tfg = tfg;
		this.daServer = daServer;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			if (scalerHandle < 0) {
				close();
			}
			daServer.sendCommand(startupScript);
			String formatCommand = "xspress2 format-run " + xspressSystemName + " 12 res-none";
			daServer.sendCommand(formatCommand);

			// clear rois
			String roiCommand = "xspress2 set-roi " + xspressSystemName + " -1";
			daServer.sendCommand(roiCommand);

			enabledChannels = new boolean[numberOfChannels];

			for (int channel = 0; channel < numberOfChannels; channel++) {
				enabledChannels[channel] = true;
				String windowCommand = "xspress2 set-window " + xspressSystemName + " " + channel + " " + 0 + " "
						+ 4096;
				daServer.sendCommand(windowCommand);
				roiCommand = "xspress2 set-roi " + xspressSystemName + " " + channel + " 100 200 1";
				daServer.sendCommand(roiCommand);
			}

			open();
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	private void open() throws DeviceException {
		Object obj;
		if (daServer != null && daServer.isConnected()) {
			if (mcaOpenCommand != null) {
				if ((obj = daServer.sendCommand(mcaOpenCommand)) != null) {
					mcaHandle = ((Integer) obj).intValue();
					if (mcaHandle < 0) {
						throw new DeviceException("Failed to create the mca handle");
					}
					logger.debug("Xspress2System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0) {
						throw new DeviceException("Failed to create the scaler handle");
					}
					logger.debug("Xspress2System: open() using scalerHandle " + scalerHandle);
				}
			}
		}
	}

	private void close() {
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + mcaHandle);
			mcaHandle = -1;
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + scalerHandle);
			scalerHandle = -1;
		}
	}

	private synchronized void sendCommand(String command, int handle) throws DeviceException {
		Object obj;
		if ((obj = daServer.sendCommand(command + handle)) == null) {
			throw new DeviceException("Null reply received from daserver during " + command);
		} else if (((Integer) obj).intValue() == -1) {
			logger.error("DummyXspress3Controller: " + command + " failed");
			close();
			throw new DeviceException("DummyXspress3Controller " + command + " failed");
		}
	}

	@Override
	public void doStop() throws DeviceException {
		// tfg.stop();
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("disable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("disable ", scalerHandle);
		}

		close();
	}

	@Override
	public void doErase() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("clear ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("clear ", scalerHandle);
		}
	}

	@Override
	public void doReset() throws DeviceException {
		doErase();
	}

	@Override
	public Integer getNumFramesToAcquire() {
		return numFramesToAcquire;
	}

	@Override
	public void setNumFramesToAcquire(Integer numFramesToAcquire) {
		this.numFramesToAcquire = numFramesToAcquire;
	}

	@Override
	public void setTriggerMode(TRIGGER_MODE mode) {
		this.mode = mode;
	}

	@Override
	public TRIGGER_MODE getTriggerMode() {
		return mode;
	}

	@Override
	public Boolean isBusy() throws DeviceException {
		return tfg.getStatus() == Timer.ACTIVE;
	}

	@Override
	public Boolean isConnected() {
		return daServer.isConnected();
	}

	@Override
	public String getStatusMessage() {
		return "";
	}

	@Override
	public int getStatus() throws DeviceException {
		if (isBusy()) {
			return Detector.BUSY;
		}
		return Detector.IDLE;
	}

	@Override
	public int getTotalFramesAvailable() throws DeviceException {
		// simulation is not good enough!
		return numFramesToAcquire;
		// return tfg.getCurrentFrame();
	}

	@Override
	public Integer getMaxNumberFrames() {
		return 4096;
	}

	@Override
	public void setROILimits(int channel, int roiNumber, int[] lowHighMCAChannels) {
		if (this.roi == null) {
			roi = new DetectorROI[numberOfChannels];
		}
		roi[roiNumber] = new DetectorROI("ROI" + roiNumber, lowHighMCAChannels[0], lowHighMCAChannels[1]);

	}

	@Override
	public Integer[] getROILimits(int channel, int roiNumber) {
		return new Integer[] { roi[roiNumber].getRoiStart(), roi[roiNumber].getRoiEnd() };
	}

	@Override
	public void setWindows(int channel, int roiNumber, int[] lowHighScalerWindowChannels) {
		if (this.windows == null) {
			windows = new DetectorROI[2];
		}
		windows[roiNumber] = new DetectorROI("SCA" + roiNumber, lowHighScalerWindowChannels[0], lowHighScalerWindowChannels[1]);
	}

	@Override
	public Integer[] getWindows(int channel, int roiNumber) {
		return new Integer[] { windows[roiNumber].getRoiStart(), windows[roiNumber].getRoiEnd() };
	}

	@Override
	public void setFilePath(String path) {
		this.path = path;
	}

	@Override
	public String getFilePath() {
		return path;
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
	public int getNumFramesPerReadout() throws DeviceException {
		return 1;
	}

	@Override
	public void setFilePrefix(String template) throws DeviceException {
		this.template = template;
	}

	@Override
	public void setNextFileNumber(int nextNumber) throws DeviceException {
		this.nextNumber = nextNumber;
	}

	@Override
	public String getFilePrefix() throws DeviceException {
		return template;
	}

	@Override
	public int getNextFileNumber() throws DeviceException {
		return nextNumber;
	}

	@Override
	public void doStart() throws DeviceException {
		// tfg.start();
	}

	@Override
	public boolean isSavingFiles() throws DeviceException {
		return false;
	}

	@Override
	public void setSavingFiles(Boolean saveFiles) throws DeviceException {
	}

	@Override
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		int numFrames = finalFrame - startFrame + 1;
		int numChannels = finalChannel - startChannel + 1;

		Double[][] results = new Double[numFrames][numChannels];

		Double counts = 10.0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int chan = 0; chan < numChannels; chan++) {
				if (isChannelEnabled(chan)) {
					results[frame][chan] = counts;
					counts += 10.0;
				} else {
					results[frame][chan] = 0.0;
				}
			}
		}

		return results;
	}

	@Override
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		return readoutDTCorrectedSCA1(startFrame, finalFrame, startChannel, finalChannel);
	}

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		// int[frame][channel][time,reset ticks, reset counts,all events, all
		// goodEvents, pileup counts]
		int numFrames = finalFrame - startFrame + 1;
		int numChannels = finalChannel - startChannel + 1;

		Integer[][][] results = new Integer[numFrames][numChannels][6];
		for (int frame = 0; frame < numFrames; frame++) {
			for (int chan = 0; chan < numChannels; chan++) {
				if (isChannelEnabled(chan)) {
					results[frame][chan] = new Integer[] { 0, 1, 2, 3, 4, 5 };
				} else {
					results[frame][chan] = new Integer[6];
				}
			}
		}

		return results;
	}

	@Override
	public Integer[][] readoutDTCParameters(int startChannel, int finalChannel) {
		// int[channel][allGoodGradient,allGoodOffset,inWindowGradient,inWindowOffset]

		int numChannels = finalChannel - startChannel + 1;

		Integer[][] results = new Integer[numChannels][4];
		for (int chan = 0; chan < numChannels; chan++) {
			results[chan] = new Integer[] { 0, 1, 2, 3 };
		}

		return results;
	}

	@Override
	public Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame, int startChannel, int finalChannel)
			throws DeviceException {
		int numFrames = finalFrame - startFrame + 1;
		int numChannels = finalChannel - startChannel + 1;

		Double[][][] results = new Double[numFrames][numChannels][getNumberROIToRead()];

		Double counts = 10.0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int chan = 0; chan < numChannels; chan++) {
				for (int roi = 0; roi < getNumberROIToRead(); roi++) {
					if (isChannelEnabled(chan)) {
						results[frame][chan][roi] = counts;
						counts += 10;
					} else {
						results[frame][chan][roi] = 0.0;
					}
				}
			}
		}
		return results;
	}

	@Override
	public double[][] readoutDTCorrectedLatestMCA(int startChannel, int finalChannel) throws DeviceException {
		return readoutDTCorrectedLatestSummedMCA(startChannel, finalChannel);
	}

	@Override
	public double[][] readoutDTCorrectedLatestSummedMCA(int startChannel, int finalChannel) throws DeviceException {
		int numChannels = finalChannel - startChannel + 1;
		// int[] rawData = daServer.getIntBinaryData("read 0 0 0 " + 4096 + " "
		// + 1 + " " + 1 + " from " + mcaHandle + " raw motorola", 4096);

		double[][] results = new double[numChannels][4096];
		Random generator = new Random();

		for (int chan = 0; chan < numChannels; chan++) {
			for (int mcaChan = 0; mcaChan < 4096; mcaChan++) {
				if (isChannelEnabled(chan)) {
					results[chan][mcaChan] = generator.nextInt(new Double(1000.0).intValue() * 10000);
				} else {
					results[chan][mcaChan] = 0.0;
				}
			}
		}

		return results;
	}

	public Tfg getTfg() {
		return tfg;
	}

	public void setTfg(Tfg tfg) {
		this.tfg = tfg;
	}

	public DummyDAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DummyDAServer daServer) {
		this.daServer = daServer;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setHDFFileAutoIncrement(boolean b) {
		// do nothing in this sim
	}

	@Override
	public void setHDFNumFramesToAcquire(int i) throws DeviceException {
		// do nothing in this sim
	}

	@Override
	public int getNumberOfChannels() {
		// defines the size of data arrays
		return numberOfChannels;
	}

	public void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	@Override
	public boolean isChannelEnabled(int channel) throws DeviceException {
		return enabledChannels[channel];
	}

	@Override
	public void enableChannel(int channel, boolean doEnable) throws DeviceException {
		enabledChannels[channel] = doEnable;
	}

	@Override
	public String getFullFileName() throws DeviceException {
		return simulationFileName;
	}

	/**
	 * An hdf file containing MCA data.
	 *
	 * @param filename
	 */
	public void setSimulationFileName(String filename){
		simulationFileName = filename;
	}

	@Override
	public void setPerformROICalculations(Boolean doCalcs) throws DeviceException {
	    // do nothing
	}

	@Override
	public int getNumberOfRois() {
		return NUMBER_ROIs;
	}

	@Override
	public int getMcaSize() {
		return MCA_SIZE;
	}

	@Override
	public int getTotalHDFFramesAvailable() throws DeviceException {
		return numFramesToAcquire;
	}

	@Override
	public void setHDFAttributes(boolean b) throws DeviceException {
		// do nothing in this sim
	}

	@Override
	public void setHDFPerformance(boolean b) throws DeviceException {
		// do nothing in this sim
	}

	@Override
	public void setHDFNumFramesChunks(int i) throws DeviceException {
		// do nothing in this sim
	}

	@Override
	public int monitorUpdateArraysAvailableFrame(int desiredPoint) throws DeviceException {
		return numFramesToAcquire;
	}

	@Override
	public void setPointsPerRow(Integer pointsPerRow) throws DeviceException {
	}

	@Override
	public ReadyForNextRow monitorReadyForNextRow(ReadyForNextRow readyForNextRow) throws DeviceException {
		return readyForNextRow;
	}

	@Override
	public void setFileEnableCallBacks(UPDATE_CTRL callback) throws DeviceException {

	}

	@Override
	public void setFileCaptureMode(CAPTURE_MODE captureMode) throws DeviceException {
	}

	@Override
	public void setFileArrayCounter(int arrayCounter) throws DeviceException {
	}

	@Override
	public void setHDFLazyOpen(boolean b) throws DeviceException {

	}

}
