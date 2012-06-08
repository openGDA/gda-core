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

package gda.epics.interfaceSpec;

import java.util.Iterator;

/**
 * utils class
 */
public class utils {
	/**
	 * @param dev1
	 * @param dev2
	 * @return boolean
	 */
	public boolean comparator(Device dev1, Device dev2) {
		return dumpDevice(dev1).equals(dumpDevice(dev2));
	}

	/**
	 * @param device
	 * @return String device
	 */
	public static String dumpDevice(Device device) {
		String res;
		res = device.getName() + " is of type " + device.getType();
		for (Iterator<String> fieldName = device.getFieldNames(); fieldName.hasNext();) {
			Field field = device.getField(fieldName.next());
			res += "\n" + field.getName();

			for (Iterator<String> attributeName = field.getAttributeNames(); attributeName.hasNext();) {
				Attribute attribute = field.getAttribute(attributeName.next());
				res += "\n" + attribute.getName() + " = " + attribute.getValue();
			}
		}
		return res;
	}
}
