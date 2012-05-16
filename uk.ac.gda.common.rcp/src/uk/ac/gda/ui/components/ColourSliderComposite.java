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

package uk.ac.gda.ui.components;

import java.util.ArrayList;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite that has two slider which can be slided towards each other or away - this is used as a histogram control
 * for the tomography alignment view.
 */
public class ColourSliderComposite extends Composite {

	private FigureCanvas figCanvas;
	private Panel topSliderHolder;
	private Triangle topTriangleFigure;
	private RectangleFigure topClosureFigure;
	private Panel bottomSliderHolder;
	private Triangle bottomTriangleFigure;
	private Shape bottomClosureFigure;
	private ColorGradientedFigure histogramRect;
	private Dragger topSliderDragger;
	private Dragger bottomSliderDragger;

	private ArrayList<IColourSliderListener> colourSliderListeners;
	private static final int INITIAL_UPPER_Y_POSITION = 80;
	private ColorGradientedFigure upperGradientedFigure;
	private IFigure lowerGradientedFigure;
	private static final int BOTTOM_SLIDER_OFFSET = 48;
	private final static int OUTER_GRADIENT_WIDTH = 28;
	private static final int TOP_LIMITING_INDEX = 27;
	private static final int BOTTOM_LIMITING_INDEX = 705;
	private static final Logger logger = LoggerFactory.getLogger(ColourSliderComposite.class);

	public void addColourSliderListener(IColourSliderListener colourSliderListener) {
		colourSliderListeners.add(colourSliderListener);
	}

	public void removeColourSliderListener(IColourSliderListener colourSliderListener) {
		colourSliderListeners.remove(colourSliderListener);
	}

	/**
	 * Slider listener which propagates the events to the composites listening to the colour slider.
	 */
	public interface IColourSliderListener {
		void updateHigherLimitMoved(Point highBasePoint, Point lowBasePoint, Point highInitialPoint,
				Point currentPosition);

		void updateLowerLimitMoved(Point highBasePoint, Point lowBasePoint, Point lowInitialPoint, Point currentPosition);
	}

	public ColourSliderComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);
		figCanvas = new FigureCanvas(this);
		figCanvas.setContents(getContents());
		figCanvas.getViewport().setContentsTracksHeight(true);
		figCanvas.getViewport().setContentsTracksWidth(true);
		figCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		colourSliderListeners = new ArrayList<ColourSliderComposite.IColourSliderListener>();
	}

	private IFigure getContents() {
		RectangleFigure boundaryFigure = new RectangleFigure() {
			@Override
			public void paint(Graphics graphics) {
				super.paint(graphics);
				graphics.drawLine(0, 57, 30, 57);// line at 70000
				graphics.drawLine(0, 150, 30, 150);// line at 60000
				graphics.drawLine(0, 243, 30, 243);// line at 50000
				graphics.drawLine(0, 335, 30, 335);// line at 40000
				graphics.drawLine(0, 427, 30, 427);// line at 30000
				graphics.drawLine(0, 518, 30, 518);// line at 20000
				graphics.drawLine(0, 611, 30, 611);// line at 10000
				graphics.drawLine(0, 704, 30, 704);// line at 0

			}
		};
		boundaryFigure.setLayoutManager(new ColourSliderCompositeLayout());
		boundaryFigure.setBackgroundColor(ColorConstants.white);
		// Top slider figure
		topSliderHolder = new Panel();
		topSliderHolder.setLayoutManager(new XYLayout());

		topTriangleFigure = new Triangle();
		topTriangleFigure.setDirection(PositionConstants.SOUTH);
		topTriangleFigure.setFill(true);
		topTriangleFigure.setBackgroundColor(ColorConstants.red);
		topTriangleFigure.setForegroundColor(ColorConstants.red);
		topTriangleFigure.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));

		topClosureFigure = new RectangleFigure();
		topClosureFigure.setBackgroundColor(ColorConstants.red);
		topClosureFigure.setForegroundColor(ColorConstants.red);
		topClosureFigure.setFill(true);

		topSliderHolder.add(topTriangleFigure, new Rectangle(0, 0, 20, 15));
		topSliderHolder.add(topClosureFigure, new Rectangle(0, 20, 20, 1));
		topSliderDragger = new Dragger();
		topSliderHolder.addMouseMotionListener(topSliderDragger);
		topSliderHolder.addMouseListener(topSliderDragger);
		topSliderHolder.setBounds(new Rectangle(5, 5, 20, 15));
		// Bottom slider figure
		bottomSliderHolder = new Panel();
		bottomSliderHolder.setLayoutManager(new XYLayout());

		bottomTriangleFigure = new Triangle();
		bottomTriangleFigure.setDirection(PositionConstants.NORTH);
		bottomTriangleFigure.setFill(true);
		bottomTriangleFigure.setBackgroundColor(ColorConstants.blue);
		bottomTriangleFigure.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
		bottomTriangleFigure.setBackgroundColor(ColorConstants.blue);
		bottomTriangleFigure.setForegroundColor(ColorConstants.blue);

		bottomClosureFigure = new RectangleFigure();
		bottomClosureFigure.setBackgroundColor(ColorConstants.blue);
		bottomClosureFigure.setFill(true);
		bottomClosureFigure.setBackgroundColor(ColorConstants.blue);
		bottomClosureFigure.setForegroundColor(ColorConstants.blue);

		bottomSliderHolder.add(bottomTriangleFigure);
		bottomSliderHolder.add(bottomClosureFigure);
		bottomSliderDragger = new Dragger();
		bottomSliderHolder.addMouseMotionListener(bottomSliderDragger);
		bottomSliderHolder.addMouseListener(bottomSliderDragger);
		bottomSliderHolder.setBounds(new Rectangle(5, 5, 20, 15));
		//
		upperGradientedFigure = new ColorGradientedFigure(ColorConstants.gray, ColorConstants.white);

		//
		lowerGradientedFigure = new ColorGradientedFigure(ColorConstants.white, ColorConstants.gray);

		histogramRect = new ColorGradientedFigure(ColorConstants.red, ColorConstants.blue);

		boundaryFigure.add(upperGradientedFigure);
		boundaryFigure.add(lowerGradientedFigure);
		boundaryFigure.add(histogramRect);
		boundaryFigure.add(topSliderHolder);
		boundaryFigure.add(bottomSliderHolder);
		return boundaryFigure;
	}

	private class ColorGradientedFigure extends RectangleFigure {
		public ColorGradientedFigure(Color color1, Color color2) {
			this.setBackgroundColor(color1);
			this.setForegroundColor(color2);
		}

		@Override
		protected void paintClientArea(Graphics graphics) {
			super.paintClientArea(graphics);
			graphics.fillGradient(getBounds(), true);
		}
	}

	private class ColourSliderCompositeLayout extends XYLayout {

		@Override
		public void layout(IFigure parent) {
			super.layout(parent);

			// Parent is the rectangle that holds both the triangles.
			Rectangle parentBounds = parent.getBounds();
			parent.setSize(30, parentBounds.height);
			//
			Rectangle topSliderHolderBounds = topSliderHolder.getBounds();
			topSliderHolder.setLocation(new Point(topSliderHolderBounds.x, INITIAL_UPPER_Y_POSITION));

			topTriangleFigure.setLocation(new Point(5, topSliderHolderBounds.y - 1));
			topClosureFigure.setLocation(new Point(5, topSliderHolderBounds.y + 15 - 1));

			/**/
			Rectangle bottomSliderHolderBounds = bottomSliderHolder.getBounds();
			bottomSliderHolder.setLocation(new Point(topSliderHolderBounds.x, parentBounds.height
					- BOTTOM_SLIDER_OFFSET));

			if (bottomSliderHolderBounds.y < topSliderHolderBounds.y + 25) {
				bottomSliderHolderBounds.setLocation(5, parentBounds.height - 50);
			}
			bottomTriangleFigure.setBounds(new Rectangle(5, bottomSliderHolderBounds.y, 20, 15));
			bottomClosureFigure.setBounds(new Rectangle(5, bottomSliderHolderBounds.y, 20, 1));

			//
			int histogramHeight = bottomSliderHolder.getBounds().y
					- (topSliderHolder.getBounds().y + topSliderHolder.getBounds().height);
			histogramRect.setBounds(new Rectangle(1,
					topSliderHolder.getBounds().y + topSliderHolder.getBounds().height, OUTER_GRADIENT_WIDTH,
					histogramHeight));

			//
			int upperGradientHeight = topSliderHolderBounds.y;
			upperGradientedFigure.setBounds(new Rectangle(1, 5, OUTER_GRADIENT_WIDTH, upperGradientHeight - 5));
			//
			int lowerGradientY = bottomSliderHolder.getLocation().y + bottomSliderHolder.getSize().height;
			int lowerGradientHeight = parentBounds.height - lowerGradientY - 5;
			lowerGradientedFigure
					.setBounds(new Rectangle(1, lowerGradientY, OUTER_GRADIENT_WIDTH, lowerGradientHeight));
		}
	}

	class Dragger extends MouseMotionListener.Stub implements MouseListener {

		private Point last;
		private Point initialTopPosition;
		private Point initialBottomPosition;

		@Override
		public void mouseReleased(MouseEvent e) {
			IFigure bottomSliderParent = bottomSliderHolder.getParent();
			int topSlHeight = topSliderHolder.getSize().height;
			if (e.getSource() == topSliderHolder) {

				for (IColourSliderListener cListener : colourSliderListeners) {
					cListener.updateHigherLimitMoved(new Point(0, 57), new Point(0, 704), new Point(
							initialTopPosition.x, initialTopPosition.y + topSlHeight),
							new Point(5, topSliderHolder.getLocation().y + topSlHeight));
				}
			} else {
				for (IColourSliderListener cListener : colourSliderListeners) {
					cListener.updateLowerLimitMoved(new Point(0, INITIAL_UPPER_Y_POSITION + topSlHeight), new Point(0,
							bottomSliderParent.getBounds().height - BOTTOM_SLIDER_OFFSET), initialBottomPosition,
							bottomSliderHolder.getLocation());
				}
			}
			int i = topSliderHolder.getLocation().y + topSlHeight;
			e.consume();
		}

		@Override
		public void mouseDoubleClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			last = e.getLocation();
			initialTopPosition = topSliderHolder.getLocation();
			initialBottomPosition = bottomSliderHolder.getLocation();
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point p = e.getLocation();
			Dimension delta = p.getDifference(last);
			Rectangle topsliderBounds = topSliderHolder.getBounds();
			Rectangle bottomSliderBounds = bottomSliderHolder.getBounds();
			if (e.getSource() == topSliderHolder) {
				// Restricted drag movement for the triangle
				// If the sum of the mouse dragged location difference and the topsliderbounds->y is greater than 0, so
				// that it doesn't move beyond the top of the panel.
				if (delta.height + topsliderBounds.y > (TOP_LIMITING_INDEX + topsliderBounds.height)) {
					// If it doesn't cross the below slider.
					if (delta.height + topsliderBounds.y + topsliderBounds.height < bottomSliderBounds.y) {
						// Move the triangle
						topSliderHolder.setLocation(new Point(topsliderBounds.x, topsliderBounds.y + delta.height));
						// topSliderHolder.setBounds(topsliderBounds.getTranslated(0, delta.height));
						// recalculate bounds for histogramrect - and set the new bounds of the coloured rectangle.
						upperGradientedFigure
								.setBounds(new Rectangle(1, 5, OUTER_GRADIENT_WIDTH, topsliderBounds.y - 5));

					}
				}
			} else if (e.getSource() == bottomSliderHolder) {
				// Restrict the bottom slider to go beyond the height of the parent panel
				int bottomSliderHolderParentHeight = bottomSliderHolder.getParent().getBounds().height;
				int bottomGradientStartY = bottomSliderBounds.y + bottomSliderBounds.height;
				if (bottomSliderBounds.y + delta.height < BOTTOM_LIMITING_INDEX) {
					// Restrict the bottom slider to cross over the top slider.
					if (bottomSliderBounds.y + delta.height > topsliderBounds.y + topsliderBounds.height) {
						bottomSliderHolder.setBounds(bottomSliderBounds.getTranslated(0, delta.height));
						// recalculate bounds for histogramrect
						lowerGradientedFigure.setBounds(new Rectangle(1, bottomGradientStartY + 2,
								OUTER_GRADIENT_WIDTH, bottomSliderHolderParentHeight - bottomGradientStartY - 5));
					}
				}
			}
			int histogramHeight = bottomSliderBounds.y - (topsliderBounds.y + topsliderBounds.height);
			histogramRect.setBounds(new Rectangle(1, topsliderBounds.y + topsliderBounds.height, OUTER_GRADIENT_WIDTH,
					histogramHeight));
			last = p;
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		ColourSliderComposite sliderComposite = new ColourSliderComposite(shell, SWT.DOWN);
		shell.setText(sliderComposite.getClass().getName());
		sliderComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	@Override
	public void dispose() {
		topSliderHolder.removeMouseListener(topSliderDragger);
		topSliderHolder.removeMouseMotionListener(topSliderDragger);
		bottomSliderHolder.removeMouseListener(bottomSliderDragger);
		bottomSliderHolder.removeMouseMotionListener(bottomSliderDragger);
		colourSliderListeners.clear();
		super.dispose();
	}
}
