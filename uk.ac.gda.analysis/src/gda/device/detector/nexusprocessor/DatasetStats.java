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

package gda.device.detector.nexusprocessor;

import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

import java.util.Collection;
import java.util.List;

import org.nexusformat.NexusFile;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class DatasetStats extends DataSetProcessorBase {

	static List<String> extraNames;
	static List<String> formats;
	static{
		extraNames = java.util.Arrays.asList(new String[] { "total" });
		formats = java.util.Arrays.asList(new String[] { "%5.5g" });
	}

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, AbstractDataset dataset) throws Exception {
		if(!enable)
			return null;
		Object sum = dataset.sum();
		Double vals[] = new Double[]{0.0};
		if( sum instanceof Number){
			vals[0]= ((Number)sum).doubleValue();
		} else {
			return null;
		}
		NXDetectorData res = new NXDetectorData();

		//the NexusDataWriter requires that the number of entries must be the same for all scan data points in a scan.
		for (int i = 0; i < extraNames.size(); i++) {
			String name = extraNames.get(i);
			res.addData(detectorName, dataName + "." + name, new int[] { 1 }, NexusFile.NX_FLOAT64,
					new double[] { vals[i] }, null, 1);
		}

		res.setDoubleVals(vals);
		return res;
	}

	@Override
	protected Collection<String> _getExtraNames() {
		return extraNames; 
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return formats;
	}

}
