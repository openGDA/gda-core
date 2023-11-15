/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;

public class DummyStringScannable extends ScannableBase {

	private String value;

	public DummyStringScannable(String name, String value) {
		super();
		setName(name);
		setInputNames(new String[] { name });
		setExtraNames(new String[0]);
		setOutputFormat(new String[] { "%s" });
		this.value = value;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		value = (String) position;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Object getPosition() {
		return value;
	}

}