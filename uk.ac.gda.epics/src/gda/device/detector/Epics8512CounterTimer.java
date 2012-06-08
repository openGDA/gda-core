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

package gda.device.detector;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.timer.DummyTfg;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.xml.EpicsRecord;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gov.aps.jca.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to use a Hytec 8512 scaler board as a counter timer via EPICS
 * 
 * It can be used as a dummy counter timer if the appropriate xml variable is
 * set.
 * 
 * The collection time needs to be set in msecs though in non-dummy, real mode
 * this is converted into seconds.
 * 
 */

public class Epics8512CounterTimer extends gda.device.detector.DetectorBase implements
		Runnable, IObserver, InitializationListener {
	private static final Logger logger=LoggerFactory.getLogger(Epics8512CounterTimer.class);
	private boolean dummy = false;
	private int totalChans;
	private int state = Detector.IDLE;
	private Thread runner;
	private boolean waiting = false;
	private long sleepTime;
	private boolean simulatedCountRequired = false;
	private double data[];
	private boolean timeChannelRequired = false;
	private Timer tfg;
	private double roundingFactor = Math.pow(10.0, 2);
	private int pointCounter = 0;

	// EPICS stuff
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private String epicsRecordName;
	private EpicsRecord epicsRecord;
	private Channel startcount = null;
	private Channel preset = null;
	private Channel countChannel[] = null;

	/**
	 * Constructor.
	 */
	public Epics8512CounterTimer() {

		// This bit needed for dummy operation
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " " + getName());
		runner.start();
		while (!waiting) {
			Thread.yield();
		}
		Thread.yield();

		// EPICS
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		if (dummy) {
			tfg = new DummyTfg();
			((Configurable) tfg).configure();
			tfg.addIObserver(this);
			data = new double[totalChans];
		}

		// Set up the PRESET, STARTCOUNT and n x CHn EPICS channels
		if (!configured && !dummy) {
			if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(
					epicsRecordName)) != null) {
				epicsRecordName = epicsRecord.getFullRecordName();
				try {
					startcount = channelManager.createChannel(epicsRecordName
							+ "STARTCOUNT.VAL");
					preset = channelManager.createChannel(epicsRecordName
							+ "PRESET.VAL");
					countChannel = new Channel[totalChans];
					for (int i = 0; i < totalChans; i++) {
						// countChannel[i]=
						// channelManager.createChannel(epicsRecordName + "CH" +
						// (i + 1) + ".VAL");
						countChannel[i] = channelManager
								.createChannel(epicsRecordName + "T" + i);
					}
				} catch (Throwable th) {
					throw new FactoryException("failed to create channels", th);
				}
			}
			configured = true;
		}
	}

	/**
	 * Sets EPICS record name
	 * 
	 * @param name
	 */
	public void setEpicsRecordName(String name) {
		this.epicsRecordName = name;
	}

	/**
	 * Returns EPICS record name
	 * 
	 * @return Record Name
	 */
	public String getEpicsRecordName() {
		return this.epicsRecordName;
	}

	/**
	 * 
	 * If dummy set class acts as dummy counter timer
	 * 
	 * @param dummy
	 */

	public void setDummyOperation(boolean dummy) {
		this.dummy = dummy;
	}

	/**
	 * Returns whether this acts as a dummy counter timer.
	 * 
	 * @return {@code true} if this is a dummy counter timer
	 */
	public boolean isDummyOperation() {
		return dummy;
	}

	public void countAsync(double time) throws DeviceException {

		collectionTime = time;
		if (dummy) {
			state = Detector.BUSY;
			sleepTime = (long) time;
			pointCounter = 0;
			synchronized (this) {
				simulatedCountRequired = true;
				notifyAll();
			}
		} else {
			setCollectionTime(collectionTime);
			start();
		}
	}

	public void start() throws DeviceException {
		if (dummy) {
			tfg.start();
		} else {
			// Set STARTCOUNT = 1 to begin counting
			try {
				controller.caput(startcount, 1);
			} catch (Throwable th) {
				throw new DeviceException("EPICS 8512 counter failed to start",
						th);
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		if (dummy) {
			tfg.stop();
		} else {
			// Set STARTCOUNT = 0 to stop counting
			try {
				controller.caput(startcount, 0);
			} catch (Throwable th) {
				throw new DeviceException("EPICS 8512 counter failed to stop",
						th);
			}
		}
	}

	/**
	 * Returns the time in msec the detector collects for during a call to
	 * collectData()
	 * 
	 * @return double
	 * @throws DeviceException 
	 */
	@Override
	public double getCollectionTime() throws DeviceException  {
		if (!dummy) {
			// get PRESET channel value and return it's value in msec
			try {
				collectionTime = controller.cagetDouble(preset) * 1000;
			} catch (Exception e) {
				throw new DeviceException("Error getting collectionTime for " + getName(),e);
			}
		}
		return collectionTime;
	}

	/**
	 * Sets the time in msecs the detector collects for during a call to
	 * collectData()
	 * 
	 * @param collectionTime
	 * @throws DeviceException 
	 * 
	 */
	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		if (!dummy) {
			// Set PRESET channel to collection time converted to seconds
			try {
				controller.caput(preset, collectionTime / 1000);
			} catch (Exception e) {
				throw new DeviceException("Error setting collectionTime for " + getName(),e);
			}
		}
		this.collectionTime = collectionTime;
	}

	/**
	 * Reads and returns the measured data counts()
	 * 
	 * @return data
	 * @throws DeviceException
	 */
	public double[] readChans() throws DeviceException {
		int startChannel = 0;

		if (timeChannelRequired) {
			data[0] = getCollectionTime();
			startChannel = 1;
		}
		for (int i = startChannel; i < data.length; i++) {
			if (dummy) {
				data[i] = (int) (Math.random() * 1000.0) * (pointCounter + 1);
				data[i] = round(data[i]);
			} else
			/**
			 * Reads and returns the measured data counts()
			 * 
			 * @return data
			 * @throws DeviceException
			 */
			{
				// get values from EPICS count channels
				try {
					data[i] = controller.cagetDouble(countChannel[i]);
				} catch (Exception e) {
					throw new DeviceException("EPICS 8512 counter " + getName() + " failed to readout",e);
				}
			}
		}
		pointCounter++;
		return data;
	}

	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	/**
	 * Returns the last stored data counts()
	 * 
	 * (use readChans() to read in and return recently measured data counts)
	 * 
	 * @return data
	 * @throws DeviceException
	 */
	@Override
	public double[] readout() throws DeviceException {
		return readChans();
	}

	/**
	 * Returns whether a time channel is required.
	 * 
	 * @return {@code true} if a time channel is required
	 */
	public boolean isTimeChannelRequired() {
		return timeChannelRequired;
	}

	/**
	 * Sets whether a time channel is required.
	 * 
	 * @param timeChannelRequired {@code true} if a time channel is required
	 */
	public void setTimeChannelRequired(boolean timeChannelRequired) {
		this.timeChannelRequired = timeChannelRequired;
	}

	/**
	 * getStatus returns 0 if idle, 1 if busy
	 * 
	 * @see gda.device.Detector#getStatus()
	 */
	@Override
	public int getStatus() throws DeviceException {

		int status = 0;
		if (dummy) {
			status = state;
		} else {
			// Get STARTCOUNT value, if =0 then busy, =1 the finished
			try {
				status = controller.cagetInt(startcount);
			} catch (Exception e) {
				throw new DeviceException(" EPICS 8512 counter " + getName() + "failed to get status", e);
			}
		}
		return status;
	}

	// Used in dummy mode to simulate counter timing
	@Override
	public void run() {
		try {
			while (runner != null) {
				synchronized (this) {
					// FIXME if the notifyIObservers is uncommented it cause a
					// fatal
					// eventChannel error. Why?
					// notifyIObservers(this, null);
					waiting = true;
					do {
						logger.debug("dummy counter timer {} main wait",
								getName());
						wait();
						logger.debug("dummy counter timer {} main wake up",
								getName());
					} while (!simulatedCountRequired);
				}
				logger.debug("dummy counter timer {} sleep wait", getName());

				Thread.sleep(sleepTime);
				data = calculateData();
				logger.debug("dummy counter timer {} sleep wake up", getName());
				state = Detector.IDLE;
			}
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage());
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	// Used in dummy mode to generate dummy data
	private double[] calculateData() {
		double[] data = new double[totalChans];

		for (int i = 0; i < totalChans; i++) {
			data[i] = (int) (Math.random() * 10.0) * (pointCounter + 1);
			data[i] = round(data[i]);
		}

		return data;
	}

	private double round(double value) {
		return Math.rint(value * roundingFactor) / roundingFactor;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		logger.debug("Epics 8512 counter timer {} sleep wait", getName());
	}

	@Override
	public void initializationCompleted() {
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Epics8512CounterTimer";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Epics8512CounterTimer";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Epics8512CounterTimer";
	}

}
