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

package uk.ac.gda.epics.client.pixium.perspective;

import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;
import uk.ac.gda.epics.client.perspective.AreaDetectorPerspective;
import uk.ac.gda.epics.client.pixium.viewfactories.CamViewFactory;
import uk.ac.gda.epics.client.pixium.viewfactories.StatusViewFactory;

/**
 * Pixium specific perspective
 */
public class PixiumPerspective extends AreaDetectorPerspective {
	private static final String PIXIUM_PLOT_VIEW_ID = "uk.ac.gda.beamline.client.pixiumplotview";
	private static final String PIXIUM_HISTOGRAMVIEW = "uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView:PixiumPlot";
	private static final String PIXIUM_SIDEPLOT = SidePlotView.ID + ":PixiumPlot";
	private static final String PIXIUM_SUBSAMPLE_PLOT_VIEWID = "uk.ac.gda.beamline.client.pixiumsubsampleplot";

	@Override
	protected String getPlotViewId() {
		return PIXIUM_PLOT_VIEW_ID;
	}

	@Override
	protected String getSidePlotViewId() {
		return PIXIUM_SIDEPLOT;
	}

	@Override
	protected String getHistogramViewPlotId() {
		return PIXIUM_HISTOGRAMVIEW;
	}

	@Override
	protected String getStatusViewId() {
		return StatusViewFactory.ID;
	}

	@Override
	protected String getCameraViewId() {
		return CamViewFactory.ID;
	}

	@Override
	protected String getSubSamplePlotViewId() {
		return PIXIUM_SUBSAMPLE_PLOT_VIEWID;
	}

}
