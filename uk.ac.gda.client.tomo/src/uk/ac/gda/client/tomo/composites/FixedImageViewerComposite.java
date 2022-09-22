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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.LineProfileSliderComposite.SliderMidpointListener;
import uk.ac.gda.client.tomo.composites.OverlayImageFigure.MOVE_AXIS;
import uk.ac.gda.client.viewer.IColourChangeListener;
import uk.ac.gda.client.viewer.SwtImagePositionTool;

/**
 * This composite is used to display the image viewer that supports both MJPG streaming and raw picture display -
 * However, the limitation to this is this contains no scroll panes and the image cannot be zoomed/panned or scrolled
 * across. The image needs to be of the right size to have to be viewed in this viewer.
 * <p>
 * This viewer is essentially coded for the tomography alignment perspective where the idea is that the entire image
 * would fit into the viewer composite.
 * <p>
 */
public class FixedImageViewerComposite extends Composite {

	private final static Logger logger = LoggerFactory.getLogger(FixedImageViewerComposite.class);

	protected Polyline profilerLine;
	private List<IColourChangeListener> colourChangeListeners = new ArrayList<IColourChangeListener>();

	protected Image mainImage;
	protected ImageData mainImgData;

	private SwtImagePositionTool imagePositionTool;

	private Canvas canvas;
	private Figure fig;
	protected Figure topFig;
	protected ImageFigure mainImgFig;
	protected FreeformLayer feedbackFigure;

	private RectangleFigure zoomRectFigure;
	private OverlayImageFigure overlayImgFig;
	/**/
	protected LineProfileSliderComposite lineprofileSliderComposite;
	private Image overlayImage;
	private ImageData overlayImageData;

	private ToolTipHelper toolTipHelper;

	private Set<ProfilePointListener> profileListeners;

	private LightweightSystem lightWeightSystem;
	private List<ZoomRectangleListener> zoomRectListeners = new ArrayList<FullImageComposite.ZoomRectangleListener>();
	protected EllipticalHighligterFigure profileHighlighter;

	public void setFeedbackCursor(int cursor) {
		topFig.setCursor(canvas.getDisplay().getSystemCursor(cursor));
	}

	public void resetFeedbackCursor() {
		topFig.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
	}

	/**
	 *
	 */
	private class TopFigureLayout extends XYLayout {
		@Override
		public Dimension getPreferredSize(IFigure container, int wHint, int hHint) {
			return new Dimension(canvas.getSize().x, canvas.getSize().y);
		}

		@Override
		public void layout(IFigure parent) {
			Point overlayImgLocation = overlayImgFig.getLocation();

			super.layout(parent);
			int imgFigX = parent.getBounds().width / 2 - getImageBounds().width / 2;
			int imgFigY = parent.getBounds().height / 2 - getImageBounds().height / 2;
			mainImgFig.setLocation(new Point(imgFigX, imgFigY));
			// If the overlay image figure has not been moved, the place it on the center of the screen.
			if (!overlayImgFig.hasImageMoved()) {
				overlayImgFig.setLocation(new Point(imgFigX, imgFigY));
			} else {
				// Else place it on the location where it was before.
				overlayImgFig.setLocation(overlayImgLocation);
			}
		}
	}

	/**
	 *
	 */
	private SliderMidpointListener sliderMidpointListener = new SliderMidpointListener() {

		@Override
		public void handleSliderMidpointMoved(int oldMidpoint, int newMidpoint) {
			profilerLine.setPoints(new PointList(new int[] { 0, newMidpoint, feedbackFigure.getBounds().width,
					newMidpoint }));
			for (ProfilePointListener profileListener : profileListeners) {
				profileListener.profileLineMoved(newMidpoint - getImageBounds().y);
			}
		}
	};

	public boolean addProfileListener(ProfilePointListener listener) {
		if (profileListeners == null) {
			profileListeners = new HashSet<FixedImageViewerComposite.ProfilePointListener>();
		}
		return profileListeners.add(listener);
	}

	public boolean removeProfileListener(ProfilePointListener listener) {
		return profileListeners.remove(listener);
	}

	public interface ProfilePointListener {
		void profileLineMoved(int y);
	}

	protected RectangleFigure getZoomRectFigure() {
		if (zoomRectFigure == null) {
			zoomRectFigure = new RectangleFigure();
			zoomRectFigure.setFill(false);
			zoomRectFigure.setForegroundColor(ColorConstants.green);
			MouseMotionForZoomFigure listener = new MouseMotionForZoomFigure(zoomRectFigure);
			zoomRectFigure.addMouseMotionListener(listener);
			zoomRectFigure.addMouseListener(listener);
		}
		return zoomRectFigure;
	}

	/**
	 * @return {@link Rectangle} bounds of the zoom rectangle.
	 */
	public Rectangle getZoomFigureBounds() {
		return getZoomRectFigure().getBounds();
	}

	/**
	 * Adds a zoom rectangle listener to the list of listeners.
	 *
	 * @param zRL
	 */
	public void addZoomRectListener(ZoomRectangleListener zRL) {
		zoomRectListeners.add(zRL);
	}

	/**
	 * Remove the specified zoom rectangle listener from the list of listeners.
	 *
	 * @param zRL
	 */
	public void removeZoomRectListener(ZoomRectangleListener zRL) {
		zoomRectListeners.remove(zRL);
	}

	public interface ZoomRectangleListener {
		/**
		 * @param bounds
		 * @param figureTopLeftRelativeImgBounds
		 * @param difference
		 */
		void zoomRectMoved(Rectangle bounds, Dimension figureTopLeftRelativeImgBounds, Dimension difference);
	}

	/**
	 * MouseMotionForZoomFigure
	 */
	private class MouseMotionForZoomFigure extends org.eclipse.draw2d.MouseMotionListener.Stub implements MouseListener {

		private final IFigure rectFigure;
		private Point location;

		protected MouseMotionForZoomFigure(IFigure figure) {
			this.rectFigure = figure;
		}

		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) {
			location = me.getLocation();
			me.consume();
		}

		@Override
		public void mouseReleased(org.eclipse.draw2d.MouseEvent me) {
		}

		@Override
		public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) {
		}

		@Override
		public void mouseDragged(org.eclipse.draw2d.MouseEvent me) {
			Point draggedLoc = me.getLocation();
			Dimension difference = draggedLoc.getDifference(location);
			// difference = difference between the previous position and the position moved to
			// restrict movement of the zoom rect

			Rectangle rectFigureBounds = rectFigure.getBounds();

			Rectangle figBoundsCopy = rectFigureBounds.getCopy();
			Dimension diffCheck = figBoundsCopy.getLocation().getDifference(getImageBounds().getLocation());
			// diffCheck = difference between the rectangle and the image bounds

			int diffCheckWidth = diffCheck.width;
			int movedWidth = difference.width;
			int diffCheckHeight = diffCheck.height;
			int movedHeight = difference.height;

			int rectFigureWidth = rectFigure.getSize().width;
			int rectFigureHeight = rectFigure.getSize().height;
			int imgBoundsWidth = getImageBounds().width;
			int imgBoundsHeight = getImageBounds().height;

			if (diffCheckWidth + movedWidth < 0) {
				movedWidth = 0;
			}
			if (diffCheckHeight + movedHeight < 0) {
				movedHeight = 0;
			}

			if (diffCheckWidth + movedWidth + rectFigureWidth > imgBoundsWidth + 1) {
				movedWidth = 0;
			}
			if (diffCheckHeight + movedHeight + rectFigureHeight > imgBoundsHeight + 1) {
				movedHeight = 0;
			}

			rectFigureBounds.translate(movedWidth, movedHeight);
			//
			Dimension figureBoundsRelativeImage = rectFigure.getBounds().getLocation()
					.getDifference(getImageBounds().getLocation());
			rectFigure.getParent().repaint();

			for (ZoomRectangleListener zrl : zoomRectListeners) {
				logger.debug("MovedWidth:" + movedWidth + "  MovedHeight:" + movedHeight);
				zrl.zoomRectMoved(getZoomFigureBounds(), figureBoundsRelativeImage, new Dimension(movedWidth,
						movedHeight));
			}
			location = draggedLoc;
		}

		@Override
		public void mouseEntered(org.eclipse.draw2d.MouseEvent me) {
			rectFigure.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEALL));
		}
	}

	public FixedImageViewerComposite(Composite parent, int style) {
		this(parent, style, false);
	}

	/**
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("deprecation")
	public FixedImageViewerComposite(Composite parent, int style, boolean showProfileComposite) {
		super(parent, style);
		org.eclipse.swt.layout.GridLayout layout = new GridLayout();

		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		this.setLayout(layout);

		if (showProfileComposite) {
			layout.numColumns = 2;
			layout.makeColumnsEqualWidth = false;
			// Add the line profile slider composite
			lineprofileSliderComposite = new LineProfileSliderComposite(this, SWT.None);
			GridData gd = new GridData(GridData.FILL_VERTICAL);
			gd.widthHint = 13;
			lineprofileSliderComposite.setLayoutData(gd);
			lineprofileSliderComposite.addSliderMidpointListener(sliderMidpointListener);
			lineprofileSliderComposite.setEnabled(false);
		}

		canvas = new Canvas(this, style);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		canvas.setLayoutData(gridData);
		canvas.setLayout(new FillLayout());

		fig = new Figure();
		FlowLayout flowLayout = new FlowLayout();

		fig.setLayoutManager(flowLayout);
		/**
		 * Di-sected the light weight system to get it tool-tip helper so that calls to updateTooltips can be called
		 *
		 * @see
		 */
		lightWeightSystem = new LightweightSystem(canvas) {

			private SWTEventDispatcher dispatcher;

			@Override
			protected EventDispatcher getEventDispatcher() {
				if (dispatcher == null) {
					dispatcher = new SWTEventDispatcher() {
						@Override
						protected ToolTipHelper getToolTipHelper() {
							toolTipHelper = super.getToolTipHelper();
							return toolTipHelper;
						}
					};
					setEventDispatcher(dispatcher);
				}
				return dispatcher;

			}
		};
		lightWeightSystem.setContents(fig);

		//
		canvas.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				fig.repaint();
			}
		});
		/**/
		mainImgFig = new ImageFigure();
		mainImgFig.setOpaque(true);
		/**/
		overlayImgFig = new OverlayImageFigure(getCanvas());
		overlayImgFig.setOpaque(false);
		/**/

		topFig = new Figure();
		topFig.setOpaque(true);

		topFig.setLayoutManager(new TopFigureLayout());
		topFig.add(mainImgFig, new Rectangle(0, 0, -1, -1));
		// topFig.add(overlayImgFig, new Rectangle(0, 0, -1, -1));
		// Add the feedback figure to show the feedback of the drawing
		feedbackFigure = new FreeformLayer() {
			// The preferred size of the feedback figure is always set to the window size.
			@Override
			public Dimension getPreferredSize(int wHint, int hHint) {
				return new Dimension(canvas.getSize().x, canvas.getSize().y);
			}
		};

		feedbackFigure.setBackgroundColor(ColorConstants.orange);

		/**/
		getZoomRectFigure().setVisible(false);
		feedbackFigure.add(getZoomRectFigure());
		/**/
		profilerLine = new Polyline();
		profilerLine.setLineWidth(2);
		profilerLine.setForegroundColor(ColorConstants.lightBlue);
		profilerLine.setVisible(false);
		feedbackFigure.add(profilerLine);
		/**/
		profileHighlighter = new EllipticalHighligterFigure();
		profileHighlighter.setVisible(false);
		feedbackFigure.add(profileHighlighter);
		/**/
		topFig.add(feedbackFigure, new Rectangle(0, 0, -1, -1));

		fig.add(topFig);
		imagePositionTool = new SwtImagePositionTool();

		flowLayout.setMajorAlignment(FlowLayout.ALIGN_LEFTTOP);
		/***/

		addCustomListeners();
	}

	public void showWindowTip(String tip) {

		feedbackFigure.repaint();
	}

	protected void addCustomListeners() {
		// do nothing in the base class
	}

	/**
	 * Method to show line profiler - this draws a horizontal line across the image viewer - the composite has a slider
	 * which is linked to the horizontal line and all this is shown when this method is invoked.
	 */
	public void showLineProfiler() {
		if (lineprofileSliderComposite != null) {
			lineprofileSliderComposite.setEnabled(true);
			Rectangle imageBounds = getImageBounds();
			lineprofileSliderComposite.setDraggerInitialLocation(imageBounds.y + (imageBounds.height / 2) - 4);
			lineprofileSliderComposite.setDraggerLimits(imageBounds.y - 5, imageBounds.y + imageBounds.height + 8);
			int midPointYLoc = lineprofileSliderComposite.getSliderMidpointYLocation();
			int feedbackFigWidth = feedbackFigure.getBounds().width;
			profilerLine.setPoints(new PointList(new int[] { 0, midPointYLoc, feedbackFigWidth, midPointYLoc }));
			profilerLine.setVisible(true);
		}
	}

	/**
	 * Show the zoom rectangle figure.
	 *
	 * @param dimension
	 */
	public void showZoomRectangleFigure(Dimension dimension) {
		getZoomRectFigure().setVisible(true);
		logger.debug("Zoom rectangle dimensionX:{}", dimension.width);
		logger.debug("Zoom rectangle dimensionY:{}", dimension.height);

		int feedBackXCenter = feedbackFigure.getSize().width / 2;
		logger.debug("feedbackfigure center x:{}", feedBackXCenter);
		int feedbackYCenter = feedbackFigure.getSize().height / 2;
		logger.debug("feedbackfigure center y:{}", feedbackYCenter);

		int zoomFigureXLoc = feedBackXCenter - dimension.width / 2;
		logger.debug("zoom figure x loc:{}", zoomFigureXLoc);

		int zoomFigureYLoc = feedbackYCenter - dimension.height / 2;
		logger.debug("zoomFigureYLoc loc:{}", zoomFigureYLoc);

		// getZoomRectFigure().setLocation(new Point(zoomFigureXLoc, zoomFigureYLoc));
		getZoomRectFigure().setLocation(new Point(getImageBounds().x, getImageBounds().y));
		// new Point(getImageBounds().x, getImageBounds().y));
		getZoomRectFigure().setSize(dimension.width, dimension.height);
	}

	/**
	 * @param colourChangeListener
	 * @return if the addition is successful
	 */
	public boolean addColourChangeListener(IColourChangeListener colourChangeListener) {
		return colourChangeListeners.add(colourChangeListener);
	}

	/**
	 * @param colourChangeListener
	 * @return true if the removal is successful
	 */
	public boolean removeColourChangeListener(IColourChangeListener colourChangeListener) {
		return colourChangeListeners.remove(colourChangeListener);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		canvas.setBackground(color);
	}

	/**
	 * Returns the bounds of the drawn image
	 *
	 * @return Point image bounds
	 */
	public Rectangle getImageBounds() {
		return mainImgFig.getBounds();
	}

	/**
	 * Reload image from a file This method must be called from the UI thread
	 *
	 * @param filename
	 *            image file
	 */
	public void loadImage(String filename) {
		if (mainImage != null && !mainImage.isDisposed()) {
			mainImage.dispose();
		}
		try {
			mainImgData = new ImageData(filename);
			mainImage = new Image(canvas.getDisplay(), mainImgData);

			mainImgFig.setImage(mainImage);
		} catch (Exception ex) {
			logger.error("Cannot load image", ex);
			throw ex;
		}

	}

	/**
	 * Reload image from a provided ImageData
	 *
	 * @param imageDataIn
	 *            ImageData
	 */
	public void loadMainImage(final ImageData imageDataIn) {
		try {
			if (!canvas.isDisposed()) {
				Image newImage = null;
				if (imageDataIn != null) {
					newImage = new Image(getDisplay(), imageDataIn);
				}
				if (!canvas.isDisposed()) {
					if (mainImage != null && !mainImage.isDisposed()) {
						mainImage.dispose();
					}
					mainImage = newImage;
					mainImgData = imageDataIn;
					mainImgFig.setImage(mainImage);
				}
			}
		} catch (Exception ex) {
			logger.error("Cannot load image", ex);
			throw ex;
		}
	}

	/**
	 * Reload image from a provided ImageData
	 *
	 * @param imageDataIn
	 *            ImageData
	 */
	public void loadOverlayImage(final ImageData imageDataIn) {
		try {
			if (!canvas.isDisposed()) {
				if (imageDataIn == null) {
					overlayImgFig.setVisible(false);
					overlayImgFig.suspend();
					overlayImgFig.imageMoved = false;
					return;
				}

				/**/
				overlayImgFig.setVisible(true);
				if (!imageDataIn.equals(overlayImageData)) {
					final Image newImage = new Image(canvas.getDisplay(), imageDataIn);
					if (!canvas.isDisposed()) {
						if (overlayImage != null && !overlayImage.isDisposed()) {
							overlayImage.dispose();
						}
						overlayImage = newImage;
						overlayImageData = imageDataIn;
						overlayImgFig.setImage(overlayImage);
						overlayImgFig.configureMouseListener();
						if (!topFig.getChildren().contains(overlayImgFig)) {
							topFig.add(overlayImgFig, new Rectangle(0, 0, -1, -1));
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Cannot load image", ex);
			throw ex;
		}
	}

	/**
	 * @return {@link SwtImagePositionTool}
	 */
	public SwtImagePositionTool getPositionTool() {
		return imagePositionTool;
	}

	/**
	 * Returns the underlying canvas of this viewer
	 *
	 * @return canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Refreshes viewer by repainting the image figure
	 */
	public void refreshView() {
		fig.repaint();
	}

	/**
	 * Returns the imagedata of the last displayed image in the viewer
	 *
	 * @return ImageData
	 */
	public ImageData getImageData() {
		return mainImgData;
	}

	/**
	 * Returns the IFigure of the image being drawn. The figure has an XYLayout and the layout manager should not be
	 * changed.
	 *
	 * @return top IFigure
	 */
	public IFigure getTopFigure() {
		return topFig;
	}

	public void setOverLayImageMoveAxis(MOVE_AXIS moveAxis) {
		overlayImgFig.setMoveAxis(moveAxis);
	}

	public void removeOverlayImage() {

		loadOverlayImage(null);
		if (topFig.getChildren().contains(overlayImgFig)) {
			topFig.remove(overlayImgFig);
		}
		overlayImgFig.suspend();
	}

	@Override
	public void dispose() {
		logger.info("Disposing FixedImageViewerComposite");
		if (mainImage != null && !mainImage.isDisposed()) {
			mainImage.dispose();
			logger.info("MainImg disposed");
		}
		if (overlayImage != null && !overlayImage.isDisposed()) {
			overlayImage.dispose();
		}
		if (canvas != null && !canvas.isDisposed()) {
			canvas.dispose();
		}
		// toolTipHelper
		// toolTipHelper.dispose();
		super.dispose();
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new FixedImageViewerComposite(shell, SWT.DOUBLE_BUFFERED);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * @return overlayImgFig
	 */
	protected OverlayImageFigure getOverlayImgFigure() {
		return overlayImgFig;
	}

}
