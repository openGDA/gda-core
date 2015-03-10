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
import gda.device.DummyDeviceAttribute;
import gda.device.TangoDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoDeviceLogger implements TangoDevice{
	private static final Logger logger = LoggerFactory.getLogger(TangoDeviceLogger.class);
	private TangoDevice device;

	TangoDeviceLogger(TangoDevice device){
		this.device = device;
	}

	public TangoDevice getDevice() {
		return device;
	}

	public void setDevice(TangoDevice device) {
		this.device = device;
	}

	@Override
	public DeviceData command_inout(String cmd) throws DevFailed {
		logger.info("command_inout: cmd:[" + cmd +"]");
		DeviceData res= device.command_inout(cmd);
		logger.info("command_inout: cmd:[" + cmd +"] res:[" + res +"]");
		return res;
	}

	@Override
	public DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed {
		logger.info("command_inout in: argin:["+argin +"]");
		DeviceData res= device.command_inout(cmd, argin);
		logger.info("command_inout: argin:["+argin +"] res:[" + res +"]");
		return res;
	}

	@Override
	public DevState state() throws DevFailed {
		logger.info("state in:");
		DevState res = device.state();
		logger.info("state: res:[" + res +"]");
		return res;
	}

	@Override
	public String status() throws DevFailed {
		logger.info("status in:");
		String res = device.status();
		logger.info("status: res:[" + res +"]");
		return res;
	}

	@Override
	public void write_attribute(DeviceAttribute devattr) throws DevFailed {
		logger.info("write_attribute: devattr:[" + new DummyDeviceAttribute( devattr) +"]");
		device.write_attribute(devattr);
	}

	@Override
	public DeviceAttribute read_attribute(String attributeName) throws DevFailed {
		logger.info("read_attribute in: attributeName:[" + attributeName +"]");
		DeviceAttribute res = device.read_attribute(attributeName);
		logger.info("read_attribute: attributeName:[" + attributeName +"] res:["+new DummyDeviceAttribute( res) +"]");
		return res;
	}

	@Override
	public String[] get_attribute_list() throws DevFailed {
		logger.info("get_attribute_list in: ");
		String[]  res = device.get_attribute_list();
		logger.info("get_attribute_list: res:["+res+"]");
		return res;
	}

	@Override
	public AttributeInfo get_attribute_info(String attributeName) throws DevFailed {
		logger.info("get_attribute_info in: attributeName:[" + attributeName +"]");
		AttributeInfo res = device.get_attribute_info(attributeName);
		logger.info("get_attribute_info: attributeName:[" + attributeName +"] res:["+res+"]");
		return res;

	
	}

	@Override
	public boolean use_db() {
		boolean res = device.use_db();
		logger.info("use_db:  res:["+res+"]");
		return res;
	}

	@Override
	public DbDatum get_property(String propertyName) throws DevFailed {
		logger.info("get_property in : propertyName:[" + propertyName +"]");
		DbDatum res = device.get_property(propertyName);
		logger.info("get_property: propertyName:[" + propertyName +"] res:["+res+"]");
		return res;
	}

	@Override
	public void put_property(DbDatum property) throws DevFailed {
		logger.info("put_property: property:[" + property +"]");
		device.put_property(property);
	}

	@Override
	public void set_timeout_millis(int millis) throws DevFailed {
		logger.info("set_timeout_millis: milliseconds:[" + millis +"]");
		device.set_timeout_millis(millis);
	}

	@Override
	public String get_name() {
		logger.info("get_name");
		String res = device.get_name();
		logger.info("get_name: res:["+res+"]");
		return res;
	}
	
	
}
