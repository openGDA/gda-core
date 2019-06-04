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

package gda.device.enumpositioner;

import static gda.device.enumpositioner.ValvePosition.CLOSE;
import static gda.device.enumpositioner.ValvePosition.OPEN;
import static gda.device.enumpositioner.ValvePosition.RESET;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A dummy class to imitate an EpicsValve
 * <p>
 * It accepts three commands: OPEN, CLOSE & RESET: see {@link gda.device.enumpositioner.ValvePosition}
 * <p>
 * If OPEN is specified, the object will first go into OPENING state, then OPEN<br>
 * If CLOSE is specified, the object will first go into CLOSING state, then CLOSED<br>
 * If RESET is specified, the object will first go into RESET state, then back to its original state<br>
 * <p>
 * The time taken to go into a requested state can be set by calling {@link #setTimeToMove(long)}
 */
@ServiceInterface(EnumPositioner.class)
public class DummyValve extends EnumPositionerBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyValve.class);

	private long timeToMove = 1000;  // time in ms to execute a move

	private enum ValveStatus {
		OPEN,
		OPENING,
		CLOSED,
		CLOSING,
		RESET;

		public static ValveStatus fromString(String stringValue) {
			return ValveStatus.valueOf(stringValue.trim().toUpperCase());
		}

		@Override
		public String toString() {
			return name().substring(0, 1) + name().substring(1).toLowerCase();
		}
	}

	private volatile ValveStatus currentStatus = ValveStatus.OPEN;
	private Future<?> moveTask = null;

	@Override
	public void configure() {
		if (!isConfigured()) {
			setPositionsInternal(Arrays.asList(OPEN, CLOSE, RESET)); // necessary to format pos command output
			setConfigured(true);
		}
	}

	/**
	 * Set off a move on a thread, creating a future to contain the result of the move<br>
	 * This function will fail if the valve is already moving, or if there is already an active future.
	 */
	@Override
	public synchronized void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (isBusy()) {
			throw new DeviceException(String.format("%s: cannot submit move as it is already moving", getName()));
		}
		moveTask = Async.submit(() -> {
			try {
				moveTo(position);
			} catch (DeviceException e) {
				logger.error("{}: exception moving to {}", getName(), position, e);
			}
		});
	}

	/**
	 * Check whether the valve is moving or a move has been queued
	 *
	 * @return true if moving or there is a pending move, false otherwise
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return super.isBusy() || (moveTask != null && !moveTask.isDone());
		} catch (Exception e) {
			throw new DeviceException("{}: exception getting busy status", getName(), e);
		}
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		final String positionString = position.toString().trim();
		if (positionString.equalsIgnoreCase(OPEN)) {
			if (currentStatus == ValveStatus.OPEN) {
				logger.debug("{}: already open - no move required", getName());
			} else {
				executeMove(ValveStatus.OPENING, ValveStatus.OPEN);
			}
		} else if (positionString.equalsIgnoreCase(CLOSE)) {
			if (currentStatus == ValveStatus.CLOSED) {
				logger.debug("{}: already closed - no move required", getName());
			} else {
				executeMove(ValveStatus.CLOSING, ValveStatus.CLOSED);
			}
		} else if (positionString.equalsIgnoreCase(RESET)) {
			executeMove(ValveStatus.RESET, currentStatus);
		} else {
			throw new IllegalArgumentException(
					String.format("%s: %s is not a valid position", getName(), positionString));
		}
	}

	/**
	 * Execute the specified list of moves<br>
	 * This function will fail if the valve is already moving, but the existence of a future is allowed.
	 *
	 * @param moves
	 *            List of moves to perform
	 * @throws DeviceException
	 */
	private void executeMove(ValveStatus movingStatus, ValveStatus finalStatus) throws DeviceException {
		synchronized (this) {
			if (super.isBusy()) {
				throw new DeviceException(String.format("Cannot move %s: it is already moving", getName()));
			}
			setPositionerStatus(EnumPositionerStatus.MOVING);
		}

		setStatus(movingStatus);
		try {
			Thread.sleep(timeToMove);
		} catch (InterruptedException e) {
			final String message = String.format("%s: exception in wait()", getName());
			logger.warn(message, e);
			Thread.currentThread().interrupt();
			setPositionerStatus(EnumPositionerStatus.ERROR);
			setStatus(ValveStatus.RESET);
			throw new DeviceException(message, e);
		}
		setStatus(finalStatus);
		setPositionerStatus(EnumPositionerStatus.IDLE);
	}

	private void setStatus(ValveStatus status) {
		logger.debug("{}: moving from {} to {}", getName(), currentStatus, status);
		currentStatus = status;
		notifyIObservers(this, currentStatus.toString());
		notifyIObservers(this, new ScannablePositionChangeEvent(currentStatus.toString()));
	}

	@Override
	public String getPosition() throws DeviceException {
		return currentStatus.toString();
	}

	/**
	 * @param position
	 *            The initial position to set.
	 */
	public void setPosition(String position) {
		if (!isConfigured()) {
			currentStatus = ValveStatus.fromString(position);
		}
	}

	/**
	 * Set the time to execute a move
	 *
	 * @param timeToMove
	 *            Time in milliseconds to execute move
	 */
	public void setTimeToMove(long timeToMove) {
		if (timeToMove < 0) {
			throw new IllegalArgumentException("timeToMove must not be negative");
		}
		this.timeToMove = timeToMove;
	}

}
