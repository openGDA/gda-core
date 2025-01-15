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

package uk.ac.gda.core.sampletransfer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * This class is used to record the current positions of a list of Scannable objects
 * before moving them. It extends the {@link MoveScannablesAction} class and adds
 * this extra recording functionality. The recorded positions will be saved in the
 * {@link StepProperties} object so the Scannables can move back to their original
 * positions later on.
 */
public class RecordPositionsAction extends MoveScannablesAction {
	private static Logger logger = LoggerFactory.getLogger(RecordPositionsAction.class);

	private List<Scannable> scannables;

	public RecordPositionsAction(String description, Map<Scannable, Double> scannableMap,
			List<Scannable> scannables) {
		super(description, scannableMap);
		this.scannables = scannables;
	}

	@Override
	public void execute(StepProperties properties) throws DeviceException {
		for (var scannable : scannables) {
			properties.getRecordedPositions().put(scannable, scannable.getPosition());
		}
		super.execute(properties);
	}

	@Override
	protected void moveScannable(Entry<Scannable, Double> scannableMap) throws DeviceException {
		scannable = scannableMap.getKey();
        Double relativePosition = scannableMap.getValue();
        var currentPosition = scannable.getPosition();
        var newPosition = (double) currentPosition + relativePosition;
        scannable.moveTo(newPosition);
        logger.info("Scannable '{}' moved to position {} successfully.", scannable.getName(), newPosition);
	}
}
