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

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.january.dataset.Dataset;

/**
 * Example DataSetProcessor that adds data to the NexusProvider Test implementation of INexusProviderDataSetProcessor.
 * Extends extraNames by 1 element, adds that value to the nx data and to the plottable data. The value increases by 1
 * for each call.
 */
public class ExampleDataSetProcessor extends DataSetProcessorBase {
	private static final String ItemName = "testVal";
	Double val = 0.;
	private List<String> extraNames = Arrays.asList(new String[] { ItemName });
	private List<String> outputFormat = Arrays.asList(new String[] { "%5.5g" });

	@Override
	public GDANexusDetectorData process(final String detName, String dataName, Dataset dataset) throws Exception {
		val = val + 1.;
		NXDetectorData res = new NXDetectorData();
		res.addData(detName, new NexusGroupData(val), null, 1);
		return res;
	}

	@Override
	public Collection<String> _getExtraNames() {
		return extraNames;
	}

	@Override
	public Collection<String> _getOutputFormat() {
		return outputFormat;
	}

	@Override
	public String getName() {
		return "NexusProviderDataSetTest";
	}


}
