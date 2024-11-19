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

import gda.device.DeviceException;
import gda.device.Scannable;

public class RecordPositionsAction extends AbstractStepAction {

	private List<Scannable> scannables;

	public RecordPositionsAction(String description, List<Scannable> scannables) {
		super(description);
		this.scannables = scannables;
	}

	@Override
	public void execute(StepProperties properties) throws DeviceException {
		for (var scannable : scannables) {
			properties.getRecordedPositions().put(scannable, scannable.getPosition());
		}
	}

	@Override
	public void terminate() throws DeviceException {
		// not required
	}

}
