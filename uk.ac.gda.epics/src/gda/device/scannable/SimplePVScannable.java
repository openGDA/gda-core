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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.epics.CAClient;
import gda.factory.FactoryException;

//Access pv's using CAClient in it's simplest form.

public class SimplePVScannable extends ScannableBase implements Scannable {

	private Object pvName;
	private CAClient ca_client = new CAClient();
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		// to make sure the column is correct in data files
		if (getInputNames().length == 1 && getInputNames()[0].equals(ScannableBase.DEFAULT_INPUT_NAME)) {
			setInputNames(new String[] { getName() });
		}
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			ca_client.caput((String) pvName, Double.parseDouble(position.toString()));
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo", e);
		}
	}
	
	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return ca_client.caget((String) pvName);
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawGetPosition", e);
		}
	}

	public Object getPvName() {
		return pvName;
	}

	public void setPvName(Object pvName) {
		this.pvName = pvName;
	}
}
