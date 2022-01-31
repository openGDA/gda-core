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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.scan.IScanParticipant;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.server.servlet.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Records the initial position of scannables before a mapping scan,
 * and moves them back to their initial position after a scan is complete,
 * whether it is completed successfully or not.
 *
 */
public class ScannablePositionRestorer implements IScanParticipant {

	private Map<String, Object> scannablesOriginalPositions;

	private static final Logger logger = LoggerFactory.getLogger(ScannablePositionRestorer.class);

	public ScannablePositionRestorer() {
		scannablesOriginalPositions = new HashMap<>();
	}

	public void addScanParticipant() {
		Services.getScanService().addScanParticipant(this);
	}

	@PrepareScan
	public void getStartScanPositions(ScanModel scanModel) throws ScanningException {
		List<String> scannablesNames = scanModel.getPointGenerator().getNames();
		var scannables = ScannableDeviceConnectorService.getInstance().getScannables(scannablesNames);
		scannablesOriginalPositions.clear();
		for (var scannable : scannables) {
			scannablesOriginalPositions.put(scannable.getName(), scannable.getPosition());
		}
	}

	@ScanFinally
	public void movePositions() throws ScanningException {
		for (var startPosition : scannablesOriginalPositions.entrySet()) {
			var currentPosition = getCurrentPosition(startPosition.getKey());
			logger.debug("Moving scannable {} from {} to {}", startPosition.getKey(), currentPosition, startPosition.getValue());
			moveScannablePosition(startPosition.getKey(), startPosition.getValue());
		}
	}

	public Object getCurrentPosition(String scannableName) throws ScanningException {
		return ScannableDeviceConnectorService.getInstance()
				.getScannable(scannableName)
				.getPosition();
	}

	public void moveScannablePosition(String scannableName, Object targetPosition) throws ScanningException {
		ScannableDeviceConnectorService.getInstance()
		.getScannable(scannableName)
		.setPosition(targetPosition);
	}

}
