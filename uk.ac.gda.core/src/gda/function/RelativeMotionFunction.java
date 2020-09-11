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

package gda.function;

import java.util.function.Function;

import javax.measure.Quantity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.util.QuantityFactory;

/**
 * Function to couple the relative motion of two scannables, not their absolute positions.
 */
public class RelativeMotionFunction<T extends Quantity<T>, R extends Quantity<R>> extends FindableConfigurableBase implements Function<Quantity<T>, Quantity<R>> {
	private static final Logger logger = LoggerFactory.getLogger(RelativeMotionFunction.class);

	/**
	 * The scannable whose target position is specified by the caller
	 */
	private ScannableMotionUnits primaryScannable;

	/**
	 * The scannable whose target position is to be calculated by this function
	 */
	private ScannableMotionUnits secondaryScannable;

	/**
	 * Function to calculate the movement of {@link #secondaryScannable} from the movement of {@link #primaryScannable}
	 */
	private Function<Quantity<T>, Quantity<R>> couplingFunction;

	@Override
	public void configure() throws FactoryException {
		if (primaryScannable == null) {
			throw new FactoryException("Primary scannable must not be null");
		}
		if (secondaryScannable == null) {
			throw new FactoryException("Secondary scannable must not be null");
		}
		if (couplingFunction == null) {
			throw new FactoryException("Coupling function must not be null");
		}
		setConfigured(true);
	}

	@Override
	public Quantity<R> apply(Quantity<T> t) {
		try {
			final Quantity<T> primaryInitialPosition = getPositionQuantity(primaryScannable);
			final Quantity<R> secondaryInitialPosition = getPositionQuantity(secondaryScannable);

			final Quantity<T> primaryMovement = t.subtract(primaryInitialPosition);
			final Quantity<R> secondaryMovement = couplingFunction.apply(primaryMovement);
			final Quantity<R> secondaryTargetPosition = secondaryInitialPosition.add(secondaryMovement);

			logger.debug("Moving primary scannable from {} to {}: secondary scannable from {} to {}",
					primaryInitialPosition, t, secondaryInitialPosition, secondaryTargetPosition);
			return QuantityFactory.createFromObject(secondaryTargetPosition, secondaryScannable.getHardwareUnitString());
		} catch (DeviceException e) {
			logger.error("Error getting device position", e);
		} catch (Exception e) {
			logger.error("Error applying conversion function", e);
		}
		return null;
	}

	private <Q extends Quantity<Q>> Quantity<Q> getPositionQuantity(ScannableMotionUnits device) throws DeviceException {
		return QuantityFactory.createFromObject(device.getPosition(), device.getHardwareUnitString());
	}

	public void setPrimaryScannable(ScannableMotionUnits primaryScannable) {
		this.primaryScannable = primaryScannable;
	}

	public void setSecondaryScannable(ScannableMotionUnits secondaryScannable) {
		this.secondaryScannable = secondaryScannable;
	}

	public void setCouplingFunction(Function<Quantity<T>, Quantity<R>> couplingFunction) {
		this.couplingFunction = couplingFunction;
	}
}
