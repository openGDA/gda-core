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

package gda.device.detector.etldetector;

import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EtlDetector;
import gda.device.Scannable;
import gda.device.detector.DetectorBase;
import gda.device.detector.EpicsScaler;
import gda.device.enumpositioner.EpicsSimpleMbbinary;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.OutOfRangeException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;

/**
 * A class to represent a single ETL scintillation detector. This detector consists of two components: a sensor and a
 * counter. The sensor is based on scintillation technology, and the counter is using one channel out of 32 on a Struck
 * Scaler card.
 */
public class ETLDetector extends DetectorBase implements EtlDetector, Detector, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(ETLDetector.class);

	private static final String OPEN = "OPEN";
	private static final String CLOSE = "CLOSE";

	private String name;

	private String scalerName;

	private String detectorName;

	private int scalerChannelIndex;

	private EpicsScaler scaler;

	private EpicsETLController detector;

	private EpicsSimpleMbbinary fastshutter = null;

	public EpicsSimpleMbbinary getFastshutter() {
		return fastshutter;
	}

	public void setFastshutter(EpicsSimpleMbbinary fastshutter) {
		this.fastshutter = fastshutter;
	}

	private boolean shutterOpened=false;

	public boolean isShutterOpened() {
		return shutterOpened;
	}

	public void setShutterOpened(boolean shutterOpened) {
		this.shutterOpened = shutterOpened;
	}


	/**
	 * The Constructor.
	 */
	public ETLDetector() {
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			//wire up objects
			if (detectorName != null) {
				if ((detector = (EpicsETLController) Finder.getInstance().find(detectorName)) == null) {
					logger.error("Scintillation sensor control - " + detectorName + " - for Detector " + getName()
							+ " not found");
					throw new FactoryException("Scintillation sensor control - " + detectorName + " - for Detector "
							+ getName() + " not found");
				}
			}
			if (scalerName != null) {
				if ((scaler = (EpicsScaler) Finder.getInstance().find(scalerName)) == null) {
					logger.error("Detector " + scaler + " not found");
					throw new FactoryException("Scaler - " + scalerName + " - for detector " + getName()
							+ " not found.");
				}
			}
			configured=true;
		}

	}
	@Override
	public void atScanStart() throws DeviceException {
		if(!shutterOpened) {
			openShutter();
		}
	}
	public void openShutter() throws DeviceException {
		if (fastshutter != null) {
			try {
				fastshutter.moveTo(OPEN);
				shutterOpened = true;
			} catch (DeviceException e) {
				throw new DeviceException("Problem opening shutter", e);
			}
		}
	}
	@Override
	public void atScanEnd() throws DeviceException {
		if(shutterOpened) {
			closeShutter();
		}
	}
	public void closeShutter() throws DeviceException{
		if (fastshutter != null) {
			try {
				fastshutter.moveTo(CLOSE);
				shutterOpened=false;
			} catch (DeviceException e) {
				throw new DeviceException("Problem closing shutter", e);
			}
		}
	}
	@Override
	public void stop() throws DeviceException {
		scaler.stop();
		if (fastshutter != null && fastshutter.getPosition().toString().equalsIgnoreCase(OPEN)) {
			fastshutter.moveTo(CLOSE);
		}
	}

	/**
	 * Sets the collection time for the scalers
	 *
	 * @param time
	 *            period to count
	 */
	@Override
	public void setCollectionTime(double time) {
		scaler.setCollectionTime(time);

	}

	@Override
	public void collectData() throws DeviceException {
		if (fastshutter != null && fastshutter.getPosition().toString().equalsIgnoreCase(CLOSE)) {
			fastshutter.moveTo(OPEN);
		}
		// scaler.clear();
		scaler.start();
	}

	@Override
	public int getStatus() throws DeviceException {
		return scaler.getStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		return scaler.readout(scalerChannelIndex);
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		/* If this.readout() returned scaler.readout() then
		 * return scaler.getDataDimensions();
		 * would be correct (as scaler.readout() returns an int[]
		 * and scaler.getDataDimensions returns { getTotalChans() }.
		 * Unfortunately, scaler.readout(x) returns an int, so
		 * we should say this.readout() is 1D. */
		return new int[] { 1 };
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		scaler.prepareForCollection();
	}

	/**
	 * Sets the high voltage input in millivolt for the detector.
	 *
	 * @param mv
	 * @throws DeviceException
	 */
	@Override
	public void setHV(int mv) throws DeviceException {
		try {
			detector.setHighVoltage(mv);
		} catch (CAException | InterruptedException | OutOfRangeException e) {
			throw new DeviceException("Exception caught on setting detector " + detectorName + " high voltage.", e);
		}
	}

	/**
	 * Gets the actual output high voltage at the detector.
	 *
	 * @return actual output high voltage at the detector
	 * @throws DeviceException
	 */
	@Override
	public int getActualHV() throws DeviceException {
		try {
			return detector.getActualHVOutput();
		} catch (CAException | InterruptedException | TimeoutException e) {
			throw new DeviceException("Exception throws on getting the actual High Voltage output from detector "
					+ detectorName, e);
		}
	}

	/**
	 * Gets the requested High Voltage from the detector register.
	 *
	 * @return requested High Voltage from the detector register
	 * @throws DeviceException
	 */
	@Override
	public int getHV() throws DeviceException {
		try {
			return detector.getHighVoltage();
		} catch (CAException | TimeoutException | InterruptedException e) {
			throw new DeviceException("Excaption throw on access High voltage registry on detector " + detectorName, e);
		}
	}

	/**
	 * Sets the window's upper threshold for the detector.
	 *
	 * @param ulim
	 * @throws DeviceException
	 */
	@Override
	public void setUpperThreshold(int ulim) throws DeviceException {
		try {
			detector.setUpperLimit(ulim);
		} catch (CAException | InterruptedException  e) {
			throw new DeviceException("Excaption throw on setting upper threshold on detector " + detectorName, e);
		}
	}

	/**
	 * Gets the window's upper threshold from the detector.
	 *
	 * @return window's upper threshold from the detector
	 * @throws DeviceException
	 */
	@Override
	public int getUpperThreshold() throws DeviceException {
		try {
			return detector.getUpperLimit();
		} catch (CAException | TimeoutException | InterruptedException e) {
			throw new DeviceException("Excaption throw on getting upper threshold on detector " + detectorName, e);
		}
	}

	/**
	 * Sets the window's lower threshold of the detector.
	 *
	 * @param llim
	 * @throws DeviceException
	 */
	@Override
	public void setLowerThreshold(int llim) throws DeviceException {
		try {
			detector.setLowerLimit(llim);
		} catch (CAException | InterruptedException e) {
			throw new DeviceException("Excaption throw on setting lower threshold on detector " + detectorName, e);
		}
	}

	/**
	 * Gets window's lower threshold from the detector.
	 *
	 * @return window's lower threshold from the detector
	 * @throws DeviceException
	 */
	@Override
	public int getLowerThreshold() throws DeviceException {
		try {
			return detector.getLowerLimit();
		} catch (CAException | TimeoutException | InterruptedException e) {
			throw new DeviceException("Excaption throw on getting lower threshold on detector " + detectorName, e);
		}
	}

	@Override
	public String toString() {
		try {
			// get the current position as an array of doubles
			Object position = getPosition();
			// if position is null then simply return the name
			if (position == null) {
				return getName() + ": Not available.";
			}
			double[] positionAsArray = ScannableUtils.getCurrentPositionArray(this);

			// if cannot create array of doubles then use position's toString
			// method
			if (positionAsArray == null || positionAsArray.length == 1) {
				return getName() + " : " + position.toString();
			}

			// else build a string of formatted positions
			String output = getName() + " : ";
			int i = 0;
			for (; i < this.inputNames.length; i++) {
				output += this.inputNames[i] + ": " + String.format(getOutputFormat()[i], positionAsArray[i]) + " ";
			}

			for (int j = 0; j < this.extraNames.length; j++) {
				output += this.extraNames[j] + ": " + String.format(getOutputFormat()[i + j], positionAsArray[i + j])
						+ " ";
			}
			return output.trim();

		} catch (PyException e) {
			logger.error(getName() + ": jython exception while getting position. " + e.toString());
			return getName();
		} catch (Exception e) {
			logger.error(getName() + ": exception while getting position. " + e.getMessage() + "; " + e.getCause(), e);
			return getName();
		}
	}

	/**
	 * Does the same job as the other formatPosition method except rather than using a supplied format string, use the
	 * index of the array of formats this object holds. This is to be used when an object has multiple elements which
	 * descibe its position and those element require different formatting.
	 *
	 * @param format
	 *            the index in the array of formats to use
	 * @param number
	 *            the number to format
	 * @return a formatted string
	 */
	public String formatPosition(int format, double number) {
		if (format < outputFormat.length) {
			return String.format(outputFormat[format], number);
		}
		return String.format(outputFormat[0], number);
	}

	/**
	 * @param theObserved
	 * @param changeCode
	 */
	public void update(Object theObserved, Object changeCode) {
		notifyIObservers(theObserved, changeCode);
	}

	/**
	 * @param channel
	 * @param l
	 * @throws DeviceException
	 */
	public void addMonitor(int channel, MonitorListener l) throws DeviceException {
		scaler.addMonitor(channel, l);
	}

	/**
	 * @return scaler channel index
	 */
	public int getScalerChannelIndex() {
		return scalerChannelIndex;
	}

	/**
	 * @param scalerChannelIndex
	 */
	public void setScalerChannelIndex(int scalerChannelIndex) {
		this.scalerChannelIndex = scalerChannelIndex;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the memoryName.
	 */
	public String getScalerName() {
		return scalerName;
	}

	/**
	 * Sets the scaler name.
	 *
	 * @param scalerName
	 *            The scaler name
	 */
	public void setScalerName(String scalerName) {
		this.scalerName = scalerName;
	}

	/**
	 * Gets the detector name.
	 *
	 * @return The detector name
	 */
	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * Sets the detector name.
	 *
	 * @param detectorName
	 *            The detector name
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}
	/**
	 * gets the scaler object
	 * @return the scaler object
	 */
	public EpicsScaler getScaler() {
		return scaler;
	}
	/**
	 * sets the scaler object
	 * @param scaler
	 */
	public void setScaler(EpicsScaler scaler) {
		this.scaler = scaler;
	}
	/**
	 * gets the detector object
	 * @return the detector object
	 */
	public EpicsETLController getDetector() {
		return detector;
	}
	/**
	 * sets the detector object
	 * @param detector
	 */
	public void setDetector(EpicsETLController detector) {
		this.detector = detector;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "ETL Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "unknown";
	}

}
