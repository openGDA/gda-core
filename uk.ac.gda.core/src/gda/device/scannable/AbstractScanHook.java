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

package gda.device.scannable;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * A base class for creating scan hooks. Allows subclasses to be included in scans without dealing
 * with any input/output.
 *
 * Provides a simple way to add functionality to certain points in a scan.
 */
public abstract class AbstractScanHook extends DeviceBase implements Scannable {
	private static final String[] EMPTY = new String[0];

	private int level;

	public AbstractScanHook(String name) {
		setName(name);
	}

	@Override
	public final void setName(String name) { // final to prevent name ever being null
		if (name == null || name.isEmpty()) throw new IllegalArgumentException("Name is required");
		super.setName(name);
	}

	@Override
	public final Object getPosition() {
		return null;
	}

	@Override
	public final void moveTo(Object position) {
		throw new UnsupportedOperationException("Scan hook types cannot be moved");
	}

	@Override
	public final void asynchronousMoveTo(Object position) {
		throw new UnsupportedOperationException("Scan hook types cannot be moved");
	}

	@Override
	public final String checkPositionValid(Object position) {
		return "Scan hooks cannot be moved";
	}

	@Override
	public void stop() throws DeviceException {}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException {
		while (isBusy()) {
			Thread.sleep(100);
		}
	}

	@Override
	public final boolean isAt(Object positionToTest) {
		return false;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public final String[] getInputNames() {
		return EMPTY;
	}

	@Override
	public final void setInputNames(String[] names) {
		throw new UnsupportedOperationException("Scan hooks have no inputs");
	}

	@Override
	public final String[] getExtraNames() {
		return EMPTY;
	}

	@Override
	public final void setExtraNames(String[] names) {
		throw new UnsupportedOperationException("Scan hooks have no output");
	}

	@Override
	public final void setOutputFormat(String[] names) {
		throw new UnsupportedOperationException("Scan hooks have no output");
	}

	@Override
	public final String[] getOutputFormat() {
		return EMPTY;
	}

	@Override
	public void atScanStart() throws DeviceException {}

	@Override
	public void atScanEnd() throws DeviceException {}

	@Override
	public void atScanLineStart() throws DeviceException {}

	@Override
	public void atScanLineEnd() throws DeviceException {}

	@Override
	public void atPointStart() throws DeviceException {}

	@Override
	public void atPointEnd() throws DeviceException {}

	@Override
	public void atLevelMoveStart() throws DeviceException {}

	@Override
	public void atLevelStart() throws DeviceException {}

	@Override
	public void atLevelEnd() throws DeviceException {}

	@Override
	public void atCommandFailure() throws DeviceException {}

	@Override
	public String toString() {
		return getName() + "<" + getClass().getCanonicalName() + ">";
	}

	@Override
	public String toFormattedString() {
		return toString();
	}
}
