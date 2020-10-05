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

import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;

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

		final double[] data;
		if (object instanceof double[]) {
			data = (double[]) object;
		} else if (object instanceof PyList) {
			// coerce PyList into double array.
			int length = ((PyList) object).__len__();
			data = new double[length];
			for (int i = 0; i < length; i++) {
				try {
					// This deals properly with Double, Long & Integer etc. but not BigInteger
					data[i] = Double.valueOf(((PyList) object).__getitem__(i).toString());
				} catch (NumberFormatException nfe) {
					try {
						// This deals with Double, BigInteger & Long literal etc. but not Long
						data[i] = Py.py2double(((PyList) object).__getitem__(i));
					} catch (Exception e) {
						throw new IllegalArgumentException("Error extracting from PyList item element: " + data[i]);
					}
				}
			}
		} else if (object instanceof int[]) {
			int[] idata = (int[]) object; // convert array from int[] to double[]
			data = new double[idata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = idata[i];
			}
		} else if (object instanceof long[]) {
			long[] ldata = (long[]) object; // convert array from long[] to double[]
			data = new double[ldata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = ldata[i];
			}
		} else if (object instanceof String[]) {
			String[] sdata = (String[]) object;
			data = new double[sdata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = Double.valueOf(sdata[i]);
			}
			Arrays.stream(sdata).mapToDouble(Double::valueOf).toArray();
		} else if (object instanceof Number[]) {
			Number[] ldata = (Number[]) object;
			data = new double[ldata.length];
			for (int i = 0; i < data.length; i++) {
				data[i] = ldata[i].doubleValue();
			}
		} else if (object instanceof Double) {
			data = new double[] { (Double) object };
		} else if (object instanceof Integer) {
			data = new double[] { (Integer) object };
		} else if (object instanceof Long) {
			data = new double[] { (Long) object };
		} else {
			throw new IllegalArgumentException("Object cannot be converted to a double array: " + object);
		}
		return data;
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
