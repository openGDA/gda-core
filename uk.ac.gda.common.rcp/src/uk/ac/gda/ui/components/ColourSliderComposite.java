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

	private int maximum = 100000;

	private int markerInterval = 10000;
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
	private ColorGradientedFigure upperGradientedFigure;
	private IFigure lowerGradientedFigure;
	private final static int OUTER_GRADIENT_WIDTH = 28;
	private static final Logger logger = LoggerFactory.getLogger(ColourSliderComposite.class);

	private int bottomLimitInPixel = 0;
	private int topLimitInPixel;

	private double maximumLimit;

	private boolean topSliderMoved = false;

	private boolean bottomSliderMoved = false;

	public void addColourSliderListener(IColourSliderListener colourSliderListener) {
		colourSliderListeners.add(colourSliderListener);
	}

	public void removeColourSliderListener(IColourSliderListener colourSliderListener) {
		colourSliderListeners.remove(colourSliderListener);
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public void setMarkerInterval(int markerInterval) {
		this.markerInterval = markerInterval;
	}

	/**
	 * Slider listener which propagates the events to the composites listening to the colour slider.
	 */
	public interface IColourSliderListener {
		void colourSliderRegion(int upperLimit, int lowerLimit);
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

	public int getCountForPixel(int pixel) {
		double pixel0 = getPixelLocation(0);
		logger.debug("Pixel at 0 count:{}", pixel0);
		double pixelMax = getPixelLocation(maximum);
		logger.debug("Pixel at pixelMax:{}", pixelMax);
		double pixelProportion = maximum / (pixel0 - pixelMax);

		double countAtPixel = (pixel0 - pixel) * pixelProportion;
		logger.debug("Pixel at countAtPixel:{}", countAtPixel);
		return (int) countAtPixel;
	}

	private IFigure getContents() {
		RectangleFigure boundaryFigure = new RectangleFigure() {
			@Override
			public void paint(Graphics graphics) {
				super.paint(graphics);

				// graphics.drawLine(0, topLimitInPixel, 30, topLimitInPixel);
				graphics.drawLine(0, bottomLimitInPixel, 30, bottomLimitInPixel);

				logger.debug("maximum:{}", maximum);
				logger.debug("markerInterval{}", markerInterval);
				int numMarkerIntervals = maximum / markerInterval;
				logger.debug("numMarkerIntervals:{}", numMarkerIntervals);
				int totalDistanceBetweenLowestAndHighestPoint = bottomLimitInPixel - topLimitInPixel;
				logger.debug("totalDistanceBetweenLowestAndHighestPoint:{}", totalDistanceBetweenLowestAndHighestPoint);
				int individualInterval = totalDistanceBetweenLowestAndHighestPoint / numMarkerIntervals;
				logger.debug("individualInterval:{}", individualInterval);

				for (int count = bottomLimitInPixel - individualInterval; count > topLimitInPixel; count = count
						- individualInterval) {
					graphics.drawLine(0, count, 30, count);
				}
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
			Dimension size = new Dimension(getBounds().width, getBounds().height);
			bottomLimitInPixel = size.height - topSliderHolder.getSize().height - 50;
			topLimitInPixel = 90;

			// Parent is the rectangle that holds both the triangles.
			Rectangle parentBounds = parent.getBounds();
			parent.setSize(30, parentBounds.height);
			//
			Rectangle topSliderHolderBounds = topSliderHolder.getBounds();
			int maxLimitPixelLoc = getPixelLocation(maximumLimit);
			int xStart = topSliderHolderBounds.x;

			if (!topSliderMoved) {
				int topSliderStart = maxLimitPixelLoc;
				if (maxLimitPixelLoc != 0) {
					topSliderStart = maxLimitPixelLoc - topSliderHolderBounds.height;
				}
				logger.debug("topSliderStart:{}", topSliderStart);
				topSliderHolder.setLocation(new Point(xStart, topSliderStart));

				topTriangleFigure.setLocation(new Point(5, topSliderHolderBounds.y - 1));
				topClosureFigure.setLocation(new Point(5, topSliderHolderBounds.y + 15 - 1));
			}
			/**/
			if (!bottomSliderMoved) {
				int bottomSliderStart = bottomLimitInPixel;// + bottomSliderHolder.getSize().height;
				Rectangle bottomSliderHolderBounds = bottomSliderHolder.getBounds();
				bottomSliderHolder.setLocation(new Point(xStart, bottomSliderStart));

				if (bottomSliderHolderBounds.y < topSliderHolderBounds.y + 25) {
					bottomSliderHolderBounds.setLocation(5, parentBounds.height - 50);
				}
				bottomTriangleFigure.setBounds(new Rectangle(5, bottomSliderHolderBounds.y, 20, 15));
				bottomClosureFigure.setBounds(new Rectangle(5, bottomSliderHolderBounds.y, 20, 1));
			}
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

		@Override
		public void mouseReleased(MouseEvent e) {
			e.consume();
		}

		@Override
		public void mouseDoubleClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			last = e.getLocation();
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point p = e.getLocation();
			Dimension delta = p.getDifference(last);
			Rectangle topSliderBounds = topSliderHolder.getBounds();
			Rectangle bottomSliderBounds = bottomSliderHolder.getBounds();
			int xStart = topSliderBounds.x;
			int heightMoved = delta.height;
			if (e.getSource() == topSliderHolder) {
				topSliderMoved = true;
				int movedY = topSliderBounds.y + delta.height;
				//
				if (movedY + topSliderBounds.height < getPixelLocation(maximumLimit)) {
					movedY = topSliderBounds.y;
					heightMoved = 0;
				} else if (movedY + topSliderBounds.height > bottomSliderBounds.y) {
					movedY = topSliderBounds.y;
					heightMoved = 0;
				}

				topSliderHolder.setLocation(new Point(xStart, movedY));
				upperGradientedFigure.setBounds(new Rectangle(1, 0, OUTER_GRADIENT_WIDTH, topSliderBounds.y));

				for (IColourSliderListener lis : colourSliderListeners) {
					lis.colourSliderRegion(getCountForPixel(topSliderBounds.y + topSliderBounds.height),
							getCountForPixel(bottomSliderBounds.y));
				}

			} else if (e.getSource() == bottomSliderHolder) {
				// Restrict the bottom slider to go beyond the height of the parent panel
				bottomSliderMoved = true;

				int movedY = bottomSliderBounds.y + heightMoved;
				//
				if (movedY > getPixelLocation(0)) {
					movedY = bottomSliderBounds.y;
					heightMoved = 0;
				} else if (movedY < topSliderBounds.y + topSliderBounds.height) {
					movedY = bottomSliderBounds.y;
					heightMoved = 0;
				}

				bottomSliderHolder.setLocation(new Point(xStart, movedY));
				lowerGradientedFigure.setBounds(new Rectangle(1, bottomSliderBounds.y + bottomSliderBounds.height,
						OUTER_GRADIENT_WIDTH, ColourSliderComposite.this.getSize().y));

				for (IColourSliderListener lis : colourSliderListeners) {
					lis.colourSliderRegion(getCountForPixel(topSliderBounds.y + topSliderBounds.height),
							getCountForPixel(bottomSliderBounds.y));
				}
			}
			int histogramHeight = bottomSliderBounds.y - (topSliderBounds.y + topSliderBounds.height);
			histogramRect.setBounds(new Rectangle(1, topSliderBounds.y + topSliderBounds.height, OUTER_GRADIENT_WIDTH,
					histogramHeight));
			last = p;
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 800));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		ColourSliderComposite sliderComposite = new ColourSliderComposite(shell, SWT.DOWN);
		shell.setText(sliderComposite.getClass().getName());
		sliderComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		sliderComposite.setMaximum(70000);
		sliderComposite.setMaximumLimit(65534);

		IColourSliderListener lis = new IColourSliderListener() {

			@Override
			public void colourSliderRegion(int upperLimit, int lowerLimit) {
				System.out.println("Upper Limit:" + upperLimit);
				System.out.println("Lower Limit:" + lowerLimit);
			}
		};

		sliderComposite.addColourSliderListener(lis);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public int getPixelLocation(double count) {
		if (bottomLimitInPixel != 0) {
			int totalNumberPixels = bottomLimitInPixel - topLimitInPixel;
			logger.debug("totalNumberPixels:{}", totalNumberPixels);
			int totalCount = maximum;

			double countsPerPixel = totalCount / totalNumberPixels;
			logger.debug("countsPerPixel:{}", countsPerPixel);
			double pixelAtCount = count / countsPerPixel;
			logger.debug("pixelAtCount:{}", pixelAtCount);
			double countLocInPixel = bottomLimitInPixel - pixelAtCount;
			logger.debug("countLocInPixel:{}", countLocInPixel);
			return (int) countLocInPixel;
		}
		return 0;
	}

	public void setMaximumLimit(double maximumLimit) {
		this.maximumLimit = maximumLimit;
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
