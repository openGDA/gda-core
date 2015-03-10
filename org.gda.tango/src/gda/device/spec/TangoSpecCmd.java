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

package gda.device.spec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

public class TangoSpecCmd extends DeviceBase {

	private static final Logger logger = LoggerFactory.getLogger(TangoSpecCmd.class);
	private TangoDeviceProxy tangoDeviceProxy;

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			try {
				tangoDeviceProxy.isAvailable();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(), e);
			}
			configured = true;
		}
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	/**
	 * @param tangoDeviceProxy The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy tangoDeviceProxy) {
		this.tangoDeviceProxy = tangoDeviceProxy;
		System.out.println("tango dev proxy!!!!!! " + tangoDeviceProxy);
	}

	public String executeCmd(String cmd) throws DeviceException {
		String value = null;
		try {
			String[] argin = new String[1];
			argin[0] = cmd;
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			System.out.println("################### " + cmd);
			DeviceData argout = tangoDeviceProxy.command_inout("ExecuteCmd", args);
			value = argout.extractString();
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		return value;
	}

}
