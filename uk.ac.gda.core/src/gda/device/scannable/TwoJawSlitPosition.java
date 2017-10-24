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

/**
 *
 */
package gda.device.scannable;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
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
/**
 *
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
		// use the finder to get the Scannable objects

		if (firstJaw == null) {
			firstJaw = (ScannableMotionUnits) Finder.getInstance().find(firstJawName);
		}

		if (secondJaw == null) {
			secondJaw = (ScannableMotionUnits) Finder.getInstance().find(secondJawName);
		}

		firstJaw.addIObserver(this);
		secondJaw.addIObserver(this);

		this.inputNames = new String[] { this.getName() };

		if( getInitialUserUnits() == null){
			try {
				unitsComponent.setHardwareUnitString(firstJaw.getUserUnits());
				unitsComponent.setUserUnits(firstJaw.getUserUnits());
			} catch (DeviceException e) {
				throw new FactoryException("Error configuring " + getName(), e);
			}
		}

		//calculate the limits and give them to the limits component
		calculateLimits();
	}

	/**
	 * If the numberTries and tolerance attributes have been set then repeatedly tries to move this Scannable until the
	 * position is within the tolerance range.
	 *
	 * @see gda.device.scannable.ScannableBase#moveTo(java.lang.Object)
	 */
	@Override
	public void moveTo(Object position) throws DeviceException {

		if (this.numberTries <= 1) {
			super.moveTo(position);
		} else {

			int numberAttempts = 0;

			//note current value
			Quantity initialPositionQuantity = getCurrentPosition();
			double initialPositionUserUnits = unitsComponent.convertObjectToUserUnitsAssumeUserUnits(initialPositionQuantity);

			try {
				//calculate motor targets based on starting positions
				Quantity[] targets = calculateTargets(position);
				// loop for numberTries times until current position is within tolerance of the target
				while (numberAttempts < this.numberTries && !isAt(position)) {
					numberAttempts++;
					moveJaws(targets);
					try {
						this.waitWhileBusy(retryTimeout);
					} catch (DeviceException e) {
						// call a halt to the motors as something's got stuck
//						if (firstJaw instanceof EpicsMotor && secondJaw instanceof EpicsMotor){
//							((EpicsMotor)firstJaw).stopGo(); //better method for epics motors
//							((EpicsMotor)secondJaw).stopGo();
//						} else {
							firstJaw.stop();
							secondJaw.stop();
//						}
						//but there might be a timeout waiting for the stop to work! Throw this exception.
						this.waitWhileBusy(retryTimeout);
					}
				}
			} catch (Exception e) {
				throw new DeviceException("Exception while operating " + getName() + ". This may have moved the slit centre. The initial centre position was  " + initialPositionUserUnits + ". ", e);
			}

			// if tried too many times then throw an exception
			if (numberAttempts >= this.numberTries) {
				throw new DeviceException("Move failed after " + this.numberTries + " attempts");
			}
		}
	}

	protected double[] getLimits(
			Quantity firstJawMin,
			Quantity firstJawMax,
			Quantity secondJawMin,
			Quantity secondJawMax)
	{
		Unit<?> units = unitsComponent.getUserUnit();
		double minimum = QuantityFactory.createFromObject(firstJawMin.plus(secondJawMin).divide(2.0),units).getAmount();
		double maximum = QuantityFactory.createFromObject(firstJawMax.plus(secondJawMax).divide(2.0),units).getAmount();
		return new double[]{ minimum, maximum};
	}

	protected void calculateLimits() {
		try {
			Double firstLowerLimit=null;
			Double firstUpperLimit=null;
			Double secondLowerLimit=null;
			Double secondUpperLimit=null;
			{
				Double[] limits = ScannableMotionBase.getInputLimits(firstJaw,0);
				if( limits != null){
					firstLowerLimit = limits[0];
					firstUpperLimit = limits[1];
				}
			}
			{
				Double[] limits = ScannableMotionBase.getInputLimits(secondJaw,0);
				if( limits != null){
					secondLowerLimit = limits[0];
					secondUpperLimit = limits[1];
				}
			}
			if( firstLowerLimit != null || firstUpperLimit != null ||
					secondLowerLimit != null || secondUpperLimit != null	)
			{
				if( firstLowerLimit == null  || firstUpperLimit == null ||
						secondLowerLimit == null || secondUpperLimit == null	){
					throw new Exception("Enable to set limuts for one of the dependent limits is null");
				}
				Unit<?> firstJawUnits = QuantityFactory.createUnitFromString(firstJaw.getUserUnits());
				Unit<?> secondJawUnits = QuantityFactory.createUnitFromString(secondJaw.getUserUnits());
				Quantity firstJawMin = QuantityFactory.createFromObject(firstLowerLimit, firstJawUnits);
				Quantity firstJawMax = QuantityFactory.createFromObject(firstUpperLimit, firstJawUnits);
				Quantity secondJawMin = QuantityFactory.createFromObject(secondLowerLimit, secondJawUnits);
				Quantity secondJawMax = QuantityFactory.createFromObject(secondUpperLimit, secondJawUnits);
				double[] limits = getLimits(firstJawMin, firstJawMax, secondJawMin, secondJawMax);
				setLowerGdaLimits(limits[0]);
				setUpperGdaLimits(limits[1]);
			}
		} catch (Exception e) {
			logger.error("exception while fetching limits in {}", getName(), e);
		}
	}


	@Override
	public String checkPositionValid(Object illDefinedPosObject) throws DeviceException {
		// perform the calculation
		Quantity[] targets = calculateTargets(illDefinedPosObject);

		// move the Scannables
		String reason = firstJaw.checkPositionValid(targets[0]);
		if (reason != null){
			return reason;
		}
		reason = secondJaw.checkPositionValid(targets[1]);
		if (reason != null){
			return reason;
		}
		return null;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		throwExceptionIfInvalidTarget(position);

		// perform the calculation
		Quantity[] targets = calculateTargets(position);
		moveJaws(targets);
	}

	private void moveJaws(Quantity[] targets) throws DeviceException{
		// move the Scannables
		firstJaw.asynchronousMoveTo(targets[0]);
		secondJaw.asynchronousMoveTo(targets[1]);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// return position as a double
		return unitsComponent.convertObjectToUserUnitsAssumeUserUnits(getCurrentPosition());
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
	protected Quantity[] calculateTargets(Object position) throws DeviceException {
		Quantity currentGap = getCurrentGap();
		// convert what is supplied to Quantity in user units
		Quantity target = QuantityFactory.createFromObject(position, this.unitsComponent.getUserUnit());
		// perform the calculation
		Quantity[] targets = new Quantity[2];
		targets[0] = target.plus(currentGap.divide(2));
		targets[1] = target.minus(currentGap.divide(2));
		return targets;
	}

	/**
	 * what is the current position as a user unit quantity?
	 *
	 * @return Quantity
	 * @throws DeviceException
	 */
	protected Quantity getCurrentPosition() throws DeviceException {
		// get the current positions in this objects user units
		Quantity p1 = QuantityFactory.createFromObject(firstJaw.getPosition(), QuantityFactory
				.createUnitFromString(this.firstJaw.getUserUnits()));
		Quantity p2 = QuantityFactory.createFromObject(secondJaw.getPosition(), QuantityFactory
				.createUnitFromString(this.secondJaw.getUserUnits()));

		// determine position as a quantity
		Quantity position;
		position = (p1.plus(p2)).divide(2.0);
		return position;
	}

	/**
	 * what is the current position as a user unit quantity?
	 *
	 * @return Quantity
	 * @throws DeviceException
	 */
	protected Quantity getCurrentGap() throws DeviceException {
		// get the current positions in this objects user units
		Quantity p1 = QuantityFactory.createFromObject(firstJaw.getPosition(), QuantityFactory
				.createUnitFromString(this.firstJaw.getUserUnits()));
		Quantity p2 = QuantityFactory.createFromObject(secondJaw.getPosition(), QuantityFactory
				.createUnitFromString(this.secondJaw.getUserUnits()));

		// determine position as a quantity
		Quantity position;
		position = p1.minus(p2).abs(); // have to use abs as -- does not make a positive!

		return position;
	}

	@Override
	public void stop() throws DeviceException {
		this.firstJaw.stop();
		this.secondJaw.stop();
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		// fan out move complete messages
		if (changeCode == ScannableStatus.IDLE) {
			notifyIObservers(this, changeCode);
		}
	}

}
