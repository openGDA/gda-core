/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.monitor.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.monitor.corba.CorbaMonitor;
import gda.device.monitor.corba.CorbaMonitorHelper;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Monitor class
 */
public class MonitorAdapter extends ScannableAdapter implements Monitor, Findable, Device, Scannable {
	private CorbaMonitor corbaMonitor;

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public MonitorAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaMonitor = CorbaMonitorHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int getElementCount() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMonitor.getElementCount();
			} catch (COMM_FAILURE cf) {
				corbaMonitor = CorbaMonitorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMonitor = CorbaMonitorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getUnit() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMonitor.getUnit();
			} catch (COMM_FAILURE cf) {
				corbaMonitor = CorbaMonitorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMonitor = CorbaMonitorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

}
