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

package gda.device.scannable;

import java.lang.reflect.Array;
import java.util.stream.IntStream;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;

import gda.data.PlottableDetectorData;

/**
 * Class to implement ScannableGetPosition from the object returned from a monitor
 * or scannable getPosition method
 */
public class ScannableGetPositionWrapper implements ScannableGetPosition {

	private final Object scannableGetPositionVal;
	private final String[] formats;

	private Object[] elements;
	private String[] stringFormattedValues;

	public ScannableGetPositionWrapper(Object scannableGetPositionVal, String[] formats){
		this.scannableGetPositionVal = scannableGetPositionVal;
		this.formats = formats;
	}

	@Override
	public int getElementCount() {
		return getElements().length;
	}

	@Override
	public String[] getStringFormattedValues() {
		if (stringFormattedValues == null) {
			stringFormattedValues = calcStringFormattedValues();
		}
		return stringFormattedValues;
	}

	private Object[] getElements() {
		if (elements == null) {
			elements = calcElements();
		}
		return elements;
	}

	private Object[] calcElements() {
		if (scannableGetPositionVal == null)
			return new Object[]{};

		if (scannableGetPositionVal instanceof Object[] objArray) {
			return objArray;
		} else if (scannableGetPositionVal instanceof PyString pyString){
			// should remain only element
			// and not be decomposed into an array of characters
			// if treated as a PySequence
			return new Object[] { pyString };
		} else if (scannableGetPositionVal instanceof PySequence pySeq){
			return IntStream.range(0, pySeq.__len__()).mapToObj(pySeq::__finditem__).toArray();
		} else if (scannableGetPositionVal.getClass().isArray()){
			return IntStream.range(0, ArrayUtils.getLength(scannableGetPositionVal))
					.mapToObj(i -> Array.get(scannableGetPositionVal, i))
					.toArray();
		} else if (scannableGetPositionVal instanceof PlottableDetectorData plottableData) {
			return plottableData.getDoubleVals();
		} else {
			return new Object[] { scannableGetPositionVal };
		}
	}

	private String[] calcStringFormattedValues() {
		final Object[] elements = getElements();
		return IntStream.range(0, elements.length)
				.mapToObj(index -> toFormattedString(elements[index], getFormatString(index)))
				.toArray(String[]::new);
	}

	private String getFormatString(int index) {
		if (formats == null || formats.length == 0) {
			return null;
		}
		return formats.length > index ? formats[index] : formats[0];
	}

	private String toFormattedString(final Object object, String format) {
		if (object != null) {
			try {
				if (format != null) {
					final Object javaObject = toJavaObject(object);
					return String.format(format,javaObject);
				} else if (object instanceof PyObject){
					return (String)(((PyObject)object).__str__()).__tojava__(String.class);
				}
			} catch(Exception e) {
				// fall through to return object.toString()
			}
			return object.toString();
		}

		return "unknown";
	}

	private Object toJavaObject(Object object) {
		if (object instanceof PyFloat pyFloat) {
			return pyFloat.__tojava__(Double.class);
		} else if (object instanceof PyInteger pyInt) {
			return pyInt.__tojava__(Integer.class);
		} else if (object instanceof PyObject pyObj) {
			final Object javaObject = pyObj.__str__().__tojava__(String.class);
			try {
				return Double.parseDouble((String) javaObject);
			} catch (Exception e) {
				// ignore as transformedObject will be unchanged
				return javaObject;
			}
		}

		return object;
	}
}