/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.currentamplifier;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.factory.Configurable;
import gda.factory.FactoryException;

public class FemtoGainScannable extends ScannableBase implements Configurable {

	private String pvName;
	private PV<String> pv;
	private static final Logger logger = LoggerFactory.getLogger(FemtoGainScannable.class);

	@Override
	public void configure() throws FactoryException {
		pv = LazyPVFactory.newStringPV(pvName);
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		throw new DeviceException("FemtoGain is read only, cannot move");
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return pv.get();
		} catch (IOException e) {
			final String msg = "Couldn't set position of " + getName();
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}
}