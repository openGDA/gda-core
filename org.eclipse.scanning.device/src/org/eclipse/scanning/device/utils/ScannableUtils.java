/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.utils;

import java.lang.reflect.Array;
import java.util.List;

import org.eclipse.dawnsci.nexus.NexusException;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * TODO: This class was created to hold the {@link #getPositionArray(Scannable)} method.
 * This method should really be in {@code gda.device.scannable.ScannableUtils} and make
 * use of the {@code toObjectArray()} method in that class. Unfortunately this plug-in cannot
 * depend on the uk.ac.gda.core plugin that contains that class as the reverse dependency
 * already exists. See DAQ-5048.
 */
public class ScannableUtils {

	private ScannableUtils() {
		// private constructor to prevent instantiation
	}

	/**
	 * Converts the position of the given {@link Scannable} (as returned by
	 * {@link Scannable#getPosition()}) as an array.
	 * <ul>
	 *   <li>The position is not an array, just an object of some kind.
	 *   	A single-valued array is returned containing that object;</li>
	 *   <li>The position is a primitive array. It is converted to an array of Objects, each
	 *      element of which is a wrapper of the primitive at that index of primitive array;</li>
	 *   <li>The position is already an object array. It is returned as is.</li>
	 * </ul>
	 *
	 * @return position as an array
	 * @throws NexusException
	 */
	public static Object[] getPositionArray(Scannable scannable) throws NexusException {
		try {
			final Object position = scannable.getPosition();

			if (position instanceof List) {
				final List<?> positionList = (List<?>)position;
				return positionList.toArray();
			}
			if (!position.getClass().isArray()) {
				// position is not an array (i.e. is a double) return array with position as single element
				return new Object[] { position };
			}

			if (position.getClass().getComponentType().isPrimitive()) {
				// position is a primitive array
				final int size = Array.getLength(position);
				Object[] outputArray = new Object[size];
				for (int i = 0; i < size; i++) {
					outputArray[i] = Array.get(position, i);
				}
				return outputArray;
			}

			// position is already an object array
			return (Object[]) position;
		} catch (DeviceException | NullPointerException e) {
			throw new NexusException("Could not get position of device: " + scannable.getName(), e);
		}
	}

}
