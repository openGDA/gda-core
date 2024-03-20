/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.scan.IScanParticipant;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class that can be added as participant to ScanService. This has several annotated functions
 * that call empty 'scan hook' functions with empty default implementations.
 * These can be easily overridden in derived classes (e.g. in Jython).
 *
 */
public class ScanHookParticipant extends FindableBase implements IScanParticipant {

	private static final Logger logger = LoggerFactory.getLogger(ScanHookParticipant.class);

	public void addScanParticipant() {
		// First remove any matching ScanHookParticipants already present
		removeScanParticipant();

		getScanService().addScanParticipant(this);
	}

	public void removeScanParticipant() {
		getScanService().getScanParticipants().remove(this);
	}

	private IScanService getScanService() {
		return ServiceProvider.getService(IScanService.class);
	}

	/* Methods annotated to call placeholder 'scan hook' functions */

	@PrepareScan
	public void prepareScan(ScanModel scanModel) {
		logger.info("prepareScan called on {}", getName());
		atPrepareForScan(scanModel);
	}

	@FileDeclared
	public void fileDeclared(String filename) {
		logger.info("fileDeclared called on {}: {}", getName(), filename);
		atFileDeclared(filename);
	}

	@ScanStart
	public void scanStart() {
		logger.info("scanStart called on {}", getName());
		atScanStart();
	}

	@ScanEnd
	public void scanEnd() {
		logger.info("scanEnd called on {}", getName());
		atScanEnd();
	}

	@ScanFinally
	public void scanFinally() {
		logger.info("scanFinally called on {}", getName());
		atScanFinally();
	}

	/* 'Scan hook' functions called by each annotated method - can be overridden from Jython */

	public void atPrepareForScan(ScanModel scanModel) {}

	public void atFileDeclared(String fileName) {}

	public void atScanStart() {}

	public void atScanEnd() {}

	public void atScanFinally() {}

	@Override
	public String toString() {
		return "ScanHookParticipant : " + getName();
	}
}
