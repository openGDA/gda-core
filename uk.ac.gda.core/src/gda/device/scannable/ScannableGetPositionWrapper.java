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

import gda.data.PlottableDetectorData;

import java.lang.reflect.Array;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;

/**
 * Class to implement ScannableGetPosition from the object returned from a monitor
 * or scannable getPosition method
 */
public class ScannableGetPositionWrapper implements ScannableGetPosition
{
	final Object scannableGetPositionVal;
	private Object [] elements;
	private String [] formats;
	private String [] stringFormattedValues;
	public ScannableGetPositionWrapper(Object scannableGetPositionVal, String [] formats){
		this.scannableGetPositionVal = scannableGetPositionVal;
		this.formats = formats;
	}


	@Override
	public int getElementCount() {
		return getElements().length;
	}

	@Override
	public String[] getStringFormattedValues() {
		if( stringFormattedValues == null){
			stringFormattedValues = calStringFormattedValues();
		}
		return stringFormattedValues;
	}

	Object[] getElements() {
		if( elements == null){
			elements = calcElements();
		}
		return elements;
	}

	Object [] calcElements(){
		if( scannableGetPositionVal == null)
			return new Object[]{};

		Object [] elements = new Object[]{scannableGetPositionVal};
		if(scannableGetPositionVal instanceof Object[]){
			elements = (Object [])scannableGetPositionVal;
		} else if (scannableGetPositionVal instanceof PyString){
			// should remain only element
			// and not be decomposed into an array of characters
			// if treated as a PySequence
			elements[0] = scannableGetPositionVal;
		} else if( scannableGetPositionVal instanceof PySequence){
			PySequence seq = (PySequence)scannableGetPositionVal;
			int len = seq.__len__();
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = seq.__finditem__(i);
			}
		} else if ( scannableGetPositionVal instanceof PyList){
			PyList seq = (PyList)scannableGetPositionVal;
			int len = seq.__len__();
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = seq.__finditem__(i);
			}
		} else if ( scannableGetPositionVal.getClass().isArray()){
			int len = ArrayUtils.getLength(scannableGetPositionVal);
			elements = new Object[len];
			for (int i = 0; i < len; i++) {
				elements[i] = Array.get(scannableGetPositionVal, i);
			}
		} else if ( scannableGetPositionVal instanceof PlottableDetectorData){
			elements = ((PlottableDetectorData)scannableGetPositionVal).getDoubleVals();
		}
		return elements;
	}

	String [] calStringFormattedValues(){
		Object [] elements = getElements();
		String[] stringFormattedValues = new String[elements.length];
		int index=0;
		for(Object object : elements){
			String val = "unknown";
			if( object != null){
				val = object.toString();
				try{
					String format = formats != null ? ( formats.length > index ? formats[index] : formats[0] )
							: null;
					if( format == null){
						if( object instanceof PyObject){
							val = (String)(((PyObject)object).__str__()).__tojava__(String.class);
						}
					} else {
						Object transformedObject=object;
						if( object instanceof PyFloat){
							transformedObject = ((PyFloat)object).__tojava__(Double.class);
						} else if( object instanceof PyInteger){
							transformedObject = ((PyInteger)object).__tojava__(Integer.class);
						} else if ( object instanceof PyObject){
							transformedObject = ((PyObject)object).__str__().__tojava__(String.class);

							try {
								transformedObject = Double.parseDouble((String) transformedObject);
							} catch (Exception e) {
								// ignore as transformedObject will be unchanged
							}
						}
						val = String.format(format,transformedObject);
					}
				}
				catch(Exception e){
					//do nothing - the default value is object.toString
				}
			}
			stringFormattedValues[index] = val;
			index++;
		}
		return stringFormattedValues;
	}
}