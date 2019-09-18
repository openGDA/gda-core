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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.QuantityFactory;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Couples together the movement of several other Scannables.
 * <p>
 * This scannable returns its position as the array of positions of the sub-Scannables, but its movement is defined by a
 * single number. The Scannables' movements are coupled to this number by functions.
 * <p>
 * There must be a Function per Scannable, else an exception will be thrown during the configure() method. Note that
 * currently all Functions return a single value, so all Scannables used here must have one input value.
 */
@ServiceInterface(ScannableMotionUnits.class)
public class CoupledScannable extends ScannableMotionUnitsBase {

	private static final Logger logger = LoggerFactory.getLogger(CoupledScannable.class);

	private List<Scannable> theScannables = new ArrayList<>();
	private List<Function<Amount<? extends Quantity>, Amount<? extends Quantity>>> theFunctions = new ArrayList<>();
	private List<String> scannableNames;

	/**
	 * Keeps track of whether each of the underlying scannables is moving, if this scannable is moved.
	 */
	private boolean[] scannablesMoving;

	/**
	 * This must be called after all Scannables and Functions added {@inheritDoc}
	 *
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		// fill the array of Scannables
		final Finder finder = Finder.getInstance();
		if (scannableNames != null && !scannableNames.isEmpty()) {
			for (String name : scannableNames) {
				final Scannable scannable = finder.find(name);

				if (scannable == null) {
					logger.warn("Error during configure of {}: scannable {} could not be found!", getName(), name);
				}
				theScannables.add(scannable);
			}
		}

		// If functions are defined (which is preferred), check that there are the same number of functions & scannables
		if (theFunctions.isEmpty()) {
			throw new FactoryException("No functions defined in '" + getName() + "'. Use IdentityFunction if no value transformation is required");
		} else if (theScannables.size() != theFunctions.size()) {
			throw new FactoryException(getName()
					+ " cannot complete configure() as arrays of Scannables and Functions are of different lengths");
		}

		// Register for updates with each scannable
		for (Scannable scannable : theScannables) {
			scannable.configure();
			scannable.addIObserver(this::handleUpdate);
		}

		// set up the arrays of input and extra names properly
		this.inputNames = new String[] { getName() };
		this.extraNames = new String[0];
		this.outputFormat = new String[] { "%5.5g" };
		scannablesMoving = new boolean[theScannables.size()];

		if (!unitsComponent.unitHasBeenSet()) {
			try {
				final Scannable first = theScannables.get(0);
				if (first instanceof ScannableMotionUnits) {
					this.setUserUnits(((ScannableMotionUnits)first).getUserUnits());
				}
			} catch (DeviceException e) {
				logger.error("Error setting the hardware units", e);
			}
		}

		setConfigured(true);
	}

	/**
	 * @return all the Scannables this operates
	 */
	public List<Scannable> getScannables() {
		return theScannables;
	}

	/**
	 * Sets the scannables this object uses. Use this instead of multiple calls to addScannable.
	 *
	 * @param theScannables
	 */
	public void setScannables(List<Scannable> theScannables) {
		this.theScannables = new ArrayList<>(theScannables);
	}

	/**
	 * Sets the functions in this coupled scannable.
	 *
	 * @param functions
	 *            the functions
	 */
	public void setFunctions(List<Function<Amount<? extends Quantity>, Amount<? extends Quantity>>> functions) {
		this.theFunctions = new ArrayList<>(functions);
	}

	/**
	 * @return all the functions this uses.
	 */
	public List<Function<Amount<? extends Quantity>, Amount<? extends Quantity>>> getFunctions() {
		return theFunctions;
	}

	/**
	 * @return the names of the Scannables this uses
	 */
	public List<String> getScannableNames() {
		return scannableNames;
	}

	/**
	 * Sets the scannable names for this coupled scannable.
	 *
	 * @param scannableNames
	 *            the scannable names
	 */
	public void setScannableNames(List<String> scannableNames) {
		logger.warn("Call to setScannableNames() in '{}' is deprecated. This function will be removed in GDA 9.11", getName());
		logger.warn("Use setScannables() instead and pass in actual Scannable objects");
		this.scannableNames = new ArrayList<>(scannableNames);
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		asynchronousMoveTo(position);
	}


	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		throwExceptionIfInvalidTarget(position);

		// if no functions, so just a fan out
		if (theFunctions.isEmpty()) {
			moveUnderlyingScannables(Collections.nCopies(theScannables.size(), position));
			return;
		}

		// loop through all functions, calculate and send command to Scannable
		final List<Amount<? extends Quantity>> targets = new ArrayList<>(theFunctions.size());
		final Unit<? extends Quantity> userUnits = QuantityFactory.createUnitFromString(getUserUnits());

		for (int i = 0; i < theFunctions.size(); i++) {
			// If scannable cannot use units, treat position as a dimensionless number
			final Unit<? extends Quantity> unit = (theScannables.get(i) instanceof ScannableMotionUnits) ? userUnits : Unit.ONE;
			targets.add(theFunctions.get(i).apply(QuantityFactory.createFromObject(position, unit)));
		}

		// if get here without an exception then perform the moves
		moveUnderlyingScannables(targets);
	}

	private void moveUnderlyingScannables(List<? extends Object> targets) throws DeviceException {
		Arrays.fill(scannablesMoving, true);
		for (int i = 0; i < theScannables.size(); i++) {
			theScannables.get(i).asynchronousMoveTo(targets.get(i));
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		// replicate behaviour of old DOF. This is probably not ideal...
		final Scannable first = theScannables.get(0);
		final Object posAmount =  ScannableUtils.getCurrentPositionArray(first)[0];
		if (first instanceof ScannableMotionUnits) {
			final Amount<? extends Quantity> sourcePositionQuantity = QuantityFactory.createFromObject(posAmount, QuantityFactory
					.createUnitFromString(((ScannableMotionUnits)first).getUserUnits()));
			return sourcePositionQuantity.to(unitsComponent.getUserUnit()).getEstimatedValue();
		}
		return posAmount;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// replicate behaviour of old DOF. This is probably not ideal...
		return getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for (Scannable thisScannable : theScannables) {
			if (thisScannable.isBusy()) {
				return true;
			}
		}
		return false;
	}

	private void handleUpdate(Object theObserved, Object changeCode) {

		boolean scannableReady = true;
		if (changeCode instanceof ScannableStatus) {

			final ScannableStatus scannableStatus = ((ScannableStatus) changeCode);
			if (scannableStatus == ScannableStatus.BUSY ||  scannableStatus== ScannableStatus.FAULT) {
				notifyIObservers(this, scannableStatus);
			} else if (scannableStatus == ScannableStatus.IDLE) {
				for (int i = 0; i < theScannables.size(); i++) {
					if (theScannables.get(i).equals(theObserved)) {
						scannablesMoving[i] = false;
					}
					if (scannablesMoving[i]) {
						scannableReady = false;
					}
				}
				if (scannableReady) {
					notifyIObservers(this, scannableStatus);
				}
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		for (Scannable thisScannable : theScannables) {
			thisScannable.stop();
		}

	}

	@Override
	public String toFormattedString() {
		return ScannableUtils.formatScannableWithChildren(this, theScannables, true);
	}
}
