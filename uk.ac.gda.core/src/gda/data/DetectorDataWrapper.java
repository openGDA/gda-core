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

package gda.data;

import gda.device.scannable.ScannableUtils;

import java.lang.reflect.Array;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;

/**
 * Helper class to wrap data returned by readout by Detectors that does not already implement
 * PlottableDetectorData
 */
public class DetectorDataWrapper implements PlottableDetectorData
{
	final Object detectorDataVal;
//	final boolean isCounterTimer;
	private Object [] elements;
	private Double [] doubleVals;
	
	/**
	 * @param detectorDataVal - the value returned by readOut
	 */
	public DetectorDataWrapper(Object detectorDataVal/*, boolean isCounterTimer*/){
		this.detectorDataVal = detectorDataVal;
//		this.isCounterTimer = isCounterTimer;
		elements = calcElements();
	}

	Double convertDetectorDataToDouble(Object data){
		try{
			if (data instanceof String) {
				return Double.parseDouble((String) data);
			} else if (data instanceof Number) {
				return ((Number) data).doubleValue();
			} else if ( data instanceof PyObject){
				try{
					Double val = (Double) ((PyObject)data).__tojava__(Double.class);
					//only return if a valid Double
					if (val != null)
						return val;

					if ( data instanceof PyString) {
						val = Double.valueOf((String) ((PyString)data).__tojava__(String.class));
						if (val != null)
							return val;
					}
					return val;
				}
				catch(Exception e){
					//do nothing
				}
				
			}
			// try the ScannableUtils method which converts anythign it can into an array of Doubles
			Double[] dataArray = ScannableUtils.objectToArray(data);
			if (dataArray != null){
				return dataArray[0];
			}
		} catch(Throwable ex){
			//do nothing - maybe warn - the value will be set as null
		}
		return null;
	}
	
	Double [] calsDoubleVals(){
		Object [] elements = getElements();
		Double[] vals = new Double[elements.length];
		int index=0;
		for(Object object : elements){
			vals[index] = convertDetectorDataToDouble(object);
			index ++;			
		}
		return vals;
		
	}

	public int getElementCount() {
		return getElements().length;
	}

	@Override
	public Double[] getDoubleVals() {
		if( doubleVals == null){
			doubleVals = calsDoubleVals();
		}
		return doubleVals;
	}

	Object[] getElements() {
		if( elements == null){
			elements = calcElements();
		}
		return elements;
	}

	Object [] calcElements(){
		Object [] elements = new Object[]{detectorDataVal};
//		if( isCounterTimer){
			if(detectorDataVal instanceof Object[]){
				elements = (Object [])detectorDataVal;
			} else if( detectorDataVal instanceof PySequence){
				PySequence seq = (PySequence)detectorDataVal;
				int len = seq.__len__();
				elements = new Object[len];
				for (int i = 0; i < len; i++) {
					elements[i] = seq.__finditem__(i);
				}				
			} else if ( detectorDataVal instanceof PyList){
				PyList seq = (PyList)detectorDataVal;
				int len = seq.__len__();
				elements = new Object[len];
				for (int i = 0; i < len; i++) {
					elements[i] = seq.__finditem__(i);
				}				
			} else if ( detectorDataVal.getClass().isArray()){
				int len = ArrayUtils.getLength(detectorDataVal);
				elements = new Object[len];
				for (int i = 0; i < len; i++) {
					elements[i] = Array.get(detectorDataVal, i);
				}
			}
//		}
		return elements;
	}

}