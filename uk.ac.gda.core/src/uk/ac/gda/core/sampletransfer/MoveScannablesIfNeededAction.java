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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Action to move scannables only if they are not already in the expected position.
 *
 * This class extends {@link MoveScannablesAction} and provides an implementation that checks the current
 * position of each scannable in the {@link #scannableMap} against its expected position. If the difference
 * is less than the epsilon value, the scannable won't be moved.
 */
public class MoveScannablesIfNeededAction extends MoveScannablesAction {
	private static Logger logger = LoggerFactory.getLogger(MoveScannablesIfNeededAction.class);

	protected MoveScannablesIfNeededAction(String description, Map<Scannable, Double> scannableMap) {
		super(description, scannableMap);
	}

	@Override
	public void execute(StepProperties properties) throws DeviceException {
	    final double epsilon = 1e-5;

	    for (var entry : scannableMap.entrySet()) {
	        var position = ((Number) entry.getKey().getPosition()).doubleValue();
	        var expectedValue = entry.getValue().doubleValue();

	        if (Math.abs(position - expectedValue) < epsilon) {
	        	 logger.info("Scannable {} is in the expected position", entry.getKey().getName());
	        } else {
	            moveScannable(entry);
	        }
	    }
	}
}
