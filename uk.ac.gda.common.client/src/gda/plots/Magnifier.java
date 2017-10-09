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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import org.jfree.data.xy.XYSeriesCollection;

/**
 * Windows used for magnification of a SimplePlot must implement this interface.
 */
interface Magnifier {
	/**
	 * Should set the SimplePlot to be magnified.
	 *
	 * @param toBeMagnified
	 *            the SimplePlot which will be magnified.
	 * @param xySeriesCollection
	 */
	public void setSimplePlot(SimplePlot toBeMagnified, XYSeriesCollection xySeriesCollection);

	/**
	 * Should toggle the visibility of the window.
	 *
	 * @param visible
	 *            the new visibility.
	 */
	public void setVisible(boolean visible);

	/**
	 * Should set the size of the window.
	 *
	 * @param size
	 *            a Dimension containing the width and height of the window.
	 */
	public void setSize(Dimension size);

	/**
	 * Should cause the window to display the given area of the plot.
	 *
	 * @param toMagnify
	 *            a Rectangle2D which specifies the area to magnify.
	 */
	public void update(Rectangle2D toMagnify);
}
