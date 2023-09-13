/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.scan.IScanParticipant;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

/**
 * This class monitors for use of the Xspress3 detector,
 * If a mapping scan is run using Xspress3 detector the MCA is disabled at the start of the scan
 * and re-enabled at the end.
 * */
public class Xspress3MCAMappingMonitor implements IScanParticipant {

	private Xspress3DataOperations dataOperations;
	private static final Logger logger = LoggerFactory.getLogger(Xspress3MCAMappingMonitor.class);
	private boolean mcaDisabled;
	private ArrayList<String> detectorNames;

	public void addScanParticipant() {
		ServiceProvider.getService(IScanService.class).addScanParticipant(this);
	}

	public void setXspress3Controller(Xspress3Controller controller) {
		this.dataOperations = new Xspress3DataOperations(controller);
	}

	public void setDetectorNames(ArrayList<String> detectorNames) {
		this.detectorNames = detectorNames;
	}

	@PrepareScan
	public void prepareMCAForScan(ScanModel scanModel) {
		mcaDisabled = false;
		List<IRunnableDevice<? extends IDetectorModel>> detectors = scanModel.getDetectors();
		for ( var detector : detectors ) {
			// Get the name of the detector being used
			String name = detector.getName();
			// If the detector is in the list of detectors then disable MCA
			if(detectorNames.contains(name)) {
				try {
					dataOperations.disableEpicsMcaStorage();
					mcaDisabled=true;
				} catch (DeviceException e) {
					logger.error("Device Exception while disabling Epics MCA storage.", e);
				}
			}
		}
	}

	@ScanFinally
	public void resetMCAStorage() {
		if (mcaDisabled) {
			try {
				dataOperations.enableEpicsMcaStorage();
			} catch (DeviceException e) {
				logger.error("Device Exception while attempting to re-enable EPics MCA storage.", e);
			}
		}

	}
}
