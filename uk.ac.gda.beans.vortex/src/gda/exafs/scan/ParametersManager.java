/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.List;


// TODO This is a class named ParametersManager which moves a scannable. It doesn't encapsulate (make sense).
abstract class ParametersManager {

	protected final List<Scannable> scannables;

	protected ParametersManager() {
		this.scannables = new ArrayList<Scannable>(31);
	}

	/**
	 * Call to configure the beamline on the given parameters
	 * 
	 * @throws Exception
	 */
	public abstract void init() throws Exception;

	protected void setScannable(final String name, final Object value) throws DeviceException {
		final Scannable scannable = (Scannable) Finder.getInstance().find(name);
		scannable.asynchronousMoveTo(value);
		scannables.add(scannable);
	}

	public boolean isBusy() throws DeviceException {
		for (Scannable s : scannables)
			if (s.isBusy())
				return true;
		return false;
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (isBusy())
			Thread.sleep(250);
	}
}
