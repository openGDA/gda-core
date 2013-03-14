/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.views.scan;

import gda.rcp.views.scan.AbstractScanPlotView;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;

public class MapPlotView extends AbstractScanPlotView {

	@Override
	protected String getCurrentPlotName(int scanNumber) {

		return "Scan-" + scanNumber + "[MicroFocus]";
	}

	@Override
	protected PlotData getX(IScanDataPoint... point) {
		return null;
	}

	@Override
	protected PlotData getY(IScanDataPoint... point) {
		return null;
	}

	@Override
	protected String getGraphTitle() {
		return "MicroFocus Map";
	}

	@Override
	protected String getXAxis() {
		return "X";
	}

	@Override
	protected String getYAxis() {
		return "y";
	}

}
