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

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An enum of the various markers allowed in SimplePlots
 */
public enum Marker {

	/**  */
	BOX(Box.class, false, "Box"),
	/**  */
	CIRCLE(Circle.class, false, "Circle"),
	/**  */
	CROSS(Cross.class, false, "Cross"),
	/**  */
	STAR(Star.class, false, "Star"),
	/**  */
	DIAMOND(Diamond.class, false, "Diamond"),
	/**  */
	SQUARE(Box.class, true, "Square"),
	/**  */
	DOT(Circle.class, true, "Dot"),
	/**  */
	LOZENGE(Diamond.class, true, "Lozenge"),
	/**  */
	VERT_LINE(VerticalLine.class, false, "Vertical Line"),
	/**  */
	HORIZ_LINE(HorizontalLine.class, false, "HorizontalLine");

	private boolean filled;

	private Constructor<?> constructor;

	private String bestName;

	private final Logger logger = LoggerFactory.getLogger("Marker");

	private Marker(Class<?> toCreate, boolean filled, String bestName) {
		this.filled = filled;
		this.bestName = bestName;
		try {
			constructor = toCreate.getDeclaredConstructor(new Class[] { int.class });
		} catch (SecurityException e) {
			logger.error("SecurityException in Marker constructor");
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException in Marker constructor");
		}
	}

	/**
	 * Returns whether or not the marker is a filled shape
	 *
	 * @return true if filled, otherwise false
	 */
	public boolean isFilled() {
		return filled;
	}

	/**
	 * Returns the Shape of the marker type for the given size
	 *
	 * @param size
	 *            the size in pixels e.g. for CIRCLE this will become the diameter
	 * @return a Shape object of the correct size
	 */
	Shape getShape(int size) {
		Shape shape = null;
		try {
			shape = (Shape) constructor.newInstance(new Object[] { new Integer(size) });
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException in Marker.getShape");
		} catch (SecurityException e) {
			logger.error("SecurityException in Marker.getShape");
		} catch (InstantiationException e) {
			logger.error("InstantiationException in Marker.getShape");
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException in Marker.getShape");
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException in Marker.getShape");
		}

		return shape;
	}

	/**
	 * Returns the display name (which may be different from the Enum name)
	 *
	 * @return the display name
	 */
	@Override
	public String toString() {
		return bestName;
	}

	/**
	 * Creates a Marker from the int given
	 *
	 * @param counter
	 * @return the Marker with that ordinal (default BOX)
	 */
	public static Marker fromCounter(int counter) {
		Marker fromCounter = BOX;
		for (Marker m : Marker.values()) {
			if (m.ordinal() == counter) {
				fromCounter = m;
				break;
			}
		}

		return fromCounter;
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class Box extends Rectangle2D.Double {
		/**
		 * @param size
		 */
		public Box(int size) {
			super(-size, -size, size * 2, size * 2);
		}
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class Circle extends Ellipse2D.Double {
		/**
		 * @param size
		 */
		public Circle(int size) {
			super(-size, -size, size * 2, size * 2);
		}
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class VerticalLine extends Line2D.Double {
		/**
		 * @param size
		 */
		public VerticalLine(int size) {
			super(0, -size, 0, size);
		}
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class HorizontalLine extends Line2D.Double {
		/**
		 * @param size
		 */
		public HorizontalLine(int size) {
			super(-size, 0, size, 0);
		}
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class Diamond extends Polygon {
		/**
		 * @param size
		 */
		public Diamond(int size) {
			super(new int[] { -size, 0, size, 0 }, new int[] { 0, -size, 0, size }, 4);
		}
	}

	/**
	 * More complicated markers have to be made up from lines in a GeneralPath. Unfortunately GeneralPath is final and
	 * so must be wrapped rather than extended. The wrapper also implements the Shape methods by delegating to the
	 * GeneralPath.
	 */
	public static class GeneralPathWrapper implements Shape {
		private GeneralPath gp = null;

		/**
		 * Creates the wrapper and adds elements to the path
		 *
		 * @param shapes
		 *            an array of Shapes to put in the path
		 */
		public GeneralPathWrapper(Shape[] shapes) {
			gp = new GeneralPath();
			for (int i = 0; i < shapes.length; i++) {
				gp.append(shapes[i], false);
			}
		}

		// Methods to implement Shape interface (all just delegate to gp)
		@Override
		public boolean contains(double x, double y) {
			return gp.contains(x, y);
		}

		@Override
		public boolean contains(double x, double y, double w, double h) {
			return gp.contains(x, y, w, h);
		}

		@Override
		public boolean intersects(double x, double y, double w, double h) {
			return gp.intersects(x, y, w, h);
		}

		@Override
		public Rectangle getBounds() {
			return gp.getBounds();
		}

		@Override
		public boolean contains(Point2D p) {
			return gp.contains(p);
		}

		@Override
		public Rectangle2D getBounds2D() {
			return gp.getBounds2D();
		}

		@Override
		public boolean contains(Rectangle2D r) {
			return gp.contains(r);
		}

		@Override
		public boolean intersects(Rectangle2D r) {
			return gp.intersects(r);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at) {
			return gp.getPathIterator(at);
		}

		@Override
		public PathIterator getPathIterator(AffineTransform at, double flatness) {
			return gp.getPathIterator(at, flatness);
		}
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class Cross extends GeneralPathWrapper {
		/**
		 * @param size
		 */
		public Cross(int size) {
			super(new Shape[] { new HorizontalLine(size), new VerticalLine(size), });
		}
	}

	/**
	 * Must be public to satisfy java security because instances of them are created and returned by the getShape method -
	 * should not be constructed directly.
	 */
	public static class Star extends GeneralPathWrapper {
		/**
		 * @param size
		 */
		public Star(int size) {
			super(new Shape[] { new HorizontalLine(size), new VerticalLine(size),
					new Line2D.Double(-size, -size, size, size), new Line2D.Double(-size, size, size, -size) });
		}
	}
}
