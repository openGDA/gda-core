/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.List;

import javax.measure.Quantity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;

/**
 * This class extends {@link CoupledScannable}, but it moves the component scannables in sequence rather than
 * simultaneously.
 * <p>
 * It was written to handle the specific requirements of I08-1, which had two devices that needed to be moved in equal
 * and opposite directions in sequence: see https://jira.diamond.ac.uk/browse/I08-317.<br>
 * However, it is written to be configurable for different types of sequential motion.
 * <p>
 * The SequentialScannable object, like {@link CoupledScannable}, is initialised with a list of scannables and functions
 * that control their relative motion.<br>
 * Additionally, you can specify the order in which the scannables should be moved:
 * <ul>
 * <li>ALWAYS_FORWARDS (default): always move the scannables in the order they are specified in the list</li>
 * <li>WITH_MOVEMENT: if the "primary" scannable (the first in the list) is to be moved in a positive direction, iterate
 * forwards over the list to move the scannables (i.e. move them in the order in which they are specified in the list);
 * if the primary scannable is moved in a negative direction, iterate backwards over the list</li>
 * <li>CONTRARY_TO_MOVEMENT: if the primary scannable is moved in a positive direction, iterate backwards over the list
 * of scannables: if it is moved in a negative direction, iterate forwards over the list</li>
 */
public class SequentialScannable extends CoupledScannable {
	private static final Logger logger = LoggerFactory.getLogger(SequentialScannable.class);

	private Order order = Order.ALWAYS_FORWARDS;
	private boolean forwards;
	private int numScannables;
	private int currentScannable;
	private List<? extends Object> targets;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			super.configure();
			// Cache number of scannables
			numScannables = theScannables.size();
			setConfigured(true);
		}
	}

	@Override
	protected void moveUnderlyingScannables(List<? extends Object> targets) throws DeviceException {
		this.targets = targets;
		forwards = isScanInForwardsOrder(theScannables.get(0), targets.get(0));
		if (forwards) {
			currentScannable = 0;
		} else {
			currentScannable = numScannables - 1;
		}
		moveCurrentScannable();
	}

	private void moveCurrentScannable() throws DeviceException {
		final Scannable scannable = theScannables.get(currentScannable);
		final Object target = targets.get(currentScannable);
		logger.debug("Moving {} to {}", scannable.getName(), target);
		scannable.asynchronousMoveTo(target);
	}

	private boolean isScanInForwardsOrder(Scannable scannable, Object target) {
		if (order == Order.ALWAYS_FORWARDS || !(target instanceof Number || target instanceof Quantity)) {
			return true;
		}
		try {
			// If the initial position is not numeric, we can't do a comparison
			final Object initialPosition = scannable.getPosition();
			if (!(initialPosition instanceof Number || initialPosition instanceof Quantity)) {
				return true;
			}

			final double initialPos = getPositionAsDouble(initialPosition);
			final double targetPos = getPositionAsDouble(target);
			final boolean positionIncreasing = targetPos > initialPos;
			return (order == Order.WITH_MOVEMENT) ? positionIncreasing : !positionIncreasing;
		} catch (Exception e) {
			logger.error("Error comparing positions for {}: assume forwards order", scannable.getName());
		}
		return true;
	}

	private double getPositionAsDouble(Object position) {
		if (position instanceof Number) {
			return ((Number) position).doubleValue();
		} else if (position instanceof Quantity) {
			return ((Quantity<?>) position).getValue().doubleValue();
		}
		throw new IllegalArgumentException("Position " + position + " cannot be converted to a double");
	}

	@Override
	protected void handleUpdate(Object theObserved, Object changeCode) {
		if (!(changeCode instanceof ScannableStatus)) {
			return;
		}
		final ScannableStatus scannableStatus = ((ScannableStatus) changeCode);

		if (scannableStatus == ScannableStatus.BUSY || scannableStatus == ScannableStatus.FAULT) {
			notifyIObservers(this, scannableStatus);
		} else if (scannableStatus == ScannableStatus.IDLE) {
			try {
				// The current scannable has finished moving - move to the next one (if any)
				if (forwards && currentScannable < numScannables - 1) {
					currentScannable++;
					moveCurrentScannable();
				} else if (!forwards && currentScannable > 0) {
					currentScannable--;
					moveCurrentScannable();
				} else {
					notifyIObservers(this, ScannableStatus.IDLE);
				}
			} catch (DeviceException e) {
				logger.error("Error moving scannables", e);
				try {
					stop();
				} catch (DeviceException e1) {
					logger.error("Error stopping scannable", e1);
				}
			}
		}
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public enum Order {
		ALWAYS_FORWARDS,
		WITH_MOVEMENT,
		CONTRARY_TO_MOVEMENT;
	}
}
