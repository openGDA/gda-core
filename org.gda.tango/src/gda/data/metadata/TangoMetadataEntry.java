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

package gda.data.metadata;

import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoDs.TangoConst;
import gda.device.TangoDeviceProxy;
import gda.factory.Configurable;
import gda.factory.FactoryException;

/**
 * A class to provide metadata from an attribute within a Tango server
 */
public class TangoMetadataEntry extends MetadataEntry implements Configurable {

	private String attributeName;
	private TangoDeviceProxy deviceProxy = null;

	@Override
	public void configure() throws FactoryException {
		try {
			deviceProxy.isAvailable();
		} catch (Exception e) {
			System.out.println(e);
			System.out.println(e.getMessage());
		}
	}
	
	public TangoDeviceProxy getTangoDeviceProxy() {
		return deviceProxy;
	}

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
	protected String readActualValue() throws Exception {
		DeviceAttribute devattr;
		int devattrDataType;
		String value = null;
		deviceProxy.isAvailable();
		devattr = deviceProxy.read_attribute(attributeName);
		devattrDataType = devattr.getType();
		if (devattrDataType == TangoConst.Tango_DEV_BOOLEAN) {
			value = String.valueOf(devattr.extractBoolean());
		} else if (devattrDataType == TangoConst.Tango_DEV_SHORT) {
			value = String.valueOf(devattr.extractShort());
		} else if (devattrDataType == TangoConst.Tango_DEV_LONG) {
			value = String.valueOf(devattr.extractLong());
		} else if (devattrDataType == TangoConst.Tango_DEV_FLOAT) {
			value = String.valueOf(devattr.extractFloat());
		} else if (devattrDataType == TangoConst.Tango_DEV_DOUBLE) {
			value = String.valueOf(devattr.extractDouble());
		} else if (devattrDataType == TangoConst.Tango_DEV_USHORT) {
			value = String.valueOf(devattr.extractUShort());
		} else if (devattrDataType == TangoConst.Tango_DEV_ULONG) {
			value = String.valueOf(devattr.extractULong());
		} else if (devattrDataType == TangoConst.Tango_DEV_LONG64) {
			value = String.valueOf(devattr.extractLong64());
		} else if (devattrDataType == TangoConst.Tango_DEV_STRING) {
			value = devattr.extractString();
		}

		return value;
	}
}
