/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package uk.ac.gda.client.tomo.composites;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Orientable;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A triangular graphical figure.
 */
public final class RotationSliderShape extends Shape implements Orientable {

	/**
	 * The direction this triangle will face. Possible values are {@link PositionConstants#NORTH},
	 * {@link PositionConstants#SOUTH}, {@link PositionConstants#EAST} and {@link PositionConstants#WEST}.
	 */
	protected int direction = NORTH;
	/**
	 * The orientation of this triangle. Possible values are {@link Orientable#VERTICAL} and
	 * {@link Orientable#HORIZONTAL}.
	 */
	protected int orientation = VERTICAL;

	/** The points of the triangle. */
	protected PointList sliderShape = new PointList(8);

	/**
	 * @see Shape#fillShape(Graphics)
	 */
	@Override
	protected void fillShape(Graphics g) {
		g.fillPolygon(sliderShape);
	}

	/**
	 * @see Shape#outlineShape(Graphics)
	 */
	@Override
	protected void outlineShape(Graphics g) {
		g.drawPolygon(sliderShape);
	}

	/**
	 * @see Figure#primTranslate(int, int)
	 */
	@Override
	public void primTranslate(int dx, int dy) {
		super.primTranslate(dx, dy);
		sliderShape.translate(dx, dy);
	}

	/**
	 * @see Orientable#setDirection(int)
	 */
	@Override
	public void setDirection(int value) {
		if ((value & (NORTH | SOUTH)) != 0)
			orientation = VERTICAL;
		else
			orientation = HORIZONTAL;
		direction = value;
		revalidate();
		repaint();
	}

	/**
	 * @see Orientable#setOrientation(int)
	 */
	@Override
	public void setOrientation(int value) {
		if (orientation == VERTICAL && value == HORIZONTAL) {
			if (direction == NORTH)
				setDirection(WEST);
			else
				setDirection(EAST);
		}
		if (orientation == HORIZONTAL && value == VERTICAL) {
			if (direction == WEST)
				setDirection(NORTH);
			else
				setDirection(SOUTH);
		}
	}

	/**
	 * @see IFigure#validate()
	 */
	@Override
	public void validate() {
		super.validate();
		Rectangle r = new Rectangle();
		r.setBounds(getBounds());
		r.shrink(getInsets());
		r.resize(-1, -1);
		int size = Math.min(r.height, r.width / 2);
		r.y += (r.height - size) / 2;

		size = Math.max(size, 1); // Size cannot be negative

		Point head = new Point(r.x + r.width / 2, r.y - 4);
		Point p2 = new Point(r.x + r.width / 2, head.y + 5);
		Point p3 = new Point(head.x - size, head.y + 5);
		Point p4 = new Point(head.x - size, head.y + 15);
		Point p5 = new Point(head.x + size, head.y + 15);
		Point p6 = new Point(head.x + size, head.y + 5);
		Point p7 = new Point(r.x + r.width / 2, head.y + 5);

		/*
		 * 
		 *     p4---------p5
		 *     -          -
		 *     -          -
		 *     p3--p2/7---p6 
		 *           -
		 *           -
		 *           -       head
		 * 
		 * 
		 */
		if (direction == SOUTH) {
			// Coarse is south
			head = new Point(r.x + r.width / 2, r.y + size + 2);
			p2 = new Point(r.x + r.width / 2, head.y - (size / 3));

			p3 = new Point(head.x - size, head.y - (size / 3));
			p4 = new Point(head.x - size, head.y - size);

			p5 = new Point(head.x + size, head.y - size);
			p6 = new Point(head.x + size, head.y - (size / 3));

			p7 = new Point(r.x + r.width / 2, head.y - (size / 3));
		}
		sliderShape.removeAllPoints();
		sliderShape.addPoint(head);
		sliderShape.addPoint(p2);
		sliderShape.addPoint(p3);
		sliderShape.addPoint(p4);
		sliderShape.addPoint(p5);
		sliderShape.addPoint(p6);
		sliderShape.addPoint(p7);

	}

}
