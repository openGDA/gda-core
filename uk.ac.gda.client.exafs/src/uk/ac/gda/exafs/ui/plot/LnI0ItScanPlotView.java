/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.plot;

import gda.rcp.views.scan.AbstractCachedScanPlotView;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.AxisMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;

/**
 *
 */
public class LnI0ItScanPlotView extends AbstractCachedScanPlotView {

	/**
	 * 
	 */
	public static final String ID = "gda.rcp.views.scan.LnI0ItScanPlotView"; //$NON-NLS-1$

	@Override
	public void configurePlot(final DataSetPlotter plotter) {
		plotter.setAxisOffset(0, -0.5, 0);
	}

	/**
	 * Optionally override to change yAxis mode.
	 * 
	 * @return mode.
	 */
	@Override
	public AxisMode getYAxisMode() {
		return AxisMode.LINEAR_WITH_OFFSET;
	}

	/**
	 * 
	 */
	public LnI0ItScanPlotView() {
		super();
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return "Scan " + scanNumber + " [ln(I0/It)]";
	}

	@Override
	protected PlotData getY(IScanDataPoint... points) {

		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		for (int i = 0; i < points.length; i++) {
			final IScanDataPoint point = points[i];
			final double[] i0anIt = ScanDataPointUtils.getI0andIt(point);
			if (Double.isNaN(i0anIt[0]) || Double.isNaN(i0anIt[1]))
				continue;
			cachedY.add(Math.log(i0anIt[0] / i0anIt[1]));
		}
		return new PlotData(getYAxis(), cachedY);
	}

	@Override
	protected String getXAxis() {
		return "Energy (eV)";
	}

	@Override
	protected String getYAxis() {
		return "ln(I0/It)";
	}

	@Override
	protected String getGraphTitle() {
		return "Absorption  -  ln(I0/It) vs. Energy";
	}

}
