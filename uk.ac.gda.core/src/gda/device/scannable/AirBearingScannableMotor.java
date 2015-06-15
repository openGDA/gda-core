/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.IAirBearingScannableMotor;
import gda.device.Scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * An extended {@link ScannableMotor} implementation that provides control to switch on/off air supply to
 * the motor's air bearing mechanism automatically when motion is requested.
 */
public class AirBearingScannableMotor extends ScannableMotor implements IAirBearingScannableMotor {
	private static final Logger logger=LoggerFactory.getLogger(AirBearingScannableMotor.class);
	private Scannable airBearingScannable;
	@Override
	public void on() throws DeviceException {
		if (airBearingScannable!=null) {
			airBearingScannable.moveTo("on");
		}
	}
	@Override
	public void off() throws DeviceException {
		if (airBearingScannable!=null) {
			airBearingScannable.moveTo("off");
		}
	}
	@Override
	public boolean isOn() throws DeviceException {
		if (airBearingScannable!=null) {
			return airBearingScannable.getPosition().toString().trim().equals("on");
		}
		return false;
	}
	@Override
	public void atScanStart() throws DeviceException {
		if (airBearingScannable!=null) {
			airBearingScannable.moveTo("on");
			logger.info("Turn air bearing on at scan start.");
		}
		super.atScanStart();
	}
	@Override
	public void atScanEnd() throws DeviceException {
		if (airBearingScannable!=null) {
			airBearingScannable.moveTo("off");
			logger.info("Turn air bearing off at scan end.");
		}
		super.atScanEnd();
	}
	@Override
	public void rawAsynchronousMoveTo(Object internalPosition) throws DeviceException {
		boolean airBearingControlHere=false;
		if (airBearingScannable!=null) {
			if (airBearingScannable.getPosition().toString().trim().equals("off")) {
				airBearingScannable.moveTo("on");
				airBearingControlHere=true;
				logger.info("Turn air bearing on in asynchronousMoveTo.");
			}
		}
		super.rawAsynchronousMoveTo(internalPosition);
		if (airBearingControlHere) {
			airBearingScannable.moveTo("off");
			logger.info("Turn air bearing off in asynchronousMoveTo.");
		}
	}
	public Scannable getAirBearingScannable() {
		return airBearingScannable;
	}
	public void setAirBearingScannable(Scannable airBearingScannable) {
		this.airBearingScannable = airBearingScannable;
	}

}
