/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.cobolddetector;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.device.AsynchronousDetector;
import gda.device.CoboldDetector;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.util.Alarm;
import gda.util.AlarmListener;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement a Cobold Time to Digital Converter for data acquisition using a Micro Channel Plate (MCP) detector
 * for Ion Imaging Spectrometry (IIS)
 */
public class CoboldTDC extends DetectorBase implements AsynchronousDetector, CoboldDetector, AlarmListener {

	private static final Logger logger = LoggerFactory.getLogger(CoboldTDC.class);

	private ArrayList<String> channelLabelList = new ArrayList<String>();
	// the LMF name without the ".lmf" at the end
	private DecimalFormat lmfNumFmt = new DecimalFormat("00000");
	private DecimalFormat max_f = new DecimalFormat("000000000");
	private Alarm spectrumUpdater = null;
	// no. milli-seconds after which spectrum is updated
	private int updateInterval = 60000;
	private Alarm scanTimer;
	private static final String END_OF_SCAN = "scan time elapsed";
	// new acquisition command and its components
	private String newCommand;
	private String comment = "no comment";
	private double maxNumEvents = 999999999;
	private boolean standAlone;
	private boolean collectingData = false;
	// 3rd party scripting interface to Cobold
	protected String coboldPCCMD = "CoboldPCCMD";
	// lmfName will always have the form "lmf<n1>_{r<n2>}.lmf" where
	// {r<n2>} is only used for multi-dimensional scans. The ".lmf" is
	// also auto-added by Cobold (with file-series disabled) so must be removed
	// on insertion onto the "New Acquisition" command
	protected String lmfName;
	protected String dataDir;
	protected boolean saveDCFsBetweenRuns;
	protected boolean readFromHardware;
	private int collectingStatus;
	private boolean savingFiles = false;

	@Override
	public void configure() {
		createNewAcquisitionCommand();
	}

	/**
	 * Constructor
	 */
	public CoboldTDC() {
		dataDir = LocalProperties.get("gda.gui.iis.dataDir", LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
		standAlone = true;
		saveDCFsBetweenRuns = true;
		readFromHardware = true;
	}

	@Override
	public void prepareForCollection() {
		createNewAcquisitionCommand();
	}

	/**
	 * @param t
	 *            the collection time
	 */
	public void setCollectTime(int t) {
		collectionTime = t;
	}

	/**
	 * Set the value of 1st hit on channel z1 via parameter 99 to stream data into the current LMF event
	 *
	 * @param z1
	 *            the value to set z1 to in LMF event
	 */
	public void setZ1(int z1) {
		// @FIXME think causes runtime error
		this.sendSynchronousCommand("parameter 99," + z1);
	}

	/**
	 * @param b
	 *            true to read from hardware
	 */
	public void setReadFromHardware(boolean b) {
		readFromHardware = b;
	}

	private void createNewAcquisitionCommand() {
		if (readFromHardware) {
			// check that lmf does not exist and make a new name if it does
			lmfName = getNextLmfName();
			StringWriter sw = new StringWriter();
			sw.write("new HARDWARE,");
			// @README don't do replaceAll(".lmf","") as the "." may be a wild char
			int i = lmfName.lastIndexOf(".lmf");
			String temp = lmfName.substring(0, i);
			sw.append("\"");
			temp = temp.replace('/', '\\');
			sw.append(temp);
			sw.append("\"");
			sw.append(",ANALYSIS,");
			sw.append(max_f.format(maxNumEvents));
			sw.append(",");
			sw.append(comment);
			sw.flush();
			newCommand = sw.toString();
		} else {
			// read from LMF file
			newCommand = "new " + lmfName;
		}
	}

	/**
	 * @param events
	 */
	public void setMaxNumEvents(int events) {
		maxNumEvents = events;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	/**
	 * Get the label string for the channel specified
	 *
	 * @return label the label string
	 * @throws DeviceException
//	 * @see gda.device.CounterTimer#getChannelLabel(int)
	 */
	@Override
	public ArrayList<String> getChannelLabelList() throws DeviceException {
		return channelLabelList;
	}

	public void setChannelLabelList(ArrayList<String> channelLabelList) {
		this.channelLabelList = channelLabelList;
	}

	/**
	 * Add channel label
	 *
	 * @param label
	 *            the label string
	 */
	public void addChannelLabel(String label) {
		channelLabelList.add(label);
	}

	/**
	 * Set the label string for the channel specified
	 *
	 * @param channel
	 *            int
	 * @param label
	 *            String
	 * @throws DeviceException
	 */
	@Override
	public void setChannelLabel(int channel, String label) throws DeviceException {
		if (channel >= 0 && channel < channelLabelList.size())
			channelLabelList.add(channel, label);
	}

	/**
	 * Get the label string for the channel specified
	 *
	 * @param channel
	 *            who's label is to be returned
	 * @return label the label string
	 * @throws DeviceException
	 */
	@Override
	public String getChannelLabel(int channel) throws DeviceException {
		String label = null;
		if (channel >= 0 && channel < channelLabelList.size())
			label = channelLabelList.get(channel);

		return label;
	}

	/**
	 * Collect data synchronously (normal timescan) or asynchronously (asynchronousTimescan - ie. not synchronous with
	 * other detectors, data readout while detector status = MONITORING). If this is an asynchronous scan then allow the
	 * monitoring of this detector while counting, ScanBase waits for all detectors to be !BUSY before reading out so
	 * set detector status to MONITORING (though this logic is the wrong way round really as this detector is BUSY and
	 * the beamMonitor is monitoring! if this is a synchronous scan (normal detector time scan) then let Scan classes
	 * wait until collection finished (status != BUSY) before detector readout. Note this does not work properly because
	 * readout will only occur if isActive (isUsedByDefault()) is true but in this case the detector may be scanned
	 * synchronously in small time between asynchronous scans.
	 *
	 * @param collectionTime
	 *            double data collection time in milli-secs
	 * @param synchronousScan
	 *            true for synchronous TimeScan, false for AsynchronousTimeScan
	 * @throws InterruptedException
	 */
	private synchronized void collect(double collectionTime, boolean synchronousScan) throws InterruptedException {
		if (!collectingData) {
			this.collectionTime = collectionTime;
			collectingStatus = synchronousScan ? Detector.BUSY : Detector.MONITORING;
			createNewAcquisitionCommand();
			logger.info("starting TDC collecting data for " + collectionTime + " milli-seconds");
			startCoboldScan(newCommand);
		}
	}

	private synchronized void waitForStatus(int state, int timeout, int interval) throws DeviceException, InterruptedException {
		if (timeout < interval)
			timeout = interval;
		// CoboldPCCMD slow so initially wait for 1 sec then check status every
		// <interval> milli-secs
		int wait = 0;
		int status;
		while (getStatus() != state && wait < timeout) {
			Thread.sleep(interval);
			wait += interval;
		}
		if ((status = getStatus()) != state)
			throw new DeviceException("Cobold waitForStatus = " + state + " timed out, Cobold Detector Status = "
					+ status);
	}

	protected void waitForStatus(int state) throws DeviceException, InterruptedException {
		// default getStatus() timeout = 5, secsinterval = 3 secs
		waitForStatus(state, 5000, 3000);
	}

	private void waitForStatus(int state, int timeout) throws DeviceException, InterruptedException {
		// default getStatus() interval = 3 secs
		waitForStatus(state, timeout, 3000);
	}

	/**
	 * Create a unique lmf name in a file series from a series name and using an lmf run tracker file to check track of
	 * file number
	 *
	 * @return String a unique lmf file name
	 */
	private String getNextLmfName() {
		// location of tracker file lmfFileSeriesName is expected to be set by
		// the java property gda.var
		String lmfPath = null;
		try {
			NumTracker lmfNum = new NumTracker("lmf");
			StringWriter nameWriter = new StringWriter();
			nameWriter = new StringWriter();
			nameWriter.write(dataDir + "/lmf");
			nameWriter.append(lmfNumFmt.format(lmfNum.getCurrentFileNumber()));
			nameWriter.append(".lmf");
			nameWriter.flush();
			lmfPath = nameWriter.toString();
			if (!standAlone) {
				lmfPath = appendSrsRunToLmfName(lmfPath);
			}

			String transferredFile = lmfPath.concat("gon");
			while (new File(lmfPath).exists() || new File(transferredFile).exists()) {
				nameWriter = new StringWriter();
				nameWriter.write(dataDir + "/lmf");
				lmfNum.incrementNumber();
				nameWriter.append(lmfNumFmt.format(lmfNum.getCurrentFileNumber()));
				nameWriter.append(".lmf");
				nameWriter.flush();
				lmfPath = nameWriter.toString();
				if (!standAlone) {
					lmfPath = appendSrsRunToLmfName(lmfPath);
				}
			}
			lmfPath = lmfPath.replace('\\', '/');
		} catch (IOException e1) {
			logger.error("IOException caught in CoboldExperimentPanel.updateLmfFileSeriesName");
		}
		return lmfPath;
	}

	/**
	 * Append the the next SRS run-number to the specified string receeded by "_r" to easily identify it and finally "_"
	 * to seperate file series no.
	 */
	private String appendSrsRunToLmfName(String s) {
		StringWriter sw = new StringWriter();

		// create the first part of the LMF name ("lmf00169", for eg)
		if (s.contains("_r"))
			sw.write(s.substring(0, s.indexOf("_r")));
		else
			sw.write(s.substring(0, s.lastIndexOf(".lmf")));

		// now add the SRS runnumber ("_r<n>")
		try {
			// the SRS runnumber tracker
			NumTracker srsNum = new NumTracker("tmp");
			sw.append("_r");
			sw.append(lmfNumFmt.format(srsNum.getCurrentFileNumber()));
		} catch (IOException e1) {
			logger.error("IOException caught in CoboldExperimentPanel.appendSrsRunToString");
		}

		// finally add the extension
		sw.append(".lmf");
		sw.flush();

		return sw.toString();
	}

	@Override
	public void alarm(Alarm anAlarm) {
		if (anAlarm.equals(scanTimer)) {
			endCollection();
		} else if (anAlarm.equals(spectrumUpdater)) {
			// resend the scheduled command and reset the alarm
			updateSpectrum();
			if (this.collectingData)
				anAlarm.reschedule();
		}
	}

	/**
	 * update the spectrum
	 */
	public void updateSpectrum() {
		sendAsynchronousCommand("update");
	}

	@Override
	public void collectData() throws DeviceException {
		// a synchronous detector scan ie will start and stop the
		// detector in sync with other detectors, must only allow this if the
		// detector is not already doing an asynchronous scan ie status =
		// MONITORING.

		try {
			collect(collectionTime, true);
		} catch (InterruptedException e) {
			String msg = getName() + " - Thread interrupted while collecting data";
			logger.error(msg, e);
			Thread.currentThread().interrupt();
			throw new DeviceException(msg, e);
		}
	}

	/**
	 * Start counting asynchronously (don't wit till finished) {@inheritDoc}
	 *
	 * @throws DeviceException
	 * @see gda.device.AsynchronousDetector#countAsync(double)
	 */
	@Override
	public void countAsync(double collectionTime) throws DeviceException {
		try {
			collect(collectionTime, false);
		} catch (InterruptedException e) {
			String msg = getName() + " - Thread interrupted while collecting data";
			logger.error(msg, e);
			Thread.currentThread().interrupt();
			throw new DeviceException(msg, e);
		}
	}

	/**
	 * Send a command to CoboldPC without waiting for command to execute
	 *
	 * @param command
	 *            the CoboldPC command String
	 */
	public void sendAsynchronousCommand(String command) {
		logger.info("CoboldTDC sending async command " + command);
		try {
			new GdaSubProcessBuilder().runCommand(coboldPCCMD, false, command);
		} catch (RuntimeException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Send a command to CoboldPC waiting for it to finish execution
	 *
	 * @param command
	 *            String containing the CoboldPC command
	 */
	public synchronized void sendSynchronousCommand(String command) {
		logger.info("CoboldTDC sending sync command " + command);
		try {
			new GdaSubProcessBuilder().runCommand(coboldPCCMD, true, command);
		} catch (RuntimeException e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * Start the Cobold scan letting the scanTimer alarm end the collection and scheduling regular spectrum updates
	 *
	 * @param newCommand
	 * @throws InterruptedException
	 */
	public void startCoboldScan(String newCommand) throws InterruptedException {
		if (readFromHardware) {
			try {
				try {
					waitForStatus(Detector.IDLE);
					sendAsynchronousCommand(newCommand);
					waitForStatus(Detector.STANDBY, 5000);
				} catch (DeviceException e) {
					// CoboldPCCMD sometimes fails so try sending New Acquisition
					// command once more
					sendAsynchronousCommand(newCommand);
					waitForStatus(Detector.STANDBY, 5000);
				}
				try {
					sendAsynchronousCommand("start");
					waitForStatus(collectingStatus, 5000);

					startCollecting();
				} catch (DeviceException e2) {
					// CoboldPCCMD sometimes fails so try sending start command once
					// more
					sendAsynchronousCommand("start");
					waitForStatus(collectingStatus, 5000);
					startCollecting();
				}
			} catch (DeviceException e) {
				logger.error("DeviceException in CoboldTDC.StartCoboldScan: " + e.getMessage());
				notifyIObservers(this, new Integer(Detector.IDLE));
			}
		} else {
			sendAsynchronousCommand(newCommand);
			sendAsynchronousCommand("start");
			sendAsynchronousCommand("update");
		}
	}

	protected void startCollecting() {
		// let CoboldExperimentPanel know that we really have started collecting
		// data
		notifyIObservers(this, new Integer(collectingStatus));

		sendAsynchronousCommand("update");
		scanTimer = new Alarm(new Double(collectionTime).intValue(), this, END_OF_SCAN);
		spectrumUpdater = new Alarm(updateInterval, this);
	}

	@Override
	public int getStatus() {
		// @FIXME not implemented for Unix, of course CoboldPC only
		// runs on Windows but could use dummy?

		int status = Detector.IDLE;
		if (savingFiles)
			status = Detector.BUSY;
		else if (System.getenv("OS").toLowerCase().contains("windows")) {
			String reply =
			// GdaSubProcessBuilder.getInstance().runCommand("DaqState.bat", true);
			new GdaSubProcessBuilder().runCommand("DaqState.bat", true);

			if (reply.contains("\"-1\"") || reply.contains("\"3\""))
				status = collectingStatus;

			else if (reply.contains("\"1\""))
				status = Detector.IDLE;
			else if (reply.contains("\"2\""))
				status = Detector.STANDBY;
			else
				status = Detector.IDLE;

			if (collectingData && status == Detector.IDLE)
				collectingData = false;
			else if (!collectingData && status == collectingStatus)
				collectingData = true;
		}

		return status;
	}

	/**
	 * {@inheritDoc} Return the file name created by the asynchronous detector since createsOwnFiles()
	 */
	@Override
	public Object readout() {
		return collectingData ? "Collecting " + lmfName : "Collected " + lmfName;
	}

	/**
	 * End the Cobold data collection by sending STOP_COMMAND and cancelling the timer and the spectrum updater
	 *
	 * @see gda.device.Detector#endCollection()
	 */
	@Override
	public void endCollection() {
		if (spectrumUpdater != null) {
			spectrumUpdater.cancel();
			updateSpectrum();
		}
		if (scanTimer != null)
			scanTimer.cancel();

		try {
			try {
				sendAsynchronousCommand("stop");
				waitForStatus(Detector.IDLE, 5000);
			} catch (DeviceException e) {
				// try stop command a second time
				sendAsynchronousCommand("stop");
				waitForStatus(Detector.IDLE, 5000);
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			logger.warn("{} - Thread interrupted while stopping motor", ie);
		} catch (DeviceException e1) {
			logger.error("stop command timed-out", e1);
		} finally {
			savingFiles = true;
			if (saveDCFsBetweenRuns)
				try {
					saveFiles();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.warn("{} - Thread interrupted while saving file. Files may not have been written correctly");
				}
			savingFiles = false;
			// let the scan know that the detector has finished collection, this
			// will end the scan
			notifyIObservers(this, new Integer(Detector.IDLE));
		}
	}

	private void saveFiles() throws InterruptedException {
		// wait for LMF, DCF & COD files to be written
		int wait = 200;
		File lmf = new File(lmfName);
		Thread.sleep(200);
		while (lmf.exists() && !lmf.canRead() && wait < 10000) {
			Thread.sleep(200);
			wait += 200;
		}
		String dcfName = saveCurrentDcf();
		logger.info("Co0boldTDC saving DCF file " + dcfName);
		Thread.sleep(200);

		File dcf = new File(dcfName);
		wait = 200;
		while (dcf.exists() && !dcf.canRead() && wait < 10000) {
			Thread.sleep(200);
			wait += 200;
		}

		String codName = saveCOD();
		File codFile = new File(codName);
		Thread.sleep(200);

		wait = 200;
		while (codFile.exists() && !codFile.canRead() && wait < 10000) {
			Thread.sleep(200);
			wait += 200;
		}
	}

	private String saveCOD() {
		int i = 0;
		// @README don't do replaceAll(".lmf","") as the "." may be a wild char
		if (lmfName.contains(".lmf"))
			i = lmfName.lastIndexOf(".lmf");

		// String codName = lmfName.substring(0, i) + ".cod";
		String codName = lmfName.substring(0, i);

		this.sendAsynchronousCommand("ExportASCII \"" + codName + "\"");
		return codName;
	}

	/**
	 * @param b
	 *            true to set stand alone mode
	 */
	public void setStandAlone(boolean b) {
		if (!b) {
			standAlone = b;
			// now change fileName and update New Acquisition command
			createNewAcquisitionCommand();
			// let CoboldExperimentPanel adjust it's standAlone status and lmf name
			notifyIObservers(this, new Boolean(standAlone));
		}
	}

	/**
	 * Save current Cobold Document File
	 *
	 * @return String name of DCF file
	 */
	public String saveCurrentDcf() {
		int i = lmfName.length();
		// @README don't do replaceAll(".lmf","") as the "." may be a wild char
		if (lmfName.contains(".lmf"))
			i = lmfName.lastIndexOf(".lmf");
		// String dcfName = lmfName.substring(0, i) + ".dcf";
		String dcfName = lmfName.substring(0, i);
		File dcf = new File(dcfName);
		logger.info("CoboldTDC attempting to save file " + dcfName);
		if (!dcf.exists())
			this.sendAsynchronousCommand("Save \"" + dcfName + "\"");

		return dcfName;

	}

	/**
	 * Restart the CoboldPC by clearing all parameters, coordinates and spectra
	 */
	public void restart() {
		sendAsynchronousCommand(CoboldCommands.RESTART);
	}

	/**
	 * Set LMF name
	 *
	 * @param name
	 *            String full path name of lmf file to be used in creation of New Acquisition command
	 */
	public void setLmfName(String name) {
		lmfName = name;
	}

	/**
	 * Set whether to auto-save DCFs between runs
	 *
	 * @param b
	 */
	public void setSaveDCFsBetweenRuns(boolean b) {
		saveDCFsBetweenRuns = b;
	}

	/**
	 * Show the defined Cobold parameters from the CoboldPC GUI
	 */
	public void showParams() {
		sendAsynchronousCommand(CoboldCommands.SHOW_PARAMS);
	}

	/**
	 * Show the defined Cobold spectra from the CoboldPC GUI
	 */
	public void showSpectra() {
		sendAsynchronousCommand(CoboldCommands.SHOW_SPECTRA);
	}

	/**
	 * Set the comment to be used in the LMF header
	 *
	 * @param comment
	 *            String the user defined comment
	 */
	public void setComment(String comment) {
		logger.info("LMF: " + comment);
		this.comment = comment;
	}

	/**
	 * Execute a file of Cobold commands for setting up an experiment in terms of pareameters, coordinates, conditions
	 * and spectra. Correct syntax assumed (see CoboldPC user manual)
	 *
	 * @param ccf
	 */
	public void executeCoboldCommandFile(String ccf) {
		sendAsynchronousCommand(CoboldCommands.EXECUTE_CCF + " \"" + ccf + "\"");
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Cobold TDC";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CoboldTDC";
	}
}
