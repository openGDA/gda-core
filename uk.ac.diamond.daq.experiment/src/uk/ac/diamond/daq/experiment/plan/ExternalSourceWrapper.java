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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;

/**
 * {@link Scannable} wrapping a {@link DoubleSupplier}
 */
public class ExternalSourceWrapper extends ScannableBase {

	private DoubleSupplier source;

	public ExternalSourceWrapper(DoubleSupplier source) {
		this(source, source.toString());
	}

	public ExternalSourceWrapper(DoubleSupplier source, String name) {
		this.source = source;
		setName(name);
	}

	@Override
	public Object getPosition() throws DeviceException {
		return source.getAsDouble();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

}
