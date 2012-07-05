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

package gda.rcp.views.scan;

import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;

/**
 * Base class for plotting multiple plots at the same time. The default implementation is
 */
public class MultipleScanPlotView extends AbstractCachedScanPlotView {

	public static final String ID = "gda.rcp.views.scan.MultipleScanPlotView";

	protected PlotData cachedAllY;

	@Override
	public void scanStopped() {
		super.scanStopped();
		if (cachedAllY == null)
			cachedAllY = new PlotData();
		cachedAllY.clear();
	}

	@Override
	public void scanStarted() {
		super.scanStarted();
		if (cachedAllY == null)
			cachedAllY = new PlotData();
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return "Scan " + scanNumber;
	}

	@Override
	protected String getGraphTitle() {
		return "Scan";
	}

	@Override
	protected void plotPointsFromService() throws Exception {

		if (cachedAllY == null)
			cachedAllY = new PlotData();
		cachedAllY.clear();
		super.plotPointsFromService();
	}

	@Override
	protected void updateCache(ArrayList<IScanDataPoint> collection, int startIndex) {
		for (int i = startIndex; i < collection.size(); i++){
			IScanDataPoint point = collection.get(i);
//			final IScanDataPoint point = sdpArray.get(i);
			final List<Double> data = Arrays.asList(point.getDetectorDataAsDoubles());
			final List<String> names = point.getDetectorHeader();
			for (int j = 0; j < data.size(); j++) {
				cachedAllY.addData(names.get(j), data.get(j));
			}
		}
	}

	@Override
	protected PlotData getY(IScanDataPoint... points) {
		return cachedAllY;
	}

	@Override
	protected String getYAxis() {
		return "Scan Data";
	}

	@Override
	protected String getXAxis() {
		return "Point Number";
	}
}