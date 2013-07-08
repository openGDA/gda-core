/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 * Version of PVScannable which returns a string. TODO new versions of these classes using LazyPVFactory should be
 * written some time
 */
public class PVStringScannable extends PVScannable {

	public PVStringScannable() {
	}

	public PVStringScannable(String name, String pv) {
		setName(name);
		this.pvName = pv;
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			return controller.cagetString(theChannel);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		} catch (CAException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		}
	}
}
