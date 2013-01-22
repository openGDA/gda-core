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

package uk.ac.gda.epics.adviewer.composites.imageviewer;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineShape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.swt.graphics.Color;

public class PointFigure extends PolylineShape {


	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.pushState();
		graphics.translate(getLocation());
		PointList points = getPoints();
		Color backgroundColor = graphics.getBackgroundColor();
		graphics.setBackgroundColor(graphics.getForegroundColor());
		for (int i = 0; i < points.size(); i++) {
			Point p = points.getPoint(i);
			graphics.drawOval(p.x, p.y, 1, 1);
			graphics.fillOval(p.x, p.y, 2, 2);
		}
		graphics.setBackgroundColor(backgroundColor);
		graphics.popState();
	}

}
