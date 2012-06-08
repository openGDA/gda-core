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

package gda.device.enumpositioner.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.enumpositioner.corba.CorbaEnumPositioner;
import gda.device.enumpositioner.corba.CorbaEnumPositionerHelper;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Enumpositioner class
 */
public class EnumpositionerAdapter extends ScannableAdapter implements EnumPositioner, Scannable, Device, Findable {

	protected CorbaEnumPositioner corbaEnumpositioner;

	/**
	 * Constructor. Calls super contructor.
	 * 
	 * @param obj
	 * @param name
	 * @param netService
	 */
	public EnumpositionerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {

		super(obj, name, netService);
		corbaEnumpositioner = CorbaEnumPositionerHelper.narrow(obj);
	}

	@Override
	public String[] getPositions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaEnumpositioner.getPositions();
			} catch (COMM_FAILURE cf) {
				corbaEnumpositioner = CorbaEnumPositionerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEnumpositioner = CorbaEnumPositionerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return EnumPositionerStatus.from_int(corbaEnumpositioner.getStatus().value());
			} catch (COMM_FAILURE cf) {
				corbaEnumpositioner = CorbaEnumPositionerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEnumpositioner = CorbaEnumPositionerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
