package gda.device.detector.xspress.xspress2data;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DAServer;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.xspress.Xspress2Detector;
import gda.factory.FactoryException;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;

public class Xspress2DAServerController implements Xspress2Controller {

	private static final Logger logger = LoggerFactory.getLogger(Xspress2DAServerController.class);

	protected final int numberOfScalers = 4; // number of values from each
												// hardware scaler (e.g. total,
												// resets, originalWindowed,
												// time)

	private Xspress2CurrentSettings settings;
	private TfgScaler ionChambersCounterTimer;
	private DAServer daServer = null;
	private Timer tfg = null;

	// Values used in DAServer commands
	private String xspressSystemName;
	private String mcaOpenCommand = null;
	private String scalerOpenCommand = null;
	private String startupScript = null;
	private int mcaHandle = -1;
	private int scalerHandle = -1;
	private int maxNumberOfFrames;

	private boolean configured;

	public Xspress2DAServerController() {
	}

	@Override
	public void configure() throws FactoryException {
		// If everything has been found send the format, region of interest,
		// windows & open commands.

		// Don't check for !configured here - we may want to configure several times with different settings.
		if (tfg != null && (daServer != null)) {

			try {
				configureDetectorFromParameters(); // 8.41-xas version does this here as well (to first clear and set rois, windows) - really needed?
				close();
				doStartupScript();
				doFormatRunCommand(determineNumberOfBits());
				configureDetectorFromParameters();
				open();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(), e);
			}
			configured = true;
		}
	}

	@Override
	public void configureDetectorFromParameters() throws DeviceException {
		// always remove all rois first
		if (LocalProperties.check("gda.xspress.mode.override")) {
			settings.getParameters().setReadoutMode(XspressDetector.READOUT_MCA);
		} else {
			doRemoveROIs();
		}

		for (DetectorElement detector : settings.getDetectorElements()) {
			doSetWindowsCommand(detector);
			if (settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
				doSetROICommand(detector);
			}
		}
	}

	@Override
	public void checkIsConnected() {
		if (!daServer.isConnected()) {
			daServer.connect();
		}
	}

	@Override
	public void start() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error sleeping for 100ms", e);
			}
			sendCommand("enable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("enable ", scalerHandle);
		}
	}

	@Override
	public void stop() throws DeviceException {
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
	public void close() throws DeviceException {
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + mcaHandle);
			mcaHandle = -1;
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + scalerHandle);
			scalerHandle = -1;
		}
	}

	@Override
	public void clear() throws DeviceException {
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
	public void reconfigure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer
		// object.
		logger.debug("Xspress2System.reconfigure(): reconnecting to: " + daServer.getName());
		try {
			daServer.reconnect();
			// does not reconfigure the tfg -- need to check if it is needed
			// If everything has been found send the open commands.
			if (tfg != null && (daServer != null)) {
				open();
			}
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		if (!daServer.isConnected()) {
			daServer.connect();
		}
		// if tfg not running with frames then clear and start the xspress
		// memory
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			clear();
			start();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return tfg.getStatus();
	}

	@Override
	public void setResolutionGrade(String resGrade) throws DeviceException {
		setResolutionGrade(resGrade, determineNumberOfBits());
	}

	@Override
	public void setResolutionGrade(String resGrade, int numberOfBits) throws DeviceException {
		settings.getParameters().setResGrade(resGrade);
		if (configured) {
			close();
			doFormatRunCommand(numberOfBits);
			open();
		}
	}

	@Override
	public int[] runOneFrame(int time) throws DeviceException {
		if (!daServer.isConnected()) {
			daServer.connect();
		}
		clear();
		start();
		tfg.clearFrameSets(); // we only want to collect a frame at a time
		tfg.countAsync(time); // run tfg for time
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		} while (tfg.getStatus() == Timer.ACTIVE);

		return readoutMca(0, 1, 4096); // NOTE 1 time frame
	}

	@Override
	public void setFullMCABits(int fullMCABits) throws DeviceException {
		if (configured) {
			settings.setFullMCABits(fullMCABits);
			close();
			doFormatRunCommand(determineNumberOfBits());
			open();
		}
	}

	/**
	 * @return int - the size in bits of the MCA array based on the readout mode
	 *         and region of interest options.
	 */
	private int determineNumberOfBits() {

		if (!settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			return settings.getFullMCABits();
		}

		int channels = findLargestChannelReadout();
		int order = 0;
		do {
			order++;
		} while (Math.pow(2, order) <= channels);

		return order;
	}

	private int findLargestChannelReadout() {
		int maxSize = 0;
		for (DetectorElement element : settings.getParameters().getDetectorList()) {
			int thisMcasize = 1; // always get an extra values for the out of
									// window counts
			for (DetectorROI roi : element.getRegionList()) {
				if (settings.getParameters().getRegionType().equals(XspressParameters.VIRTUALSCALER)) {
					thisMcasize++;
				} else {
					thisMcasize += roi.getRoiEnd() - roi.getRoiStart() + 1;
				}
			}
			if (maxSize < thisMcasize) {
				maxSize = thisMcasize;
			}
		}
		return maxSize;
	}

	private synchronized void sendCommand(String command, int handle) throws DeviceException {
		Object obj;
		if ((obj = daServer.sendCommand(command + handle)) == null) {
			throw new DeviceException("Null reply received from daserver during " + command);
		} else if (((Integer) obj).intValue() == -1) {
			logger.error("Command: " + command + " failed");
			close();
			throw new DeviceException(command + " failed");
		}
	}
	/**
	 * Create a valid string to use for setting resolution threshold;
	 * Returns original string if resolution grade is not for setting resolution threshold mode (i.e. NONE or ALLGRADES).
	 * @param resThresholdString
	 * @return
	 */
	private String makeValidResThresholdString( String resThresholdString ) throws DeviceException {
		if ( resThresholdString !=null && resThresholdString.startsWith(ResGrades.THRESHOLD) ) {
			// Try to extract resolution threshold value from resolution grade string
			try {
				String [] splitString = resThresholdString.split(" ");
				String resGradeValString = splitString[ splitString.length-1 ];
				Double resThresholdValue = Double.parseDouble(resGradeValString);
				resThresholdString = splitString[0] + " " + resThresholdValue.toString();
			}
			catch( Exception ex ) {
				String message = "Problem parsing resolution grade value from string '"+resThresholdString+"'";
				logger.error(message, ex);
				throw new DeviceException(message, ex);
			}
		}
		return resThresholdString;
	}

	/**
	 * execute the startup script on da.server
	 *
	 * @throws DeviceException
	 */
	private void doStartupScript() throws DeviceException {
		Object obj = null;
		if (daServer != null && daServer.isConnected()) {
			if (startupScript != null) {
				String newResGrade = settings.getParameters().getResGrade();
				// override the res-grade if the readout mode is scalers only or
				// saclers + mca
				if (!settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
					newResGrade = ResGrades.NONE;
				}
				newResGrade = makeValidResThresholdString(newResGrade);

				startupScript = "xspress2 format-run 'xsp1' " + newResGrade;
				if ((obj = daServer.sendCommand(startupScript)) == null) {
					throw new DeviceException("Null reply received from daserver during " + startupScript);
				} else if (((Integer) obj).intValue() == -1) {
					throw new DeviceException(startupScript + " failed");
				} else {
					maxNumberOfFrames = ((Integer) obj).intValue();
					logger.info("Xspress2System startup script - reply  was: " + maxNumberOfFrames);
				}
			}
		}
	}

	/**
	 * Execute the format-run command on da.server. This sets the resgrade.
	 */
	private void doFormatRunCommand(int numberOfBits) throws DeviceException {
		String newResGrade = settings.getParameters().getResGrade();
		// override the res-grade if the readout mode is scalers only or saclers
		// + mca
		if (!settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			newResGrade = ResGrades.NONE;
		}
		newResGrade = makeValidResThresholdString(newResGrade);

		String formatCommand = "xspress2 format-run " + xspressSystemName + " " + numberOfBits + " " + newResGrade;
		if (daServer != null && daServer.isConnected()) {
			Integer numFrames = ((Integer) daServer.sendCommand(formatCommand)).intValue();
			if (numFrames == null) {
				throw new DeviceException("Null reply received from daserver during " + formatCommand);
			} else if (numFrames == -1) {
				throw new DeviceException(formatCommand + " failed");
			} else if (numFrames < maxNumberOfFrames) {
				maxNumberOfFrames = numFrames;
				logger.info("Xspress2System formatCommand - maximum time frames achievable: " + maxNumberOfFrames);
			} else {
				logger.info("Xspress2System formatCommand - maximum time frames achievable: " + numFrames
						+ " but limited to " + maxNumberOfFrames + " by startupscript");
			}
		}
	}

	public void doRemoveROIs() throws DeviceException {
		Object obj;
		int rc;
		String roiCommand = "xspress2 set-roi " + xspressSystemName + " -1";
		if ((obj = daServer.sendCommand(roiCommand)) != null) {
			if ((rc = ((Integer) obj).intValue()) < 0) {
				throw new DeviceException("Xspress2System error removing regions of interest: " + rc);
			}
		}

	}

	@Override
	public void doSetROICommand(DetectorElement detector) throws DeviceException {
		Object obj;
		int rc;
		String roiCommand = "xspress2 set-roi " + xspressSystemName + " " + detector.getNumber();
		List<DetectorROI> regionList = detector.getRegionList();
		if (regionList.isEmpty())
		 {
			return; // No regions for detector element.
		}
		for (DetectorROI region : regionList) {
			roiCommand += " " + region.getRoiStart() + " " + region.getRoiEnd() + " " + calculateRegionBins(region);
		}
		if ((obj = daServer.sendCommand(roiCommand)) != null) {
			if ((rc = ((Integer) obj).intValue()) < 0) {
				throw new DeviceException("Xspress2System error setting regions of interest: " + rc);
			}
		}
	}

	private int calculateRegionBins(DetectorROI region) {
		int regionBins = 1; // 1 means a virtual scaler
		if (settings.getParameters().getRegionType() != null
				&& settings.getParameters().getRegionType().equals(XspressROI.MCA)) {
			// else regionBins should be the size of the MCA. (DAserver will not
			// accept any other values).
			regionBins = region.getRoiEnd() - region.getRoiStart() + 1;
		}
		return regionBins;
	}

	@Override
	public void doSetWindowsCommand(DetectorElement detector) throws DeviceException {
		Object obj;
		int rc;
		String windowCommand = "xspress2 set-window " + xspressSystemName + " " + detector.getNumber() + " "
				+ detector.getWindowStart() + " " + detector.getWindowEnd();
		if ((obj = daServer.sendCommand(windowCommand)) != null) {
			if ((rc = ((Integer) obj).intValue()) < 0) {
				throw new DeviceException("Xspress2System error setting windows: " + rc);
			}
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
					logger.info("Xspress2System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0) {
						throw new DeviceException("Failed to create the scaler handle");
					}
					logger.info("Xspress2System: open() using scalerHandle " + scalerHandle);
				}
			}

			if ((obj = daServer.sendCommand("xspress2 get-res-bins " + xspressSystemName)) != null) {
				settings.setMcaGrades(((Integer) obj).intValue());
				logger.info("Xspress2System: mcaGrades " + settings.getMcaGrades());
			}
		}
	}

	@Override
	public Double getI0() {
		Double I0 = 1.0;
		if (settings.getMcaGrades() == Xspress2Detector.ALL_RES && ionChambersCounterTimer != null) {
			try {
				I0 = ionChambersCounterTimer.readout()[0];
			} catch (DeviceException e) {
				logger.error("Exception while trying to fetch I0 to normalise scalers for each res grade", e);
			}
		}
		return I0;
	}

	@Override
	public int getTotalFrames() throws NumberFormatException, DeviceException {
		return Integer.parseInt(tfg.getAttribute("TotalFrames").toString());
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		// this value will be non-zero if collecting from a series of time
		// frames outside of the continuous scan mechanism
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			return 0;
		}
		return getNumberFramesFromTFGStatus();
	}

	public int getNumberFramesFromTFGStatus() throws DeviceException {
		String[] cmds = new String[] { "status show-armed", "progress", "status", "full", "lap", "frame" };
		HashMap<String, String> currentVals = new HashMap<String, String>();
		for (String cmd : cmds) {
			currentVals.put(cmd, runDAServerCommand("tfg read " + cmd).toString());
			logger.info("tfg read " + cmd + ": " + currentVals.get(cmd));
		}

		if (currentVals.isEmpty()) {
			return 0;
		}

		// else either scan not started (return -1) or has finished (return
		// continuousParameters.getNumberDataPoints())

		// if started but nothing collected yet
		if (currentVals.get("status show-armed").equals("EXT-ARMED")) {
			return 0;
		}

		// if frame is non-0 then work out the current frame
		if (!currentVals.get("frame").equals("0")) {
			String numFrames = currentVals.get("frame");
			return extractCurrentFrame(Integer.parseInt(numFrames));
		}

		return Integer.parseInt(tfg.getAttribute("TotalFrames").toString());
	}

	@Override
	public synchronized int[] readoutMca(int startFrame, int numberOfFrames, int mcaSize) throws DeviceException {
		int[] value = null;
		if (mcaHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				value = daServer.getIntBinaryData(
						"read 0 0 " + startFrame + " " + mcaSize + " " + settings.getNumberOfDetectors()
								* settings.getMcaGrades() + " " + numberOfFrames + " from " + mcaHandle
								+ " raw motorola", settings.getNumberOfDetectors() * settings.getMcaGrades() * mcaSize
								* numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		return value;
	}

	@Override
	public synchronized int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException {
		int[] value = null;
		if (scalerHandle < 0) {
			open();
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				value = daServer.getIntBinaryData(
						"read 0 0 " + startFrame + " " + numberOfScalers + " " + settings.getNumberOfDetectors() + " "
								+ numberOfFrames + " from " + scalerHandle + " raw motorola",
						settings.getNumberOfDetectors() * numberOfScalers * numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		return value;
	}

	private Object runDAServerCommand(String command) throws DeviceException {
		Object obj = null;
		if (daServer != null && daServer.isConnected()) {
			if ((obj = daServer.sendCommand(command)) == null) {
				throw new DeviceException("Null reply received from daserver during " + command);
			}
			return obj;
		}
		return null;
	}

	private int extractCurrentFrame(int frameValue) {
		if (isEven(frameValue)) {
			Integer numFrames = frameValue / 2;
			return numFrames;
		}
		Integer numFrames = (frameValue - 1) / 2;
		return numFrames;
	}

	private boolean isEven(int x) {
		return (x % 2) == 0;
	}

	public TfgScaler getIonChambersCounterTimer() {
		return ionChambersCounterTimer;
	}

	public void setIonChambersCounterTimer(TfgScaler ionChambersCounterTimer) {
		this.ionChambersCounterTimer = ionChambersCounterTimer;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public Timer getTfg() {
		return tfg;
	}

	public void setTfg(Timer tfg) {
		this.tfg = tfg;
	}

	public String getMcaOpenCommand() {
		return mcaOpenCommand;
	}

	public void setMcaOpenCommand(String mcaOpenCommand) {
		this.mcaOpenCommand = mcaOpenCommand;
	}

	public String getScalerOpenCommand() {
		return scalerOpenCommand;
	}

	public void setScalerOpenCommand(String scalerOpenCommand) {
		this.scalerOpenCommand = scalerOpenCommand;
	}

	public String getStartupScript() {
		return startupScript;
	}

	public void setStartupScript(String startupScript) {
		this.startupScript = startupScript;
	}

	public String getXspressSystemName() {
		return xspressSystemName;
	}

	public void setXspressSystemName(String xspressSystemName) {
		this.xspressSystemName = "'" + xspressSystemName + "'";
	}

	public int getMaxNumberOfFrames() {
		return maxNumberOfFrames;
	}

	public Xspress2CurrentSettings getCurrentSettings() {
		return settings;
	}

	@Override
	public void setCurrentSettings(Xspress2CurrentSettings settings) {
		this.settings = settings;
	}
}
