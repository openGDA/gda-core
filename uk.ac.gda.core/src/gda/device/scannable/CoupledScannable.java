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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.function.Function;
import gda.observable.IObserver;
import gda.util.QuantityFactory;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Couples together the movement of several other Scannables.
 * <p>
 * This scannable returns its position as the array of positions of the sub-Scannables, but its movement is defined by a
 * single number. The Scannables' movements are coupled to this number by functions.
 * <p>
 * There must be a Function per Scannable, else an exception will be thrown during the configure() method. Note that
 * currently all Functions return a single value, so all Scannables used here must have one input value.
 */
public class CoupledScannable extends ScannableMotionUnitsBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(CoupledScannable.class);

	private Scannable[] theScannables = new Scannable[0];
	private Function[] theFunctions = new Function[0];
	private String[] scannableNames;

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

		// fill the array of Scannables
		Finder finder = Finder.getInstance();
		if (scannableNames != null && scannableNames.length > 0) {
			for (String name : scannableNames) {
				Findable scannable = finder.find(name);

				if (scannable == null || !(scannable instanceof Scannable)) {
					logger.warn("Error during configure of " + name + ": scannable " + name
							+ " could not be found!");
				}
				theScannables = (Scannable[]) ArrayUtils.add(theScannables, finder.find(name));
			}
		}

		// check that the arrays are the same length
		if (theFunctions.length != 0 && theScannables.length != theFunctions.length) {
			throw new FactoryException(getName()
					+ " cannot complete configure() as arrays of Scannables and Functions are of different lengths");
		}
		for (Scannable scannable : theScannables) {
			scannable.addIObserver(this);
		}

		// set up the arrays of input and extra names properly
		this.inputNames = new String[] { getName() };
		this.extraNames = new String[0];
		this.outputFormat = new String[] { "%5.5g" };
		scannablesMoving = new boolean[theScannables.length];
		if(!unitsComponent.unitHasBeenSet()){
			try {
				Scannable first = theScannables[0];
				if( first instanceof ScannableMotionUnits){
					this.setUserUnits(((ScannableMotionUnits)first).getUserUnits());
				}
			} catch (DeviceException e) {
				logger.error("Error setting the hardware units", e);
			}
		}

		setConfigured(true);
	}

	/**
	 * Adds a Scannable.
	 *
	 * @param newScannable
	 */
	public void addScannable(Scannable newScannable) {
		theScannables = (Scannable[]) ArrayUtils.add(theScannables, newScannable);
	}

	/**
	 * @return all the Scannables this operates
	 */
	public Scannable[] getScannables() {
		return theScannables;
	}

	/**
	 * Sets the scannables this object uses. Use this instead of multiple calls to addScannable.
	 *
	 * @param theScannables
	 */
	public void setScannables(Scannable[] theScannables) {
		this.theScannables = theScannables;
	}

	/**
	 * Adds a function to the list
	 *
	 * @param newFunction
	 */
	public void addFunction(Function newFunction) {
		if (!ArrayUtils.contains(theFunctions, newFunction)) {
			theFunctions = (Function[]) ArrayUtils.add(theFunctions, newFunction);
		}
	}

	/**
	 * Sets the functions in this coupled scannable.
	 *
	 * @param functions
	 *            the functions
	 */
	public void setFunctions(List<Function> functions) {
		this.theFunctions = new Function[0];
		for (Function func : functions) {
			addFunction(func);
		}
	}

	// Setter that takes the same type as the return type of the getter (for Spring)
	public void setFunctions(Function[] functions) {
		setFunctions(Arrays.asList(functions));
	}

	/**
	 * @return all the functions this uses.
	 */
	public Function[] getFunctions() {
		return theFunctions;
	}

	/**
	 * @return the names of the Scannables this uses
	 */
	public String[] getScannableNames() {
		return this.scannableNames;
	}

	/**
	 * @param newScannableName
	 *            The Scannable name to add.
	 */
	public void addScannableName(String newScannableName) {
		if (!ArrayUtils.contains(scannableNames, newScannableName)) {
			scannableNames = (String[]) ArrayUtils.add(scannableNames, newScannableName);
		}
	}

	/**
	 * Sets the scannable names for this coupled scannable.
	 *
	 * @param scannableNames
	 *            the scannable names
	 */
	public void setScannableNames(List<String> scannableNames) {
		this.scannableNames = new String[0];
		for (String name : scannableNames) {
			addScannableName(name);
		}
	}

	/**
	 * Alternate setter to match the getter
	 *
	 * @param scannableNames
	 */
	public void setScannableNames(String[] scannableNames) {
		this.scannableNames = new String[0];
		for (String name : scannableNames) {
			addScannableName(name);
		}

	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		asynchronousMoveTo(position);
	}


	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		throwExceptionIfInvalidTarget(position);

		// if no functions, so just a fan out
		if (theFunctions.length == 0) {
			Object[] targets = new Object[theScannables.length];
			Arrays.fill(targets, position);
			moveUnderlyingScannables(targets);
			return;
		}

		// loop through all functions, calculate and send command to Scannable
		Quantity targets[] = new Quantity[theFunctions.length];
		for (int i = 0; i < theFunctions.length; i++) {

			// need to generate the correct Quantity object
			// if the scannable can use units:
			if (theScannables[i] instanceof ScannableMotionUnits) {
				Unit<? extends Quantity> targetUnit = QuantityFactory
						.createUnitFromString(((ScannableMotionUnits) theScannables[i]).getUserUnits());
				targets[i] = theFunctions[i].evaluate(QuantityFactory.createFromObject(position, targetUnit));
			}
			// else treat position as a dimensionless number
			else {
				targets[i] = theFunctions[i].evaluate(QuantityFactory.createFromObject(position, Unit.ONE));
			}
		}

		// if get here without an exception then perform the moves
		moveUnderlyingScannables(targets);
	}

	private void moveUnderlyingScannables(Object[] targets) throws DeviceException {
		Arrays.fill(scannablesMoving, true);
		for (int i = 0; i < theScannables.length; i++) {
			theScannables[i].asynchronousMoveTo(targets[i]);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		// replicate behaviour of old DOF. This is probably not ideal...
		Scannable first = theScannables[0];
		Object posAmount =  ScannableUtils.getCurrentPositionArray(theScannables[0])[0];
		if( first instanceof ScannableMotionUnits ){
			Quantity sourcePositionQuantity = QuantityFactory.createFromObject(posAmount, QuantityFactory
					.createUnitFromString(((ScannableMotionUnits)first).getUserUnits()));
			return (sourcePositionQuantity.to(unitsComponent.getUserUnit())).getAmount();
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

	@Override
	public void update(Object theObserved, Object changeCode) {

		boolean scannableReady = true;
		if ( changeCode instanceof ScannableStatus) {

			int scanStatus = ((ScannableStatus) changeCode).getStatus();
			if(scanStatus == ScannableStatus.BUSY ||  scanStatus== ScannableStatus.FAULT)
				notifyIObservers(this,new ScannableStatus(getName(), scanStatus));


			else if(scanStatus == ScannableStatus.IDLE)
			{
				for (int i =0 ; i< theScannables.length; i++)
				{
					if(theScannables[i].equals(theObserved))
						scannablesMoving[i] = false;
					if(scannablesMoving[i])
						scannableReady = false;
				}
				if(scannableReady)
					notifyIObservers(this,new ScannableStatus(getName(), scanStatus));
			}

		}
	}

	@Override
	public void stop() throws DeviceException {
		for (Scannable thisScannable : theScannables) {
			thisScannable.stop();
		}

	}


}
