/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import java.util.List;

import org.nexusformat.NexusFile;

public class NXDetectorDataDoubleAppender implements NXDetectorDataAppender {

	static private final int[] SINGLE_DIMENSION = new int[] { 1 };
	
	private final List<String> elementNames;
	
	private final List<Double> elementValues;
	

	public NXDetectorDataDoubleAppender(List<String> elementNames, List<Double> elementValues) {
		this.elementNames = elementNames;
		this.elementValues = elementValues;
	}

	/**
	 * 
	 */
	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		// TODO Auto-generated method stub

		for (int i = 0; i < elementNames.size(); i++) {
			String name = elementNames.get(i);
			Double value = elementValues.get(i);
			data.setPlottableValue(name, value);
			data.addData(detectorName, name, SINGLE_DIMENSION, NexusFile.NX_FLOAT64, new double[] { value }, null, null);
		}

	}

}
