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
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

public class MoveRelativeAction extends MoveScannablesAction {
	private static Logger logger = LoggerFactory.getLogger(MoveRelativeAction.class);

	public MoveRelativeAction(String description, Map<Scannable, Double> scannableMap) {
		super(description, scannableMap);
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
