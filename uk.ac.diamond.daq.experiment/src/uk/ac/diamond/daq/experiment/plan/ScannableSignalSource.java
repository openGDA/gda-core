/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FindableBase;

public class ScannableSignalSource extends FindableBase implements DoubleSupplier {

	private static final Logger logger = LoggerFactory.getLogger(ScannableSignalSource.class);
	private Scannable scannable;

	@Override
	public double getAsDouble() {
		try {
			return (double) scannable.getPosition();
		} catch (DeviceException e) {
			logger.error("Could not read position from scannable", e);
			return 0;
		}
	}

	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

}
