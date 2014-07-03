/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.hrpd.typedpvscannables;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener;
import gda.device.DeviceException;
import gda.device.scannable.PVScannable;
import gda.device.scannable.ScannableUtils;
import gda.epics.LazyPVFactory;

/**
 * A simple PV scannable that facilitates access to a single EPICS PV of DBR_Double type. It only supports get and set value
 * from the EPICS PV specified. It is different from the {@link PVScannable} in that it does not monitor PV value
 * changes by design.
 * 
 * for monitoring a single PV of Double type, please see {@link EpicsDoubleDataListener}.
 */

public class DoublePVScannable extends EpicsPVScannable {
	private static final Logger logger=LoggerFactory.getLogger(DoublePVScannable.class);
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		super.asynchronousMoveTo(position);
		double target = ScannableUtils.objectToArray(position)[0];
		try {
			isBusy = true;
			LazyPVFactory.newDoublePV(getPvName()).putNoWait(target, this);
		} catch (IOException e) {
			isBusy = false;
			throw new DeviceException(e.getMessage(), e);
		}
	}

	@Override
	public Double getPosition() throws DeviceException {
		try {
			return LazyPVFactory.newDoublePV(getPvName()).get();
		} catch (IOException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		logger.info("{} initialisation completed.", getName());
	}
}
