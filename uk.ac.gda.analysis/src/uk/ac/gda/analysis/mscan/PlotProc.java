/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.mscan;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 * Send dataset to plot view
 */
public class PlotProc implements MalcolmSwmrProcessor {

	private static final Logger logger = LoggerFactory.getLogger(PlotProc.class);

	private String plotName = "Area Detector";

	@Override
	public void initialise(NexusScanInfo info, NexusObjectWrapper<NXdetector> nexusWrapper) {
		// not required for this processor as it doesn't contribute data
	}

	@Override
	public void processFrame(Dataset data, SliceFromSeriesMetadata metaSlice) {
		logger.debug("Start of processFrame");
		try {
			SDAPlotter.imagePlot(plotName, data.squeeze());
		} catch (Exception e) {
			logger.error("Failed to plot detector frame", e);
		}
		logger.debug("End of processFrame");
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

}
