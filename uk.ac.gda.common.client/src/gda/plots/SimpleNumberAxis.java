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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.ui.RectangleEdge;

/**
 * Extends NumberAxis only so that we can attempt to keep track of the format of the tick mark labels so that the same
 * format can be used elsewhere, for example mouse tracking display.
 */
class SimpleNumberAxis extends NumberAxis {
	private int digits;

	/**
	 * Same as super constructor but also initializes number of digits to 1.
	 *
	 * @param label
	 *            the axis label
	 */
	SimpleNumberAxis(String label) {
		super(label);
		digits = 1;
	}

	/**
	 * Extends the superclass method in order to keep track of the format of the axis labels. If the TickUnit has
	 * changed then recalculate the number of digits being displayed.
	 *
	 * @param g2
	 *            the 2D graphics context
	 * @param state
	 *            the axis state
	 * @param dataArea
	 *            the data area
	 * @param edge
	 *            the rectangle edge
	 * @return the list of ticks
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Tick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
		double before = getTickUnit().getSize();
		List<Tick> result = super.refreshTicks(g2, state, dataArea, edge);
		double after = getTickUnit().getSize();

		if (before != after) {
			// There may be a better method than this BFI which gets the
			// TickUnit to write itself to string (e.g. "1.0025"), then
			// counts where the first non-zero after the decimal point is
			// (3 in this example), then adds one.
			// NB this method may go wrong if the value needs displaying in
			// the exponential way.
			double size = getTickUnit().getSize();
			String string = getTickUnit().valueToString(size);
			String afterThePoint = string.substring(string.indexOf('.') + 1);
			digits = 1;
			while (afterThePoint.startsWith("0")) {
				digits++;
				afterThePoint = afterThePoint.substring(1);
			}
			digits += 1;
		}
		return result;
	}

	/**
	 * Gets the number of digits to be displayed
	 *
	 * @return the number of digits to be displayed
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * Overrides the superclass method so that an AxisChangeEvent is sent when data is added (needed to make
	 * dependentXAxis work).
	 */
	@Override
	protected void autoAdjustRange() {
		super.autoAdjustRange();
		notifyListeners(new AxisChangeEvent(this));
	}
}
