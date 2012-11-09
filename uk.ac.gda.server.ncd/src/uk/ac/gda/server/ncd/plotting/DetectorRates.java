/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.plotting;

import java.io.Serializable;

public class DetectorRates implements Serializable {
	public String detName, detType;
	public float countingTime, maxCounts, integratedCounts;
	@Override
	public String toString() {
		return String.format("%s detector %s has been counting for %5.3f seconds and aquired %5.5g total counts (%5.5g in the peak)",
				detType, detName, countingTime, integratedCounts, maxCounts);
	}
}