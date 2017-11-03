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

package gda.device.monitor;

import java.lang.reflect.Array;

import org.jscience.physics.units.Unit;
import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.detector.EpicsScaler;
import gda.device.detector.etldetector.ETLDetector;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;


/**
 * A class to monitor the beam using an EPICS-based Detector, such as a multi-channel EpicsScaler
 * {@link gda.device.detector.EpicsScaler}, or an ETLDetecor
 * {@link gda.device.detector.etldetector.ETLDetector}. This class monitors the specified detector count value and
 * checks whether the data count rate has fallen below a specified threshold. If so the scan is paused and a new thread
 * is spawned to monitor the detector until the data is above the threshold value for a given number of consecutive
 * reads before the scan is resumed.
 */
public class EpicsBeamMonitor extends MonitorBase implements Runnable, Monitor, MonitorListener, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsBeamMonitor.class);

	private double threshold = 1.0;

	private int waitTime = 1;

	private int consecutiveCountsAboveThreshold = 6;

	private double countTime = 1;

	private String detectorName;

	private Detector detector;

	private int channel = 0;

	private int nOkReads = 0;

	private boolean monitorOn = false;

	private boolean pausedByBeamMonitor = false;

	/**
	 * Empty constructor for Castor configuration.
	 */
	public EpicsBeamMonitor() {

	}

	/**
	 * Constructor
	 *
	 * @param detectorName
	 *            the name of the detector instance.
	 * @param channel
	 *            the detector channel to use for beam monitoring.
	 * @param threshold
	 *            the threshold when the scan is paused/resumed.
	 * @param waitTime
	 *            the time (in secs) between successive monitors whilst the scan is paused.
	 * @param consecutiveCountsAboveThreshold
	 *            the number of successful readings above the threshold.
	 * @param countTime
	 *            the counting time.
	 */
	public EpicsBeamMonitor(String detectorName, int channel, double threshold, int waitTime,
			int consecutiveCountsAboveThreshold, double countTime) {
		this.detectorName = detectorName;
		this.channel = channel;
		this.threshold = threshold;
		this.waitTime = waitTime;
		this.consecutiveCountsAboveThreshold = consecutiveCountsAboveThreshold;
		this.countTime = countTime;

		try {
			configure();
		} catch (FactoryException e) {
			logger.error("Configure() failed", e);
			e.printStackTrace();
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (detectorName != null) {
				detector = (Detector) Finder.getInstance().find(detectorName);
				if (detector != null) {
					if (detector instanceof ETLDetector) {
						ETLDetector d = (ETLDetector) detector;
						try {
							d.addMonitor(d.getScalerChannelIndex(), this);
						} catch (DeviceException e) {
							throw new FactoryException("Can not add monitor to " + d.getName(), e);
						}
					} else if (detector instanceof EpicsScaler) {
						EpicsScaler d = (EpicsScaler) detector;
						try {
							d.addMonitor(channel, this);
						} catch (DeviceException e) {
							throw new FactoryException("Can not add monitor to " + d.getName(), e);
						}
					} else {
						throw new FactoryException("Detector: " + detector.getName()
								+ " as BeamMonitor is not supported");
					}
				} else {
					throw new FactoryException("Can not find detector " + detectorName);
				}
			}
			configured = true;
		}
	}

	/**
	 * @return Returns the threshold.
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            The threshold to set.
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return Returns the waitTime.
	 */
	public int getWaitTime() {
		return waitTime;
	}

	/**
	 * @param waitTime
	 *            The waitTime to set.
	 */
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	/**
	 * @return Returns the consecutiveCountsAboveThreshold.
	 */
	public int getConsecutiveCountsAboveThreshold() {
		return consecutiveCountsAboveThreshold;
	}

	/**
	 * @param consecutiveCountsAboveThreshold
	 *            The consecutiveCountsAboveThreshold to set.
	 */
	public void setConsecutiveCountsAboveThreshold(int consecutiveCountsAboveThreshold) {
		this.consecutiveCountsAboveThreshold = consecutiveCountsAboveThreshold;
	}

	/**
	 * @return Returns the countTime.
	 */
	public double getCountTime() {
		return countTime;
	}

	/**
	 * @param countTime
	 *            The countTime to set.
	 */
	public void setCountTime(double countTime) {
		this.countTime = countTime;
	}

	/**
	 * @return Returns the detectorName.
	 */
	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * @param detectorName
	 *            The detectorName to set.
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	/**
	 * @return Returns the channel.
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            The channel to set.
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return nOkReads;
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 1;
	}

	@Override
	public String getUnit() throws DeviceException {
		// just a number
		return Unit.ONE.toString();
	}

	@Override
	public synchronized void run() {
		logger.warn("scan stopped waiting for beam on ");
		while (monitorOn && nOkReads < consecutiveCountsAboveThreshold) {
			logger.info("waiting between samples for " + waitTime + " secs");
			try {
				wait(waitTime * 1000);
				if (monitorCountsReached())
					nOkReads++;
				else
					nOkReads = 0;
				logger.info("no of reads above threshold now " + nOkReads + " (requires "
						+ consecutiveCountsAboveThreshold + ")");
			} catch (InterruptedException e) {
				// Deliberately do nothing
			}
			// if you abort or halt or stop scan during no beam, this thread should stop
			if (JythonServerFacade.getInstance().getScanStatus() == Jython.IDLE) {
				break;
			}
		}
		if (monitorOn && JythonServerFacade.getInstance().getScanStatus() == Jython.PAUSED && pausedByBeamMonitor) {
			// only restart or resume if paused by this beam monitor, manual pause excluded
			if (LocalProperties.check("gda.device.monitor.resumeScan", true)) {
				JythonServerFacade.getInstance().resumeCurrentScan();
			} else {
				JythonServerFacade.getInstance().restartCurrentScan();
			}
			pausedByBeamMonitor = false;
		}
		// reset paused flag
		if (JythonServerFacade.getInstance().getScanStatus() == Jython.IDLE) {
			pausedByBeamMonitor = false;
		}
	}

	private boolean monitorCountsReached() {
		boolean countsReached = false;
		if (countTime > 0) {
			try {
				detector.setCollectionTime(countTime);
				detector.collectData();
				while (detector.getStatus() == Detector.BUSY) {
					wait(100);
				}
				if (detector instanceof EpicsScaler) {
					// fill an array with the new data
					Double[] data = ScannableUtils.objectToArray(detector.readout());
					logger.debug("read data was " + data[channel]);
					if (data[channel] > threshold) {
						countsReached = true;
					}
				} else if (detector instanceof ETLDetector) {
					double data = Double.parseDouble(((ETLDetector) detector).readout().toString());
					logger.debug("read data was " + data);
					if (data > threshold) {
						countsReached = true;
					}
				}
			} catch (DeviceException ex) {
			} catch (InterruptedException ex) {
			}
		}
		return countsReached;
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		DBR dbr = arg0.getDBR();
		if (detector instanceof ETLDetector) {
			double currentData = 0;
			if (dbr.isDOUBLE()) {
				currentData = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr.isFLOAT() || dbr.isINT() || dbr.isSHORT()) {
				try {
					currentData = ((DBR_Double) dbr.convert(DBRType.DOUBLE)).getDoubleValue()[0];
				} catch (CAStatusException e) {
					logger.error("DBR data convert failed", e);
				}
			} else {
				logger.warn("Monitor must return numeric data type. Type returned is {}", dbr.getType());
			}
			if (monitorOn && JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
				// do not pause if already paused or IDLE
				// then check the level against the threshold
				if (currentData <= threshold) {
					JythonServerFacade.getInstance().pauseCurrentScan();
					pausedByBeamMonitor = true;
					Thread thread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
					thread.start();
				}
			}
		} else if (detector instanceof EpicsScaler) {
			double[] currentData = new double[dbr.getCount()];
			if (dbr.isDOUBLE()) {
				currentData = ((DBR_Double) dbr).getDoubleValue();
			} else if (dbr.isFLOAT() || dbr.isINT() || dbr.isSHORT()) {
				try {
					currentData = ((DBR_Double) dbr.convert(DBRType.DOUBLE)).getDoubleValue();
				} catch (CAStatusException e) {
					logger.error("DBR data convert failed", e);
					e.printStackTrace();
				}
			} else {
				logger.warn("Monitor must return numeric data type. Type returned is {}", dbr.getType());
			}
			if (monitorOn && JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
				// then check the level against the threshold
				if (currentData != null && currentData.length > channel && currentData[channel] <= threshold) {
					JythonServerFacade.getInstance().pauseCurrentScan();
					pausedByBeamMonitor = true;
					Thread thread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
					thread.start();
				}
			}
		}
	}

	/**
	 * checks beam monitor flag
	 *
	 * @return boolean
	 */
	public boolean isMonitorOn() {
		return monitorOn;
	}

	/**
	 * set beam monitor flag
	 *
	 * @param monitorOn
	 */
	public void setMonitorOn(boolean monitorOn) {
		this.monitorOn = monitorOn;
	}

	/**
	 * switch on beam monitoring
	 */
	public void on() {
		setMonitorOn(true);
	}

	/**
	 * switch off beam monitoring
	 */
	public void off() {
		setMonitorOn(false);
	}

	@Override
	public String toFormattedString() {
		String myString = "";
		try {
			Object position = this.getPosition();

			if (position == null) {
				logger.warn("getPosition() from " + this.getName() + " returns NULL.");
				return valueUnavailableString();
			}
			// print out simple version if only one inputName and
			// getPosition and getReportingUnits do not return arrays.
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				myString += this.getName() + " : ";
				if (position instanceof String) {
					myString += position.toString();
				} else {
					myString += String.format(getOutputFormat()[0], Double.parseDouble(position.toString()));
				}
			} else {
				myString += this.getName() + " : ";
				if (position instanceof PySequence) {
					for (int i = 0; i < ((PySequence) position).__len__(); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += String.format(getOutputFormat()[i], Double.parseDouble(((PySequence) position)
								.__finditem__(i).toString()));
					}
				} else {
					for (int i = 0; i < Array.getLength(position); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += String.format(getOutputFormat()[i], Double.parseDouble(Array.get(position, i)
								.toString()));
					}
				}

			}
			myString = myString + " " + getUnit();
		} catch (Exception e) {
			logger.warn("Exception formatting {}", getName(), e);
		}

		final String result = myString.trim();
		return result.isEmpty() ? valueUnavailableString() : result;
	}

}
