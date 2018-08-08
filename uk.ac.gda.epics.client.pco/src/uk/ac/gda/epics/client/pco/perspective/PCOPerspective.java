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

package uk.ac.gda.epics.client.pco.perspective;

import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;
import uk.ac.gda.epics.client.pco.viewfactories.CamViewFactory;
import uk.ac.gda.epics.client.pco.viewfactories.StatusViewFactory;
import uk.ac.gda.epics.client.perspective.AreaDetectorPerspective;

/**
 * PCO specific perspective
 */
public class PCOPerspective extends AreaDetectorPerspective {
	private static final String PCO_PLOT_VIEW_ID = "uk.ac.gda.beamline.client.pcoplotview";
	private static final String PCO_HISTOGRAMVIEW = "uk.ac.diamond.scisoft.analysis.rcp.views.HistogramView:PCOPlot";
	private static final String PCO_SIDEPLOT = SidePlotView.ID + ":PCOPlot";
	private static final String PCO_SUBSAMPLE_PLOT_VIEWID = "uk.ac.gda.beamline.client.pcosubsampleplot";

	@Override
	protected String getPlotViewId() {
		return PCO_PLOT_VIEW_ID;
	}

	@Override
	protected String getSidePlotViewId() {
		return PCO_SIDEPLOT;
	}

	@Override
	protected String getHistogramViewPlotId() {
		return PCO_HISTOGRAMVIEW;
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
		return PCO_SUBSAMPLE_PLOT_VIEWID;
	}

}
