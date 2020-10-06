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

import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;

/**
 * Class with static members used to convert between various objects
 */
public class TypeConverters {

	/**
	 * Attempts to convert the given object into a list of strings.
	 * @param element element to convert
	 */
	public static List<String> toStringList(Object element) {
		if (element instanceof String) {
			return Arrays.asList((String)element);
		} else if (element instanceof Number) {
			return Arrays.asList(((Number) element).toString());
		} else if (element instanceof Number[]) {
			return Arrays.stream((Number[]) element).map(Object::toString).collect(toList());
		} else if (element.getClass().isArray()) {
			final int length = Array.getLength(element);
			final List<String> result = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				result.add(Array.get(element, i).toString());
			}
			return result;
		} else if (element instanceof PySequence) {
			final int length = ((PySequence) element).__len__();
			final List<String> result = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				final PyObject item = ((PySequence) element).__finditem__(i);
				result.add(item instanceof PyNone ? "none" : item.toString());
			}
			return result;
		} else {
			return Arrays.asList(element.toString());
		}
	}
}
