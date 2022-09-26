/*-
O * Copyright © 2011 Diamond Light Source Ltd.
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overlay Image Figure - to display images on top of the main image
 */
public class OverlayImageFigure extends ImageFigure {

	private final static Logger logger = LoggerFactory.getLogger(OverlayImageFigure.class);

	public enum MOVE_AXIS {
		/**
		 *
		 */
		X_AXIS {

			@Override
			public Point performDrag(Point movedPoint, MouseEvent mouseEvent) {
				Point p = mouseEvent.getLocation();
				Dimension delta = p.getDifference(movedPoint);
				Figure f = ((Figure) mouseEvent.getSource());
				// Restricted drag movement for the triangle
				if (delta.width + f.getBounds().width >= 0) {
					if (f.getLocation().x + delta.width + 20 < f.getParent().getSize().width) {
						f.setBounds(f.getBounds().getTranslated(delta.width, 0));
					}
				}
				return p;
			}
		},

		/**
		 *
		 */
		Y_AXIS {

			@Override
			public Point performDrag(Point movedPoint, MouseEvent mouseEvent) {
				Point p = mouseEvent.getLocation();
				Dimension delta = p.getDifference(movedPoint);
				Figure f = ((Figure) mouseEvent.getSource());
				// Restricted drag movement for the triangle
				if (delta.height + f.getBounds().height >= 0) {
					if (f.getLocation().y + delta.height + 20 < f.getParent().getSize().height) {
						f.setBounds(f.getBounds().getTranslated(0, delta.height));
						logger.info("figure new bounds" + f.getBounds());
					}
				}
				return p;
			}
		};

		public abstract Point performDrag(Point movedPoint, MouseEvent mouseEvent);

	}

	public interface OverlayImgFigureListener {
		void performOverlayImgMoved(Point initialPoint, Point movedPoint, Dimension difference);

		void cancelMove();

		void mouseClicked();
	}

	private Set<OverlayImgFigureListener> listeners = new HashSet<OverlayImageFigure.OverlayImgFigureListener>();

	/**
	 * @param listener
	 */
	public void addOverlayImgFigureListener(OverlayImgFigureListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param listener
	 */
	public void removeOverlayImgFigureListener(OverlayImgFigureListener listener) {
		listeners.remove(listener);
	}

	boolean imageMoved = false;

	private MOVE_AXIS moveAxis = MOVE_AXIS.X_AXIS;

	private final Canvas canvas;

	/**
	 * @param moveAxis
	 */
	protected void setMoveAxis(MOVE_AXIS moveAxis) {
		this.moveAxis = moveAxis;
	}

	public OverlayImageFigure(Canvas canvas) {
		this.canvas = canvas;
		configureMouseListener();
	}

	public void configureMouseListener() {
		this.addMouseMotionListener(mouseListener);
		this.addMouseListener(mouseListener);

		canvas.addKeyListener(keyListener);
	}

	@Override
	public void paint(Graphics graphics) {
		// Setting the alpha of the image to be laid in this figure to 25
		graphics.setAlpha(80);
		super.paint(graphics);
	}

	private KeyListener keyListener = new KeyListener() {

		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent ke) {
			if (ke.keyCode == SWT.ESC) {
				suspend();
				//
				for (OverlayImgFigureListener lis : listeners) {
					lis.cancelMove();
				}
			}
		}

		@Override
		public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
			// do nothing
		}

	};
	private Dragger mouseListener = new Dragger();

	class Dragger extends MouseMotionListener.Stub implements MouseListener {
		private Point movedPoint;
		private Point initialPoint;

		/**
		 * This field is used to digest whether the pressed/drag was generated from here. We need a 2 step process, to
		 * stop and then to drag the image. Somehow, the drag gets notified before the press on this listener.<br>
		 * So to ensure that the mouse-press has been issued from here, this field is used.
		 */
		private boolean isGeneratedWithin = false;

		@Override
		public void mouseReleased(MouseEvent e) {
			if (isGeneratedWithin) {
				isGeneratedWithin = false;
				logger.info("dragger mouse released");
				Dimension locationDifference = e.getLocation().getDifference(initialPoint);
				//
				for (OverlayImgFigureListener lis : listeners) {
					lis.performOverlayImgMoved(initialPoint, movedPoint, locationDifference);
				}
				e.consume();
			}
		}

		@SuppressWarnings("unused")
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseDoubleClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			initialPoint = e.getLocation();
			movedPoint = e.getLocation();
			isGeneratedWithin = true;
			for (OverlayImgFigureListener lis : listeners) {
				lis.mouseClicked();
			}
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (isGeneratedWithin) {
				Point newMovedPoint = moveAxis.performDrag(movedPoint, e);
				if (!newMovedPoint.equals(movedPoint)) {
					imageMoved = true;
					movedPoint = newMovedPoint;
				}
			}

			// moved = true;
		}

	}

	public boolean hasImageMoved() {
		return imageMoved;
	}

	public void suspend() {
		if (getImage() != null) {
			getImage().dispose();
		}
		this.removeMouseListener(mouseListener);
		this.removeMouseMotionListener(mouseListener);
		canvas.removeKeyListener(keyListener);
		this.setSize(1, 1);
	}
}
