/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.beamcondition;

import static gda.jython.JythonStatus.IDLE;
import static gda.jython.JythonStatus.PAUSED;
import static gda.jython.JythonStatus.RUNNING;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerStatus;
import gda.jython.JythonStatus;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Background beam monitor to pause scans when the beam condition is not acceptable.
 * <p>
 * Checks can be added and removed using the {@link #addCheck(BeamCondition)} and {@link #removeCheck(BeamCondition)}
 * methods.
 */
public class BeamMonitor extends BeamConditionBase {
	private static final Logger logger = LoggerFactory.getLogger(BeamMonitor.class);

	/** The collection of checks that all have to pass for the beam to be considered acceptable */
	private Collection<BeamCondition> checks = new LinkedHashSet<>();

	/** Whether this monitor is currently enabled */
	private boolean monitoring;

	/**
	 * Whether the scan has been paused by this monitor.
	 * <p>
	 * If a scan is paused by a user, it should not resume if the beam is dropped and returned.
	 */
	private boolean pausedByThisMonitor;

	/** Reference to the background monitoring process running in the common thread pool */
	private ScheduledFuture<?> monitoringProcess;

	private long delay;
	private TimeUnit unit;

	public BeamMonitor(long delay, TimeUnit unit) {
		this.delay = delay;
		this.unit = unit;
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}
	/**
	 * Create the beam monitor and start the background process monitoring the beam
	 */
	public BeamMonitor() {
		this(100, MILLISECONDS);
	}

	/**
	 * Check the condition of the beam and pause or resume scans if necessary.
	 * <p>
	 * This is called repeatedly from the common thread pool and shouldn't be called directly.
	 */
	private void run() {
		if (monitoring) {
			if (beamOn()) {
				if (pausedByThisMonitor) {
					if (scanIs(PAUSED)) {
						InterfaceProvider.getTerminalPrinter().print("Beam back - resuming");
						logger.info("Resuming scan");
						resume();
					} else {
						logger.warn("Scan was resumed while beam was off");
					}
					pausedByThisMonitor = false;
				}
			} else {
				if (!pausedByThisMonitor) {
					pauseRunningScans();
				} else if (scanIs(IDLE)) {
					pausedByThisMonitor = false;
				}
			}
		}
	}

	/** Pause any scans that are running */
	private void pauseRunningScans() {
		if (scanIs(RUNNING)) {
			InterfaceProvider.getTerminalPrinter().print("Beam lost - pausing scan");
			InterfaceProvider.getTerminalPrinter().print(detail());
			InterfaceProvider.getCurrentScanController().pauseCurrentScan();
			pausedByThisMonitor = true;
		}
	}

	private void resume() {
		InterfaceProvider.getCurrentScanController().resumeCurrentScan();
	}

	private boolean scanIs(JythonStatus status) {
		JythonServerStatus jss = InterfaceProvider.getJythonServerStatusProvider().getJythonServerStatus();
		return jss.scanStatus == status;
	}

	public void on() {
		logger.info("Enabling beam monitor");
		if (monitoringProcess == null || monitoringProcess.isDone()) {
			monitoringProcess = Async.scheduleWithFixedDelay(this::run, delay, delay, unit);
		}
		monitoring = true;
	}

	public void off() {
		logger.info("Disabling beam monitor");
		monitoring = false;
		if (pausedByThisMonitor && scanIs(PAUSED)) {
			logger.info("BeamMonitor switched off - resuming paused scan");
			InterfaceProvider.getTerminalPrinter().print("Beam monitor disabled - resuming scan");
			resume();
		}
		pausedByThisMonitor = false;
	}

	/** Add a condition to be checked when determining whether the beam is acceptable */
	public void addCheck(BeamCondition check) {
		checks.add(check);
	}

	/** Remove a condition and prevent it being checked to determine the beam condition */
	public void removeCheck(BeamCondition check) {
		checks.remove(check);
	}

	/** Remove all checks */
	public void clearChecks() {
		checks.clear();
	}

	/** Check if the beam currently passing all conditions */
	@Override
	public boolean beamOn() {
		return checks.stream().allMatch(BeamCondition::beamOn);
	}

	/** Shutdown this monitor and prevent it controlling scans */
	public void shutdown() {
		if (monitoringProcess != null) {
			monitoringProcess.cancel(true);
		}
	}

	@Override
	public String toString() {
		return String.format("BeamMonitor: %s (Beam %s)", monitoring ? "On" : "Off", beamOn() ? "On" : "Off");
	}

	/**
	 * Format this beam monitor to give a more detailed description of the state of the beam
	 */
	public String detail() {
		return getName() + "\n    " + checks.stream()
				.map(BeamCondition::toString)
				.collect(joining("\n    "));
	}
}
