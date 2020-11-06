/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.util;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;

/**
 * Class with static members used to convert between various objects
 */
public class TypeConverters {

	private TypeConverters() {
		// private constructor to prevent instantiation
	}

	/**
	 * Attempts to convert the given object into an array of doubles.
	 * @param object object to convert
	 * @return double array
	 * @throws IllegalArgumentException if the object cannot be converted into a double array
	 */
	public static double[] toDoubleArray(Object object) throws IllegalArgumentException {
		// Note: this method is similar to ScannableUtils.objectToArray, but that
		// method has many flaws and should be rewritten see DAQ-3180

		if (object instanceof double[]) {
			return (double[]) object;
		} else if (object instanceof PyList) {
			// coerce PyList into double array.
			final PyList list = (PyList)object;
		    return IntStream.range(0, list.__len__())
		            .mapToObj(list::pyget)
		            .map(pyo -> pyo instanceof PyString
		                    ? Double.valueOf(pyo.toString())
		                    : Py.tojava(pyo, Number.class))
		            .mapToDouble(Number::doubleValue)
		            .toArray();
		} else if (object instanceof int[]) {
			return Arrays.stream((int[]) object).mapToDouble(Double::valueOf).toArray();
		} else if (object instanceof long[]) {
			return Arrays.stream((long[]) object).mapToDouble(Double::valueOf).toArray();
		} else if (object instanceof String[]) {
			return Arrays.stream((String[]) object).mapToDouble(Double::valueOf).toArray();
		} else if (object instanceof Number[]) {
			return Arrays.stream((Number[]) object).mapToDouble(Number::doubleValue).toArray();
		} else if (object instanceof Number) {
			return new double[] { ((Number) object).doubleValue() };
		}

		throw new IllegalArgumentException("Object cannot be converted to a double array: " + object);
	}

	/**
	 * Attempts to convert the given object into a list of strings.
	 * @param object element to convert
	 * @return list of strings
	 */
	public static List<String> toStringList(Object object) {
		if (object instanceof String) {
			return Arrays.asList((String)object);
		} else if (object instanceof Number) {
			return Arrays.asList(((Number) object).toString());
		} else if (object instanceof Number[]) {
			return Arrays.stream((Number[]) object).map(Object::toString).collect(toList());
		} else if (object.getClass().isArray()) {
			final int length = Array.getLength(object);
			final List<String> result = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				result.add(Array.get(object, i).toString());
			}
			return result;
		} else if (object instanceof PySequence) {
			final int length = ((PySequence) object).__len__();
			final List<String> result = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				final PyObject item = ((PySequence) object).__finditem__(i);
				result.add(item instanceof PyNone ? "none" : item.toString());
			}
			return result;
		} else {
			return Arrays.asList(object.toString());
		}
	}

}
