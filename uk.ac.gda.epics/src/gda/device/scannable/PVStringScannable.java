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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Version of PVScannable which returns a string. TODO new versions of these classes using LazyPVFactory should be
 * written some time
 */
@ServiceInterface(Scannable.class)
public class PVStringScannable extends PVScannable {

	private static final Logger logger = LoggerFactory.getLogger(PVStringScannable.class);

	private String lastPosition = "";

	public PVStringScannable() {
	}

	public PVStringScannable(String name, String pv) {
		setName(name);
		this.pvName = pv;
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			if (theChannel.get().isBYTE()) {
				byte[] byteArray = controller.cagetByteArray(theChannel);
				return new String(byteArray).trim();
			} else {
				return controller.cagetString(theChannel);
			}
		} catch (InterruptedException | TimeoutException | CAException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		try {
			// TODO: This should be made asynchronous
			controller.caput(theChannel, position.toString());
		} catch (CAException | InterruptedException | NullPointerException e) {
			throw new DeviceException("Could not set the position of {}", getName(), e);
		}
	}

	@Override
	public void monitorChanged(MonitorEvent event) {
		logger.debug("monitorChanged called for {} ({})", getName(), pvName);

		final DBR dbr = event.getDBR();
		String currentPosition;
		if (dbr.isBYTE()) {
			final BYTE bytes = (BYTE) dbr;
			currentPosition = new String(bytes.getByteValue());
		} else if (dbr.isSTRING()) {
			final STRING str = (STRING) dbr;
			currentPosition = str.getStringValue()[0];
		} else {
			logger.debug("New position {} is not a string", dbr);
			return;
		}

		if (!currentPosition.equals(lastPosition)) {
			logger.debug("New position : {}", currentPosition);
			notifyObserversOfNewPosition(currentPosition);
			lastPosition = currentPosition;
		}
	}
}
