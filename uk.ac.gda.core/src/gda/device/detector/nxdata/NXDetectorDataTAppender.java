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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.NXDetectorData;

/**
 * This generic class only supports Integer or Double, see the validation in {@link #appendTo(NXDetectorData, String)}
 *
 * Since we can't say {@code <T extends Double | Integer>} we constrain it to Number, so most inappropriate uses will result in a
 * compile-time error rather than a run-time error.
 *
 * @param <T>
 */
public class NXDetectorDataTAppender <T extends Number> implements NXDetectorDataAppender {

	private final List<String> elementNames;
	private final List<T> elementValues;

	public NXDetectorDataTAppender(List<String> elementNames, List<T> elementValues) {
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
			String name = String.valueOf(elementNames.get(i)); // DAQ-4543 handle PyString convert to Java String
			INexusTree valdata = null;
			T t = elementValues.get(i);
			if( t instanceof Double){
				data.setPlottableValue(name, (Double)t);
				valdata = data.addData(detectorName, name, new NexusGroupData((Double) t), null, null, null, true);
			} else if( t instanceof Integer){
				data.setPlottableValue(name, ((Integer)t).doubleValue());
				valdata = data.addData(detectorName, name, new NexusGroupData((Integer) t), null, null, null, true);
			}
			if(valdata ==null)
				throw new RuntimeException("Only Double or Integer currently supported by NXDetectorDataTAppender");
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
		@SuppressWarnings("rawtypes")
		NXDetectorDataTAppender other = (NXDetectorDataTAppender) obj;
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
		StringBuilder str = new StringBuilder("NXDetectorDataDoubleAppender:");
		for (int i = 0; i < elementNames.size(); i++) {
			str.append(" ");
			str.append(elementNames.get(i));
			str.append(":");
			str.append(elementValues.get(i));
		}
		return str.toString();
	}
}
