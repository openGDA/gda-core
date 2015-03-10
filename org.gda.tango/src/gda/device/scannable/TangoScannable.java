/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoDs.TangoConst;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a scannable from any Tango attribute.
 */
public class TangoScannable extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(TangoScannable.class);
	private String attributeName;
	private TangoDeviceProxy deviceProxy = null;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			deviceProxy.isAvailable();
		} catch (Exception e) {
			throw new FactoryException(e.getMessage());
		}
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return deviceProxy;
	}

	/**
	 * @param deviceProxy
	 *            The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy deviceProxy) {
		this.deviceProxy = deviceProxy;
	}

	/**
	 * @return Returns the Tango attributeName.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @param attributeName
	 *            The Tango attributeName to set.
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		deviceProxy.isAvailable();
		try {
			DeviceAttribute devattr = deviceProxy.read_attribute(attributeName);
			int devattrDataType = devattr.getType();
			double dpos = 0.0;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			if (position instanceof Integer) {
				dpos = (Integer) position;
			} else if (position instanceof Double) {
				dpos = (Double) position;
			} else if (position instanceof Short) {
				dpos = (Short) position;
			} else if (position instanceof Float) {
				dpos = (Float) position;
			} else if (position instanceof Long) {
				dpos = (Long) position;
			}
			if (devattrDataType == TangoConst.Tango_DEV_SHORT) {
				devattr = new DeviceAttribute(attributeName, (short) dpos);
			} else if (devattrDataType == TangoConst.Tango_DEV_LONG) {
				devattr = new DeviceAttribute(attributeName, (int) dpos);
			} else if (devattrDataType == TangoConst.Tango_DEV_LONG64) {
				devattr = new DeviceAttribute(attributeName, (long) dpos);
			} else if (devattrDataType == TangoConst.Tango_DEV_DOUBLE) {
				devattr = new DeviceAttribute(attributeName, dpos);
			} else if (devattrDataType == TangoConst.Tango_DEV_FLOAT) {
				devattr = new DeviceAttribute(attributeName, (float) dpos);
			} else if (devattrDataType == TangoConst.Tango_DEV_USHORT) {
				devattr = new DeviceAttribute(attributeName, (short) dpos);
			} else if (devattrDataType == TangoConst.Tango_DEV_ULONG) {
				devattr = new DeviceAttribute(attributeName, (int) dpos);
			}
			deviceProxy.write_attribute(devattr);
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}

	}

	@Override
	public Object getPosition() throws DeviceException {
		DeviceAttribute devattr;
		int devattrDataType;
		Object value = 0;
		deviceProxy.isAvailable();
		try {
			devattr = deviceProxy.read_attribute(attributeName);
			devattrDataType = devattr.getType();
			if (devattrDataType == TangoConst.Tango_DEV_SHORT) {
				value = devattr.extractShort();
			} else if (devattrDataType == TangoConst.Tango_DEV_LONG) {
				value = devattr.extractLong();
			} else if (devattrDataType == TangoConst.Tango_DEV_LONG64) {
				value = devattr.extractLong64();
			} else if (devattrDataType == TangoConst.Tango_DEV_DOUBLE) {
				value = devattr.extractDouble();
			} else if (devattrDataType == TangoConst.Tango_DEV_FLOAT) {
				value = devattr.extractFloat();
			} else if (devattrDataType == TangoConst.Tango_DEV_USHORT) {
				value = devattr.extractUShort();
			} else if (devattrDataType == TangoConst.Tango_DEV_ULONG) {
				value = devattr.extractULong();
			} else if (devattrDataType == TangoConst.Tango_DEV_STRING) {
				value = devattr.extractString();
			} else {
				throw new DeviceException("Attribute type " + devattrDataType + " is not implemented yet");
			}
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}

		return value;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
}
