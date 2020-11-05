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

import org.springframework.beans.factory.InitializingBean;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
import gda.device.TangoDeviceProxy;
import gda.device.base.Base;

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
}
