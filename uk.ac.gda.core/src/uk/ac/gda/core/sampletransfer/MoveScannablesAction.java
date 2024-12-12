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

public class MoveScannablesAction extends AbstractStepAction {
	private static Logger logger = LoggerFactory.getLogger(MoveScannablesAction.class);

	protected Scannable scannable;
	protected Map<Scannable, Double> scannableMap;

	public MoveScannablesAction(String description, Map<Scannable, Double> scannableMap) {
		super(description);
		this.scannableMap = scannableMap;
	}

	@Override
	public void execute(StepContext context) throws DeviceException {
		logger.info("Executing MoveScannablesAction.");
		for (var entry : scannableMap.entrySet()) {
			moveScannable(entry);
		}
		logger.info("MoveScannablesAction execution completed.");
	}

	protected void moveScannable(Entry<Scannable, Double> scannableMap) throws DeviceException {
		scannable = scannableMap.getKey();
        Double position = scannableMap.getValue();
        scannable.moveTo(position);
        logger.info("Scannable '{}' moved to position {} successfully.", scannable.getName(), position);
	}

	// FIXME incomplete implementation and unit test required
	@Override
    public void terminate() throws DeviceException {
        if (scannable != null && scannable.isBusy()) {
        	scannable.stop();
            logger.info("Scannable '{}' stopped successfully.", scannable.getName());
        }
	}
}
