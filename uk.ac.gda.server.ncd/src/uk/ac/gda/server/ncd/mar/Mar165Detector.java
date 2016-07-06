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

import gda.data.PathConstructor;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.MARLoader;

/**
 * Controls the MarCCD Mosaic detector.
 */
@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class Mar165Detector extends DetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(Mar165Detector.class);

	/**
	 * Suffix of image files this detector creates. Not cbf by default
	 */
	public static final String SUFFIX = "mccd";

	/**
	 * The largest dimension in mm of this detector
	 */
	public static final int DIMENSION = 300;

	public MarCCDController mc;

	protected String host = "localhost";

	protected int port = 2222;

	private int rawMode = MarCCDController.CORRECTED;

	private int darkMode = MarCCDController.DARKSAMPLE;

	private short tracker = 0;

	private String dirTemplate = "/tmp/";

	public Mar165Detector() {
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

			configured = true;
			takeDarkExposure();
		} catch (Exception e) {
			logger.error("exception in configure: ", e);
			throw new FactoryException("exception in configure", e);
		}
	}

	/**
	 * Reconfigure the detector interface, if it has been cut off
	 * 
	 * @throws FactoryException
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
	@Override
	public void stop() {
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
		Integer mode = new Integer(-1);
		if (value instanceof Integer) {
			mode = (Integer) value;
		}

		// binning options
		if (name.equalsIgnoreCase(MarCCDController.BINNING)) {
			if (mode.equals(MarCCDController.BINTWO)) {
				mc.setBin(mode);
			} else if (mode.equals(MarCCDController.BINTHREE)) {
				mc.setBin(mode);
			} else if (mode.equals(MarCCDController.BINFOUR)) {
				mc.setBin(mode);
			} else if (mode.equals(MarCCDController.BINEIGHT)) {
				mc.setBin(mode);
			} else {
				throw new DeviceException("Unknown binning mode requested for MarCCDDetector: "+mode);
			}
			takeDarkExposure();
		}
		// raws options
		else if (name.equalsIgnoreCase(MarCCDController.RAWDATA)) {
			if (mode.equals(MarCCDController.RAW)) {
				setReadType(MarCCDController.RAW);
			} else {
				setReadType(MarCCDController.CORRECTED);
			}
		} else if (name.equalsIgnoreCase(MarCCDController.DARKDATA)) {
			if (mode.equals(MarCCDController.DARKINTERVAL)) {
				setDarkType(MarCCDController.DARKINTERVAL);
			} else if (mode.equals(MarCCDController.DARKSAMPLE)) {
				setDarkType(MarCCDController.DARKSAMPLE);
			} else if (mode.equals(MarCCDController.DARKNO)) {
				setDarkType(MarCCDController.DARKNO);
			} else {
				throw new DeviceException("Unknown dark mode requested for MarCCDDetector: "+mode);
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

	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			return mc.getSize();
		} catch (IOException e) {
			throw new DeviceException("error getting dimensions", e);
		}
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
		tracker = (short) ((tracker + 1) % 10);

		logger.info("Start acquire called");
		mc.start();
	}

	public void stopSlow() throws DeviceException {
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
					logger.info("reading first frame into scratch buffer");

					mc.readout(2); // put first frame into scratch buffer
				} else {				
					logger.info("reading second frame into background buffer");
					mc.readout(1); // put second frame into background buffer. will dezinger this one
				}
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
		return false;
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
		start();
	}

	@Override
	public float[] readout() throws DeviceException {
		String path = PathConstructor.createFromTemplate(dirTemplate);
		String filename = path + "/" + getName() + "-" + tracker + "." + SUFFIX;

		new File(filename).delete();
		
		mc.readout(rawMode, filename);

		File file = new File(filename);

		while (!file.canRead()) {
			logger.info("waiting for " + filename + " to appear.");
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				throw new DeviceException("interrupted in readout waiting for file", e);
			}
		}

		MARLoader marLoader = new MARLoader(filename);
		DataHolder dataHolder;
		try {
			dataHolder = marLoader.loadFile();
		} catch (ScanFileHolderException e) {
			throw new DeviceException("error reading mar file", e);
		}
		
		float[] data = (float[]) dataHolder.getDataset(0).cast(Dataset.FLOAT32).getBuffer();
		
		file.delete();
		return data;
	}

	public String getDirTemplate() {
		return dirTemplate;
	}

	public void setDirTemplate(String dirTemplate) {
		this.dirTemplate = dirTemplate;
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		return 0.0;
	}
}
