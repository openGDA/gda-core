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

package gda.device.impl;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import gda.device.TangoDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoDeviceImpl implements TangoDevice {
	private static final Logger logger = LoggerFactory.getLogger(TangoDeviceImpl.class);
	private DeviceProxy deviceProxy = null;

	public TangoDeviceImpl(String deviceName) {
		try {
			if (deviceName != null) {
				deviceProxy = new DeviceProxy(deviceName);
			}
		} catch (DevFailed e) {
			logger.warn("The tango server {} is not running: {}", deviceName, e.errors[0].desc);
		}
	}

	@Override
	public String get_name() {
		return deviceProxy.get_name();
	}

	@Override
	public DeviceData command_inout(String cmd) throws DevFailed {
		return deviceProxy.command_inout(cmd);
	}

	@Override
	public DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed {
		return deviceProxy.command_inout(cmd, argin);
	}

	@Override
	public DevState state() throws DevFailed {
		return deviceProxy.state();
	}

	@Override
	public String status() throws DevFailed {
		return deviceProxy.status();
	}

	@Override
	public void write_attribute(DeviceAttribute devattr) throws DevFailed {
		deviceProxy.write_attribute(devattr);
	}

	@Override
	public DeviceAttribute read_attribute(String attributeName) throws DevFailed {
		return deviceProxy.read_attribute(attributeName);
	}

	@Override
	public String[] get_attribute_list() throws DevFailed {
		return deviceProxy.get_attribute_list();
	}

	@Override
	public AttributeInfo get_attribute_info(String attributeName) throws DevFailed {
		return deviceProxy.get_attribute_info(attributeName);
	}

	@Override
	public boolean use_db() {
		return deviceProxy.use_db();
	}

	@Override
	public DbDatum get_property(String propertyName) throws DevFailed {
		return deviceProxy.get_property(propertyName);
	}

	@Override
	public void put_property(DbDatum property) throws DevFailed {
		deviceProxy.put_property(property);
	}

	@Override
	public void set_timeout_millis(int millis) throws DevFailed {
		deviceProxy.set_timeout_millis(millis);
	}
}
