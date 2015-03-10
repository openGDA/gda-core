/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

public class TangoShutter extends ValveBase implements EnumPositioner {

	private static final Logger logger = LoggerFactory.getLogger(TangoShutter.class);
	private TangoDeviceProxy dev;

	@Override
	public void configure() throws FactoryException{
		if(!configured){
			if(positions == null || positions.size() == 0 ){
				this.positions = new Vector<String>();
				this.positions.add(OPEN);
				this.positions.add(CLOSE);
			}
			configured = true;
		}
	}
	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return dev;
	}

	/**
	 * @param dev The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy dev) {
		this.dev = dev;
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			switch (dev.state().value()) {
			case DevState._OPEN:
				return positions.get(0);
			case DevState._CLOSE:
				return positions.get(1);
			default:
				return "UNKNOWN";
			}
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			return "FAULT";
		}
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		try {
			// get the status
			switch (dev.state().value()) {
			case DevState._OPEN:
			case DevState._CLOSE:
				return EnumPositionerStatus.IDLE;
			case DevState._MOVING:
				return EnumPositionerStatus.MOVING;
			default:
				return EnumPositionerStatus.ERROR;
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(e.errors[0].desc);
			throw ex;
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		try {
			// check top ensure a correct string has been supplied
			String cmd = position.toString();
			if (positions.contains(cmd)) {
				dev.command_inout(cmd);
			} else {
				// if get here then wrong position name supplied
				throw new DeviceException(getName() + ": demand position " + position.toString()
					+ " is not acceptable");
			}
		} catch (Throwable th) {
			throw new DeviceException("failed to move to" + position.toString(), th);
		}
		notifyIObservers(this, getPosition());
	}

	@Override
	public String toFormattedString() {
		String result = "";
		try {
			result =  dev.status();
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
		}
		return result;
	}
}
