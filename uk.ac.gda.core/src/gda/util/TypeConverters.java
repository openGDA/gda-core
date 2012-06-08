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

import java.lang.reflect.Array;
import java.util.Vector;

import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;

/**
 * Class with static members used to convert between various objects
 */
public class TypeConverters {
//	/**
//	 * @param dataList
//	 * @param output
//	 */
//	static public void makeDoublesFromObjects(List<Object> dataList, Vector<Double> output) {
//		for (Object element : dataList) {
//			makeDoublesFromObject(output, element);
//		}
//	}

	/**
	 * @param output
	 * @param element
	 */
	static public void makeStringsFromObject(Vector<String> output, Object element) {
		if (element instanceof String) {
			output.add((String)element);
		} else if (element instanceof Number) {
			output.add(((Number) element).toString());
		} else if( element instanceof Number[]){
			Number[] dd = (Double[])element;
			for( Number d : dd){
				output.add(d.toString());
			}
		} else if (element.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(element); i++) {
				output.add(Array.get(element, i).toString());
			}
		} else if (element instanceof PySequence) {
			int length = ((PySequence) element).__len__();
			for (int i = 0; i < length; i++) {

				PyObject item = ((PySequence) element).__finditem__(i);

				if (item instanceof PyNone) {
					output.add("none");
				} else {
					output.add(item.toString());
				}
			}
		} else if (element instanceof PyList) {
			int length = ((PyList) element).__len__();
			for (int i = 0; i < length; i++) {

				PyObject item = ((PyList) element).__finditem__(i);

				if (item instanceof PyNone) {
					output.add("none");
				} else {
					output.add(item.toString());
				}
			}
		} else {
			output.add(element.toString());
		}
	}
}
