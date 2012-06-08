/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version of EpicsValve which uses callback for the status rather than looking at the status PV.
 */
public class EpicsValveCallback extends EpicsValve implements EnumPositioner, MonitorListener, Scannable {
	private static final Logger logger = LoggerFactory.getLogger(EpicsValveCallback.class);

	private PutCallbackListener putCallbackListener;

	public EpicsValveCallback() {
		super();
		putCallbackListener = new PutCallbackListener();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			// check top ensure a correct string has been supplied
			if (positions.contains(position.toString())) {
				controller.caput(currentPositionChnl, position.toString(), putCallbackListener);
				positionerStatus = EnumPositionerStatus.MOVING;
				return;
			}
		} catch (Throwable th) {
			positionerStatus = EnumPositionerStatus.ERROR;
			throw new DeviceException("failed to move to" + position.toString(), th);
		}
		// if get here then wrong position name supplied
		throw new DeviceException(getName() + ": demand position " + position.toString()
				+ " not acceptable. Should be one of: " + ArrayUtils.toString(positions));
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return positionerStatus;
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		try {
			// no matter what message is received, send observers latest status and position
			EnumPositionerStatus status = fetchEpicsStatus();
			if (status != positionerStatus) {
				positionerStatus = status;
				notifyIObservers(this, status);

				if (status == EnumPositionerStatus.IDLE && currentPositionChnl != null && positions != null) {
					notifyIObservers(this, getPosition());
				}
			}

		} catch (DeviceException e) {
			logger.debug(e.getClass() + " while updating EpicsPositioner " + getName() + " : " + e.getMessage());
		}
	}
	
	public class PutCallbackListener implements PutListener {
		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
						event.getStatus());
				positionerStatus = EnumPositionerStatus.ERROR;
				return;
			}
			positionerStatus = EnumPositionerStatus.IDLE;
		}
	}
}
