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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.OverlayImageFigure.OverlayImgFigureListener;

/**
 * This represents the left window image viewer in the Tomography Alignment view - It shows the full image on the view,
 * also features for Cross Hair, Zoom Rectangle, profiling across with draggable slider.
 */
public class FullImageComposite extends FixedImageViewerComposite {
	private static final String HIDE_CROSSHAIR = "Hide Crosshair";
	private static final String SHOW_CROSSHAIR = "Show Crosshair";
	private final static Logger logger = LoggerFactory.getLogger(FullImageComposite.class);
	// private RectangleFigure rectFigure;
	private Polyline horizontalTop;
	private Polyline horizontalBottom;
	private Polyline verticalLeft;
	private Polyline verticalRight;
	private boolean roiWidgetMoved = false;
	private LinePointMouseListener linePointMouseListener;
	private Ellipse leftTopPoint;
	private Ellipse rightTopPoint;
	private Ellipse leftBottomPoint;
	private Ellipse rightBottomPoint;
	protected Polyline crossWireVertical1;

	protected Polyline crossWireHorizontal2;
	protected Polyline crossWireVertical2;

	private ListenerList listenerList = new ListenerList();

	public void addRoiPointsListener(IRoiPointsListener roiPointsListener) {
		listenerList.add(roiPointsListener);
	}

	public void removeRoiPointsListener(IRoiPointsListener roiPointsListener) {
		listenerList.remove(roiPointsListener);
	}

	public interface IRoiPointsListener {
		/**
		 * Invokes the listeners that the roi boundaries have changed
		 * 
		 * @param directionChanged
		 * @param x1
		 * @param y1
		 * @param x2
		 * @param y2
		 */
		public void roiPointsChanged(int directionChanged, int x1, int y1, int x2, int y2);
	}

	/**
	 * Hides the cross wire
	 */
	public void hideCrossWire1() {
		if (crossWireVertical1.isVisible()) {
			crossWireVertical1.setVisible(false);
		}
	}

	public void showCrossWire1() {
		if (!crossWireVertical1.isVisible()) {
			crossWireVertical1.setVisible(true);
		}
	}

	/**
	 * Hides the cross wire
	 */
	public void hideCrossWire2() {
		if (crossWireHorizontal2.isVisible()) {
			crossWireHorizontal2.setVisible(false);
		}
		if (crossWireVertical2.isVisible()) {
			crossWireVertical2.setVisible(false);
		}
	}

	public void showCrossWire2() {
		crossWireVertical2.setPoints(new PointList(new int[] { feedbackFigure.getSize().width / 2, 0,
				feedbackFigure.getSize().width / 2, feedbackFigure.getSize().height }));

		crossWireHorizontal2.setPoints(new PointList(new int[] { 0, feedbackFigure.getSize().height / 2,
				feedbackFigure.getSize().width, feedbackFigure.getSize().height / 2 }));
		if (!crossWireHorizontal2.isVisible()) {
			crossWireHorizontal2.setVisible(true);
		}
		if (!crossWireVertical2.isVisible()) {
			crossWireVertical2.setVisible(true);
		}
	}

	public FullImageComposite(Composite parent, int style) {
		super(parent, style);
	}

	public FullImageComposite(Composite parent, int style, boolean showProfileComposite) {
		super(parent, style, showProfileComposite);

		linePointMouseListener = new LinePointMouseListener();

		horizontalTop = new Polyline();
		horizontalBottom = new Polyline();
		verticalLeft = new Polyline();
		verticalRight = new Polyline();

		horizontalTop.setVisible(false);
		horizontalBottom.setVisible(false);
		verticalLeft.setVisible(false);
		verticalRight.setVisible(false);

		horizontalTop.setLineWidth(2);
		horizontalTop.setForegroundColor(ColorConstants.darkGreen);

		horizontalBottom.setLineWidth(2);
		horizontalBottom.setForegroundColor(ColorConstants.darkGreen);

		verticalLeft.setLineWidth(2);
		verticalLeft.setForegroundColor(ColorConstants.darkGreen);
		verticalRight.setLineWidth(2);
		verticalRight.setForegroundColor(ColorConstants.darkGreen);

		// rectFigure = new RectangleFigure();
		// feedbackFigure.add(rectFigure, new Rectangle(20, 20, 200, 200));

		leftTopPoint = new Ellipse();
		leftTopPoint.setSize(5, 5);
		leftTopPoint.setForegroundColor(ColorConstants.red);
		leftTopPoint.setOpaque(true);
		leftTopPoint.setFill(true);
		leftTopPoint.setLineWidth(3);
		leftTopPoint.setLayoutManager(new XYLayout());
		leftTopPoint.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENW));
		leftTopPoint.setVisible(false);
		leftTopPoint.addMouseListener(linePointMouseListener);
		leftTopPoint.addMouseMotionListener(linePointMouseListener);

		rightTopPoint = new Ellipse();
		rightTopPoint.setSize(5, 5);
		rightTopPoint.setForegroundColor(ColorConstants.red);
		rightTopPoint.setOpaque(true);
		rightTopPoint.setFill(true);
		rightTopPoint.setLineWidth(3);
		rightTopPoint.setLayoutManager(new XYLayout());
		rightTopPoint.setVisible(false);
		rightTopPoint.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENE));
		rightTopPoint.addMouseListener(linePointMouseListener);
		rightTopPoint.addMouseMotionListener(linePointMouseListener);

		leftBottomPoint = new Ellipse();
		leftBottomPoint.setSize(5, 5);
		leftBottomPoint.setForegroundColor(ColorConstants.red);
		leftBottomPoint.setOpaque(true);
		leftBottomPoint.setFill(true);
		leftBottomPoint.setLineWidth(3);
		leftBottomPoint.setLayoutManager(new XYLayout());
		leftBottomPoint.setVisible(false);
		leftBottomPoint.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENE));
		leftBottomPoint.addMouseListener(linePointMouseListener);
		leftBottomPoint.addMouseMotionListener(linePointMouseListener);

		rightBottomPoint = new Ellipse();
		rightBottomPoint.setSize(5, 5);
		rightBottomPoint.setForegroundColor(ColorConstants.red);
		rightBottomPoint.setOpaque(true);
		rightBottomPoint.setFill(true);
		rightBottomPoint.setLineWidth(3);
		rightBottomPoint.setLayoutManager(new XYLayout());
		rightBottomPoint.setVisible(false);
		rightBottomPoint.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENW));
		rightBottomPoint.addMouseListener(linePointMouseListener);
		rightBottomPoint.addMouseMotionListener(linePointMouseListener);

		feedbackFigure.add(horizontalTop);
		feedbackFigure.add(horizontalBottom);
		feedbackFigure.add(verticalLeft);
		feedbackFigure.add(verticalRight);
		feedbackFigure.add(leftTopPoint);
		feedbackFigure.add(leftBottomPoint);
		feedbackFigure.add(rightTopPoint);
		feedbackFigure.add(rightBottomPoint);

		crossWireHorizontal2 = new Polyline();
		crossWireHorizontal2.setLineWidth(1);
		crossWireHorizontal2.setForegroundColor(ColorConstants.orange);
		crossWireHorizontal2.setVisible(false);
		feedbackFigure.add(crossWireHorizontal2);

		crossWireVertical2 = new Polyline();
		crossWireVertical2.setLineWidth(1);
		crossWireVertical2.setForegroundColor(ColorConstants.orange);
		crossWireVertical2.setVisible(false);
		feedbackFigure.add(crossWireVertical2);
		//
		crossWireVertical1 = new Polyline();
		crossWireVertical1.setLineWidth(1);
		crossWireVertical1.setForegroundColor(ColorConstants.white);
		crossWireVertical1.setVisible(false);
		feedbackFigure.add(crossWireVertical1);

	}

	private class LinePointMouseListener implements MouseMotionListener, MouseListener {

		private boolean mouseLock;
		@SuppressWarnings("unused")
		private Point location;

		@Override
		public void mousePressed(MouseEvent me) {
			mouseLock = true;
			location = me.getLocation();
			me.consume();
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (mouseLock) {
				mouseLock = false;
				for (Object listener : listenerList.getListeners()) {
					Dimension horizontalTopFirstPoint = horizontalTop.getPoints().getFirstPoint()
							.getDifference(getImageBounds().getLocation());
					Dimension horizontalBottomLastPoint = horizontalBottom.getPoints().getLastPoint()
							.getDifference(getImageBounds().getLocation());
					logger.debug("x1: " + horizontalTopFirstPoint.width + "  y1:" + horizontalTopFirstPoint.height
							+ "  x2:" + horizontalBottomLastPoint.width + "  y2:" + horizontalBottomLastPoint.height);
					Object source = me.getSource();

					int direction = SWT.LEFT;
					if (source == rightBottomPoint || source == rightTopPoint || source == verticalRight) {
						direction = SWT.RIGHT;
					}
					((IRoiPointsListener) listener).roiPointsChanged(direction, horizontalTopFirstPoint.width,
							horizontalTopFirstPoint.height, horizontalBottomLastPoint.width,
							horizontalBottomLastPoint.height);

				}
			}
		}

		@Override
		public void mouseDoubleClicked(MouseEvent me) {

		}

		@Override
		public void mouseDragged(MouseEvent me) {
			if (mouseLock) {
				Point draggedLoc = me.getLocation();

				int x1 = draggedLoc.x;
				int y1 = draggedLoc.y;
				if (me.getSource() == leftTopPoint) {
					if (x1 < getImageBounds().x) {
						x1 = getImageBounds().x;
					} else if (x1 > verticalRight.getPoints().getFirstPoint().x - 20) {
						x1 = verticalRight.getPoints().getFirstPoint().x - 20;
					}
					if (y1 < getImageBounds().y) {
						y1 = getImageBounds().y;
					} else if (y1 > horizontalBottom.getPoints().getFirstPoint().y - 20) {
						y1 = horizontalBottom.getBounds().y - 20;
					}

					// horizontal top and vertical left

					int horizontalBottomY = horizontalBottom.getPoints().getFirstPoint().y;
					int verticalRightX = verticalRight.getPoints().getFirstPoint().x;

					horizontalTop.setPoints(new PointList(new int[] { x1, y1, verticalRightX, y1 }));
					verticalLeft.setPoints(new PointList(new int[] { x1, y1, x1, horizontalBottomY }));
					horizontalBottom.setPoints(new PointList(new int[] { x1, horizontalBottomY, verticalRightX,
							horizontalBottomY }));
					verticalRight.setPoints(new PointList(new int[] { verticalRightX, y1, verticalRightX,
							horizontalBottomY }));

					leftTopPoint.setLocation(new Point(x1 - 2, y1 - 2));
					leftBottomPoint.setLocation(new Point(x1 - 2, horizontalBottomY - 2));
					rightTopPoint.setLocation(new Point(verticalRightX - 2, y1 - 2));
				} else if (me.getSource() == leftBottomPoint) {
					if (x1 < getImageBounds().x) {
						x1 = getImageBounds().x;
					} else if (x1 > verticalRight.getPoints().getFirstPoint().x - 20) {
						x1 = verticalRight.getPoints().getFirstPoint().x - 20;
					}
					if (y1 < horizontalTop.getPoints().getFirstPoint().y + 20) {
						y1 = horizontalTop.getPoints().getFirstPoint().y + 20;
					} else if (y1 > getImageBounds().y + getImageBounds().height) {
						y1 = getImageBounds().y + getImageBounds().height;
					}

					int horizontalTopY = horizontalTop.getPoints().getFirstPoint().y;
					int verticalRightX = verticalRight.getPoints().getFirstPoint().x;

					// horizontal bottom and vertical left

					horizontalBottom.setPoints(new PointList(new int[] { x1, y1, verticalRightX, y1 }));
					verticalLeft.setPoints(new PointList(new int[] { x1, horizontalTopY, x1, y1 }));
					horizontalTop.setPoints(new PointList(new int[] { x1, horizontalTopY, verticalRightX,
							horizontalTopY }));
					verticalRight.setPoints(new PointList(new int[] { verticalRightX, horizontalTopY, verticalRightX,
							y1 }));

					leftTopPoint.setLocation(new Point(x1 - 2, horizontalTopY - 2));
					leftBottomPoint.setLocation(new Point(x1 - 2, y1 - 2));
					rightBottomPoint.setLocation(new Point(verticalRightX - 2, y1 - 2));
				} else if (me.getSource() == rightTopPoint) {
					// horizontal top and vertical right
					if (x1 < verticalLeft.getPoints().getFirstPoint().x + 20) {
						x1 = verticalLeft.getPoints().getFirstPoint().x + 20;
					} else if (x1 > getImageBounds().x + getImageBounds().width) {
						x1 = getImageBounds().x + getImageBounds().width;
					}
					if (y1 < getImageBounds().y) {
						y1 = getImageBounds().y;
					} else if (y1 > horizontalBottom.getPoints().getFirstPoint().y - 20) {
						y1 = horizontalBottom.getBounds().y - 20;
					}

					int horizontalBottomY = horizontalBottom.getPoints().getFirstPoint().y;
					int verticalLeftX = verticalLeft.getPoints().getFirstPoint().x;

					horizontalTop.setPoints(new PointList(new int[] { verticalLeftX, y1, x1, y1 }));
					verticalRight.setPoints(new PointList(new int[] { x1, y1, x1, horizontalBottomY }));

					verticalLeft.setPoints(new PointList(new int[] { verticalLeftX, y1, verticalLeftX,
							horizontalBottomY }));
					horizontalBottom.setPoints(new PointList(new int[] { verticalLeftX, horizontalBottomY, x1,
							horizontalBottomY }));

					rightTopPoint.setLocation(new Point(x1 - 2, y1 - 2));
					leftTopPoint.setLocation(new Point(verticalLeftX - 2, y1 - 2));
					rightBottomPoint.setLocation(new Point(x1 - 2, horizontalBottomY - 2));
				} else if (me.getSource() == rightBottomPoint) {
					// horizontal bottom and vertical right
					if (x1 < verticalLeft.getPoints().getFirstPoint().x + 20) {
						x1 = verticalLeft.getPoints().getFirstPoint().x + 20;
					} else if (x1 > getImageBounds().x + getImageBounds().width) {
						x1 = getImageBounds().x + getImageBounds().width;
					}
					if (y1 < horizontalTop.getPoints().getFirstPoint().y + 20) {
						y1 = horizontalTop.getPoints().getFirstPoint().y + 20;
					} else if (y1 > getImageBounds().y + getImageBounds().height) {
						y1 = getImageBounds().y + getImageBounds().height;
					}
					int horizontalTopY = horizontalTop.getPoints().getFirstPoint().y;
					int verticalLeftX = verticalLeft.getPoints().getFirstPoint().x;

					horizontalBottom.setPoints(new PointList(new int[] { verticalLeftX, y1, x1, y1 }));
					verticalRight.setPoints(new PointList(new int[] { x1, horizontalTopY, x1, y1 }));

					horizontalTop.setPoints(new PointList(
							new int[] { verticalLeftX, horizontalTopY, x1, horizontalTopY }));
					verticalLeft
							.setPoints(new PointList(new int[] { verticalLeftX, horizontalTopY, verticalLeftX, y1 }));
					rightBottomPoint.setLocation(new Point(x1 - 2, y1 - 2));
					rightTopPoint.setLocation(new Point(x1 - 2, horizontalTopY - 2));
					leftBottomPoint.setLocation(new Point(verticalLeftX - 2, y1 - 2));

				} else if (me.getSource() == horizontalTop) {
					x1 = verticalLeft.getPoints().getFirstPoint().x;

					if (y1 < getImageBounds().y) {
						y1 = getImageBounds().y;
					} else if (y1 > horizontalBottom.getPoints().getFirstPoint().y - 20) {
						y1 = horizontalBottom.getBounds().y - 20;
					}
					int x2 = verticalRight.getPoints().getFirstPoint().x;
					int y2 = y1;
					horizontalTop.setPoints(new PointList(new int[] { x1, y1, x2, y2 }));

					int verticalRightX = verticalRight.getPoints().getFirstPoint().x;
					int horizontalBottomY = horizontalBottom.getPoints().getFirstPoint().y;

					verticalLeft.setPoints(new PointList(new int[] { x1, y1, x1,
							horizontalBottom.getPoints().getFirstPoint().y }));

					verticalRight.setPoints(new PointList(new int[] { verticalRightX, y1, verticalRightX,
							horizontalBottomY }));

					leftTopPoint.setLocation(new Point(x1 - 2, y1 - 2));
					rightTopPoint.setLocation(new Point(verticalRightX - 2, y1 - 2));

					location = draggedLoc;

				} else if (me.getSource() == horizontalBottom) {

					x1 = verticalLeft.getPoints().getFirstPoint().x;

					if (y1 < horizontalTop.getPoints().getFirstPoint().y + 20) {
						y1 = horizontalTop.getPoints().getFirstPoint().y + 20;
					} else if (y1 > getImageBounds().y + getImageBounds().height) {
						y1 = getImageBounds().y + getImageBounds().height;
					}

					int x2 = verticalRight.getPoints().getFirstPoint().x;
					int y2 = y1;

					horizontalBottom.setPoints(new PointList(new int[] { x1, y1, x2, y2 }));

					int horizontalUpY = horizontalTop.getPoints().getFirstPoint().y;
					int verticalRightX = verticalRight.getPoints().getFirstPoint().x;

					verticalLeft.setPoints(new PointList(new int[] { x1, horizontalUpY, x1, y1 }));
					verticalRight.setPoints(new PointList(
							new int[] { verticalRightX, horizontalUpY, verticalRightX, y1 }));
					//
					leftBottomPoint.setLocation(new Point(x1 - 2, y1 - 2));
					rightBottomPoint.setLocation(new Point(verticalRightX - 2, y1 - 2));
					//
					location = draggedLoc;

				} else if (me.getSource() == verticalLeft) {

					y1 = horizontalTop.getPoints().getFirstPoint().y;

					if (x1 < getImageBounds().x) {
						x1 = getImageBounds().x;
					} else if (x1 > verticalRight.getPoints().getFirstPoint().x - 20) {
						x1 = verticalRight.getPoints().getFirstPoint().x - 20;
					}
					int y2 = horizontalBottom.getBounds().y;
					int x2 = x1;

					verticalLeft.setPoints(new PointList(new int[] { x1, y1, x2, y2 }));

					int verticalRightX = verticalRight.getPoints().getFirstPoint().x;
					int horizontalBottomY = horizontalBottom.getPoints().getFirstPoint().y;

					horizontalTop.setPoints(new PointList(new int[] { x1, y1, verticalRightX, y1 }));
					horizontalBottom.setPoints(new PointList(new int[] { x1, horizontalBottomY, verticalRightX,
							horizontalBottomY }));
					//
					leftTopPoint.setLocation(new Point(x1 - 2, y1 - 2));
					leftBottomPoint.setLocation(new Point(x1 - 2, horizontalBottomY - 2));
					//
					location = draggedLoc;

				} else if (me.getSource() == verticalRight) {
					y1 = horizontalTop.getPoints().getFirstPoint().y;

					if (x1 < verticalLeft.getPoints().getFirstPoint().x + 20) {
						x1 = verticalLeft.getPoints().getFirstPoint().x + 20;
					} else if (x1 > getImageBounds().x + getImageBounds().width) {
						x1 = getImageBounds().x + getImageBounds().width;
					}
					int y2 = horizontalBottom.getBounds().y;
					int x2 = x1;
					verticalRight.setPoints(new PointList(new int[] { x1, y1, x2, y2 }));

					int verticalLeftX = verticalLeft.getPoints().getFirstPoint().x;
					int horizontalBottomY = horizontalBottom.getPoints().getFirstPoint().y;

					horizontalTop.setPoints(new PointList(new int[] { verticalLeftX, y1, x2, y1 }));
					horizontalBottom.setPoints(new PointList(new int[] { verticalLeftX, horizontalBottomY, x1,
							horizontalBottomY }));

					//
					rightTopPoint.setLocation(new Point(x1 - 2, y1 - 2));
					rightBottomPoint.setLocation(new Point(x1 - 2, horizontalBottomY - 2));
					//

					location = draggedLoc;

				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent me) {

		}

		@Override
		public void mouseExited(MouseEvent me) {
			mouseLock = false;
		}

		@Override
		public void mouseHover(MouseEvent me) {

		}

		@Override
		public void mouseMoved(MouseEvent me) {

		}

	}

	@Override
	public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint) {
		org.eclipse.swt.graphics.Point computedSize = super.computeSize(wHint, hHint);
		// logger.info("computeSize1->widthHint:" + wHint + ":hHint:" + hHint + ":ComputedSize:" + computedSize);
		return computedSize;
	}

	@Override
	public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint, boolean changed) {
		org.eclipse.swt.graphics.Point computedSize = super.computeSize(wHint, hHint, changed);
		// logger.info("computeSize2->widthHint:" + wHint + ":H:" + hHint + ":c:" + changed + ":cs:" + computedSize);
		// logger.info("canvas size->" + getCanvas().getBounds().height + ":width:" + getCanvas().getBounds().width);
		return computedSize;
	}

	@Override
	public org.eclipse.swt.graphics.Rectangle computeTrim(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		return super.computeTrim(x, y, width, height);
	}

	/**
	 * Adds listener to the overlayImageFigure drag motion
	 * 
	 * @param listener
	 */
	public void addOverlayImageFigureListener(OverlayImgFigureListener listener) {
		getOverlayImgFigure().addOverlayImgFigureListener(listener);
	}

	public void removeOverlayImageFigureListener(OverlayImgFigureListener listener) {
		getOverlayImgFigure().removeOverlayImgFigureListener(listener);
	}

	public Rectangle getProfilerLineBounds() {
		return profilerLine.getBounds();
	}

	public void hideProfileHighlighter() {
		if (profileHighlighter != null) {
			profileHighlighter.setVisible(false);
		}
	}

	public void showProfileHighlighter() {
		if (profileHighlighter != null) {
			profileHighlighter.setVisible(true);
		}
	}

	public void moveProfileHighlighter(double xVal) {
		if (profileHighlighter.isVisible()) {
			// translating x to the imagebounds
			xVal = xVal + getImageBounds().x;
			profileHighlighter.moveTo((int) xVal, getProfilerLineBounds().y);
		}
	}

	public void hideLineProfiler() {
		if (lineprofileSliderComposite != null) {
			profilerLine.setVisible(false);
			lineprofileSliderComposite.setEnabled(false);
			hideProfileHighlighter();
		}
	}

	/**
	 * Hides the zoom rectangle.
	 */
	public void hideZoomRectangleFigure() {
		getZoomRectFigure().setVisible(false);
	}

	/**
	 * @param width
	 */
	public void moveCrossHairTo(int width) {
		crossWireVertical1.setPoints(new PointList(new int[] { width, 0, width, feedbackFigure.getBounds().height }));
		showCrossWire1();
	}

	public int getImageCenterX() {
		return getImageBounds().x + ((getImageBounds().width) / 2);
	}

	public int getCrossWire1XRelativeToImage() {
		return crossWireVertical1.getBounds().x - getImageBounds().x;
	}

	public void resetRoiWidgets() {
		horizontalTop.setPoints(new PointList(new int[] { getImageBounds().x, getImageBounds().y,
				getImageBounds().x + getImageBounds().width, getImageBounds().y }));
		horizontalBottom.setPoints(new PointList(new int[] { getImageBounds().x,
				getImageBounds().y + getImageBounds().height, getImageBounds().x + getImageBounds().width,
				getImageBounds().y + getImageBounds().height }));
		verticalLeft.setPoints(new PointList(new int[] { getImageBounds().x, getImageBounds().y, getImageBounds().x,
				getImageBounds().y + getImageBounds().height }));
		verticalRight.setPoints(new PointList(new int[] { getImageBounds().x + getImageBounds().width,
				getImageBounds().y, getImageBounds().x + getImageBounds().width,
				getImageBounds().y + getImageBounds().height }));
		leftTopPoint.setLocation(new Point(horizontalTop.getPoints().getFirstPoint().x - 2, verticalLeft.getPoints()
				.getFirstPoint().y - 2));
		leftBottomPoint.setLocation(new Point(horizontalBottom.getPoints().getFirstPoint().x - 2, verticalLeft
				.getPoints().getLastPoint().y - 2));
		rightTopPoint.setLocation(new Point(horizontalTop.getPoints().getLastPoint().x - 2, verticalRight.getPoints()
				.getFirstPoint().y - 2));
		rightBottomPoint.setLocation(new Point(horizontalBottom.getPoints().getLastPoint().x - 2, verticalRight
				.getPoints().getLastPoint().y - 2));

		setBoundaryLinesVisible();
	}

	private void setBoundaryLinesVisible() {
		horizontalTop.setVisible(true);
		horizontalBottom.setVisible(true);
		verticalLeft.setVisible(true);
		verticalRight.setVisible(true);
	}

	public void enableRoiWidgets() {

		if (!roiWidgetMoved) {
			resetRoiWidgets();
			roiWidgetMoved = true;
		}
		setBoundaryLinesVisible();
		horizontalTop.setCursor(null);
		horizontalBottom.setCursor(null);
		verticalLeft.setCursor(null);
		verticalRight.setCursor(null);

		horizontalTop.addMouseListener(linePointMouseListener);
		horizontalBottom.addMouseListener(linePointMouseListener);
		verticalLeft.addMouseListener(linePointMouseListener);
		verticalRight.addMouseListener(linePointMouseListener);

		horizontalTop.addMouseMotionListener(linePointMouseListener);
		horizontalBottom.addMouseMotionListener(linePointMouseListener);
		verticalLeft.addMouseMotionListener(linePointMouseListener);
		verticalRight.addMouseMotionListener(linePointMouseListener);

		leftTopPoint.setVisible(true);
		leftBottomPoint.setVisible(true);
		rightTopPoint.setVisible(true);
		rightBottomPoint.setVisible(true);

		horizontalBottom.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
		verticalLeft.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
		verticalRight.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
		horizontalTop.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));

		// rectFigure.setBounds(new Rectangle(20, 20, 200, 200));
		// rectFigure.setVisible(true);
	}

	public void disableRoiWidget() {
		leftTopPoint.setVisible(false);
		leftBottomPoint.setVisible(false);
		rightTopPoint.setVisible(false);
		rightBottomPoint.setVisible(false);

		horizontalTop.setCursor(null);
		horizontalBottom.setCursor(null);
		verticalLeft.setCursor(null);
		verticalRight.setCursor(null);

		horizontalTop.removeMouseListener(linePointMouseListener);
		horizontalBottom.removeMouseListener(linePointMouseListener);
		verticalLeft.removeMouseListener(linePointMouseListener);
		verticalRight.removeMouseListener(linePointMouseListener);

		horizontalTop.removeMouseMotionListener(linePointMouseListener);
		horizontalBottom.removeMouseMotionListener(linePointMouseListener);
		verticalLeft.removeMouseMotionListener(linePointMouseListener);
		verticalRight.removeMouseMotionListener(linePointMouseListener);

	}

	public void setValidRoiPoints(PointList validPoints) {
		//
		Dimension d = new Dimension(getImageBounds().x, getImageBounds().y);
		Point firstPoint = validPoints.getFirstPoint().translate(d);
		Point lastPoint = validPoints.getLastPoint().translate(d);
		logger.debug(String.format("x1 :%d  y1:%d    x2:%d    y2:%d", firstPoint.x, firstPoint.y, lastPoint.x,
				firstPoint.y));

		horizontalTop.setPoints(new PointList(new int[] { firstPoint.x, firstPoint.y, lastPoint.x, firstPoint.y }));

		horizontalBottom.setPoints(new PointList(new int[] { firstPoint.x, lastPoint.y, lastPoint.x, lastPoint.y }));

		verticalLeft.setPoints(new PointList(new int[] { firstPoint.x, firstPoint.y, firstPoint.x, lastPoint.y }));

		verticalRight.setPoints(new PointList(new int[] { lastPoint.x, firstPoint.y, lastPoint.x, lastPoint.y }));

		leftTopPoint.setLocation(new Point(firstPoint.x - 2, firstPoint.y - 2));
		leftBottomPoint.setLocation(new Point(firstPoint.x - 2, lastPoint.y - 2));

		rightTopPoint.setLocation(new Point(lastPoint.x - 2, firstPoint.y - 2));
		rightBottomPoint.setLocation(new Point(lastPoint.x - 2, lastPoint.y - 2));
	}

	public int[] getRoiPoints() {
		if (horizontalTop.getPoints().size() < 1) {
			return null;
		}
		if (horizontalBottom.getPoints().size() < 2) {
			return null;
		}
		Point firstPoint = horizontalTop.getPoints().getFirstPoint();
		Point lastPoint = horizontalBottom.getPoints().getLastPoint();
		return new int[] { firstPoint.x, firstPoint.y, lastPoint.x, lastPoint.y };
	}

	public Polyline getCrossWire1Vertical() {
		return crossWireVertical1;
	}
}
