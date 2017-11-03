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

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.IBeamMonitor;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.currentamplifier.EpicsCurrAmpSingle;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * A worker object operating at background to monitor the photon beam availability using an ion chamber. This class
 * monitors the specified ion chamber amplifier value and checks whether it has fallen below a specified threshold. If
 * so any scan is paused. On reading above the threshold, the paused scan is either resumed or restarted depending on a
 * java parameter {@code gda.device.monitor.resumeScan} setting. This monitor behaviour can be switched on and off as
 * required.
 */
public class IonChamberBeamMonitor extends MonitorBase implements Monitor, Scannable, Runnable, IObserver, IBeamMonitor {
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
	private volatile boolean monitorOn = false;

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

	private ObservableComponent observableComponent = new ObservableComponent();


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
			boolean aboveThreshold = aboveThreshold(getCurrentValue());
			if (!aboveThreshold) {
				if (!pausedByBeamMonitor && !InterfaceProvider.getJythonServerStatusProvider().getJythonServerStatus().areScriptAndScanIdle()) {
					// only pause if scan running and value fall below the threshold
					InterfaceProvider.getCurrentScanController().pauseCurrentScan();
					InterfaceProvider.getScriptController().pauseCurrentScript();
					pausedByBeamMonitor = true;
					JythonServerFacade.getInstance().print("Data collection PAUSED - NO BEAM ON SAMPLE. ");
					logger.warn("Data Collection PAUSED WAITING FOR BEAM ON SAMPLE.");
				}
			} else {
				if (pausedByBeamMonitor) {
					if (InterfaceProvider.getJythonServerStatusProvider().getJythonServerStatus().isScriptOrScanPaused()) {
						// only restart or resume scan if paused by this beam monitor, manual pause excluded
						if (LocalProperties.check("gda.device.monitor.resumeScan", true)) {
							InterfaceProvider.getCurrentScanController().resumeCurrentScan();
						} else {
							InterfaceProvider.getCurrentScanController().restartCurrentScan();
						}
						InterfaceProvider.getScriptController().resumeCurrentScript();
						JythonServerFacade.getInstance().print("Beam is back, resume or restart data collection.");
						logger.info("Beam is back, resume or restart scan.");
					}
				}
				pausedByBeamMonitor = false;
			}
			notifyIObservers(this, isBeamOn());
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
	@Override
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
	@Override
	public void on() {
		if (!monitorOn)
			setMonitorOn(true);
	}

	/**
	 * switch off beam monitoring
	 */
	@Override
	public void off() {
		if (monitorOn)
			setMonitorOn(false);
		if (pausedByBeamMonitor) {
			InterfaceProvider.getCurrentScanController().resumeCurrentScan();
			InterfaceProvider.getScriptController().resumeCurrentScript();
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
		} catch (Exception e) {
			logger.warn("Exception formatting {}", getName(), e);
		}

		final String result = myString.trim();
		return (result.isEmpty() ? valueUnavailableString() : result);
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
	@Override
	public Boolean isBeamOn() {
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
