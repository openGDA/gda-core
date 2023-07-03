/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

public class NXDetectorDataDoubleArrayBatchAppender implements NXDetectorDataAppender{
	List<String> elementNames;
	List<Double[]> elementValues;

	public NXDetectorDataDoubleArrayBatchAppender(List<String> elementNames, List<Double[]> elementValues) {
		this.elementNames = elementNames;
		this.elementValues = elementValues;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		try {
			for (int i = 0; i < elementNames.size(); i++) {
				String name = String.valueOf(elementNames.get(i)); // DAQ-4543 handle PyString convert to Java String
				Double[] t = elementValues.get(i);
				if (t.length==1){
					data.setPlottableValue(name, t[0]);
					data.addData(detectorName, name, new NexusGroupData(t[0].doubleValue()), null, null, null, true);
				} else {
					data.setPlottableValue(name, Arrays.asList(t).stream().mapToDouble(Double::doubleValue).average().getAsDouble());
					data.addData(detectorName, name, new NexusGroupData(Stream.of(t).mapToDouble(Double::doubleValue).toArray()), null, null, null, true);
				}
			}

		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

}
