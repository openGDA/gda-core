package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DummyDAServer;
import gda.device.timer.Tfg;
import gda.factory.FactoryException;
import gda.factory.Findable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.detector.xspress3.ROI;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
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
public class DummyXspress3Controller implements Xspress3Controller, Findable {

	private static final Logger logger = LoggerFactory
			.getLogger(DummyXspress3Controller.class);

	private static final int NUMBER_CHANNELS = 4;
	// Tfg is only for simulation purposes to get current state (in real system
	// Xspress3 hardware would know what is going on)
	// Tfg should not be driven by this simulation as Xspress3Controllers only
	// have access to Xspress3 hardware which has no frame times
	private Tfg tfg;
	private DummyDAServer daServer;
	private String name = "not set";
	private String xspressSystemName = "'xs3'";
	private String mcaOpenCommand = "xspress2 open-mca " + xspressSystemName;
	private String scalerOpenCommand = "xspress2 open-scalers "
			+ xspressSystemName;
	private String startupScript = "xspress2 format-run " + xspressSystemName
			+ " res-none";
	private int mcaHandle = -1;
	private int scalerHandle = -1;
	private Integer numFramesToAcquire;
	private TRIGGER_MODE mode;
	private ROI[] roi;
	private ROI[] windows;
	private String path;
	private int numRoiToRead;
	private String template;
	private int nextNumber;

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
			String formatCommand = "xspress2 format-run " + xspressSystemName
					+ " 12 res-none";
			daServer.sendCommand(formatCommand);

			// clear rois
			String roiCommand = "xspress2 set-roi " + xspressSystemName + " -1";
			daServer.sendCommand(roiCommand);

			// set windows and rois for 4-element
			String windowCommand = "xspress2 set-window " + xspressSystemName
					+ " " + 0 + " " + 0 + " " + 4096;
			daServer.sendCommand(windowCommand);
			windowCommand = "xspress2 set-window " + xspressSystemName + " "
					+ 1 + " " + 0 + " " + 4096;
			daServer.sendCommand(windowCommand);
			windowCommand = "xspress2 set-window " + xspressSystemName + " "
					+ 2 + " " + 0 + " " + 4096;
			daServer.sendCommand(windowCommand);
			windowCommand = "xspress2 set-window " + xspressSystemName + " "
					+ 3 + " " + 0 + " " + 4096;
			daServer.sendCommand(windowCommand);

			roiCommand = "xspress2 set-roi " + xspressSystemName
					+ " 0 100 200 1";
			daServer.sendCommand(roiCommand);
			roiCommand = "xspress2 set-roi " + xspressSystemName
					+ " 1 100 200 1";
			daServer.sendCommand(roiCommand);
			roiCommand = "xspress2 set-roi " + xspressSystemName
					+ " 2 100 200 1";
			daServer.sendCommand(roiCommand);
			roiCommand = "xspress2 set-roi " + xspressSystemName
					+ " 3 100 200 1";
			daServer.sendCommand(roiCommand);

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
						throw new DeviceException(
								"Failed to create the mca handle");
					}
					logger.info("Xspress2System: open() using mcaHandle "
							+ mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0) {
						throw new DeviceException(
								"Failed to create the scaler handle");
					}
					logger.info("Xspress2System: open() using scalerHandle "
							+ scalerHandle);
				}
			}
		}
	}

	private void close() throws DeviceException {
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + mcaHandle);
			mcaHandle = -1;
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + scalerHandle);
			scalerHandle = -1;
		}
	}

	private synchronized void sendCommand(String command, int handle)
			throws DeviceException {
		Object obj;
		if ((obj = daServer.sendCommand(command + handle)) == null) {
			throw new DeviceException(
					"Null reply received from daserver during " + command);
		} else if (((Integer) obj).intValue() == -1) {
			logger.error("DummyXspress3Controller: " + command + " failed");
			close();
			throw new DeviceException("DummyXspress3Controller " + command
					+ " failed");
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
	public void setROILimits(int channel, int roiNumber,
			int[] lowHighMCAChannels) {
		if (this.roi == null) {
			roi = new ROI[NUMBER_CHANNELS];
		}
		roi[roiNumber] = new ROI("ROI" + roiNumber, lowHighMCAChannels[0],
				lowHighMCAChannels[1]);

	}

	@Override
	public Integer[] getROILimits(int channel, int roiNumber) {
		return new Integer[] { roi[roiNumber].getStart(),
				roi[roiNumber].getEnd() };
	}

	@Override
	public void setWindows(int channel, int roiNumber,
			int[] lowHighScalerWindowChannels) {
		if (this.windows == null) {
			windows = new ROI[2];
		}
		windows[roiNumber] = new ROI("SCA" + roiNumber,
				lowHighScalerWindowChannels[0], lowHighScalerWindowChannels[1]);
	}

	@Override
	public Integer[] getWindows(int channel, int roiNumber) {
		return new Integer[] { windows[roiNumber].getStart(),
				windows[roiNumber].getEnd() };
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
	public void setNumberROIToRead(int numRoiToRead)
			throws IllegalArgumentException {
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
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		int numFrames = finalFrame - startFrame + 1;
		int numChannels = finalChannel - startChannel + 1;

		Double[][] results = new Double[numFrames][numChannels];

		Double counts = 10.0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int chan = 0; chan < numChannels; chan++) {
				results[frame][chan] = counts;
				counts += 10.0;
			}
		}

		return results;
	}

	@Override
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		return readoutDTCorrectedSCA1(startFrame, finalFrame, startChannel,
				finalChannel);
	}

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		// int[frame][channel][time,reset ticks, reset counts,all events, all
		// goodEvents, pileup counts]
		int numFrames = finalFrame - startFrame + 1;
		int numChannels = finalChannel - startChannel + 1;

		Integer[][][] results = new Integer[numFrames][numChannels][6];
		for (int frame = 0; frame < numFrames; frame++) {
			for (int chan = 0; chan < numChannels; chan++) {
				results[frame][chan] = new Integer[] { 0, 1, 2, 3, 4, 5 };
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
	public Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		int numFrames = finalFrame - startFrame + 1;
		int numChannels = finalChannel - startChannel + 1;

		Double[][][] results = new Double[numFrames][numChannels][getNumberROIToRead()];

		Double counts = 10.0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int chan = 0; chan < numChannels; chan++) {
				for (int roi = 0; roi < getNumberROIToRead(); roi++) {
					results[frame][chan][roi] = counts;
					counts += 10;
				}
			}
		}
		return results;
	}

	@Override
	public Double[][] readoutDTCorrectedLatestMCA(int startChannel,
			int finalChannel) throws DeviceException {
		return readoutDTCorrectedLatestSummedMCA(startChannel, finalChannel);
	}

	@Override
	public Double[][] readoutDTCorrectedLatestSummedMCA(int startChannel,
			int finalChannel) throws DeviceException {
		int numChannels = finalChannel - startChannel + 1;
		int[] rawData = daServer.getIntBinaryData("read 0 0 0 " + 4096 + " "
				+ 1 + " " + 1 + " from " + mcaHandle + " raw motorola", 4096);

		Double[][] results = new Double[numChannels][4096];
		for (int chan = 0; chan < numChannels; chan++) {
			for (int mcaChan = 0; mcaChan < 4096; mcaChan++) {
				results[chan][mcaChan] = (double) rawData[mcaChan];
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
		// TODO Auto-generated method stub
		return name;
	}

}
