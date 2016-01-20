/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.scannable.ScannableBase;
import gda.epics.EpicsConstants;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gov.aps.jca.CAException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Hiden RGA is a gas mass analyser for use on Spectroscopy beamlines.
 * <p>
 * It will be shared across different beamlines but only Spectroscopy ones for
 * the moment. If other beamlines need this then this class maybe should be
 * moved into its a different repository.
 * <p>
 * This class operates in two modes: either it can be part of a stwp scan, but
 * it will force the scan to run slow enough that the Hiden can take a new
 * reading for every data point.
 * <p>
 * Alternatively it will write its own file and take readings as fast as it can
 * and add timestamps so that the data can be compared with fly scan data
 * offline.
 * <p>
 * If it is already recording to a file and it is included in a scan that it
 * will throw an exception which will abort the scan.
 */
public class HidenRGAScannable extends ScannableBase implements IObserver, HidenRGA {

	public static final String RECORDING_STARTED = "RGA recording started";
	public static final String RECORDING_STOPPED = "RGA recording stopped";

	private static final Logger logger = LoggerFactory.getLogger(HidenRGAScannable.class);
	private static final String FORMAT = "%.6g";
	private static final String DATE_FORMAT = "HH:mm:ss:SSS d MMM yyyy";
	private static final String TIME_FORMAT = "HH:mm:ss:SSS";
	private boolean useAuxiliaryInputs = true;

	private class HidenFileWriter extends Thread {

		private String filename;
		private boolean shouldStop = false;
		private Date startTime;
		private DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		private DateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
		private Double[][] massesData = new Double[21][];
		private Double[] valveData;
		private Double[] tempData;
		private Double[] timeStampData;
		private int lastDataPoint;

		public HidenFileWriter(String filename) {
			this.filename = filename;
		}

		@Override
		public void run() {
			shouldStop = false;
			// open file
			try (BufferedWriter buffer = new BufferedWriter(new FileWriter(filename))) {
				lastDataPoint = 0;
				boolean headerWritten = false;
				// infinite loop
				while (!shouldStop) {
					if (lastScanCycleUsed != lastScanCycleSeen) {
						if (!headerWritten) {
							writeHeader(buffer);
							headerWritten = true;
						}

						if (collectionRate >= 1) {
							recordNewEntry(buffer);
						} else {
							recordMissingEntries(buffer);
						}
						lastScanCycleUsed = lastScanCycleSeen;
					}
					if (messageToAppend != null) {
						buffer.write(messageToAppend);
						buffer.flush();
						messageToAppend = null;
					}
					sleep();
				}
			} catch (DeviceException e) {
				logger.error("DeviceException when reading the RGA to file", e);
			} catch (IOException e1) {
				logger.error("IOException when trying to create RGA file " + filename, e1);
			} finally {
				try {
					stopRGA();
					String message = HidenRGAScannable.this.getName() + " has stopped recording";
					logger.info(message);
					InterfaceProvider.getTerminalPrinter().print(message);
					HidenRGAScannable.this.notifyIObservers(this, RECORDING_STOPPED);
				} catch (IOException e) {
					logger.error("IOException when stopping the RGA after writing to file", e);
				}
			}
		}

		private void sleep() {

			// increase the sleep time if the collectionRate attribute has been set
			int sleepTime_ms = 50;
			if (collectionRate >= 1) {
				sleepTime_ms = collectionRate * 100; // use 1/10th of the collection time to avoid overshooting too much
			}

			try {
				Thread.sleep(sleepTime_ms);
			} catch (InterruptedException e) {
				shouldStop = true;
			}
		}

		private void writeHeader(BufferedWriter buffer) throws IOException {
			startTime = new Date(); // used for 'slow' (throttled) data collection

			Double dataCollectionStartEpicsEpoch = controller.timestampPV.get(1)[0]; // in Epics epoch

			// Check if start time is zero (occurs while EPICS is waiting for the RGA to start scanning)
			while (Math.abs(dataCollectionStartEpicsEpoch.doubleValue()) < 0.01
					&& (new Date().getTime() - startTime.getTime()) < 60000) { // timeout after a minute
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				dataCollectionStartEpicsEpoch = controller.timestampPV.get(1)[0];
			}

			Date dataCollectionStart;
			if (Math.abs(dataCollectionStartEpicsEpoch.doubleValue()) < 0.01) {
				logger.warn("Timeout while waiting for timestamp from RGA controller");
				dataCollectionStart = startTime;
			} else {
				Double dataCollectionStartInJavaEpoch = dataCollectionStartEpicsEpoch + EpicsConstants.EPICS_EPOCH_OFFSET;
				dataCollectionStart = new Date(dataCollectionStartInJavaEpoch.longValue());
			}

			String[] columns = getExtraNames();

			StringBuffer header = new StringBuffer();
			header.append("# Scan started: ");
			header.append(dateFormat.format(dataCollectionStart));
			header.append("\n");
			header.append("Absolute Time");
			header.append("\t");
			header.append("Relative Time (ms)");
			for (String column : columns) {
				header.append("\t");
				header.append(column);
			}
			header.append("\n");

			buffer.write(header.toString());
			buffer.flush();
		}

		private void recordNewEntry(BufferedWriter buffer) throws DeviceException, IOException {
			Date now = new Date();
			long timeSinceStart = now.getTime() - startTime.getTime();
			double[] latestMasses = controller.readout();
			Double valve = null;
			Double temp = null;
			if (isUseAuxiliaryInputs()) {
				valve = controller.readValve();
				temp = controller.readtemp();
			}
			recordSingleEntry(buffer, now, timeSinceStart, latestMasses, valve, temp);
		}

		private void recordMissingEntries(BufferedWriter buffer) throws DeviceException, IOException {

			int availableDataPoints = controller.dataPointsCountPV.get();

			for (int chan = 0; chan < masses.size(); chan++) {
				massesData[chan] = controller.dataPVs[chan].get();
			}
			if (isUseAuxiliaryInputs()) {
				valveData = controller.valveDataPV.get(availableDataPoints);
				tempData = controller.tempDataPV.get(availableDataPoints);
			}
			timeStampData = controller.timestampPV.get(availableDataPoints);

			for (int cycle = lastDataPoint; cycle < availableDataPoints; cycle++) {
				logger.debug("Doing cycle " + cycle);
				double[] latestMasses = new double[masses.size()];
				for (int chan = 0; chan < masses.size(); chan++) {
					latestMasses[chan] = massesData[chan][cycle];
				}
				long absTime = timeStampData[cycle].longValue();
				long relativeTime = absTime - timeStampData[0].longValue();

				Date absTimeInJavaEpoch = new Date(absTime + EpicsConstants.EPICS_EPOCH_OFFSET);

				if (isUseAuxiliaryInputs()) {
					recordSingleEntry(buffer, absTimeInJavaEpoch, relativeTime, latestMasses, valveData[cycle], tempData[cycle]);
				} else {
					recordSingleEntry(buffer, absTimeInJavaEpoch, relativeTime, latestMasses, null, null);
				}
			}
			lastDataPoint = availableDataPoints;
		}

		private void recordSingleEntry(BufferedWriter buffer, Date absTime, long relativeTime, double[] latestMasses, Double valve, Double temp) throws IOException {

			StringBuffer lineToWrite = new StringBuffer();
			lineToWrite.append(timeFormat.format(absTime));
			lineToWrite.append("\t");
			lineToWrite.append(String.format("%d", relativeTime));
			for (double mass : latestMasses) {
				lineToWrite.append("\t");
				lineToWrite.append(String.format(FORMAT, mass));
			}
			if (valve != null) {
				lineToWrite.append("\t");
				lineToWrite.append(String.format(FORMAT, valve));
			}
			if (temp != null) {
				lineToWrite.append("\t");
				lineToWrite.append(String.format(FORMAT, temp));
			}
			lineToWrite.append("\n");

			buffer.write(lineToWrite.toString());
			buffer.flush();
		}

		public void stopWriting() {
			shouldStop = true;
		}
	}

	private String epicsPrefix;
	private Set<Integer> masses = new LinkedHashSet<Integer>();
	private HidenRGAController controller;
	private HidenFileWriter fileWriterThread;
	private volatile int lastScanCycleSeen = 0;
	private volatile int lastScanCycleUsed = 0;
	private volatile String messageToAppend;
	private boolean inAScan = false;
	private int collectionRate = -1; // the minimum time between data collections, in seconds

	public HidenRGAScannable() {
		setInputNames(new String[] {});
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			try {
				controller.setUseAuxiliaryInputs(isUseAuxiliaryInputs());//this must be called before connect.
				controller.connect();
				controller.addIObserver(this);
				configured = true;
			} catch (CAException e) {
				throw new FactoryException("CAException when trying to connect ot RGA", e);
			}
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		if (isRecording()) {
			throw new DeviceException("Cannot include " + getName()
					+ " in a scan as it is recording data to a file. Call its stopRecording() method.");
		}
		try {
			inAScan = true;
			startRGA();
		} catch (IOException e) {
			throw new DeviceException("IOException while reading from " + getName(), e);
		}
	}

	private void startRGA() throws IOException {
		controller.setMasses(masses);
		controller.setContinuousCycles();
		controller.writeToRGA();
		controller.startScan();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			inAScan = false;
			stopRGA();
		} catch (IOException e) {
			throw new DeviceException("IOException while reading from " + getName(), e);
		}
	}

	private boolean isRecording() {
		return fileWriterThread != null && fileWriterThread.isAlive();
	}

	@Override
	public void stop() throws DeviceException {
		atCommandFailure();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		try {
			inAScan = false;
			stopRGA();
		} catch (IOException e) {
			throw new DeviceException("IOException while reading from " + getName(), e);
		}
	}

	private void stopRGA() throws IOException {
		controller.stopScan();
	}

	@Override
	public void startRecording() throws IOException {

		if (isRecording()) {
			return;
		}

		// create a new file
		String filename = nextRGAFilename();

		// start the RGA
		startRGA();

		// start new thread to update the file every new scan cycle
		startFileWritingThread(filename);
	}

	private void startFileWritingThread(String filename) {
		fileWriterThread = new HidenFileWriter(filename);
		fileWriterThread.start();
		String message = getName() + " has started recording to " + filename + " with masses " + getMasses().toString();
		logger.info(message);
		InterfaceProvider.getTerminalPrinter().print(message);
		notifyIObservers(this, RECORDING_STARTED);
	}

	private String nextRGAFilename() throws IOException {
		String dataDir = PathConstructor.createFromDefaultProperty();

		String numTrackerDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		NumTracker runs = new NumTracker("hiden_rga", numTrackerDir);
		int thisFileNumber = runs.incrementNumber();

		String fullFilename = dataDir + IPath.SEPARATOR + getName() + thisFileNumber + ".dat";
		return fullFilename;
	}

	@Override
	public void stopRecording() {
		// end the thread writing the file, which should in turn stop the RGA
		if (isRecording()) {
			fileWriterThread.stopWriting();
		}
	}

	@Override
	public String toFormattedString() {
		if (configured) {
			return super.toFormattedString();
		}

		return getName() + ": not connected";
	}

	@Override
	public Object getPosition() throws DeviceException {

		synchronized (this) {

			if (!configured) {
				int numberZeroes = getExtraNames().length;
				return new int[numberZeroes];
			}

			try {
				double[] latestMasses = controller.readout();
				if (isUseAuxiliaryInputs()) {
					double valve = controller.readValve();
					double temp = controller.readtemp();

					latestMasses = ArrayUtils.add(latestMasses, valve);
					latestMasses = ArrayUtils.add(latestMasses, temp);
				}
				return latestMasses;
			} catch (IOException e) {
				throw new DeviceException("IOException while reading from " + getName(), e);
			} finally {
				if (inAScan) {
					lastScanCycleUsed = lastScanCycleSeen;
				}
			}
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		// external to this class to the rest of the GDA, the RGA is busy if it
		// is recording
		if (isRecording()) {
			return true;
		}
		if (!inAScan) {
			return false;
		}
		// we are busy if no scanCycle value update since readout
		return lastScanCycleUsed == lastScanCycleSeen;
	}

	public HidenRGAController getController() {
		return controller;
	}

	public void setController(HidenRGAController controller) {
		this.controller = controller;
	}

	public String getEpicsPrefix() {
		return epicsPrefix;
	}

	public void setEpicsPrefix(String epicsPrefix) {
		this.epicsPrefix = epicsPrefix;
	}

	@Override
	public Set<Integer> getMasses() {
		return masses;
	}

	@Override
	public void setMasses(int[] masses) {
		this.masses.clear();
		for (int mass : masses) {
			this.masses.add(mass);
		}
		setExtraNames();
	}

	private void setExtraNames() {
		String[] newNames = new String[masses.size()];
		String[] outputFormat = new String[masses.size()];
		Iterator<Integer> it = masses.iterator();
		for (int index = 0; index < masses.size(); index++) {
			newNames[index] = it.next() + "_amu";
			outputFormat[index] = FORMAT;
		}
		if (isUseAuxiliaryInputs()) {
			newNames = (String[]) ArrayUtils.add(newNames, "rga_valve");
			outputFormat = (String[]) ArrayUtils.add(outputFormat, FORMAT);
			newNames = (String[]) ArrayUtils.add(newNames, "rga_temp");
			outputFormat = (String[]) ArrayUtils.add(outputFormat, FORMAT);
		}
		this.setExtraNames(newNames);
		this.setOutputFormat(outputFormat);
	}

	@Override
	public int getCollectionRate() {
		return collectionRate;
	}

	@Override
	public void setCollectionRate(int collectionRate) {
		this.collectionRate = collectionRate;
	}

	@Override
	public void update(Object source, Object arg) {
		// mark that new data has arrived since the last readout, if we are part
		// of a scan or recording
		if (inAScan || isRecording()) {
			lastScanCycleSeen = (int) arg;
		}
	}

	public boolean isUseAuxiliaryInputs() {
		return useAuxiliaryInputs;
	}

	public void setUseAuxiliaryInputs(boolean useAuxiliaryInputs) {
		this.useAuxiliaryInputs = useAuxiliaryInputs;
	}

	/**
	 * Add a message to the RGA data file. This can be used for recording, say,
	 * scan numbers and sample conditions to allow them to be easily linked to
	 * the corresponding RGA data
	 *
	 * @param message
	 */
	public void addToOutputFile(String message) {
		String formattedMessage = String.format("# %s\n", message);
		if (isRecording()) {
			if (messageToAppend == null) {
				messageToAppend = formattedMessage;
			} else {
				messageToAppend += formattedMessage;
			}
		} else {
			logger.warn("RGA is not recording a file, could not append message '{}'", message);
		}
	}
}
