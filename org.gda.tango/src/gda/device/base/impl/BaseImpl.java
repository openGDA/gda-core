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

package gda.device.base.impl;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
import gda.device.Scannable;
import gda.device.TangoDeviceProxy;
import gda.device.base.Base;
import gda.device.scannable.TangoScannable;
import gda.factory.FactoryException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

public class BaseImpl implements Base, InitializingBean {

	private static final String COMMAND_INIT = "init";

	private TangoDeviceProxy deviceProxy;

	public TangoDeviceProxy getTangoDeviceProxy() {
		return deviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy deviceProxy) {
		this.deviceProxy = deviceProxy;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getTangoDeviceProxy() == null)
			throw new IllegalStateException("deviceProxy not set");

	}

	@Override
	public String toString() {
		return "BaseImpl [deviceProxy=" + getTangoDeviceProxy() + "]";
	}

	private void sendSimpleCommand(String command) throws DevFailed {
		getTangoDeviceProxy().sendSimpleCommand(command);
	}

	@Override
	public void init() throws DevFailed {
		sendSimpleCommand(COMMAND_INIT);
	}

	@Override
	public DevState getState() throws DevFailed {
		return getTangoDeviceProxy().state();
	}
	
	@Override
	public String getStatus() throws DevFailed {
		return getTangoDeviceProxy().status();
	}

	@Override
	public String[] getAttrStringValueList(String attributeName) throws DevFailed {
		DeviceData argin = new DeviceData();
		argin.insert(attributeName);
		DeviceData command_inout = getTangoDeviceProxy().command_inout("getAttrStringValueList", argin);
		return command_inout.extractStringArray();
	}

	Map<String, TangoScannable> tangoScannableMap = null; 

	@Override
	public Scannable getControlScannable(String name, String attributeName, String format) throws FactoryException {
		if( tangoScannableMap == null){
			tangoScannableMap = new HashMap<String, TangoScannable>();
		}
		if( format == null)
			format="";
		String key= name+":"+attributeName+":"+format;
		TangoScannable tangoScannable;
		if( ( tangoScannable = tangoScannableMap.get(key)) == null){
			tangoScannable = new TangoScannable();
			tangoScannable.setTangoDeviceProxy(getTangoDeviceProxy());
			tangoScannable.setAttributeName(attributeName);
			tangoScannable.setName(name);
			tangoScannable.setInputNames(new String[]{name});
			if( !format.isEmpty())
				tangoScannable.setOutputFormat(new String[]{format});
			tangoScannable.configure();
			tangoScannableMap.put(key, tangoScannable);
		}
		return tangoScannable;
	}
	/*
	 * public String getAttributeAsString(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsString(attributeName); } public void setAttribute(String attributeName,
	 * String value) throws DevFailed { getTangoDeviceProxy().setAttribute(attributeName, value); } public double
	 * getAttributeAsDouble(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsDouble(attributeName); } public void setAttribute(String attributeName,
	 * double value) throws DevFailed { getTangoDeviceProxy().setAttribute(attributeName, value); } public boolean
	 * getAttributeAsBoolean(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsBoolean(attributeName); } public void setAttribute(String attributeName,
	 * boolean value) throws DevFailed { getTangoDeviceProxy().setAttribute(attributeName, value); } public int
	 * getAttributeAsInt(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsInt(attributeName); } public long getAttributeAsLong(String attributeName)
	 * throws DevFailed { return getTangoDeviceProxy().getAttributeAsLong(attributeName); } public void
	 * setAttribute(String attributeName, long value) throws DevFailed {
	 * getTangoDeviceProxy().setAttribute(attributeName, value); } public String[] getAttributeAsStringArray(String
	 * attributeName) throws DevFailed { return getTangoDeviceProxy().getAttributeAsStringArray(attributeName); } public
	 * void setAttribute(String attributeName, String[] value, int dim_x, int dim_y) throws DevFailed {
	 * getTangoDeviceProxy().setAttribute(attributeName, value, dim_x, dim_y); } public int[]
	 * getAttributeAsIntArray(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsIntArray(attributeName); } public long[] getAttributeAsULongArray(String
	 * attributeName) throws DevFailed { return getTangoDeviceProxy().getAttributeAsLongArray(attributeName); } public
	 * void setAttribute(String attributeName, int[] value, int dim_x, int dim_y) throws DevFailed {
	 * getTangoDeviceProxy().setAttribute(attributeName, value, dim_x, dim_y); } public void setAttribute(String
	 * attributeName, long[] value, int dim_x, int dim_y) throws DevFailed {
	 * getTangoDeviceProxy().setAttribute(attributeName, value, dim_x, dim_y); } public double[]
	 * getAttributeAsDoubleArray(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsDoubleArray(attributeName); } public void setAttribute(String attributeName,
	 * double[] value, int dim_x, int dim_y) throws DevFailed { getTangoDeviceProxy().setAttribute(attributeName, value,
	 * dim_x, dim_y); } public boolean[] getAttributeAsBooleanArray(String attributeName) throws DevFailed { return
	 * getTangoDeviceProxy().getAttributeAsBooleanArray(attributeName); } public void setAttribute(String attributeName,
	 * boolean[] value, int dim_x, int dim_y) throws DevFailed { getTangoDeviceProxy().setAttribute(attributeName,
	 * value, dim_x, dim_y); }
	 */
}
