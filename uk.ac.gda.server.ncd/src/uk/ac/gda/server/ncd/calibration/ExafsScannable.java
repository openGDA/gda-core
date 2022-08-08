/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.calibration;

import static java.lang.Math.log;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;

public class ExafsScannable extends ScannableBase {
	private Scannable incident;
	private Scannable transmission;

	public ExafsScannable() {
		setInputNames(new String[0]);
		setExtraNames(new String[] {"exafs"}); // default - overridden with name
		setOutputFormat(new String[] {"%.4f"});
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		var itValue = ScannableUtils.objectToDouble(incident.getPosition());
		var i0Value = ScannableUtils.objectToDouble(transmission.getPosition());
		if (i0Value <= 0 || itValue < 0) {
			// if i0 is zero there is no beam
			return 0;
		}
		return -log(itValue/i0Value);
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// This scannable doesn't move
	}

	public Scannable getIncident() {
		return incident;
	}

	public void setIncident(Scannable incident) {
		this.incident = incident;
	}

	public Scannable getTransmission() {
		return transmission;
	}

	public void setTransmission(Scannable transmission) {
		this.transmission = transmission;
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		setExtraNames(new String[] {name});
	}
}
