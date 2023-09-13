/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IDeviceWatchdogModel;
import org.eclipse.scanning.api.scan.IScanParticipant;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

/*
 * Records whether the defined watchdogs are enabled or not before a mapping scan,
 * then disables those watchdogs if one of the specified unpausable detectors is being used,
 * and finally sets the watchdogs back to their original settings after the scan is complete,
 * whether it was completed successfully or not.
 *
 */
public class UnpausableDetectorsWatchdogMonitor implements IScanParticipant {
	private List<String> unpausableDetectors;
	private List<Boolean> watchdogsEnabled;
	private List<String> watchdogsToDisable;
	private static final Logger logger = LoggerFactory.getLogger(UnpausableDetectorsWatchdogMonitor.class);
	private boolean watchdogsDisabled;

	public UnpausableDetectorsWatchdogMonitor() {
		watchdogsEnabled = new ArrayList<Boolean>();
		watchdogsDisabled = false;
	}

	public void addScanParticipant() {
		ServiceProvider.getService(IScanService.class).addScanParticipant(this);
	}

	public void setUnpausableDetectors(List<String> detectors) {
		this.unpausableDetectors = detectors;
	}

	public void setWatchdogsToDisable(List<String> watchdogs) {
		this.watchdogsToDisable = watchdogs;
	}

	@PrepareScan
	public void prepareWatchdogsForScan(ScanModel scanModel) {
		watchdogsDisabled=false;
		// Check whether the watchdogs are enabled before the scan starts
		for (String watchdogName : watchdogsToDisable) {
			watchdogsEnabled.add(isWatchdogEnabled(watchdogName));
		}
		List<IRunnableDevice<? extends IDetectorModel>> detectors = scanModel.getDetectors();
		for ( var detector : detectors ) {
			// Get the name of the detector being used
			String name = detector.getName();
			// If the detector is in the list of detectors then disable topup_watchdog
			if(unpausableDetectors.contains(name)) {
				logger.info("Disabling {} due to the selected detector being incompatable with pausing",watchdogsToDisable.toString());
				setWatchdog(false);
				watchdogsDisabled=true;
			}
		}
	}

	@ScanFinally
	public void resetWatchdogs(){
		if(watchdogsDisabled) {
			logger.info("Resetting watchdogs back to their pre-scan states.");
			for (boolean enabled : watchdogsEnabled) {
				setWatchdog(enabled);
			}
		}
	}

	/**
	 * Enable and disable topup_watchdog
	 **/
	private void setWatchdog(boolean status) {
		for (String watchdogName : watchdogsToDisable) {
			logger.info("Setting {} to {}.",watchdogName, status);
			setWatchdogEnabled(watchdogName, status);
		}
	}

	private IDeviceWatchdog<? extends IDeviceWatchdogModel> getWatchdog(String watchdogName){
		return ServiceProvider.getService(IDeviceWatchdogService.class).getWatchdog(watchdogName);
	}

	private boolean isWatchdogEnabled(String watchdogName) {
		return (getWatchdog(watchdogName).isEnabled());
	}

	private void setWatchdogEnabled(String watchdogName, boolean enabled) {
		getWatchdog(watchdogName).setEnabled(enabled);
	}

}
