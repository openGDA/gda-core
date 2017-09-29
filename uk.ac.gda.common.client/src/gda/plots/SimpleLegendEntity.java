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

import java.awt.Shape;

import org.jfree.chart.entity.ChartEntity;

/**
 * Extends ChartEntity so that we can have ChartEntity objects which are connected with particular SimpleXYSeries in the
 * plot legend.
 *
 * @see ChartEntity
 */
class SimpleLegendEntity extends ChartEntity {
	private SimpleXYSeries sxys;

	/**
	 * Creates a SimpleLegendEntity by adding a SimpleXYSeries to an ordinary ChartEntity
	 *
	 * @param area
	 * @param toolTipText
	 * @param urlText
	 * @param sxys
	 */
	SimpleLegendEntity(Shape area, String toolTipText, String urlText, SimpleXYSeries sxys) {
		super(area, toolTipText, urlText);
		this.sxys = sxys;
	}

	/**
	 * Creates a SimpleLegendEntity by creating a new ChartEntity from an existing one and adding an SimpleXYSeries.
	 *
	 * @param ce
	 * @param sxys
	 */
	SimpleLegendEntity(ChartEntity ce, SimpleXYSeries sxys) {
		super(ce.getArea(), ce.getToolTipText(), ce.getURLText());
		this.sxys = sxys;
	}

	/**
	 * Returns the SimpleXYSeries connected to this SimpleLegendEntity
	 *
	 * @return the SimpleXYSeries
	 */
	public SimpleXYSeries getSeries() {
		return sxys;
	}
}
