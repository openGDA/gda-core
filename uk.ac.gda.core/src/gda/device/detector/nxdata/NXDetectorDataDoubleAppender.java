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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.NXDetectorData;

import java.text.MessageFormat;
import java.util.List;

import org.nexusformat.NexusFile;

public class NXDetectorDataDoubleAppender implements NXDetectorDataAppender {

	static private final int[] SINGLE_DIMENSION = new int[] { 1 };
	private final List<String> elementNames;
	private final List<Double> elementValues;

	public NXDetectorDataDoubleAppender(List<String> elementNames, List<Double> elementValues) {
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
			Double value = elementValues.get(i);
			data.setPlottableValue(name, value);
			INexusTree valdata = data.addData(detectorName, name, SINGLE_DIMENSION, NexusFile.NX_FLOAT64, new double[] { value }, null, null);
			valdata.addChildNode(new NexusTreeNode("local_name",NexusExtractor.AttrClassName, valdata, new NexusGroupData(String.format("%s.%s", detectorName, name))));
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
		NXDetectorDataDoubleAppender other = (NXDetectorDataDoubleAppender) obj;
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
			Double value = elementValues.get(i);
			str = str + " " + name + ":" + value;
		}
		return str;
	}
}