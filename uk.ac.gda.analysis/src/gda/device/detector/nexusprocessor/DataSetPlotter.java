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

import java.util.Collection;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * DataSetProcessor that sends data to client for plotting
 */
public class DataSetPlotter extends DataSetProcessorBase {

	private String plotName="Plot 1";

	@Override
	public GDANexusDetectorData process(String detName, String dataName, Dataset dataset) throws Exception {
		if (enable) {
			SDAPlotter.imagePlot(plotName, dataset);
		}
		return null;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	@Override
	protected Collection<String> _getExtraNames() {
		return null;
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return null;
	}
}
