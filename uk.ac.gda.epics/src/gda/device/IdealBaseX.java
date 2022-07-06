/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.enumpositioner.EpicsPositionerLookup;
import gda.device.scannable.ScannableMotor;
import uk.ac.gda.api.remoting.ServiceInterface;


/**
 * Scannable which wraps a motor and associated multipositioner to work around motion issues
 * in K11's base x:
 *
 * <ul>
 * <li>
 * Instead of returning the real motor position, {@link #rawGetPosition()} returns the position
 * the motor would nominally be, trusting the positioner's position and a positioner reverse lookup.
 *
 * <li>
 * {@link #rawAsynchronousMoveTo(Object)} behaves as expected except that the move will be silently ignored
 * if the demand position is within deadband of current motor position.
 * </ul>
 */
@ServiceInterface(IScannableMotor.class)
public class IdealBaseX extends ScannableMotor { // NOSONAR - Java is my name; Spaghetti code is my game

	private static final Logger logger = LoggerFactory.getLogger(IdealBaseX.class);

	private final IScannableMotor motor;
	private final EnumPositioner positioner;
	private final EpicsPositionerLookup lookup;

	public IdealBaseX(IScannableMotor motor, EnumPositioner positioner, EpicsPositionerLookup lookup) {
		this.motor = motor;
		this.positioner = positioner;
		this.lookup = lookup;

		setMotor(motor.getMotor());
	}

	/**
	 * Does not move if demand position is within deadband of current position.
	 */
	@Override
	public void rawAsynchronousMoveTo(Object internalPosition) throws DeviceException {
		var current = (double) motor.getPosition();
		var tolerance = motor.getDemandPositionTolerance();
		var lowerBound = current - tolerance;
		var upperBound = current + tolerance;

		var demand = ((Number) internalPosition).doubleValue();

		if (lowerBound <= demand && demand <= upperBound) {
			logger.debug("Move ignored because demand position within tolerance of current position");
			return;
		}

		motor.asynchronousMoveTo(demand);
	}

	/**
	 * Returns an ideal position i.e. the position we would be in
	 * if the current position given by the multipositioner were inpos.
	 */
	@Override
	public Object rawGetPosition() throws DeviceException {
		String labelledPosition = positioner.getPosition().toString();
		try {
			return lookup.lookup(labelledPosition);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new DeviceException(interrupted);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

}
