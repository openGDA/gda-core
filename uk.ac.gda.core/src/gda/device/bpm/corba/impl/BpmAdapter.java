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

package gda.device.bpm.corba.impl;

import gda.device.BPM;
import gda.device.DeviceException;
import gda.device.bpm.corba.CorbaBPM;
import gda.device.bpm.corba.CorbaBPMHelper;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the BPM class
 */
public class BpmAdapter extends DeviceAdapter implements BPM {
	private CorbaBPM corbaBPM;

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
	public BpmAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaBPM = CorbaBPMHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public double getX() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaBPM.getX();
			} catch (COMM_FAILURE cf) {
				corbaBPM = CorbaBPMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaBPM = CorbaBPMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getY() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaBPM.getY();
			} catch (COMM_FAILURE cf) {
				corbaBPM = CorbaBPMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaBPM = CorbaBPMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
