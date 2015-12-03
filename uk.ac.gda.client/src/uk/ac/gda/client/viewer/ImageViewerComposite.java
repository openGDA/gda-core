/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.viewer;

import gda.rcp.GDAClientActivator;
import gda.rcp.ImageConstants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;
import org.eclipse.dawnsci.plotting.api.jreality.tool.ImagePositionListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.histogram.functions.GlobalColourMaps;

/**
 * Composite to preview images. The composite is self contained with zoom and panning options similar to the QT viewer
 * in EPICS.
 * <p>
 */
public class ImageViewerComposite extends Composite {
	private static final String ZOOM_TO_FIT = "Zoom to Fit";
	private static final String RESET_VIEW = "Reset View";
	private static final String ZOOM_OUT = "Zoom Out";
	private static final String ZOOM_IN = "Zoom In";
	private final static Logger logger = LoggerFactory.getLogger(ImageViewerComposite.class);
	private List<IRectFigureListener> rectFigureListeners = new ArrayList<IRectFigureListener>();

	private List<IColourChangeListener> colourChangeListeners = new ArrayList<IColourChangeListener>();

	public boolean addRectFigureListener(IRectFigureListener rectFigListener) {
		return rectFigureListeners.add(rectFigListener);
	}

	public boolean removeRectFigureListener(IRectFigureListener rectFigureListener) {
		return rectFigureListeners.remove(rectFigureListener);
	}

	private final class LocalViewPort extends Viewport {
		@Override
		protected void readjustScrollBars() {
			// expand left and right (or top and bottom)
			// by half the extent size so that we can scroll the containing
			// figure by half its width
			// We don't base the overall width on the getContents().getBounds() as that
			// may be the wrong size, force it to the width of the image itself

			Rectangle imageBounds = getImageBounds();

			int clientHeight = getClientArea().height;
			getVerticalRangeModel().setAll(-clientHeight / 2, clientHeight, clientHeight / 2 + imageBounds.height);

			int clientWidth = getClientArea().width;
			getHorizontalRangeModel().setAll(-clientWidth / 2, clientWidth, clientWidth / 2 + imageBounds.width);

		}
	}

	private static final float DEFAULT_ZOOM_LEVEL = 1.0f;
	private static final float MAX_ZOOM_LEVEL = 5.0f;
	private static final float MIN_ZOOM_LEVEL = 0.10f;

	/* Determines how much to zoom in/out per zoom step */
	private static final float ZOOM_CHANGE_FACTOR = 0.05f;
	protected boolean panInProgress = false;
	private Point panStartMousePoint;
	private Point panStartViewPoint;

	private Image image;
	private ImageData imageData;

	protected boolean showingEnlargedImage = false;
	private SwtImagePositionTool imagePositionTool;

	private FigureCanvas canvas;
	private Figure fig;
	protected Figure topFig;
	protected ImageFigure imgFig;
	protected FreeformLayer feedbackFigure;
	protected ZoomContainer zoomContainer;
	protected ScrollPane scrollPane;

	private boolean keepZoomFit = false;
	private float zoomLevel;
	private CCombo cmbColourMap;
	// private Cursor cursor;

	private static boolean logging = false;

	public ImageViewerComposite(Composite parent, int style, boolean showFalseColourDropDown) {
		super(parent, style);
		this.setLayout(new org.eclipse.swt.layout.GridLayout());
		final SashForm sashForm = new SashForm(this, SWT.HORIZONTAL | SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		// root.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		canvas = new FigureCanvas(sashForm, style);
		canvas.setBackground(ColorConstants.white);

		// make this mode-dependent
		// cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_CROSS);
		// canvas.setCursor(cursor);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		// gridData.widthHint = 700;
		canvas.setLayoutData(gridData);
		canvas.setBackground(ColorConstants.black);

		LightweightSystem lws = new LightweightSystem(canvas);
		fig = new Figure();
		FlowLayout layout = new FlowLayout();
		fig.setLayoutManager(layout);
		lws.setContents(fig);
		/* Add mouse listeners to the canvas. */
		initialize();
		this.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (keepZoomFit) {
					zoomFit();
				}
			}
		});

		imgFig = new ImageFigure();

		topFig = new Figure();
		topFig.setLayoutManager(new XYLayout());
		topFig.add(imgFig, new Rectangle(0, 0, -1, -1));
		// Add the feedback figure to show the feedback of the drawing
		feedbackFigure = new FreeformLayer();
		topFig.add(feedbackFigure, new Rectangle(0, 0, -1, -1));

		// panning support
		scrollPane = new ScrollPane();
		scrollPane.setViewport(new LocalViewPort());
		scrollPane.setContents(topFig);
		scrollPane.setScrollBarVisibility(ScrollPane.NEVER);
		scrollPane.getViewport().setContentsTracksWidth(true);
		scrollPane.getViewport().setContentsTracksHeight(true);

		// zoom support
		zoomLevel = DEFAULT_ZOOM_LEVEL;
		zoomContainer = new ZoomContainer();
		zoomContainer.add(scrollPane);
		zoomContainer.setZoom(zoomLevel);

		fig.add(zoomContainer);
		layout.setMajorAlignment(FlowLayout.ALIGN_LEFTTOP);
		// layout.setConstraint(zoomContainer, BorderLayout.TOP);

		/***/

		createZoomWidgetComposite(sashForm);
		if (showFalseColourDropDown) {
			/* The below code doesn't work yet - can't device a way to get the colours work with the MJPG viewer. */
			Composite colourSchemeComposite = new Composite(this, SWT.None);
			colourSchemeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			colourSchemeComposite.setLayout(new GridLayout(2, false));

			final Label scheme = new Label(colourSchemeComposite, SWT.NONE);
			scheme.setText("Colour Scheme");
			scheme.setLayoutData(new GridData());

			/**/
			cmbColourMap = new CCombo(colourSchemeComposite, SWT.BORDER | SWT.READ_ONLY);
			cmbColourMap.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					falseColourChange();
				}

			});
			cmbColourMap.setToolTipText("Change the color scheme.");
			cmbColourMap.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			cmbColourMap.setEnabled(false);
			/*
			 * Composite colourSchemePlotter = new Composite(colourSchemeComposite, SWT.None);
			 * colourSchemeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			 */

			GlobalColourMaps.InitializeColourMaps();
			fillupColourMapBox();
			/**/
		}

		// The below initialize list
		initializeListeners(this);
	}

	/**
	 * Constructor for the image viewer
	 *
	 * @param parent
	 *            the parent of this viewer
	 * @param style
	 *            the style of this viewer
	 */
	public ImageViewerComposite(Composite parent, int style) {
		this(parent, style, true);

	}

	/**
	 * Creates the widget/tools for zooming in, out etc.
	 *
	 * @param sashForm
	 */
	private void createZoomWidgetComposite(final SashForm sashForm) {
		Composite zoomWidgetComposite = new Group(sashForm, SWT.NONE);
		zoomWidgetComposite.setLayout(new GridLayout(1, true));
		zoomWidgetComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		sashForm.setWeights(new int[] { 92, 7 });
		Button zoomInButton = new Button(zoomWidgetComposite, SWT.PUSH);
		zoomInButton.setImage(GDAClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_ZOOM_IN));
		zoomInButton.setToolTipText(ZOOM_IN);
		zoomInButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoomIn();
			}

		});
		GridData gd = new GridData();
		zoomInButton.setLayoutData(gd);

		Button zoomOutButton = new Button(zoomWidgetComposite, SWT.PUSH);
		zoomOutButton.setImage(GDAClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_ZOOM_OUT));
		zoomOutButton.setToolTipText(ZOOM_OUT);
		zoomOutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoomOut();
			}

		});
		gd = new GridData();
		zoomOutButton.setLayoutData(gd);

		Button resetButton = new Button(zoomWidgetComposite, SWT.PUSH);
		resetButton.setToolTipText(RESET_VIEW);
		resetButton.setImage(GDAClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_RESET_ZOOM));
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetView();
			}
		});
		gd = new GridData();
		resetButton.setLayoutData(gd);

		Button fitButton = new Button(zoomWidgetComposite, SWT.PUSH);
		fitButton.setImage(GDAClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_ZOOM_FIT));
		fitButton.setToolTipText(ZOOM_TO_FIT);
		fitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoomFit();
			}
		});
		gd = new GridData();
		fitButton.setLayoutData(gd);
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

	/**
	 * Change the false colour scheme
	 */
	private void falseColourChange() {

		/*
		 * MessageDialog.openInformation(cmbColourMap.getShell(), "To be implemented",
		 * "The false colour stuff needs to be implemented."); return;
		 */

		int selectNr = cmbColourMap.getSelectionIndex();
		redSelect = GlobalColourMaps.colourSelectList.get(selectNr * 4);
		greenSelect = GlobalColourMaps.colourSelectList.get(selectNr * 4 + 1);
		blueSelect = GlobalColourMaps.colourSelectList.get(selectNr * 4 + 2);
		alphaSelect = GlobalColourMaps.colourSelectList.get(selectNr * 4 + 3);
		// Inform all listeners about the colour changes.
		for (IColourChangeListener colourChangeListener : colourChangeListeners) {
			colourChangeListener.doChangeColours(redSelect, greenSelect, blueSelect, alphaSelect);
		}
	}

	private void fillupColourMapBox() {
		for (int i = 0; i < GlobalColourMaps.colourMapNames.length; i++) {
			cmbColourMap.add(GlobalColourMaps.colourMapNames[i]);
		}

		// buildSelection();
	}

	private void zoomOut() {
		float zoomChangeFactor = -3 * ZOOM_CHANGE_FACTOR + 1;
		float newZoomLevel = zoomLevel * zoomChangeFactor;
		setZoomLevel(newZoomLevel);
	}

	private void zoomIn() {
		float zoomChangeFactor = 3 * ZOOM_CHANGE_FACTOR + 1;
		float newZoomLevel = zoomLevel * zoomChangeFactor;
		setZoomLevel(newZoomLevel);
	}

	// private DataSetPlotter datasetPlotter;
	private int redSelect;
	private int greenSelect;
	private int blueSelect;
	private int alphaSelect;

	/**
	 * Initialize listeners and position tools
	 */
	private void initialize() {
		imagePositionTool = new SwtImagePositionTool();

		// Zoom listener
		canvas.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!keepZoomFit) {
					int count = event.count;
					float zoomChangeFactor = count * ZOOM_CHANGE_FACTOR + 1;
					float newZoomLevel = zoomLevel * zoomChangeFactor;
					setZoomLevel(newZoomLevel);
				}
			}
		});

		fig.addMouseListener(new org.eclipse.draw2d.MouseListener.Stub() {
			@Override
			public void mousePressed(org.eclipse.draw2d.MouseEvent me) {

				if (!keepZoomFit && me.button == 2) {
					panInProgress = true;
					panStartMousePoint = me.getLocation();
					panStartViewPoint = getViewLocation().getScaled(zoomLevel);
				}
				if (image != null) {
					Rectangle imageBounds = getImageBounds();
					imagePositionTool.activate(me, imageBounds.x, imageBounds.y, zoomLevel);
				}
			}

			@Override
			public void mouseReleased(org.eclipse.draw2d.MouseEvent me) {
				if (image != null) {
					Rectangle imageBounds = getImageBounds();
					imagePositionTool.deactivate(me, imageBounds.x, imageBounds.y, zoomLevel);
				}
				panInProgress = false;
			}
		});

		fig.addMouseMotionListener(new MouseMotionListener.Stub() {
			@Override
			public void mouseDragged(org.eclipse.draw2d.MouseEvent me) {
				if (image != null) {
					if (panInProgress) {
						Point currentPoint = me.getLocation();
						Dimension difference = currentPoint.getDifference(panStartMousePoint);
						Point newPoint = panStartViewPoint.getTranslated(difference.getNegated()).getScaled(
								1 / zoomLevel);
						scrollPane.scrollTo(newPoint);
					}
					Rectangle imageBounds = getImageBounds();
					imagePositionTool.perform(me, imageBounds.x, imageBounds.y, zoomLevel);
				}
			}
		});
	}

	/**
	 * Returns the current location of the viewport for scrolling
	 *
	 * @return viewLocation
	 */
	private Point getViewLocation() {
		Point viewLocation = scrollPane.getViewport().getViewLocation();
		return viewLocation;
	}

	/**
	 * Returns the bounds of the drawn image
	 *
	 * @return Point image bounds
	 */
	public Rectangle getImageBounds() {
		feedbackFigure.setBounds(imgFig.getBounds());
		return imgFig.getBounds();
	}

	/**
	 * Reload image from a file This method must be called from the UI thread
	 *
	 * @param filename
	 *            image file
	 */
	public void loadImage(String filename) {
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}
		try {
			imageData = new ImageData(filename);
			image = new Image(canvas.getDisplay(), imageData);

			imgFig.setImage(image);
			centerScrollBars();
		} catch (Exception ex) {
			logger.error("Cannot load image", ex);
		}

	}

	/**
	 * Reload image from a provided ImageData
	 *
	 * @param imageDataIn
	 *            ImageData
	 */
	public void loadImage(final ImageData imageDataIn) {
		try {
			if (!canvas.isDisposed()) {

				// PaletteData paletteData = new PaletteData(Math.abs(redSelect), Math.abs(greenSelect),
				// Math.abs(blueSelect));
				// imageDataIn.palette = paletteData;
				final Image newImage = new Image(canvas.getDisplay(), imageDataIn);
				if (!canvas.isDisposed()) {
					if (image != null && !image.isDisposed()) {
						image.dispose();
					}
					image = newImage;
					imageData = imageDataIn;
					imgFig.setImage(image);

				}
			}
		} catch (Exception ex) {
			logger.error("Cannot load image", ex);
		}
	}

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

	private void centerScrollBars() {
		Viewport viewport = scrollPane.getViewport();
		if (image == null || image.isDisposed()) {
			viewport.setHorizontalLocation(0);
			viewport.setVerticalLocation(0);
		} else {
			// The updating of the extents that are use below happens
			// in a delayed way after zoom is set, therefore tell the update
			// manager to perform those calculations immediately so we can
			// proceed
			zoomContainer.getUpdateManager().performValidation();
			Rectangle bounds = getImageBounds();
			int horizontalExtent = viewport.getHorizontalRangeModel().getExtent();
			int horizontalValue = -((horizontalExtent - bounds.width) / 2);
			viewport.setHorizontalLocation(horizontalValue);
			int verticalExtent = viewport.getVerticalRangeModel().getExtent();
			int verticalValue = -((verticalExtent - bounds.height) / 2);
			viewport.setVerticalLocation(verticalValue);
		}
	}

	/**
	 * Resets the viewer to its default zoom level
	 */
	public void resetView() {
		if (keepZoomFit) {
			zoomFit();
		} else {
			zoomLevel = DEFAULT_ZOOM_LEVEL;
			zoomContainer.setZoom(zoomLevel);
			centerScrollBars();
		}
	}

	/**
	 * Sets the zoom and scroll so that the picture takes up the full client area
	 */
	public void zoomFit() {
		centerScrollBars();
		Rectangle clientArea = fig.getClientArea();
		Rectangle imageArea = getImageBounds();
		float heightZoomLevel = ((float) clientArea.height) / imageArea.height;
		float widthZoomLevel = ((float) clientArea.width) / imageArea.width;
		float zoomLevel = Math.min(heightZoomLevel, widthZoomLevel);
		setZoomLevel(zoomLevel);
	}

	public boolean isKeepZoomFit() {
		return keepZoomFit;
	}

	public void setKeepZoomFit(boolean keepZoomFit) {
		this.keepZoomFit = keepZoomFit;
		if (keepZoomFit) {
			zoomFit();
		}
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
		return imageData;
	}

	/* package */static double calcScrollFactor(RangeModel model) {
		int min = model.getMinimum();
		int max = model.getMaximum();
		int v = model.getValue();
		int e = model.getExtent();

		int x = -min + v;
		int y = max - v - e;

		return ((double) x) / y;
	}

	/* package */static int calcPositionFromScrollFactor(RangeModel model, double scrollFactor) {
		double f = scrollFactor;
		int min = model.getMinimum();
		int max = model.getMaximum();
		int e = model.getExtent();

		if (Double.isInfinite(f)) {
			return max - e;
		}

		return (int) ((f * max - f * e + min) / (1 + f));
	}

	private void setZoomLevel(float newZoomLevel) {
		Viewport viewport = scrollPane.getViewport();
		RangeModel horizontalRangeModel = viewport.getHorizontalRangeModel();
		RangeModel verticalRangeModel = viewport.getVerticalRangeModel();
		double horizontalScrollFactor = calcScrollFactor(horizontalRangeModel);
		double verticalScrollFactor = calcScrollFactor(verticalRangeModel);

		newZoomLevel = Math.max(MIN_ZOOM_LEVEL, newZoomLevel);
		newZoomLevel = Math.min(MAX_ZOOM_LEVEL, newZoomLevel);
		zoomLevel = newZoomLevel;
		zoomContainer.setZoom(zoomLevel);

		// this recalculates the range models values
		zoomContainer.getUpdateManager().performValidation();

		int newHorizontalValue = calcPositionFromScrollFactor(horizontalRangeModel, horizontalScrollFactor);
		int newVerticalValue = calcPositionFromScrollFactor(verticalRangeModel, verticalScrollFactor);
		viewport.setHorizontalLocation(newHorizontalValue);
		viewport.setVerticalLocation(newVerticalValue);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new ImageViewerComposite(shell, SWT.DOUBLE_BUFFERED);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void handleStartFeedbackFigure(@SuppressWarnings("unused") MouseEvent event) {

	}

	private void handleEndFeedbackFigure(@SuppressWarnings("unused") MouseEvent event) {

	}

	private void initializeListeners(ImageViewerComposite viewer) {
		viewer.getCanvas().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				log(event, "Mouse Down");
				handleStartFeedbackFigure(event);
			}

			@Override
			public void mouseUp(MouseEvent event) {
				log(event, "Mouse Up");
				handleEndFeedbackFigure(event);
			}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				log(event, "Mouse Double Clicked");
			}
		});

		viewer.getCanvas().addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				log(event, "Mouse Wheel");
			}
		});

		ImagePositionListener newListener = new ImagePositionListener() {
			private RectangleFigure rectFigure;
			private int startX;
			private int startY;

			private RectangleFigure getFeedbackRectFigure() {
				if (rectFigure == null) {
					rectFigure = new RectangleFigure();
				}
				return rectFigure;
			}

			@Override
			public void imageStart(IImagePositionEvent event) {
				double[] position = event.getPosition();
				int[] imagePosition = event.getImagePosition();

				System.out
						.println(String
								.format("newListener->imageStart->Position0 :%1$s  Position1: %2$s  imagePosition0:%3$s   imagePosition1:%4$s",
										(int) position[0], (int) position[1], imagePosition[0], imagePosition[1]));

				feedbackFigure.add(getFeedbackRectFigure());
				getFeedbackRectFigure().setFill(false);
				getFeedbackRectFigure().setForegroundColor(ColorConstants.black);
				startX = (int) position[0];
				startY = (int) position[1];
				handleFeedbackStart(getFeedbackRectFigure(), startX, startY);
			}

			@Override
			public void imageFinished(IImagePositionEvent event) {
				double[] position = event.getPosition();
				int[] imagePosition = event.getImagePosition();
				System.out
						.println(String
								.format("newListener->imageFinished->Position0 :%1$s  Position1: %2$s  imagePosition0:%3$s   imagePosition1:%4$s",
										(int) position[0], (int) position[1], imagePosition[0], imagePosition[1]));
				handleFeedbackEnd(getFeedbackRectFigure(), startX, startY, (int) position[0], (int) position[1]);
			}

			@Override
			public void imageDragged(IImagePositionEvent event) {
				double[] position = event.getPosition();
				int[] imagePosition = event.getImagePosition();
				System.out
						.println(String
								.format("newListener->imageDragged->Position0 :%1$s  Position1: %2$s  imagePosition0:%3$s   imagePosition1:%4$s",
										(int) position[0], (int) position[1], imagePosition[0], imagePosition[1]));
				updateFeedbackFigure(getFeedbackRectFigure(), startX, startY, (int) position[0], (int) position[1]);
			}

		};
		viewer.getPositionTool().addImagePositionListener(newListener, null);
	}

	private void updateFeedbackFigure(RectangleFigure rectFigure, int startX, int startY, int laterX, int laterY) {
		rectFigure.setBounds(new Rectangle(startX, startY, laterX - startX, laterY - startY));
	}

	private void handleFeedbackStart(RectangleFigure rectFigure, int posX, int posY) {
		rectFigure.setBounds(new Rectangle(posX, posY, 0, 0));
	}

	private void handleFeedbackEnd(@SuppressWarnings("unused") RectangleFigure rectFigure, int startX, int startY, int laterX, int laterY) {
		for (IRectFigureListener rectFigureListener : rectFigureListeners) {
			rectFigureListener.performTask(startX, startY, laterX, laterY);
		}
	}

	private static void log(MouseEvent event, String tag) {
		if (logging) {
			String toString = tag + " " + (event.button == 1 ? "Left" : event.button == 3 ? "Right" : event.button)
					+ " button x=" + event.x + " y=" + event.y + " count=" + event.count;
			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append(toString);
			strBuffer.append("\n");

			System.out.println(strBuffer.toString());
		}
	}

	private static void log(Event event, String tag) {
		if (logging) {
			String toString = tag + " x=" + event.x + " y=" + event.y + " count=" + event.count;

			StringBuffer strBuffer = new StringBuffer();
			strBuffer.append(toString);
			strBuffer.append("\n");

			System.out.println(strBuffer.toString());
		}
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
}
