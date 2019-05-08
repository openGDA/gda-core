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

import static java.math.RoundingMode.HALF_EVEN;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;
import org.python.core.PyTuple;

import gda.util.QuantityFactory;

/**
 * Some functions for converting the the objects used as positions by Scannables.
 */
public final class PositionConvertorFunctions {

	/**
	 * Number of decimal places to round estimated values of {@link Amount}s
	 */
	private static final int ROUNDING_FACTOR = 12;

	private PositionConvertorFunctions() {
		// Prevent Instances
	}

	/**
	 * Converts an object to an object array. If the object is an array it is caste directly to an array, otherwise it
	 * is put into a single element array. No length checking is performed.
	 *
	 * @param object
	 * @return definitely an object array.
	 */
	public static Object[] toObjectArray(Object object) {

		if (object == null) {
			return null;
		}

		// will capture any non-primitive array
		if (object instanceof Object[]) {
			return (Object[]) object;
		}

		if (object instanceof PyString) { // PyString is a type of PySequence, so we need a special case
			return new Object[] { object };
		}

		if (object instanceof PySequence) {
			int length = ((PySequence) object).__len__();
			Object[] objectArray = new Object[length];
			for (int i = 0; i < length; i++) {
				Object item = ((PySequence) object).__finditem__(i);
				if (!(item instanceof PyNone)){
					objectArray[i] = item;
				}
			}
			return objectArray;
		}

		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			return list.toArray(new Object[] {});
		}

		if (object.getClass().isArray()) {
			int length = Array.getLength(object);
			Object[] objectArray = new Object[length];
			for (int i = 0; i < length; i++) {
				Object item = Array.get(object, i);
				if (!(item instanceof PyNone)){
					objectArray[i] = item;
				}
			}
			return objectArray;
		}

		// The object is not an array of understandable form. Assume it is a single element
		return new Object[] { object };

	}

	/**
	 * Attempts to convert an object array to the targetObject's container type. Returns a PyTuple if the target is a
	 * PyTuple. Returns a PyList if the target is any other PySequence (includes PyList, but by special exception, not
	 * PyString). Returns an Object[] if the target is a List or array. Otherwise assumes the target was a single
	 * element and returns the first element of objectArray.
	 *
	 * @param objectArray
	 * @param targetObject
	 * @return object
	 */
	public static Object toParticularContainer(Object[] objectArray, Object targetObject) {

		if (objectArray == null) {
			return null;
		}

		if (targetObject instanceof PyString) { // PyString is a type of PySequence, so we need a special case
			return objectArray[0];
		}

		if (targetObject instanceof PyTuple) {
			try {
				PyObject[] pyObjectArray = toPyObjectArray(objectArray);
				return new PyTuple(pyObjectArray, false);
			} catch (IllegalArgumentException e) {
				return objectArray;
			}
		}

		if (targetObject instanceof PySequence) {
			try {
				PyObject[] pyObjectArray = toPyObjectArray(objectArray);
				return new PyList(pyObjectArray);
			} catch (IllegalArgumentException e) {
				return objectArray;
			}
		}

		if (targetObject instanceof List<?>) {
			return objectArray;
		}

		if (targetObject.getClass().isArray()) {
			return objectArray;
		}

		// The object is not an array of understandable form. Assume it is a single element
		// TODO at risk of breaking code, should check that the objectArray has only one element before doing this and
		// throw exception otherwise.
		return objectArray[0];

	}

	public static PyObject toPyObject(Object object) {

		if (object == null) {
			return null;
		}

		if (object instanceof PyObject) {
			return (PyObject) object;
		}

		if (object instanceof String) {
			return new PyString((String) object);
		}

		if (object instanceof Float) {
			return new PyFloat(((Float) object).floatValue());
		}

		if (object instanceof Double) {
			return new PyFloat(((Double) object).doubleValue());
		}

		if (object instanceof Number) {
			return new PyInteger(((Number) object).intValue());
		}

		throw new IllegalArgumentException("Could not convert " + object.toString() + " to a PyObject.");

	}

	public static PyObject[] toPyObjectArray(Object[] objectArray) {
		PyObject[] pyObjectArray = new PyObject[objectArray.length];
		for (int i = 0; i < objectArray.length; i++) {
			pyObjectArray[i] = toPyObject(objectArray[i]);
		}
		return pyObjectArray;
	}

	/**
	 * Returns the length of the Array/List that the object would be coerced into.
	 *
	 * @param object
	 * @return length
	 */
	public static int length(Object object) {
		return toObjectArray(object).length;
	}

	/**
	 * Converts an array to an object. If the array has only one element then this element is returned otherwise the
	 * array is simply down-casted to an object.
	 *
	 * @param objectArray
	 * @return either an object, or an object array.
	 */
	public static Object toObject(Object[] objectArray) {

		if (objectArray == null) {
			return null;
		}

		if (objectArray.length == 1) {
			return objectArray[0];
		}
		return objectArray; // as Object!
	}

	/**
	 * Attempts to convert and Object to a Double. The Object may be null, a String, a Number, any PyObject coercable to
	 * Double, or a Quantity (where the Amount will be taken).
	 * <p>
	 * Note: May throw various Unchecked exceptions!
	 *
	 * @param object
	 * @return a Double
	 */
	@SuppressWarnings("unchecked")
	public static Double toDouble(Object object) {

		try {
			if (object == null) {
				return null;
			} else if (object instanceof String) {
				return Double.parseDouble((String) object);
			} else if (object instanceof Number) {
				return ((Number) object).doubleValue();
			} else if (object instanceof PyString) {
				return ((PyString) object).atof();
			} else if (object instanceof PyObject) {
				return (Double) ((PyObject) object).__tojava__(Double.class);
			} else if (object instanceof Amount) {
				return roundEstimatedValue(((Amount<? extends Quantity>) object).getEstimatedValue());
			}
		} catch (PyException | NumberFormatException ex) {
			// Ignore this error and throw generic error below
		}

		throw new IllegalArgumentException("Could not convert " + object.toString() + " to a double.");
	}

	/**
	 * Converts an array of Objects to an array of Doubles if possible.
	 *
	 * @param objectArray
	 */
	public static Double[] toDoubleArray(Object[] objectArray) {

		if (objectArray == null) {
			return null;
		}

		Double[] doubleArray = new Double[objectArray.length];

		for (int i = 0; i < objectArray.length; i++) {
			doubleArray[i] = toDouble(objectArray[i]);
		}
		return doubleArray;
	}

	/**
	 * Converts an Object List to a Double array if possible.
	 *
	 * @param objectList
	 * @return a Double array
	 */
	public static Double[] toDoubleArray(List<Object> objectList) {

		return toDoubleArray(objectList.toArray());

	}

	//

	/**
	 * Converts an Object to a Double array if possible. Uses toDouble() for conversion.
	 *
	 * @param objectArray
	 * @return A Double array
	 */
	public static Double[] toDoubleArray(Object objectArray) {

		if (objectArray == null) {
			return null;
		}
		return toDoubleArray(toObjectArray(objectArray));
	}

	public static Amount<? extends Quantity>[] toQuantityArray(final Object[] objectArray, final Unit<?> targetUnit) {
		if (objectArray == null) {
			return null;
		}
		Amount<? extends Quantity>[] quantityArray = new Amount<?>[objectArray.length];
		for (int i = 0; i < objectArray.length; i++) {
			quantityArray[i] = toQuantity(objectArray[i], targetUnit);
		}
		return quantityArray;
	}

	public static Amount<? extends Quantity>[] toQuantityArray(final Amount<? extends Quantity>[] quantityArray, final Unit<?> targetUnit) {
		if (quantityArray == null) {
			return null;
		}
		Amount<? extends Quantity>[] targetQuantityArray = new Amount<?>[quantityArray.length];
		for (int i = 0; i < quantityArray.length; i++) {
			targetQuantityArray[i] = (quantityArray[i]==null) ? null : quantityArray[i].to(targetUnit);
		}
		return targetQuantityArray;
	}

	public static Integer toInteger(Object object) {

		if (object == null) {
			return null;
		}
		if (object instanceof String) {
			return Integer.parseInt((String) object);
		}
		if (object instanceof Number) {
			return ((Number) object).intValue();
		}
		if (object instanceof PyString) {
			return ((PyString) object).atoi();
		}
		if (object instanceof PyObject) {
			return (Integer) ((PyObject) object).__tojava__(Integer.class);
		}
		if (object instanceof Amount) {
			return (int) ((Amount<?>) object).getEstimatedValue();
		}

		throw new IllegalArgumentException("Could not convert " + object.toString() + " to an integer.");
	}

	/**
	 * Converts an array of Objects to an array of Integers if possible.
	 *
	 * @param objectArray
	 */
	public static Integer[] toIntegerArray(Object[] objectArray) {

		if (objectArray == null) {
			return null;
		}
		Integer[] integerArray = new Integer[objectArray.length];
		for (int i = 0; i < objectArray.length; i++) {
			integerArray[i] = toInteger(objectArray[i]);
		}
		return integerArray;
	}

	public static Integer[] toIntegerArray(Object objectArray) {

		if (objectArray == null) {
			return null;
		}
		return toIntegerArray(toObjectArray(objectArray));
	}


	public static <Q extends Quantity> Amount<Q> toQuantity(final Object object, final Unit<Q> targetUnit) {
		if (object == null) {
			return null;
		}

		if (object instanceof Amount) {
			@SuppressWarnings("unchecked")
			final Amount<? extends Quantity> amount = (Amount<? extends Quantity>) object;
			return amountToQuantity(amount, targetUnit);
		}

		if (object instanceof String) {
			return stringToQuantity((String) object, targetUnit);
		}

		if (object instanceof PyString) {
			return stringToQuantity(((PyString) object).toString(), targetUnit);
		}

		// Assume it is parseable to double. toDouble throws an IllegalArgumentException if it canot parse object
		return Amount.valueOf(toDouble(object), targetUnit);
	}

	private static <Q extends Quantity> Amount<Q> stringToQuantity(final String amountString, final Unit<Q> targetUnit) {
		final Amount<? extends Quantity> amount = QuantityFactory.createFromString(amountString);
		if (amount == null) {
			throw new IllegalArgumentException("Could not parse string '" + amountString + "' to a quantity.");
		}
		return amountToQuantity(amount, targetUnit);
	}

	private static <Q extends Quantity> Amount<Q> amountToQuantity(final Amount<? extends Quantity> amount, final Unit<Q> targetUnit) {
		if (amount.getUnit() == Dimensionless.UNIT) {
			return Amount.valueOf(amount.getEstimatedValue(), targetUnit);
		}
		return amount.to(targetUnit);
	}

	public static Double[] toAmountArray(final Amount<? extends Quantity>[] quantityArray) {
		final Double[] ammountArray = new Double[quantityArray.length];
		for (int i = 0; i < quantityArray.length; i++) {
			ammountArray[i] = (quantityArray[i]==null) ? null : roundEstimatedValue(quantityArray[i].getEstimatedValue());
		}
		return ammountArray;
	}

	private static double roundEstimatedValue(double estimatedValue) {
		return BigDecimal.valueOf(estimatedValue).setScale(ROUNDING_FACTOR, HALF_EVEN).doubleValue();
	}

}
