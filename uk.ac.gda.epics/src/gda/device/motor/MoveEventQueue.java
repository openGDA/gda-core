/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.motor;

import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.motor.EpicsMotor.STATUSCHANGE_REASON;

class MoveEventQueue implements Runnable {

	private class MoveEvent {
		final MotorStatus newStatus;
		final EpicsMotor motor;
		final EpicsMotor.STATUSCHANGE_REASON reason;

		MoveEvent(EpicsMotor motor, MotorStatus newStatus, EpicsMotor.STATUSCHANGE_REASON reason) {
			this.motor = motor;
			this.newStatus = newStatus;
			this.reason = reason;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MoveEventQueue.class);
	private final Vector<MoveEvent> items = new Vector<MoveEvent>();
	private final MoveEvent[] itemsToBeHandledType = new MoveEvent[0];
	private boolean killed = false;
	private Thread thread = null;

	public void addMoveCompleteEvent(EpicsMotor motor, MotorStatus newStatus, EpicsMotor.STATUSCHANGE_REASON reason) throws MotorException {
		synchronized (items) {
			logger.trace("Motor - {} addMoveCompleteEvent: newStatus = {}, reason = {}", motor.getName(), newStatus, reason);
			/*
			 * If reason = MOVETO then we need to perform the actual move now in the calling thread so that exceptions can be passed back to the caller. We set
			 * status here to busy so that any DMOV =1 events that happen between now and the caput callback do not cause the positioner to unlock early. Note
			 * that the positioner is locked by the calling thread so no DMOV events can change status until this thread releases the lock.
			 */
			if (reason == EpicsMotor.STATUSCHANGE_REASON.MOVETO) {
				motor.changeStatusAndNotify(MotorStatus.BUSY, EpicsMotor.STATUSCHANGE_REASON.START_MOVETO);
			}
			/*
			 * only add if an item for the same motor and status does not already exist
			 */
			boolean add = true;
			final Iterator<MoveEvent> iter = items.iterator();
			while (iter.hasNext()) {
				final MoveEvent item = iter.next();
				if (item.motor == motor && item.reason == reason) {
					// status is unknown if CAPUT_MOVECOMPLETE_IN_ERROR
					if (reason != STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE_IN_ERROR) {
						if ((item.newStatus == null && newStatus == null)
								|| (item.newStatus != null && newStatus != null && item.newStatus.equals(newStatus))) {
							add = false;
							break;
						}
					}
				}
			}
			if (add) {
				items.add(new MoveEvent(motor, newStatus, reason));
				if (thread == null) {
					thread = uk.ac.gda.util.ThreadManager.getThread(this);
					thread.start();
				}
				items.notifyAll();
			}
		}
	}

	public void dispose() {
		killed = true;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				MoveEvent[] itemsToBeHandled = null;
				synchronized (items) {
					if (!killed && items.isEmpty())
						items.wait();
					if (!items.isEmpty()) {
						itemsToBeHandled = items.toArray(itemsToBeHandledType);
						items.clear();
					}
				}
				if (itemsToBeHandled != null) {
					final int numItems = itemsToBeHandled.length;
					for (int index = 0; index < numItems; index++) {
						try {
							final MoveEvent item = itemsToBeHandled[index];
							item.motor.changeStatusAndNotify(item.newStatus, item.reason);
						} catch (Exception ex) {
							logger.error("changeStatusAndNotify exception", ex);
						}
					}
				}
			} catch (Throwable th) {
				logger.error("run exception", th);
			}
		}
	}

}