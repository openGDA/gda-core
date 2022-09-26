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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import java.util.ArrayList;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 *
 */
public class CrossWireMouseListener implements MouseListener, MouseMotionListener {

	private boolean crossWireMousePressed;
	private Point initialPoint;

	public interface CrosswireListener {

		/**
		 * @param pixelMoved
		 */
		public void performAction(int pixelMoved);
	}

	private ArrayList<CrosswireListener> crossWireListeners = new ArrayList<CrossWireMouseListener.CrosswireListener>();
	private int imageCentre = -1;
	private int min;
	private int max;

	public boolean addCrossWireListener(CrosswireListener crossWireListener) {
		return this.crossWireListeners.add(crossWireListener);
	}

	public boolean removeCrossWireListener(CrosswireListener crossWireListener) {
		return this.crossWireListeners.remove(crossWireListener);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if (crossWireMousePressed) {
			Polyline source = (Polyline) me.getSource();
			PointList points = source.getPoints();
			Rectangle bounds = points.getBounds();

			int h = bounds.height;
			if (me.x == imageCentre) {
				source.setForegroundColor(ColorConstants.darkBlue);
			} else {
				source.setForegroundColor(ColorConstants.white);
			}

			if (me.x == min || me.x == max) {
				source.setForegroundColor(ColorConstants.red);
			} else {
				source.setForegroundColor(ColorConstants.white);
			}

			int xVal = me.x;
			if (me.x <= min) {
				xVal = min;
			} else if (me.x >= max) {
				xVal = max;
			}

			source.setPoints(new PointList(new int[] { xVal, 0, xVal, h }));
		}

	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
		if (crossWireMousePressed) {
			Polyline source = (Polyline) me.getSource();
			PointList points = source.getPoints();
			Rectangle bounds = points.getBounds();

			int h = bounds.height;

			source.setPoints(new PointList(new int[] { initialPoint.x, 0, initialPoint.x, h }));
			crossWireMousePressed = false;
		}
	}

	@Override
	public void mouseHover(MouseEvent me) {
		// Do nothing

	}

	@Override
	public void mouseMoved(MouseEvent me) {
		// Do nothing
		if (crossWireMousePressed) {
//			Polyline source = (Polyline) me.getSource();
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		crossWireMousePressed = true;
		Polyline source = (Polyline) me.getSource();
		source.setTolerance(40);
		initialPoint = me.getLocation();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		me.consume();
		crossWireMousePressed = false;
		Point finalLocation = me.getLocation();
		finalLocation.getDifference(initialPoint);
		for (CrosswireListener lis : crossWireListeners) {
			lis.performAction(finalLocation.x - initialPoint.x);
		}
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		crossWireMousePressed = false;
		Polyline source = (Polyline) me.getSource();
		PointList points = source.getPoints();
		Rectangle bounds = points.getBounds();

		int h = bounds.height;

		int x = bounds.x;
		if (imageCentre != -1) {
			source.setPoints(new PointList(new int[] { imageCentre, 0, imageCentre, h }));
			for (CrosswireListener lis : crossWireListeners) {
				lis.performAction(imageCentre - x);
			}
		}

	}

	public void setImageCentre(int imageCentre) {
		this.imageCentre = imageCentre;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setMax(int max) {
		this.max = max;
	}

}