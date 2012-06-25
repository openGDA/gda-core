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

package uk.ac.gda.client.tomo.composites;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;

/**
 *
 */
public class EllipticalHighligterFigure extends Ellipse {

	// private static final Logger logger = LoggerFactory.getLogger(EllipticalHighligterFigure.class);

	public EllipticalHighligterFigure() {
		this.setSize(8, 8);
		this.setForegroundColor(ColorConstants.darkBlue);
		this.setBackgroundColor(ColorConstants.white);
		setOpaque(false);
		setFill(false);
		this.setLineWidth(3);
		this.setLayoutManager(new XYLayout());
	}

	public void moveTo(int xVal, int yVal) {
		setLocation(new Point(xVal - 4, yVal - 3));
	}
}
