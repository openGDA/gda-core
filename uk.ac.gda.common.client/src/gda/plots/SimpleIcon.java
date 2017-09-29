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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

/**
 * Class to provide very simple icons which can be used, for example, on the menus used to choose Markers, Types,
 * Patterns.
 *
 * @see LinePropertiesEditor
 */
class SimpleIcon implements Icon {
	private Shape shape;

	private Stroke stroke = new BasicStroke(1);

	private boolean filled = false;

	private int height = 18;

	private int width = 18;

	/**
	 * Creates a SimpleIcon to represent a line.
	 *
	 * @param width
	 *            the width for the Icon
	 * @param height
	 *            the height for the Icon
	 */
	SimpleIcon(int width, int height) {
		shape = new Line2D.Double(0, 0, width, 0);
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a SimpleIcon to represent a Marker.
	 *
	 * @param marker
	 *            the Marker
	 * @param width
	 *            the width for the Icon
	 * @param height
	 *            the height for the Icon
	 */
	SimpleIcon(Marker marker, int width, int height) {
		shape = marker.getShape(5);
		filled = marker.isFilled();
		this.height = height;
		this.width = width;
	}

	/**
	 * Creates a SimpleIcon to represent a particular value of an Enum.
	 *
	 * @param e
	 *            the Enum
	 * @param width
	 *            the width for the Icon
	 * @param height
	 *            the height for the Icon
	 */
	SimpleIcon(Enum<?> e, int width, int height) {
		if (Marker.class.equals(e.getClass())) {
			Object o = e;
			shape = ((Marker) o).getShape(5);
			filled = ((Marker) o).isFilled();
			this.height = height;
			this.width = width;
		}

		if (Type.class.equals(e.getClass())) {
			Object o = e;
			Type type = (Type) o;
			GeneralPath gp = new GeneralPath();
			if (type.getDrawPoints()) {
				gp.append(new Rectangle2D.Double(-1.5, -1.5, 3.0, 3.0), false);
			}
			if (type.getDrawLine()) {
				gp.append(new Line2D.Double(0, 0, width, 0), false);
			}
			if (type.getDrawPoints()) {
				gp.append(new Rectangle2D.Double(width - 1.5, -1.5, 3.0, 3.0), false);
			}
			shape = gp;
			this.width = width;
		}
		if (Pattern.class.equals(e.getClass())) {
			Object o = e;
			Pattern pattern = (Pattern) o;
			shape = new Line2D.Double(0, 0, width, 0);
			stroke = pattern.getStroke(1);
			this.width = width;
		}
	}

	/**
	 * Part of Icon interface, returns the width.
	 *
	 * @return the width
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	/**
	 * Part of Icon interface, returns the height.
	 *
	 * @return the height
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/**
	 * Part of the Icon interface, paints the Icon in the given Component with the given Graphics at the given x and y
	 * position.
	 *
	 * @param c
	 *            the Component being painted into
	 * @param g
	 *            the Graphics to use
	 * @param x
	 *            the x position (within the Graphics)
	 * @param y
	 *            the y position (within the Graphics)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g;
		Stroke saved = g2.getStroke();
		g2.setStroke(stroke);
		g2.translate(width / 2, height / 2);
		if (filled) {
			g2.fill(shape);
		} else {
			g2.draw(shape);
		}
		g2.translate(-width / 2, -height / 2);
		g2.setStroke(saved);
	}
}
