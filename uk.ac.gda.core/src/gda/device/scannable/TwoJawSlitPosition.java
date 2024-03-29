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

package gda.device.scannable;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.util.QuantityFactory;

/**
 * Position of the centre of a gap in a two-jaw slit.
 * <p>
 * Works in two modes. First mode where motor position positive directions are in the same direction and the second
 * where positive for both axes is away from the middle. In both cases zero is the central position where the two jaws
 * meet.
 * <p>
 * It is assumed that the first jaw has the higher motor position than the second.
 */
public class TwoJawSlitPosition extends ScannableMotionUnitsBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(TwoJawSlitPosition.class);

	private double retryTimeout = 5;
	protected ScannableMotionUnits firstJaw;
	protected ScannableMotionUnits secondJaw;
	protected String firstJawName;
	protected String secondJawName;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		// use the finder to get the Scannable objects
		if (firstJaw == null) {
			firstJaw = Finder.find(firstJawName);
		}

		if (secondJaw == null) {
			secondJaw = Finder.find(secondJawName);
		}

		firstJaw.addIObserver(this);
		secondJaw.addIObserver(this);

		inputNames = new String[] { getName() };

		if (getInitialUserUnits() == null) {
			try {
				unitsComponent.setHardwareUnitString(firstJaw.getUserUnits());
				unitsComponent.setUserUnits(firstJaw.getUserUnits());
			} catch (DeviceException e) {
				throw new FactoryException("Error configuring " + getName(), e);
			}
		}

		// calculate the limits and give them to the limits component
		calculateLimits();

		setConfigured(true);
	}

	/**
	 * If the numberTries and tolerance attributes have been set then repeatedly tries to move this Scannable until the
	 * position is within the tolerance range.
	 *
	 * @see gda.device.scannable.ScannableBase#moveTo(java.lang.Object)
	 */
	@Override
	public void moveTo(Object position) throws DeviceException {

		if (numberTries <= 1) {
			super.moveTo(position);
		} else {

			int numberAttempts = 0;

			// note current value
			final Quantity<Length> initialPositionQuantity = getCurrentPosition();
			final double initialPositionUserUnits = initialPositionQuantity.to(userUnitFromUnitsComponent()).getValue().doubleValue();

			try {
				// calculate motor targets based on starting positions
				final Quantity<Length>[] targets = calculateTargets(position);
				// loop for numberTries times until current position is within tolerance of the target
				while (numberAttempts < numberTries && !isAt(position)) {
					numberAttempts++;
					moveJaws(targets);
					try {
						waitWhileBusy(retryTimeout);
					} catch (DeviceException e) {
						// call a halt to the motors as something's got stuck
						firstJaw.stop();
						secondJaw.stop();
						// but there might be a timeout waiting for the stop to work! Throw this exception.
						waitWhileBusy(retryTimeout);
					}
				}
			} catch (Exception e) {
				throw new DeviceException("Exception while operating " + getName()
						+ ". This may have moved the slit centre. The initial centre position was  "
						+ initialPositionUserUnits + ". ", e);
			}

			// if tried too many times then throw an exception
			if (numberAttempts >= numberTries) {
				throw new DeviceException("Move failed after " + numberTries + " attempts");
			}
		}
	}

	protected double[] getLimits(
			Quantity<Length> firstJawMin,
			Quantity<Length> firstJawMax,
			Quantity<Length> secondJawMin,
			Quantity<Length> secondJawMax)
	{
		final double minimum = firstJawMin.add(secondJawMin).divide(2.0).to(userUnitFromUnitsComponent()).getValue().doubleValue();
		final double maximum = firstJawMax.add(secondJawMax).divide(2.0).to(userUnitFromUnitsComponent()).getValue().doubleValue();
		return new double[] { minimum, maximum };
	}

	protected void calculateLimits() {
		try {
			Double firstLowerLimit = null;
			Double firstUpperLimit = null;
			Double secondLowerLimit = null;
			Double secondUpperLimit = null;

			final Double[] firstJawLimits = ScannableMotionBase.getInputLimits(firstJaw, 0);
			if (firstJawLimits != null) {
				firstLowerLimit = firstJawLimits[0];
				firstUpperLimit = firstJawLimits[1];
			}
			final Double[] secondJawLimits = ScannableMotionBase.getInputLimits(secondJaw, 0);
			if (secondJawLimits != null) {
				secondLowerLimit = secondJawLimits[0];
				secondUpperLimit = secondJawLimits[1];
			}

			if (firstLowerLimit != null || firstUpperLimit != null || secondLowerLimit != null
					|| secondUpperLimit != null) {
				if (firstLowerLimit == null || firstUpperLimit == null || secondLowerLimit == null
						|| secondUpperLimit == null) {
					throw new DeviceException("Unable to set limits if one of the dependent limits is null");
				}
				final Unit<Length> firstJawUnits = QuantityFactory.createUnitFromString(firstJaw.getUserUnits());
				final Unit<Length> secondJawUnits = QuantityFactory.createUnitFromString(secondJaw.getUserUnits());
				final Quantity<Length> firstJawMin = QuantityFactory.createFromObject(firstLowerLimit, firstJawUnits);
				final Quantity<Length> firstJawMax = QuantityFactory.createFromObject(firstUpperLimit, firstJawUnits);
				final Quantity<Length> secondJawMin = QuantityFactory.createFromObject(secondLowerLimit, secondJawUnits);
				final Quantity<Length> secondJawMax = QuantityFactory.createFromObject(secondUpperLimit, secondJawUnits);
				final double[] limits = getLimits(firstJawMin, firstJawMax, secondJawMin, secondJawMax);
				setLowerGdaLimits(limits[0]);
				setUpperGdaLimits(limits[1]);
			}
		} catch (Exception e) {
			logger.error("exception while fetching limits in {}", getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	protected Unit<Length> userUnitFromUnitsComponent() {
		return (Unit<Length>) unitsComponent.getUserUnit();
	}

	@Override
	public String checkPositionValid(Object illDefinedPosObject) throws DeviceException {
		// perform the calculation
		final Quantity<? extends Quantity<?>>[] targets = calculateTargets(illDefinedPosObject);

		// move the Scannables
		String reason = firstJaw.checkPositionValid(targets[0]);
		if (reason != null) {
			return reason;
		}
		reason = secondJaw.checkPositionValid(targets[1]);
		if (reason != null) {
			return reason;
		}
		return null;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		throwExceptionIfInvalidTarget(position);

		// perform the calculation
		final Quantity<? extends Quantity<?>>[] targets = calculateTargets(position);
		moveJaws(targets);
	}

	private void moveJaws(Quantity<? extends Quantity<?>>[] targets) throws DeviceException {
		// move the Scannables
		firstJaw.asynchronousMoveTo(targets[0]);
		secondJaw.asynchronousMoveTo(targets[1]);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// return position as a double
		return getCurrentPosition().to(userUnitFromUnitsComponent()).getValue().doubleValue();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return firstJaw.isBusy() || secondJaw.isBusy();
	}

	/**
	 * @return Returns the firstJaw.
	 */
	public ScannableMotionUnits getFirstJaw() {
		return firstJaw;
	}

	/**
	 * @param firstJaw
	 *            The firstJaw to set.
	 */
	public void setFirstJaw(ScannableMotionUnits firstJaw) {
		this.firstJaw = firstJaw;
	}

	/**
	 * @return Returns the secondJaw.
	 */
	public ScannableMotionUnits getSecondJaw() {
		return secondJaw;
	}

	/**
	 * @param secondJaw
	 *            The secondJaw to set.
	 */
	public void setSecondJaw(ScannableMotionUnits secondJaw) {
		this.secondJaw = secondJaw;
	}

	/**
	 * @return Returns the firstJawName.
	 */
	public String getFirstJawName() {
		return firstJawName;
	}

	/**
	 * @param firstJawName
	 *            The firstJawName to set.
	 */
	public void setFirstJawName(String firstJawName) {
		this.firstJawName = firstJawName;
	}

	/**
	 * @return Returns the secondJawName.
	 */
	public String getSecondJawName() {
		return secondJawName;
	}

	/**
	 * @param secondJawName
	 *            The secondJawName to set.
	 */
	public void setSecondJawName(String secondJawName) {
		this.secondJawName = secondJawName;
	}

	//
	/**
	 * given a position, where should the motors be moved to?
	 *
	 * @param position
	 * @return Quantity[2]
	 * @throws DeviceException
	 */
	protected Quantity<Length>[] calculateTargets(Object position) throws DeviceException {
		final Quantity<Length> currentGap = getCurrentGap();
		// convert what is supplied to Quantity in user units
		final Quantity<Length> target = QuantityFactory.createFromObject(position, userUnitFromUnitsComponent());
		// perform the calculation
		@SuppressWarnings("unchecked")
		final Quantity<Length>[] targets = new Quantity[2];
		targets[0] = target.add(currentGap.divide(2));
		targets[1] = target.subtract(currentGap.divide(2));
		return targets;
	}

	/**
	 * what is the current position as a user unit quantity?
	 *
	 * @return Quantity
	 * @throws DeviceException
	 */
	protected Quantity<Length> getCurrentPosition() throws DeviceException {
		// get the current positions in this objects user units
		final Quantity<Length> p1 = QuantityFactory.createFromObject(firstJaw.getPosition(), firstJaw.getUserUnits());
		final Quantity<Length> p2 = QuantityFactory.createFromObject(secondJaw.getPosition(), secondJaw.getUserUnits());

		// determine position as a quantity
		return p1.add(p2).divide(2.0);
	}

	/**
	 * what is the current position as a user unit quantity?
	 *
	 * @return Quantity
	 * @throws DeviceException
	 */
	protected Quantity<Length> getCurrentGap() throws DeviceException {
		// get the current positions in this objects user units
		final Quantity<Length> p1 = QuantityFactory.createFromObject(firstJaw.getPosition(), firstJaw.getUserUnits());
		final Quantity<Length> p2 = QuantityFactory.createFromObject(secondJaw.getPosition(), secondJaw.getUserUnits());

		// determine position as a quantity
		return p1.subtract(p2); // TODO: may have to check for absolute position
	}

	@Override
	public void stop() throws DeviceException {
		firstJaw.stop();
		secondJaw.stop();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		// fan out move complete messages
		if (changeCode == ScannableStatus.IDLE) {
			notifyIObservers(this, changeCode);
		}
	}

}
