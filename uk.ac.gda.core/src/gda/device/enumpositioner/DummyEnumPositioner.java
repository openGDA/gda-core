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

import static gda.device.EnumPositionerStatus.IDLE;
import static gda.device.EnumPositionerStatus.MOVING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A dummy class implementing the EnumPositioner for testing.
 */
@ServiceInterface(EnumPositioner.class)
public class DummyEnumPositioner extends EditableEnumPositionerBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyEnumPositioner.class);

	private static final String INVALID_POSITION_MESSAGE = "Position '%s' is invalid for %s";

	/** time in ms between polling for stop request */
	private static final int POLLING_TIME = 100;

	/** default time in ms to perform move */
	private static final int DEFAULT_MOVE_TIME = 500;

	private int currentPositionIndex = 0;

	/** Time (in ms) to perform move */
	private long timeToMove = DEFAULT_MOVE_TIME;

	private volatile EnumPositionerStatus currentStatus = IDLE;
	private volatile boolean inPos = true;
	private volatile boolean stopRequested = false;

	@Override
	public void configure() {
		this.inputNames = new String[] { getName() };
		setConfigured(true);
	}

	/**
	 * Add a possible position to the list of positions.
	 *
	 * @param position
	 */
	@Override
	public synchronized void addPosition(String position) {
		if (!containsPosition(position)) {
			super.addPosition(position);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// Set status to "moving" in case the positioner is polled before the async task starts
		setStatus(MOVING);
		Async.execute(() -> {
			try {
				moveTo(position);
			} catch (DeviceException e) {
				logger.error("Error moving {} to position {}", getName(), position, e);
				setStatus(IDLE);
			}
		});
	}

	@Override
	public String getPosition() throws DeviceException {
		return getPosition(currentPositionIndex);
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return currentStatus;
	}

	@Override
	public void stop() throws DeviceException {
		stopRequested = true;
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		final String positionString = position.toString();
		stopRequested = false;

		// Check validity of the requested position
		if (!containsPosition(positionString)) {
			setStatus(IDLE);
			throw new DeviceException(String.format(INVALID_POSITION_MESSAGE, positionString, getName()));
		}

		// find in the positionNames array the index of the string
		if (!getPosition().equals(positionString)) {
			inPos = false;
			setStatus(MOVING);
			logger.debug("{} moving to {}", getName(), positionString);

			final long numPolls = timeToMove / POLLING_TIME;
			for (int i = 0; i < numPolls; i++) {
				if (stopRequested) {
					break;
				}
				try {
					Thread.sleep(POLLING_TIME);
				} catch (InterruptedException e) {
					logger.error("Interrupted during simulated move", e);
					Thread.currentThread().interrupt();
				}
			}

			if (!stopRequested) {
				currentPositionIndex = getPositionIndex(positionString);
				inPos = true;
			}
		}
		setStatus(IDLE);
	}

	private void setStatus(EnumPositionerStatus status) {
		if (status != currentStatus) {
			currentStatus = status;
			notifyIObservers(this, status);
		}
	}

	@Override
	public boolean isInPos() throws DeviceException {
		return inPos;
	}

	public void setPosition(String position) throws DeviceException {
		final int positionIndex = getPositionIndex(position);
		if (positionIndex < 0) {
			throw new DeviceException(String.format(INVALID_POSITION_MESSAGE, position, getName()));
		}
		currentPositionIndex = positionIndex;
	}

	public void setTimeToMove(long timeToMove) {
		this.timeToMove = timeToMove;
	}

	public long getTimeToMove() {
		return timeToMove;
	}

}
