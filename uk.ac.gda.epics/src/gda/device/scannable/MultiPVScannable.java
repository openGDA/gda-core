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

package gda.device.scannable;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;

/**
 * A multi-PV Scannable which operates like a normal ScannableMotionUnit Scannable, but
 * read to and writes from separate PVs.
 */
public class MultiPVScannable extends ScannableMotionUnitsBase {

	private static final Logger logger = LoggerFactory.getLogger(MultiPVScannable.class);

	private String writePVname;
	private String readPVname;

	private ReadOnlyPV<Double> pvRead;
	private PV<Double> pvWrite;

	@Override
	public void configure() throws FactoryException {
		if (readPVname != null && !readPVname.isEmpty()) {
			pvRead = LazyPVFactory.newReadOnlyDoublePV(readPVname);
		} else {
			throw new FactoryException("readPV is not set for" + this.getName());
		}
		if (readPVname != null && !writePVname.isEmpty()) {
			pvWrite = LazyPVFactory.newDoublePV(writePVname);
		} else {
			throw new FactoryException("writePV is not set for" + this.getName());
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			pvWrite.putWait(ScannableUtils.objectToArray(position)[0]);
		} catch (IOException e) {
			final String msg = "Couldn't set position of " + getName();
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return pvRead.get();
		} catch (IOException e) {
			final String msg = "Couldn't get position of " + getName();
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	public String getWritePV() {
		return writePVname;
	}

	public void setWritePV(String writePV) {
		this.writePVname = writePV;
	}

	public String getReadPV() {
		return readPVname;
	}

	public void setReadPV(String readPV) {
		this.readPVname = readPV;
	}
}
