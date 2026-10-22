/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.hrpd.pmac;

/**
 * This data object defines the safe position to park a device when it is not in use while operating other devices around it.
 * It assumes that any position greater than the specified safe position is safe.
 */
public class SafeWhenGreaterThanPosition extends SafePosition {
	@Override
	public boolean test(Double t) {
		return t >= (requiredPosition - tolerance);
	}
}
