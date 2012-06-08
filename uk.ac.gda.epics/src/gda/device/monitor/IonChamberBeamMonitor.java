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

import gda.configuration.properties.LocalProperties;
import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.currentamplifier.EpicsCurrAmpSingle;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.ScriptBase;
import gda.observable.IObserver;
import gda.scan.ScanBase;

import java.lang.reflect.Array;

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A worker object operating at background to monitor the photon beam availability using an ion chamber. This class
 * monitors the specified ion chamber amplifier value and checks whether it has fallen below a specified threshold. If
 * so any scan is paused. On reading above the threshold, the paused scan is either resumed or restarted depending on a
 * java parameter {@code gda.device.monitor.resumeScan} setting. This monitor behaviour can be switched on and off as
 * required.
 */
public class IonChamberBeamMonitor extends MonitorBase implements Monitor, Scannable, Runnable, IObserver {
	/**
	 * the logger instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(IonChamberBeamMonitor.class);
	/**
	 * a configurable threshold
	 */
	private double threshold = 0.1;
	/**
	 * name of the monitor
	 */
	private String monitorName;
	/**
	 * current amplifier instance
	 */
	private CurrentAmplifier monitor;

	/**
	 * switch for monitor on/off bl#2342 bug
	 */
	private volatile boolean monitorOn = true;

	/**
	 * internal flag to identify scan paused by this object
	 */
	private volatile boolean pausedByBeamMonitor = false;
	/**
	 * photon beam on/off flag
	 */
	private volatile boolean beamOn = false;
	/**
	 * the current value of this monitor
	 */
	private volatile double currentValue;

	/**
	 * Empty constructor for Castor configuration.
	 */
	public IonChamberBeamMonitor() {

	}

	/**
	 * Constructor
	 * 
	 * @param detectorName
	 *            the name of the detector instance.
	 * @param threshold
	 *            the threshold when the scan is paused/resumed.
	 */
	public IonChamberBeamMonitor(String detectorName, double threshold) {
		this.monitorName = detectorName;
		this.threshold = threshold;

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
			if (monitor == null) {
				if (monitorName != null) {
					monitor = (CurrentAmplifier) Finder.getInstance().find(monitorName);
				}
			}
			if (monitor != null) {
				monitor.addIObserver(this);
			} else {
				throw new FactoryException("Can not find monitor " + monitorName);
			}
			configured = true;
		}
		startMonitoring();
	}

	/**
	 * 
	 */
	private void startMonitoring() {
		if (configured && monitorOn) {
			uk.ac.gda.util.ThreadManager.getThread(this).start();
		}
	}

	@Override
	public void atScanStart() {
		if (!monitorOn)
			setMonitorOn(true);
	}

	@Override
	public void atScanEnd() {
		if (monitorOn)
			setMonitorOn(false);
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

	@Override
	public Object getPosition() throws DeviceException {
		return monitor.getPosition();
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 1;
	}

	@Override
	public String getUnit() throws DeviceException {
		// just a number
		return monitor.getGain();
	}

	private boolean aboveThreshold(double data) {
		boolean thresholdReached = false;
		if (data <= threshold) {
			thresholdReached = false;
			setBeamOn(false);
		} else {
			thresholdReached = true;
			setBeamOn(true);
		}

		return thresholdReached;
	}

	/**
	 * defines actions of the beam monitor thread. On beam down, all scans in jython pause; on beam back, the scan
	 * resumes or restart; on interruption, abort any scan. {@inheritDoc}
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (monitorOn) {
			if (!(aboveThreshold(getCurrentValue()))) {
				if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
					// only pause if scan running and value fall below the threshold
					JythonServerFacade.getInstance().pauseCurrentScan();
					JythonServerFacade.getInstance().setScanStatus(Jython.PAUSED);
					ScanBase.paused = true;
					pausedByBeamMonitor = true;
					JythonServerFacade.getInstance().print("SCAN PAUSED - NO BEAM ON SAMPLE. ");
					logger.warn("SCAN PAUSED WAITING FOR BEAM ON SAMPLE.");
				}
				if (JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING) {
					// only pause if script running and value fall below the threshold
					JythonServerFacade.getInstance().pauseCurrentScript();
					JythonServerFacade.getInstance().setScriptStatus(Jython.PAUSED);
					ScriptBase.paused = true;
					pausedByBeamMonitor = true;
					JythonServerFacade.getInstance().print("SCRIPT PAUSED - NO BEAM ON SAMPLE. ");
					logger.warn("SCRIPT PAUSED WAITING FOR BEAM ON SAMPLE.");

				}
			} else {
				if (pausedByBeamMonitor) {
					if (JythonServerFacade.getInstance().getScanStatus() == Jython.PAUSED) {
						// only restart or resume scan if paused by this beam monitor, manual pause excluded
						if (LocalProperties.check("gda.device.monitor.resumeScan", true)) {
							JythonServerFacade.getInstance().resumeCurrentScan();
						} else {
							JythonServerFacade.getInstance().restartCurrentScan();
						}
						JythonServerFacade.getInstance().setScanStatus(Jython.RUNNING);
						ScanBase.paused = false;
						JythonServerFacade.getInstance().print("Beam is back, resume or restart scan.");
						logger.info("Beam is back, resume or restart scan.");
						pausedByBeamMonitor = false;
					}
					if (JythonServerFacade.getInstance().getScriptStatus() == Jython.PAUSED) {
						// only resume if paused by this beam monitor, manual pause excluded
						JythonServerFacade.getInstance().resumeCurrentScript();
						JythonServerFacade.getInstance().setScriptStatus(Jython.RUNNING);
						ScriptBase.paused = false;
						JythonServerFacade.getInstance().print("Beam is back, resume script running.");
						logger.info("Beam is back, resume script running.");
						pausedByBeamMonitor = false;
					}
				}
			}
			delay(100);
		}
	}

	private void delay(long timeInMilliSeconds) {
		try {
			Thread.sleep(timeInMilliSeconds);
		} catch (InterruptedException e) {
			// no op
		}
	}

	/**
	 * {@inheritDoc}a given number (the default is 5) consecutive
	 * 
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof EpicsCurrAmpSingle.CurrentMonitorListener) {
			currentValue = ((Double) changeCode).doubleValue();
		} else if (theObserved instanceof EpicsCurrAmpSingle.OverloadMonitorListener) {
			CurrentAmplifier.Status status = (CurrentAmplifier.Status) changeCode;
			if (status == CurrentAmplifier.Status.OVERLOAD) {
				logger.warn("Amplifier {} status becomes {}", getName(), status);
			} else {
				logger.info("Amplifier {} status is back to {}", getName(), status);
			}
		} else if (theObserved instanceof EpicsCurrAmpSingle.ModeMonitorListener) {
			logger.info("Amplifier {} Mode is changed to {}", getName(), changeCode);
		} else if (theObserved instanceof EpicsCurrAmpSingle.GainMonitorListener) {
			logger.info("Amplifier {} Gain is changed to {}", getName(), changeCode);
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
		startMonitoring();
	}

	/**
	 * switch on beam monitoring
	 */
	public void on() {
		if (!monitorOn)
			setMonitorOn(true);
	}

	/**
	 * switch off beam monitoring
	 */
	public void off() {
		if (monitorOn)
			setMonitorOn(false);
		if (pausedByBeamMonitor) {
			if (ScanBase.paused)
				ScanBase.paused = false;
			if (ScriptBase.isPaused())
				ScriptBase.setPaused(false);
		}
	}

	@Override
	public String toFormattedString() {
		String myString = "";
		try {
			Object position = this.getPosition();

			if (position == null) {
				logger.warn("getPosition() from " + this.getName() + " returns NULL.");
				return this.getName() + " : NOT AVAILABLE";
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
		} catch (NumberFormatException e) {
			logger.error("Number Format Exception ", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("Array Index out of bounds ", e);
		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument ", e);
		} catch (DeviceException e) {
			logger.error("Device Exception ", e);
		}
		return myString.trim();
	}

	/**
	 * gets monitor name
	 * 
	 * @return monitor name
	 */
	public String getMonitorName() {
		return monitorName;
	}

	/**
	 * sets monitor name
	 * 
	 * @param monitorName
	 */
	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

	/**
	 * gets the current amplifier object
	 * 
	 * @return the current amplifier object
	 */
	public CurrentAmplifier getMonitor() {
		return monitor;
	}

	/**
	 * sets the current amplifier object
	 * 
	 * @param monitor
	 */
	public void setMonitor(CurrentAmplifier monitor) {
		this.monitor = monitor;
	}

	/**
	 * @return boolean
	 */
	public boolean isBeamOn() {
		return beamOn;
	}

	/**
	 * @param beamOn
	 */
	public void setBeamOn(boolean beamOn) {
		this.beamOn = beamOn;
	}

	/**
	 * Returns the current value of this monitor.
	 * 
	 * @return the current value
	 */
	public double getCurrentValue() {
		return currentValue;
	}

}
