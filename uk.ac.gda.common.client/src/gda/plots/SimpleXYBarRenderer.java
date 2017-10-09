/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.plots;

import java.awt.BasicStroke;
import java.awt.geom.Line2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;

/**
 * Extends XYBarRenderer in order to provide some additional facilities.
 */
class SimpleXYBarRenderer extends XYBarRenderer {
	/**
	 * Creates a SimpleLegendItem for a SimpleXYSeries in the dataset.
	 *
	 * @param datasetIndex
	 *            which dataset (left or right effectively)
	 * @param series
	 *            which data series
	 * @return the created legend
	 */
	@Override
	public LegendItem getLegendItem(int datasetIndex, int series) {
		LegendItem result = null;

		XYPlot plot = getPlot();
		if (plot != null) {
			SimpleXYSeriesCollection dataset = (SimpleXYSeriesCollection) plot.getDataset(datasetIndex);
			if (dataset != null) {
				SimpleXYSeries sxys = (SimpleXYSeries) dataset.getSeries(series);
				if (sxys.isVisible() && sxys.isVisibleInLegend()) {
					result = new LegendItem(sxys.getName(), sxys.getName(), null, null, sxys.isDrawMarkers(), sxys
							.getSymbol(), sxys.getFilled(), sxys.getSymbolPaint(), !sxys.getFilled(), sxys
							.getSymbolPaint(), new BasicStroke(), sxys.isDrawLines(), new Line2D.Double(-8.0, 0.0, 8.0,
							0.0), sxys.getStroke(), sxys.getPaint());
				}
			}
		}
		return result;

	}
}