/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosConfigurableScannableInfo;

public class SpecsPhoibosConfigurableScannable {

	private Scannable scannable;
	private String scannableDescription;


	public SpecsPhoibosConfigurableScannable(Scannable scannable, String scannableDescription) {
		this.scannable = scannable;
		this.scannableDescription = scannableDescription;
	}

	public String getScannableName() {
		return scannable.getName();
	}

	public String getScannableDescription() {
		return scannableDescription;
	}

	public SpecsPhoibosConfigurableScannableInfo getInfo() {
		return new SpecsPhoibosConfigurableScannableInfo(getScannableName(), getScannableDescription());
	}

	public boolean isCalled(String name) {
		return getScannableName().equals(name);
	}

	public void asynchronousMoveTo(double value) throws DeviceException {
		scannable.asynchronousMoveTo(value);
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		scannable.waitWhileBusy();
	}

	public Object getPosition() throws DeviceException {
		return scannable.getPosition();
	}

	public String checkPositionValid(Object position) throws DeviceException {
		return scannable.checkPositionValid(position);
	}
}