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
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gov.aps.jca.CAException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
 * It will be shared across different beamlines but only Spectroscopy ones for the moment. If other beamlines need this
 * then this class should be moved into its own plugin.
 * <p>
 * This class operates in two modes: either it can be part of a stwp scan, but it will force the scan to run slow enough
 * that the Hiden can take a new reading for every data point.
 * <p>
 * Alternatively it will write its own file and take readings as fast as it can and add timestamps so that the data can
 * be compared with fly scan data offline.
 * <p>
 * If it is already recording to a file and it is included in a scan that it will throw an exception which will abort
 * the scan.
 */
public class HidenRGAScannable extends ScannableBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(HidenRGAScannable.class);
	private static final String FORMAT = "%.3f";

	private class HidenFileWriter extends Thread {

		private String filename;
		private boolean shouldStop = false;

		public HidenFileWriter(String filename) {
			this.filename = filename;
		}

		@Override
		public void run() {
			shouldStop = false;
			// open file
			try (BufferedWriter buffer = new BufferedWriter(new FileWriter(filename))) {
				writeHeader(buffer);
				// infinite loop
				while (!shouldStop) {
						if (lastScanCycleUsed != lastScanCycleSeen){
						recordNewEntry(buffer);
						lastScanCycleUsed = lastScanCycleSeen;
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						shouldStop = true;
					}
				}
			} catch (DeviceException e) {
				logger.error("DeviceException when reading the RGA to file", e);
			} catch (IOException e1) {
				logger.error("IOException when trying to create RGA file " + filename, e1);
			} finally {
				try {
					stopRGA();
				} catch (IOException e) {
					logger.error("IOException when stopping the RGA after writing to file", e);
				}
			}
		}

		private void writeHeader(BufferedWriter buffer) throws IOException {
			String[] columns = getExtraNames();

			StringBuffer lineToWrite = new StringBuffer();
			lineToWrite.append("Timestamp");
			lineToWrite.append("\t");
			for (String column : columns) {
				lineToWrite.append(column);
				lineToWrite.append("\t");
			}
			lineToWrite.append("\n");

			buffer.write(lineToWrite.toString());
		}

		private void recordNewEntry(BufferedWriter buffer) throws DeviceException, IOException {
			Date timestamp = new Date();
			double[] latestMasses = controller.readout();
			double valve = controller.readValve();
			double temp = controller.readtemp();

			StringBuffer lineToWrite = new StringBuffer();
			lineToWrite.append(String.format("%d",timestamp.getTime()));
			lineToWrite.append("\t");
			for (double mass : latestMasses) {
				lineToWrite.append(String.format(FORMAT, mass));
				lineToWrite.append("\t");
			}
			lineToWrite.append(String.format(FORMAT, valve));
			lineToWrite.append("\t");
			lineToWrite.append(String.format(FORMAT, temp));
			lineToWrite.append("\n");

			buffer.write(lineToWrite.toString());
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
	private boolean inAScan = false;

	public HidenRGAScannable() {
		setInputNames(new String[] {});
	}

	@Override
	public void configure() throws FactoryException {
		try {
			controller.connect();
			controller.addIObserver(this);
		} catch (CAException e) {
			throw new FactoryException("CAException when trying to connect ot RGA", e);
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

	public void startRecording() throws IOException {
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
	}

	private String nextRGAFilename() throws IOException {
		String dataDir = PathConstructor.createFromDefaultProperty();

		String numTrackerDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		NumTracker runs = new NumTracker("hiden_rga", numTrackerDir);
		int thisFileNumber = runs.incrementNumber();

		String fullFilename = dataDir + IPath.SEPARATOR + getName() + thisFileNumber + ".dat";
		return fullFilename;
	}

	public void stopRecording() {
		// end the thread writing the file, which should in turn stop the RGA
		if (isRecording()) {
			fileWriterThread.stopWriting();
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		
		synchronized (this){
		try {
			double[] latestMasses = controller.readout();
			double valve = controller.readValve();
			double temp = controller.readtemp();

			latestMasses = ArrayUtils.add(latestMasses, valve);
			latestMasses = ArrayUtils.add(latestMasses, temp);

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
		// external to this class to the rest of the GDA, the RGA is busy if it is recording
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

	public Set<Integer> getMasses() {
		return masses;
	}

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
		newNames = (String[]) ArrayUtils.add(newNames, "valve");
		outputFormat = (String[]) ArrayUtils.add(outputFormat, FORMAT);
		newNames = (String[]) ArrayUtils.add(newNames, "temp");
		outputFormat = (String[]) ArrayUtils.add(outputFormat, FORMAT);

		this.setExtraNames(newNames);
	}

	@Override
	public void update(Object source, Object arg) {
		// mark that new data has arrived since the last readout, if we are part of a scan or recording
		if (inAScan || isRecording()) {
			lastScanCycleSeen = (int) arg;
		}
	}
}
