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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

public class MoveRecordedPositionsAction extends AbstractStepAction {
	private static Logger logger = LoggerFactory.getLogger(MoveRecordedPositionsAction.class);

	private Scannable scannable;
	private List<Scannable> scannables;

	public MoveRecordedPositionsAction(String description, List<Scannable> scannables) {
		super(description);
		this.scannables = scannables;
	}

	@Override
	public void execute(StepProperties properties) throws DeviceException {
		for (var item : scannables) {
			scannable = item;
			scannable.moveTo(properties.getRecordedPositions().get(scannable));
		}
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
