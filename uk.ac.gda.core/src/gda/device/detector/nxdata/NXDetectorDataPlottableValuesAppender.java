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

package gda.device.detector.nxdata;

import gda.device.detector.NXDetectorData;

/**
 * Adds extra plottable values to NXDetectorData without adding them to the NeXus tree
 */
public class NXDetectorDataPlottableValuesAppender implements NXDetectorDataAppender {

	private final String[] extraNames;
	private final Double[] plottableValues;

	public NXDetectorDataPlottableValuesAppender(String[] extraNames, Double[] plottableValues) {
		this.extraNames = extraNames;
		this.plottableValues = plottableValues;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		for (int index = 0; index < extraNames.length && index < plottableValues.length; index++) {
			data.setPlottableValue(extraNames[index], plottableValues[index]);
		}
	}
}
