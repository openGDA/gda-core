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

package gda.device.detector.datalogger;

import gda.device.DataLogger;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DetectorBase;
import gda.factory.Findable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simulator class for a DataLogger. It has four arrays of eight numbers which change according to the
 * "countStrings"/"countDoubles" variables. It also has a Poller, which has a default pollTime of 10 seconds. The
 * pollDone() method changes the dataValues in rotation and notifies the DataLogger's observers.
 *
 */
public class DummyDataLogger extends DetectorBase implements DataLogger, Detector, Findable, Runnable, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(DummyDataLogger.class);

	private long pollTime = 1000;

	// private int loggerID = -1;
	// private Poller poller;
	private int timeout = 100;

	// data for updating DataLoggerMonitor
	private String[] dataStringValues;

	// data for updating Scan
	private double[] dataDoubleValues;

	private String serialDeviceName;

	private boolean connected = false;

	private int countStrings = 1;

	private int countDoubles = 1;

	// uses a default of 8 channels for now.
	private int noOfChannels = 8;

	private String[] valuesString1 = { "1.0", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0" };

	private String[] valuesString2 = { "10.0", "20.0", "30.0", "40.0", "50.0", "60.0", "70.0", "80.0" };

	private String[] valuesString3 = { "11.0", "22.0", "33.0", "44.0", "55.0", "66.0", "77.0", "88.0" };

	private String[] valuesString4 = { "15.0", "25.0", "35.0", "45.0", "55.0", "65.0", "75.0", "85.0" };

	private double[] valuesDouble1 = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0 };

	private double[] valuesDouble2 = { 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0 };

	private double[] valuesDouble3 = { 11.0, 22.0, 33.0, 44.0, 55.0, 66.0, 77.0, 88.0 };

	private double[] valuesDouble4 = { 15.0, 25.0, 35.0, 45.0, 55.0, 65.0, 75.0, 85.0 };

	private Thread runner;

	private int status = 0;

	// **** Default constructor, getter and setter methods for CASTOR ****
	// //

	/**
	 * Default constructor for the Dummy Data Logger
	 */
	public DummyDataLogger() {
	}

	/**
	 * This configures and starts a Poller and calls the connect method.
	 */
	@Override
	public void configure()/* throws FactoryException */
	{
		// super.configure();

		try {
			connect();

			runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " " + getName());
			runner.start();
		} catch (DeviceException e) {
			logger.error("DummyDataLogger: Exception caught in configure. " + e.toString());
		}
	}

	/**
	 * Returns the name of this logger device.
	 *
	 * @return Returns the serialDeviceName.
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * Sets the name of this logger device.
	 *
	 * @param serialDeviceName
	 *            The serialDeviceName to set.
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * Returns the value of the local pollTime variable.
	 *
	 * @return pollTime.
	 */
	public long getPolltime() {
		return pollTime;
	}

	/**
	 * Sets the local pollTime variable.
	 *
	 * @param pollTime
	 *            The pollTime to set.
	 */
	public void setPolltime(long pollTime) {
		this.pollTime = pollTime;
	}

	// **** Methods for the DummyDataLogger itself **** //

	/**
	 * Real logger opens port connection to the logger device. This just sets the "connected" flag to true.
	 *
	 * @exception DeviceException
	 *                if the device cannot be found
	 */
	@Override
	public void connect() throws DeviceException {
		logger.info("DummyDataLogger is connecting");
		connected = true;
	}

	/**
	 * Real logger closes the port connection to the logger device. This just sets the "connected" flag to false.
	 *
	 * @exception DeviceException
	 *                if the device cannot be found
	 */
	@Override
	public void disconnect() throws DeviceException {
		connected = false;
	}

	/**
	 * Sets the local timeout variable.
	 *
	 * @param timeout
	 *            is the value to be set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the value of the local timeout variable.
	 *
	 * @return timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Gets the value of the local noOfChannels variable.
	 *
	 * @return noOfChannels
	 * @exception DeviceException
	 *                if the device cannot be found
	 */
	@Override
	public int getNoOfChannels() throws DeviceException {
		return noOfChannels;
	}

	/**
	 * This method changes the dataStringValues String array by rotating through four arrays of eight numbers according
	 * to the "countStrings" variable. Used for generating data to update the DataLoggerMonitor with. N.B. Sets
	 * dataStringValues to reference a random array of preset numbers.
	 */
	private void readValuesToStrings() {
		// FIXME Make values more random - or more Gaussian
		switch (countStrings) {
		case 1:
			dataStringValues = valuesString1;
			break;
		case 2:
			dataStringValues = valuesString2;
			break;
		case 3:
			dataStringValues = valuesString3;
			break;
		case 4:
			dataStringValues = valuesString4;
			break;
		}

		if (countStrings < 4)
			countStrings++;
		else
			countStrings = 1;
	}

	/**
	 * This method changes the dataStringValues String array by rotating through four arrays of eight numbers according
	 * to the "countStrings" variable. Used for generating data to pass back in response to a Scan collectData call.
	 * N.B. Sets dataDoubleValues to reference a random array of preset numbers.
	 */
	private void readValuesToDoubles() {
		// FIXME Make values more random - or more Gaussian
		switch (countDoubles) {
		case 1:
			dataDoubleValues = valuesDouble1;
			break;
		case 2:
			dataDoubleValues = valuesDouble2;
			break;
		case 3:
			dataDoubleValues = valuesDouble3;
			break;
		case 4:
			dataDoubleValues = valuesDouble4;
			break;
		}

		if (countDoubles < 4)
			countDoubles++;
		else
			countDoubles = 1;
	}

	@Override
	public void collectData() {
		readValuesToDoubles();
		logger.debug("****************" + System.currentTimeMillis());
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public int[] getDataDimensions() {
		int[] dims = { noOfChannels };
		return dims;
	}

	@Override
	public Object readout() throws DeviceException {
		return dataDoubleValues;
	}

	/**
	 * Calls readValuesToStrings() and fills the dataStringValues array, then notifies its observers. DataLoggerMonitor
	 * should display the data and scans should ignore the data. Called each time the pollTime elapses, implementing
	 * Runnable interface.
	 */
	@Override
	public void run() {
		if (runner == null) {
			logger.warn("No thread to read data logger values for " + this.getName());
		}

		while (runner != null) {
			try {
				if (connected) {
					// Read from Datalogger and convert to string array
					// which is desired format for DataLoggerMonitor
					// display.
					readValuesToStrings();
					status = Detector.STANDBY;
					notifyIObservers(this, dataStringValues);
				}
				Thread.sleep(pollTime);
			} catch (InterruptedException ie) {
				logger.error("DummyDataLogger: run() interrupted", ie);
				return;
			}
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy Data Logger";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "dummy 2313";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Dummy";
	}

}
