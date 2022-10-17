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

package gda.device;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.impl.TangoDeviceImpl;

public class TangoDeviceProxy {
	private TangoDevice tangoDevice = null;

	public TangoDeviceProxy() {
	}

	/**
	 * Useful for passing in a dummy Tango connection
	 * @param deviceProxy
	 */
	public TangoDeviceProxy(TangoDevice deviceProxy) {
		this.tangoDevice = deviceProxy;
	}
	
	/**
	 * Creates a real Tango connection. 
	 * @param deviceName
	 */
	public TangoDeviceProxy(String deviceName) {
		tangoDevice = new TangoDeviceImpl(deviceName);
	}

	public TangoDevice getTangoDevice() {
		return tangoDevice;
	}

	public String get_name() {
		return tangoDevice.get_name();
	}

	public DeviceData command_inout(String cmd) throws DevFailed {
		return tangoDevice.command_inout(cmd);
	}

	public DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed {
		return tangoDevice.command_inout(cmd, argin);
	}

	public DevState state() throws DevFailed {
		return tangoDevice.state();
	}

	public String status() throws DevFailed {
		return tangoDevice.status();
	}

	public void write_attribute(DeviceAttribute devattr) throws DevFailed {
		tangoDevice.write_attribute(devattr);
	}

	public DeviceAttribute read_attribute(String attributeName) throws DevFailed {
		return tangoDevice.read_attribute(attributeName);
	}

	public String[] get_attribute_list() throws DevFailed {
		return tangoDevice.get_attribute_list();
	}

	public AttributeInfo get_attribute_info(String attributeName) throws DevFailed {
		return tangoDevice.get_attribute_info(attributeName);
	}

	public boolean use_db() {
		return tangoDevice.use_db();
	}

	public DbDatum get_property(String propertyName) throws DevFailed {
		return tangoDevice.get_property(propertyName);
	}

	public void put_property(DbDatum property) throws DevFailed {
		tangoDevice.put_property(property);
	}

	public void set_timeout_millis(int millis) throws DevFailed {
		tangoDevice.set_timeout_millis(millis);
	}

	public void isAvailable() throws DeviceException {
		try {
			// Is the device still connected or just started
			// if (deviceProxy.isAlready_connected()) {
			DevState state = tangoDevice.state();
			if (state == DevState.FAULT)
				throw new DeviceException("Tango device server " + tangoDevice.get_name() + " shows fault");
			// } else {
			// deviceProxy.build_connection();
			// }
		} catch (DevFailed e) {
			// device has lost connection
			throw new DeviceException("Tango device server " + tangoDevice.get_name() + " failed");
		} catch (Exception e) {
			throw new DeviceException("Tango device server stuffed");
		}
	}

	public void sendSimpleCommand(String command) throws DevFailed {
		tangoDevice.command_inout(command);
	}

	public long getLongFromCommand(String command) throws DevFailed {
		DeviceData command_inout = command_inout(command);
		return command_inout.extractLong();
	}

	public long getLongFromCommand(String command, DeviceData argin) throws DevFailed {
		return command_inout(command, argin).extractLong();
	}
	public String getStringFromCommand(String command) throws DevFailed {
		DeviceData command_inout = command_inout(command);
		return command_inout.extractString();
	}

	public String getStringFromCommand(String command, DeviceData argin) throws DevFailed {
		return command_inout(command, argin).extractString();
	}
	
	public String getAttributeAsString(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractString();
	}

	public boolean getAttributeAsBoolean(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractBoolean();
	}

	public String[] getAttributeAsStringArray(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractStringArray();
	}

	protected void checkAttributeRead(String attributeName, DeviceAttribute read_attribute) throws DevFailed {
		if (read_attribute.hasFailed()) {
			throw new DevFailed(new DevError[] { new DevError(tangoDevice.toString() + " : error reading attribute - "
					+ attributeName, ErrSeverity.ERR, "", "") });
		}
	}

	/**
	 * Use this method to extract a shortScalar
	 */
	public short getAttributeAsUChar(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractUChar();
	}

	/**
	 * Use this method to extract a shortScalar
	 */
	public short getAttributeAsShort(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractShort();
	}

	/**
	 * Use this method to extract a longScalar
	 */
	public int getAttributeAsInt(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractLong();
	}

	public long getAttributeAsLong(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractULong();
	}

	public double getAttributeAsDouble(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractDouble();
	}

	public double getAttributeAsFloat(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractFloat();
	}
	/**
	 * The conversion from C to Java by Tango developers seems to have misunderstood the long data types of Java. The
	 * method is extractULongArray
	 */
	public long[] getAttributeAsLongArray(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractULongArray();
	}

	/**
	 * The conversion from C to Java by Tango developers seems to have misunderstood the long data types of Java. The
	 * method is extractULongArray
	 */
	public long[] getAttributeAsULongArray(String attributeName) throws DevFailed {
		return getAttributeAsLongArray(attributeName);
	}

	public int[] getAttributeAsIntArray(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractLongArray();
	}

	public double[] getAttributeAsDoubleArray(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractDoubleArray();
	}

	public boolean[] getAttributeAsBooleanArray(String attributeName) throws DevFailed {
		DeviceAttribute read_attribute = read_attribute(attributeName);
		checkAttributeRead(attributeName, read_attribute);
		return read_attribute.extractBooleanArray();
	}

	public void setAttribute(String attributeName, String value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, String[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

	public void setAttribute(String attributeName, boolean value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, boolean[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

	public void setAttribute(String attributeName, byte value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, byte[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

	public void setAttribute(String attributeName, short value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, short[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

	public void setAttribute(String attributeName, int value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, int[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

	public void setAttribute(String attributeName, long value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, long[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

	public void setAttribute(String attributeName, double value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}

	public void setAttribute(String attributeName, float value) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value));
	}
	
	public void setAttribute(String attributeName, double[] value, int dim_x, int dim_y) throws DevFailed {
		write_attribute(new DeviceAttribute(attributeName, value, dim_x, dim_y));
	}

}

