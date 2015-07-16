/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.util;

public final class BoolUtils {

	/**
	 * Inverts a Boolean object, handling the <code>null</code> condition without NPE. Nulls are treated as false, thus
	 * true is returned. Also allows concise inversion without boxing. @param bool Boolean object to invert @return
	 * Inverted Boolean object
	 */
	public static Boolean negate(final Boolean bool) {
		boolean negative = (bool == null) ? true : !bool.booleanValue();
		return Boolean.valueOf(negative);
	}
}
