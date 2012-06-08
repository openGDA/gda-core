/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * A version of EpicsPositioner which does not use DMOV at all but relies on the callback mecnanism to determine when
 * the move has completed.
 */
public class EpicsPositionerCallback extends EpicsPositioner {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPositionerCallback.class);

	private PutCallbackListener putCallbackListener;

	public EpicsPositionerCallback() {
		super();
		putCallbackListener = new PutCallbackListener();
		// remove the dmov listener
		dmovMonitor = null;
	}

	public class PutCallbackListener implements PutListener {

		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
				positionerStatus = EnumPositionerStatus.ERROR;
				return;
			}
			positionerStatus = EnumPositionerStatus.IDLE;
		}

	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// find in the positionNames array the index of the string
		if (positions.contains(position.toString())) {
			int target = positions.indexOf(position.toString());
			try {
				if (getStatus() == EnumPositionerStatus.MOVING) {
					logger.warn("{} is busy", getName());
					return;
				}
				positionerStatus = EnumPositionerStatus.MOVING;
				controller.caput(select, target, putCallbackListener);
			} catch (Throwable th) {
				positionerStatus = EnumPositionerStatus.ERROR;
				throw new DeviceException(select.getName() + " failed to moveTo " + position, th);
			}
		}
	}

}
