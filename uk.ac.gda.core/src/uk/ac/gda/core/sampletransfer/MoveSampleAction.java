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

public class MoveSampleAction extends AbstractStepAction {
	private static Logger logger = LoggerFactory.getLogger(MoveSampleAction.class);

	private Scannable scannable;
	private Map<SampleSelection, Double> scannableMap;

	public MoveSampleAction(String description, Scannable scannable, Map<SampleSelection, Double> scannableMap) {
		super(description);
		this.scannable = scannable;
		this.scannableMap = scannableMap;
	}

	@Override
	public void execute(StepProperties properties) throws DeviceException {
		var sample = properties.getSample();
		scannable.moveTo(scannableMap.get(sample));
	}

	@Override
	public void terminate() throws DeviceException {
        if (scannable != null && scannable.isBusy()) {
        	scannable.stop();
            logger.info("Scannable '{}' stopped successfully.", scannable.getName());
        }
	}
}
