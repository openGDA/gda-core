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

package uk.ac.gda.server.ncd.mar;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DetectorBase;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.util.Sleep;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.ThreadManager;

/**
 * Controls the MarCCD Mosaic detector.
 */
@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class MarCCDDetector extends DetectorBase implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(MarCCDDetector.class);

	/**
	 * Suffix of image files this detector creates. Not cbf by default
	 */
	public static final String SUFFIX = "mccd";

	/**
	 * The largest dimension in mm of this detector
	 */
	public static final int DIMENSION = 300;

	public MarCCDController mc;

	protected String host = "marccd01";

	protected int port = 2222;

	private int rawMode = MarCCDController.CORRECTED;

	private int darkMode = MarCCDController.DARKSAMPLE;

	private Timer timer;

	private NumTracker tracker;

	private volatile boolean acquiring = false;

	private String filename;

	private Thread readoutThread;

	public MarCCDDetector() {
	}

	@Override
	public void configure() throws FactoryException {
		if (mc != null) {
			try {
				mc.close();
			} catch (Exception e) {
				// ignored
			}
		}
		try {
			mc = new MarCCDController(host, port);
			mc.initialize();
			if (readoutThread == null) {
				readoutThread = ThreadManager.getThread(this, getName() + " readout");
				readoutThread.start();
			}
			if (tracker == null) {
				tracker = new NumTracker(getName());
			}
			configured = true;
		} catch (Exception e) {
			logger.error("exception in configure: ", e);
			throw new FactoryException("exception in configure", e);
		}
	}

	/**
	 * Reconfigure the detector interface, if it has been cut off
	 * @throws FactoryException 
	 * 
	 * @see gda.device.DeviceBase#reconfigure()
	 */
	@Override
	public void reconfigure() throws FactoryException {
		configure();
	}

	/**
	 * @param hostname
	 */
	public void setHost(String hostname) {
		host = hostname;
	}

	/**
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Stops a data acquisition started with "start"
	 */
	public void abort() {
		if (configured) {
			mc.abort();
		}
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isAcquiring() throws DeviceException {
		if (mc.isAcquiring()) {
			return true;
		}
		return false;
	}

	@Override
	public int getStatus() throws DeviceException {

		if (mc.isReading()) {
			return Detector.MONITORING;
		} else if (mc.isError()) {
			return Detector.FAULT;
		} else if (mc.isAcquiring()) {
			return Detector.BUSY;
		} else if (acquiring) {
			return Detector.BUSY;
		}
		return Detector.IDLE;
	}

	/**
	 * Get current binning mode
	 * 
	 * @return bin 2,3,4 or 8
	 * @throws DeviceException
	 */
	public int getBinMode() throws DeviceException {
		return mc.getBin()[0];
	}

	/**
	 * Change binning mode
	 * 
	 * @param mode
	 *            2,3,4 or 8
	 */
	public void setBinMode(int mode) {
		mc.setBin(mode);
	}

	/**
	 * @param type
	 */
	public void setReadType(int type) {
		rawMode = type;
		mc.setCorrected(type);
	}

	/**
	 * @return int
	 */
	public int getReadType() {
		if (mc.getCorrected() != rawMode) {
			logger.warn("Raw mode settings are not correct"); // this value should equal rawMode
		}
		return rawMode;
	}

	/**
	 * This won't change anything about how the scripts actually collect data, only in scripts
	 * 
	 * @return Returns the darkMode.
	 */
	public int getDarkType() {
		return darkMode;
	}

	/**
	 * @param darkMode
	 *            The darkMode to set.
	 */
	public void setDarkType(int darkMode) {
		this.darkMode = darkMode;
	}

	@Override
	public void setAttribute(String name, Object value) throws DeviceException {
		Integer I = new Integer(-1);
		if (value instanceof Integer) {
			I = (Integer) value;
		}

		// binning options
		if (name.equalsIgnoreCase(MarCCDController.BINNING)) {
			if (I.equals(MarCCDController.BINTWO)) {
				mc.setBin(I);
			}
			if (I.equals(MarCCDController.BINTHREE)) {
				mc.setBin(I);
			}
			if (I.equals(MarCCDController.BINFOUR)) {
				mc.setBin(I);
			}
			if (I.equals(MarCCDController.BINEIGHT)) {
				mc.setBin(I);
			} else {
				logger.info("Unknown binning mode requested for MarCCDDetector");
			}
		}
		// raws options
		else if (name.equalsIgnoreCase(MarCCDController.RAWDATA)) {
			if (I.equals(MarCCDController.RAW)) {
				setReadType(MarCCDController.RAW);
			} else {
				setReadType(MarCCDController.CORRECTED);
			}
		} else if (name.equalsIgnoreCase(MarCCDController.DARKDATA)) {
			if (I.equals(MarCCDController.DARKINTERVAL)) {
				setDarkType(MarCCDController.DARKINTERVAL);
			} else if (I.equals(MarCCDController.DARKSAMPLE)) {
				setDarkType(MarCCDController.DARKSAMPLE);
			} else if (I.equals(MarCCDController.DARKNO)) {
				setDarkType(MarCCDController.DARKNO);
			} else {
				logger.info("Unknown dark mode requested for MarCCDDetector");
			}
		} else {
			super.setAttribute(name, value);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object result = null;

		if (attributeName.equalsIgnoreCase(MarCCDController.BINNING)) {
			result = getBinMode();
		} else if (attributeName.equalsIgnoreCase(MarCCDController.RAWDATA)) {
			result = getReadType();
		} else if (attributeName.equalsIgnoreCase(MarCCDController.DARKDATA)) {
			result = getDarkType();
		} else {
			result = super.getAttribute(attributeName);
		}
		return result;
	}

	public void start() throws DeviceException {
		if (!configured) {
			throw new DeviceException("not configured");
		}
		if (isAcquiring()) {
			logger.info("clearing detector");
			mc.readout(3); // make sure detector is clear before trying to read
			mc.waitUntilReadingStarted();
			mc.waitUntilReadingDone();
		}
		logger.info("Start acquire called");
		mc.start();

	}

	@Override
	public void stop() throws DeviceException {
		if (!configured) {
			throw new DeviceException("not configured");
		}
		mc.readout(0);
		mc.correct();
	}

	/**
	 * Procedure derived from MarCCD software manual v 0.10.17, 2006. Appendix 3
	 */
	public void takeDarkExposure() {

		logger.info("Collecting two images, then dezingering.");
		try {
			if (isAcquiring()) {
				logger.info("found in acquiring state, should be reading: clearing detector");
				mc.readout(3); // make sure detector is clear before trying
			}
			for (int i = 0; i < 2; ++i) {
				double darkTime = 1; // use 1 sec, no point in doing the entire data collection time.
				logger.info("collect dark " + i + ": start");
				start();
				long elapsedTime = 0, startTime;
				Date date = new Date();

				startTime = date.getTime();
				elapsedTime = (date.getTime() - startTime) / 1000;

				while (elapsedTime < darkTime) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException ex) {
					}
					date = new Date();
					elapsedTime = (date.getTime() - startTime) / 1000;
				}

				if (i == 0) {
					mc.readout(2); // put first frame into scratch buffer
				} else {
					mc.readout(1); // put second frame into background buffer. will dezinger this one
				}
				mc.waitUntilReadingStarted();
				mc.waitUntilAllDone();
			}
			mc.dezinger(1);
			logger.info("Dark dezingering done, background collection complete");
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (DeviceException e) {
			logger.error("Error during dark exposures:" + e.getMessage());
		}
	}

	public void waitForReady() throws DeviceException {
		mc.waitUntilAllDone();
	}

	/**
	 * Wait until reading is done. Reading should have been started and status should reflect this, or else it will
	 * return immediately. Will allow more rapid data collections.
	 * 
	 * @throws DeviceException
	 */
	public void waitWhileReading() throws DeviceException {
		mc.waitUntilReadingDone();
	}

	/**
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Mar 165 SX";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CCD";
	}

	@Override
	public void collectData() throws DeviceException {
		timer.start();
		start();
		filename = null;
		acquiring = true;
	}

	@Override
	public Object readout() throws DeviceException {
		return filename;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Timer getTimer() {
		return timer;
	}

	@Override
	public void run() {
		while (true) {
			while (!acquiring) {
				Sleep.sleep(50);
			}
			try {
				String path = PathConstructor.createFromDefaultProperty();
				filename = path + "/" + getName() + "-" + tracker.incrementNumber() + "." + SUFFIX;
				while (timer.getStatus() != Timer.IDLE) {
					Sleep.sleep(50);
				}
				mc.readout(rawMode, filename);
			} catch (Exception e) {
				logger.error("Error in acquisition/readout", e);
			}
			acquiring = false;
		}
	}
}