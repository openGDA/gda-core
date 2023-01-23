/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import java.util.ArrayList;

import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import gda.rcp.views.scan.AbstractCachedScanPlotView;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;

@SuppressWarnings("deprecation")
public class LnI0ItScanPlotView extends AbstractCachedScanPlotView {

	public static final String ID = "gda.rcp.views.scan.LnI0ItScanPlotView"; //$NON-NLS-1$

	private String yAxis = "ln(I0/It)";
	private String graphTitle = "Absorption  -  ln(I0/It) vs. Energy";

	/**
	 * Optionally override to change yAxis mode.
	 *
	 * @return mode.
	 */
	@Override
	public AxisMode getYAxisMode() {
		return AxisMode.LINEAR_WITH_OFFSET;
	}

	public LnI0ItScanPlotView() {
		super();
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return graphTitle;
	}

	@Override
	protected void updateCache(ArrayList<IScanDataPoint> collection, int startIndex) {
		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		for (int i = startIndex; i < collection.size(); i++){
			IScanDataPoint point = collection.get(i);
			graphTitle = "Scan: " + point.getScanIdentifier();
			double x = point.getAllValuesAsDoubles()[0];
			double ffi0 = ScanDataPointUtils.getFFI0(point);
			double ffi1 = ScanDataPointUtils.getFFI1(point);
			double ff = ScanDataPointUtils.getFF(point);
			double i0 = ScanDataPointUtils.getI0(point);
			double i1 = ScanDataPointUtils.getI1(point);
			double it = ScanDataPointUtils.getIt(point);
			if (Double.isNaN(i0) && Double.isNaN(i1))
				continue;
			if (!Double.isNaN(ffi0)) {
				cachedY.add(ffi0);
				cachedX.add(x);
				yAxis = "FF/I0";
				graphTitle += " Fluorescence  -  FF/I0 vs. Energy";
			} else if (!Double.isNaN(ffi1)) {
				cachedY.add(ffi1);
				cachedX.add(x);
				yAxis = "FF/I1";
				graphTitle += " Fluorescence  -  FF/I1 vs. Energy";
			}  else if (!Double.isNaN(ff)) {
				Double y = ff / i0;
				if (y.isInfinite() || y.isNaN()) {
					y = 0.0;
				}
				cachedY.add(y);
				cachedX.add(x);
				yAxis = "FF/I0";
				graphTitle += " Fluorescence  -  FF/I0 vs. Energy";
			} else if (!Double.isNaN(it)) {
				Double y = Math.log(i0 / it);
				if (y == null || y.isInfinite() || y.isNaN()){
					y = 0.0;
				}
				cachedY.add(y);
				cachedX.add(x);
				yAxis = "ln(I0/It)";
				graphTitle += " Absorption  -  ln(I0/It) vs. Energy";
			} else
				continue;
		}
	}

	@Override
	protected IPlotData getY(IScanDataPoint... points) {
		Dataset yValues = DatasetFactory.createFromList(cachedY);
		yValues.setName(getYAxisName());
		return new DataSetPlotData(getYAxisName(), yValues);
	}

	@Override
	protected String getXAxisName() {
		return "Energy (eV)";
	}

	@Override
	protected String getYAxisName() {
		return yAxis;
	}
}
