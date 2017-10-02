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
import gda.device.scannable.component.UnitsComponent;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.util.QuantityFactory;
import gda.util.converters.IQuantityConverter;

/**
 * Replacement for CombinedDOF. This Scannable operates a ScannableMotionUnits scannable via a
 * LookupTableQuantityConverter: the table this converter uses is used to convert between the 'user' position and the
 * position fetched from / commanded to the underlying Scannable.
 * <p>
 * The underlying Scannable should only have a single value input (getInputNames().length ==1);
 */
public class ConvertorScannable extends ScannableMotionUnitsBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ConvertorScannable.class);

	private ScannableMotionUnits theScannable;
	private IQuantityConverter theConvertor;
	private String convertorName = "";
	private String theScannableName;
	private String convertorUnitString;

	/**
	 * Constructor for Castor
	 */
	public ConvertorScannable() {
		unitsComponent = new UnitsComponentForConvertorScannable();
	}

	/**
	 * Constructor for unit testing. If this constructor is used then the configure method should not be used to use the
	 * finder to get object references..
	 *
	 * @param name
	 * @param theScannable
	 * @param theConvertor
	 */
	public ConvertorScannable(String name, ScannableMotionUnits theScannable, IQuantityConverter theConvertor) {
		this();
		this.setName(name);
		setInputNames(new String[] {name});
		this.theScannable = theScannable;
		// set up the units component for this object based on the underlying scannable

		this.theConvertor = theConvertor;
		theScannableName = theScannable.getName();
		configureHardwareUnits();

		configured = true;
	}

	@Override
	public void configure() {

		// find the scannable objects
		if( theScannable == null){
			theScannable = Finder.getInstance().find(theScannableName);
		}
		this.inputNames = new String[] { getName() };

		// find the convertor object
		if (theConvertor == null && convertorName != null) {
			theConvertor = (IQuantityConverter) Finder.getInstance().find(convertorName);
		}
		configureHardwareUnits();
		theScannable.addIObserver(this);
		configured = true;
	}

	private void configureHardwareUnits()
	{
		try {
			this.setHardwareUnitString(theConvertor.getAcceptableSourceUnits().get(0).toString());
		} catch (DeviceException e) {
			logger.error("Error setting the hardware units - try setting convertorUnitString", e);
		} //the above is a more useful error - try to see if there are any other problems 2011-05-17 JA
	}
	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		String report = checkPositionValid(externalPosition);
		if (report != null) {
			throw new DeviceException(report);
		}
		super.asynchronousMoveTo(externalPosition);
	}

	/*
	 * If the converter does not support conversion from target to source then in rawGetPosition we need a value to return.
	 * lastmoveTo_internalPositionQuantity holds the last value in internal units that was successfully sent to the scannable.
	 */
	private Quantity lastmoveTo_internalPositionQuantity=null;
	@Override
	public void rawAsynchronousMoveTo(Object internalPositionAmount) throws DeviceException {

		try {
			//clear position so that if an exception happens the cached value is invalid
			lastmoveTo_internalPositionQuantity = null;
			Quantity internalPositionQuantity = convertInternalPositionAmountToInternalQuantity(internalPositionAmount);
			theScannable.asynchronousMoveTo(internalToTarget(internalPositionQuantity));
			lastmoveTo_internalPositionQuantity = internalPositionQuantity;
		} catch (Exception e) {
			throw new DeviceException("Could not (raw)move " + getName() + ": " + e);
		}
	}

	private Quantity convertInternalPositionAmountToInternalQuantity(Object internalPositionAmount){
		Unit<?> internalUnit = unitsComponent.getHardwareUnit();
		Quantity internalQuantity = PositionConvertorFunctions.toQuantity(internalPositionAmount, internalUnit);

		// convert to a quantity in hardware units. These should be the same uses the table is expecting otheriwse the
		// converter will throw an exception
		if (convertorUnitString != null) {
			Unit<?> convertorUnit = QuantityFactory.createUnitFromString(convertorUnitString);
			internalQuantity = PositionConvertorFunctions.toQuantity(internalQuantity, convertorUnit);
		}
		return internalQuantity;

	}
	private Object internalToTarget(Quantity internalPositionQuantity) throws Exception {
		try {
			return theConvertor.toTarget(internalPositionQuantity);
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) throw e;
			throw new Exception("Could not convert " + internalPositionQuantity.toString() + "." + theConvertor.toString(), e);
		}
	}

	/*
	 * ScannableMotion considers source as the object to be driven.
	 * Convertors consider source as the driver
	 */
	private Object sourceToInternal(Object sourcePositionAmount) throws Exception {

		// theScannable will have returned in its internal (user) units. Make this explicit.
		Quantity sourcePositionQuantity = QuantityFactory.createFromObject(sourcePositionAmount, QuantityFactory
				.createUnitFromString(theScannable.getUserUnits()));

		Quantity targetPositionInTargetUnits = sourcePositionQuantity.to(theConvertor.getAcceptableTargetUnits().get(0));

		Quantity currentPositionInHardwareUnits;
		try {
			currentPositionInHardwareUnits = theConvertor.toSource(targetPositionInTargetUnits);
			if (Double.isInfinite(currentPositionInHardwareUnits.doubleValue())) {
				logger.debug("ConverterScannable.rawGetPosition. NewValue for " + getName() + " is infinite. ");
				currentPositionInHardwareUnits = Quantity.valueOf(0, unitOf(currentPositionInHardwareUnits));
			}
		} catch (Exception e) {
			throw new Exception("Could not convert " + targetPositionInTargetUnits.toString() + "." + theConvertor.toString(), e);
		}

		return currentPositionInHardwareUnits;
	}

	/**
	 * Returns the {@link Unit} of the given {@link Quantity}.
	 */
	private static Unit<? extends Quantity> unitOf(Quantity q) {

		// Should always work, since Unit has a bounded type parameter whose
		// upper bound is Quantity
		@SuppressWarnings("unchecked")
		final Unit<? extends Quantity> u = q.getUnit();

		return u;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		if(!theConvertor.handlesTtoS()) {
			if (lastmoveTo_internalPositionQuantity == null) {
				final String msg = String.format(
					"Converter for scannable '%s' does not support target → source conversion, and there is no 'last source position'",
					getName());
				logger.warn(msg);
			}
			return lastmoveTo_internalPositionQuantity;
		}
		Object sourcePosition = theScannable.getPosition();
		try {
			return sourceToInternal(sourcePosition);
		} catch (Exception e) {
			throw new DeviceException("Could not (raw) get position of " + getName() + ": " + e);
		}
	}
	/**
	 * Checks the ConvertorScannable's Scannable limits, and then wrapped Scannable's limits (using converted values)
	 */
	@Override
	public String checkPositionValid(Object externalPosition) throws DeviceException  {

		// 1. Check the the ConvertorScannable's Scannable limits
		String scannableLimitsMsg = super.checkPositionValid(externalPosition);
		if (scannableLimitsMsg!=null) {
			return scannableLimitsMsg;
		}
		// 2. check theScannable's limits (using converted values)
		Object internalPosition = externalToInternal(externalPosition);
		Object targetPosition;
		try {
			targetPosition = internalToTarget(convertInternalPositionAmountToInternalQuantity(internalPosition));
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				throw new DeviceException(e);
			}
			throw new DeviceException("Could check position of " + getName() + ": " + e);
		}
		return theScannable.checkPositionValid(targetPosition);
	}

	@Override
	public boolean rawIsBusy() throws DeviceException {
		return theScannable.isBusy();
	}

	/**
	 * @return Returns the convertorName.
	 */
	public String getConvertorName() {
		return convertorName;
	}

	/**
	 * @param convertorName
	 *            The convertorName to set.
	 */
	public void setConvertorName(String convertorName) {
		this.convertorName = convertorName;
	}

	/**
	 * @return Returns the theScannableNames.
	 */
	public String getScannableName() {
		return theScannableName;
	}

	/**
	 * @param newScannableName
	 *            The Scannable name to add.
	 */
	public void setScannableName(String newScannableName) {
		theScannableName = newScannableName;
	}

	public ScannableMotionUnits getScannable() {
		return theScannable;
	}

	public void setScannable(ScannableMotionUnits scannable) {
		this.theScannable = scannable;
	}

	public IQuantityConverter getConvertor() {
		return theConvertor;
	}

	public void setConvertor(IQuantityConverter convertor) {
		this.theConvertor = convertor;
	}

	public void setConvertorUnitString(String convertorUnitString) {
		this.convertorUnitString = convertorUnitString;
	}

	public String getConvertorUnitString() {
		return convertorUnitString;
	}

	@Override
	public void update(Object source, Object arg) {
		if( arg instanceof ScannableStatus){
			notifyIObservers(this, arg);
		}
	}
}

/**
 * A units component designed for use within a ConvertorScannable. For now the only difference is that it allows
 * incompatible user and hardware units to be set.
 */
class UnitsComponentForConvertorScannable extends UnitsComponent {
	@Override
	public void setUserUnits(String userUnitsString) throws DeviceException {
		// TODO Auto-generated method stub
		super.setUserUnits(userUnitsString);
	}

	@Override
	public void setHardwareUnitString(String hardwareUnitString) throws DeviceException {
		// TODO Auto-generated method stub
		super.setHardwareUnitString(hardwareUnitString);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void actuallySetUserUnits(String userUnitsString) throws DeviceException {
		Quantity userUnitQuantity = QuantityFactory.createFromTwoStrings("1.0", userUnitsString);
		userUnit = userUnitQuantity.getUnit();
	}
}