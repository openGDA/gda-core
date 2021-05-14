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

import java.text.MessageFormat;
import java.util.List;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.NXDetectorData;

/**
 * This generic class only supports String or Link target path, see the validation in {@link #appendTo(NXDetectorData, String)}
 *
 */
public class NXDetectorDataStringAppender implements NXDetectorDataAppender {

	private final List<String> elementNames;
	private final List<String> elementValues;

	public NXDetectorDataStringAppender(List<String> elementNames, List<String> elementValues) {
		if (elementNames.size() != elementValues.size()) {
			throw new IllegalArgumentException(MessageFormat.format(
					"Length of elementNames[{0}] != elementValues[{1}]", elementNames.size(), elementValues.size()));
		}
		this.elementNames = elementNames;
		this.elementValues = elementValues;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		for (int i = 0; i < elementNames.size(); i++) {
			String name = elementNames.get(i);
			String t = elementValues.get(i);
			if (t.startsWith("nxfile://")) {
				data.addExternalFileLink(detectorName, name, t, 0);
			}
			data.addData(detectorName, name, new NexusGroupData(t), null, null, null, false);
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
		NXDetectorDataStringAppender other = (NXDetectorDataStringAppender) obj;
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
		StringBuilder str = new StringBuilder("NXDetectorDataStringAppender:");
		for (int i = 0; i < elementNames.size(); i++) {
			str.append(" ");
			str.append(elementNames.get(i));
			str.append(":");
			str.append(elementValues.get(i));
		}
		return str.toString();
	}
}
