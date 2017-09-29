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

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * Extends StandardXYToolTipGenerator to provide more information for the ToolTips associated with data points in lines.
 */
class SimpleXYToolTipGenerator extends StandardXYToolTipGenerator {
	/**
	 * Generates a tool tip text item for a particular item within a series.
	 *
	 * @param data
	 *            the dataset.
	 * @param series
	 *            the series index (zero-based).
	 * @param item
	 *            the item index (zero-based).
	 * @return the tool tip text.
	 */
	@Override
	public String generateToolTip(XYDataset data, int series, int item) {
		String result = data.getSeriesKey(series) + ", item " + item;
		Number x = new Double(data.getXValue(series, item));
		result = result + " x: " + getXFormat().format(x);

		Number y = new Double(data.getYValue(series, item));
		result = result + ", y: " + getYFormat().format(y);
		return result;
	}
}
