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

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.component.UnitsComponent;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.util.QuantityFactory;
import gda.util.converters.IQuantityConverter;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Replacement for CombinedDOF. This Scannable operates a ScannableMotionUnits scannable via a
 * LookupTableQuantityConverter: the table this converter uses is used to convert between the 'user' position and the
 * position fetched from / commanded to the underlying Scannable.
 * <p>
 * The underlying Scannable should only have a single value input (getInputNames().length ==1);
 */
@ServiceInterface(ScannableMotionUnits.class)
public class ConvertorScannable extends ScannableMotionUnitsBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ConvertorScannable.class);

	private ScannableMotionUnits theScannable;
	private IQuantityConverter theConvertor;
	private String convertorName = "";
	private String theScannableName;
	private String convertorUnitString;

	private Amount<? extends Quantity> lastmoveTo_internalPositionQuantity = null;

	public ConvertorScannable() {
		unitsComponent = new UnitsComponentForConvertorScannable();
	}

	@Override
	public void configure() {
		// find the scannable objects
		if (theScannable == null) {
			theScannable = Finder.getInstance().find(theScannableName);
		}
		this.inputNames = new String[] { getName() };

		// find the convertor object
		if (theConvertor == null && convertorName != null) {
			theConvertor = Finder.getInstance().find(convertorName);
		}
		configureHardwareUnits();
		theScannable.addIObserver(this);
		setConfigured(true);
	}

	private void configureHardwareUnits() {
		try {
			this.setHardwareUnitString(theConvertor.getAcceptableSourceUnits().get(0));
		} catch (DeviceException e) {
			logger.error("Error setting the hardware units - try setting convertorUnitString", e);
		} // the above is a more useful error - try to see if there are any other problems 2011-05-17 JA
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		final String report = checkPositionValid(externalPosition);
		if (report != null) {
			throw new DeviceException(report);
		}
		super.asynchronousMoveTo(externalPosition);
	}

	/*
	 * If the converter does not support conversion from target to source then in rawGetPosition we need a value to
	 * return. lastmoveTo_internalPositionQuantity holds the last value in internal units that was successfully sent to
	 * the scannable.
	 */
	@Override
	public void rawAsynchronousMoveTo(Object internalPositionAmount) throws DeviceException {
		try {
			// clear position so that if an exception happens the cached value is invalid
			lastmoveTo_internalPositionQuantity = null;
			final Amount<? extends Quantity> internalPositionQuantity = convertInternalPositionAmountToInternalQuantity(internalPositionAmount);
			theScannable.asynchronousMoveTo(internalToTarget(internalPositionQuantity));
			lastmoveTo_internalPositionQuantity = internalPositionQuantity;
		} catch (Exception e) {
			throw new DeviceException("Could not (raw)move " + getName() + ": " + e);
		}
	}

	private Amount<? extends Quantity> convertInternalPositionAmountToInternalQuantity(Object internalPositionAmount) {
		final Unit<? extends Quantity> internalUnit = unitsComponent.getHardwareUnit();
		Amount<? extends Quantity> internalQuantity = PositionConvertorFunctions.toQuantity(internalPositionAmount, internalUnit);

		// convert to a quantity in hardware units. These should be the same uses the table is expecting otheriwse the
		// converter will throw an exception
		if (convertorUnitString != null) {
			final Unit<? extends Quantity> convertorUnit = QuantityFactory.createUnitFromString(convertorUnitString);
			internalQuantity = PositionConvertorFunctions.toQuantity(internalQuantity, convertorUnit);
		}
		return internalQuantity;
	}

	private Object internalToTarget(Amount<? extends Quantity> internalPositionQuantity) throws Exception {
		try {
			return theConvertor.toTarget(internalPositionQuantity);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new Exception("Could not convert " + internalPositionQuantity.toString() + "." + theConvertor.toString(), e);
		}
	}

	/*
	 * ScannableMotion considers source as the object to be driven. Convertors consider source as the driver
	 */
	private Object sourceToInternal(Object sourcePositionAmount) throws Exception {
		// theScannable will have returned in its internal (user) units. Make this explicit.
		final Amount<? extends Quantity> sourcePositionQuantity = QuantityFactory.createFromObject(sourcePositionAmount,
				QuantityFactory.createUnitFromString(theScannable.getUserUnits()));

		final Unit<? extends Quantity> acceptableTargetUnits = Unit.valueOf(theConvertor.getAcceptableTargetUnits().get(0));
		final Amount<? extends Quantity> targetPositionInTargetUnits = sourcePositionQuantity.to(acceptableTargetUnits);

		try {
			Amount<? extends Quantity> currentPositionInHardwareUnits = theConvertor.toSource(targetPositionInTargetUnits);
			if (Double.isInfinite(currentPositionInHardwareUnits.getEstimatedValue())) {
				logger.debug("ConverterScannable.rawGetPosition. NewValue for " + getName() + " is infinite. ");
				currentPositionInHardwareUnits = Amount.valueOf(0, currentPositionInHardwareUnits.getUnit());
			}
			return currentPositionInHardwareUnits;
		} catch (Exception e) {
			throw new Exception("Could not convert " + targetPositionInTargetUnits.toString() + "." + theConvertor.toString(), e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		if (!theConvertor.handlesTtoS()) {
			if (lastmoveTo_internalPositionQuantity == null) {
				final String msg = String.format(
					"Converter for scannable '%s' does not support target → source conversion, and there is no 'last source position'",
					getName());
				logger.warn(msg);
			}
			return lastmoveTo_internalPositionQuantity;
		}
		try {
			return sourceToInternal(theScannable.getPosition());
		} catch (Exception e) {
			throw new DeviceException("Could not (raw) get position of " + getName() + ": " + e);
		}
	}

	/**
	 * Checks the ConvertorScannable's Scannable limits, and then wrapped Scannable's limits (using converted values)
	 */
	@Override
	public String checkPositionValid(Object externalPosition) throws DeviceException {
		// 1. Check the the ConvertorScannable's Scannable limits
		final String scannableLimitsMsg = super.checkPositionValid(externalPosition);
		if (scannableLimitsMsg != null) {
			return scannableLimitsMsg;
		}
		// 2. check theScannable's limits (using converted values)
		final Object internalPosition = externalToInternal(externalPosition);
		final Object targetPosition;
		try {
			targetPosition = internalToTarget(convertInternalPositionAmountToInternalQuantity(internalPosition));
		} catch (IllegalArgumentException e) {
			throw new DeviceException(e);
		} catch (Exception e) {
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
		if (arg instanceof ScannableStatus) {
			notifyIObservers(this, arg);
		}
	}

	/**
	 * A units component designed for use within a ConvertorScannable. For now the only difference is that it allows
	 * incompatible user and hardware units to be set.
	 */
	private class UnitsComponentForConvertorScannable extends UnitsComponent {
		@Override
		protected void actuallySetUserUnits(String userUnitsString) throws DeviceException {
			userUnit = Unit.valueOf(userUnitsString);
		}
	}
}