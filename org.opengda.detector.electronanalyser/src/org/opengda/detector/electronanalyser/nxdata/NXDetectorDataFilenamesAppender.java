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

package org.opengda.detector.electronanalyser.nxdata;

import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.nxdata.NXDetectorDataAppender;

import java.text.MessageFormat;
import java.util.List;
/**
 * 
 */
public class NXDetectorDataFilenamesAppender implements NXDetectorDataAppender {


	private final List<String> elementNames;

	private final List<String> elementValues;

	private List<Double> totalIntensity;


	public NXDetectorDataFilenamesAppender(List<String> elementNames, List<String> elementValues, List<Double>totalIntensity) {
		if (elementNames.size() != elementValues.size()) {
			throw new IllegalArgumentException(MessageFormat.format(
					"Length of elementNames[{0}] != elementValues[{1}]", elementNames.size(), elementValues.size()));
		}
		this.elementNames = elementNames;
		this.elementValues = elementValues;
		this.totalIntensity=totalIntensity;
	}

	/**
	 * 
	 */
	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		data.addFileNames(detectorName, "data", elementValues.toArray(new String[] {}),true, true);
		for (int i = 0; i < elementNames.size(); i++) {
			String name = elementNames.get(i);
			String value = elementValues.get(i);
			Double intensity=totalIntensity.get(i);
			data.setPlottableValue(name, intensity);
			data.addExternalFileLink(detectorName, name+"-imagedata", "nxfile://" +value+ "#entry1/instrument/detector/image_data", true, true);
			data.addExternalFileLink(detectorName, name+"-spectrumdata", "nxfile://" +value+ "#entry1/instrument/detector/spectrum_data", true, true);
			data.addExternalFileLink(detectorName, name+"-externaliodata", "nxfile://" +value+ "#entry1/instrument/detector/external_io_data", true, true);
		}
		if (data instanceof NXDetectorDataWithFilepathForSrs) {
			((NXDetectorDataWithFilepathForSrs)data).addFileNames(detectorName, elementValues.toArray(new String[]{}));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elementNames == null) ? 0 : elementNames.hashCode());
		result = prime * result + ((elementValues == null) ? 0 : elementValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NXDetectorDataFilenamesAppender other = (NXDetectorDataFilenamesAppender) obj;
		if (elementNames == null) {
			if (other.elementNames != null)
				return false;
		} else if (!elementNames.equals(other.elementNames))
			return false;
		if (elementValues == null) {
			if (other.elementValues != null)
				return false;
		} else if (!elementValues.equals(other.elementValues))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String str = "NXDetectorDataDoubleAppender:";
		for (int i = 0; i < elementNames.size(); i++) {
			String name = elementNames.get(i);
			String value = elementValues.get(i);
			str = str + " " + name + ":" + value;
		}
		return str;
	}

}
