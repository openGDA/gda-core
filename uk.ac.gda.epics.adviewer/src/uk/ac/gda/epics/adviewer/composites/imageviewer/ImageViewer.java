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

package uk.ac.gda.epics.adviewer.composites.imageviewer;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An image viewer that supports zooming and panning of the image implemented using SWT and draw2d
 * <p>
 */
public class ImageViewer {

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

	private String currentDir = ""; /* remembering file open directory */

	private FigureCanvas canvas;
	private Figure fig;
	protected Figure topFig;
	protected ImageFigure imgFig;
	protected ZoomContainer zoomContainer;
	protected ScrollPane scrollPane;

	private boolean keepZoomFit = false;
	private float zoomLevel;
	//private Cursor cursor;
	
	private static Group listenersGroup;
	private static Text eventConsole;
	private static boolean logging;	
	private static Label statusLabel;
	private static Label imageLabel;

	/**
	 * Constructor for the image viewer
	 * 
	 * @param parent
	 *            the parent of this viewer
	 * @param style
	 *            the style of this viewer
	 */
	public ImageViewer(Composite parent, int style) {
		canvas = new FigureCanvas(parent, style);
		canvas.setBackground(ColorConstants.white);

		// make this mode-dependent
		//cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_CROSS);		
		//canvas.setCursor(cursor);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
//		gridData.widthHint = 700;
		canvas.setLayoutData(gridData);

		LightweightSystem lws = new LightweightSystem(canvas);
		fig = new Figure();
		BorderLayout layout = new BorderLayout();
		fig.setLayoutManager(layout);
		lws.setContents(fig);

		parent.addListener(SWT.Resize, new Listener() {
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
		layout.setConstraint(zoomContainer, BorderLayout.CENTER);

		initialize();
	}

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

		fig.addMouseListener(new MouseListener.Stub() {
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
				mouseDraggedMovedHandler(me);
			}

			@Override
			public void mouseMoved(org.eclipse.draw2d.MouseEvent me) {
				mouseDraggedMovedHandler(me);
			}

			private void mouseDraggedMovedHandler(org.eclipse.draw2d.MouseEvent me) {
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
		return imgFig.getBounds();
	}

	/**
	 * Release resources held by this viewer.
	 */
	public void dispose() {
		if (image != null && !image.isDisposed()) {
			image.dispose();
		}

		if (canvas != null) {
			canvas.dispose();
		}
	}

	/**
	 * Callback function of button "open". Will open a file dialog, and choose the image file. It supports image formats
	 * supported by Eclipse.
	 */
	public void onFileOpen() {
		FileDialog fileChooser = new FileDialog(canvas.getShell(), SWT.OPEN);
		fileChooser.setText("Open image file");
		fileChooser.setFilterPath(currentDir);
		fileChooser.setFilterExtensions(new String[] { "*.gif;*.jpg;*.png;*.ico;*.bmp" });
		fileChooser.setFilterNames(new String[] { "SWT image" + " (gif, jpg, png, ico, bmp)" });
		String filename = fileChooser.open();
		if (filename != null) {
			loadImage(filename);
			currentDir = fileChooser.getFilterPath();
		}
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
		imageData = new ImageData(filename);
		image = new Image(canvas.getDisplay(), imageData);
		imgFig.setImage(image);
		centerScrollBars();
	}

	/**
	 * Reload image from a provided ImageData
	 * 
	 * @param imageDataIn
	 *            ImageData
	 */
	public void loadImage(final ImageData imageDataIn) {

		if (!canvas.isDisposed()) {
			final Image newImage = new Image(canvas.getDisplay(), imageDataIn);

			if (!canvas.isDisposed()){
				canvas.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						if (image != null && !image.isDisposed()) {
							image.dispose();
						}
						image = newImage;
						imageData = imageDataIn;
						imgFig.setImage(image);
					}
				});
			}
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

	/**
	 * Causes this viewer to have focus
	 * 
	 * @return true if the viewer gained focus, false otherwise
	 */
	public boolean setFocus() {
		return canvas.setFocus();
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

	static boolean layoutReset=false;
	static int size=10;

	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new org.eclipse.swt.layout.GridLayout(4, false));
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		final SashForm sashForm = new SashForm (shell, SWT.HORIZONTAL | SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		final ImageViewer imageViewer = new ImageViewer(sashForm, SWT.DOUBLE_BUFFERED);
		
		
		Composite composite = new Group(sashForm, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		sashForm.setWeights(new int[] {75,25});

		Button loadButton = new Button(composite, SWT.PUSH);
		loadButton.setText("Load Image");
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageViewer.onFileOpen();
				imageViewer.zoomFit();
			}
		});

		Button resetButton = new Button(composite, SWT.PUSH);
		resetButton.setText("Reset View");
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageViewer.resetView();
			}
		});

		Button fitButton = new Button(composite, SWT.PUSH);
		fitButton.setText("Zoom Fit");
		fitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageViewer.zoomFit();
			}
		});
		final Button keepFitButton = new Button(composite, SWT.CHECK);
		keepFitButton.setText("Keep Zoom Fit");
		keepFitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageViewer.setKeepZoomFit(keepFitButton.getSelection());
			}
		});


		final Button sendImagesBtn = new Button(composite, SWT.PUSH);
		sendImagesBtn.setText("Send Image");
		sendImagesBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				Dimension imageSize = new Dimension(640, 480);
				final Image image = new Image(display, imageSize.width, imageSize.height);
				GC gc = new GC(image);
				
				Color backgroundColour = display.getSystemColor(SWT.COLOR_BLUE);
				gc.setBackground(backgroundColour);
				gc.fillOval(0, 0, Math.min(size,imageSize.width), Math.min(size,imageSize.height));
				size++;

				display.asyncExec(new Runnable(){


					@Override
					public void run() {
						imageViewer.loadImage(image.getImageData());
						if (!layoutReset){
							layoutReset = true;
							imageViewer.getCanvas().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									imageViewer.resetView();
									TestFigure testFigure = new TestFigure();
									Rectangle testFigurePosition = new Rectangle(0, 0, -1, -1);		
									imageViewer.getTopFigure().add( testFigure, testFigurePosition);
								}
							});
						}						
					}
					
				});
			}
		});
		
		// Label to show status and cursor location in image.
		statusLabel = new Label(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		statusLabel.setLayoutData(gridData);
		gridData.horizontalSpan = 2;
		statusLabel.setText("Mouse position at: ");
		
		imageLabel = new Label(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		imageLabel.setLayoutData(gridData);
		imageLabel.setText("Image position at: ");		
		
		createListenersGroup(composite);
		initializeListeners(imageViewer);
		

		shell.open();

		ImageData imageDataIn;
		try {
			imageDataIn = new ImageData("C:\\Users\\Public\\Pictures\\Sample Pictures\\Tulips.jpg");
			imageViewer.loadImage(imageDataIn);
		} catch (Exception e1) {
			// unable to load file, probably not running on Tracy's computer
			imageViewer.onFileOpen();
		}
		imageViewer.zoomFit();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	private static void initializeListeners(ImageViewer viewer){
		viewer.getCanvas().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				log(event, "Mouse Down");
			}
			@Override
			public void mouseUp(MouseEvent event) {
				log(event, "Mouse Up");
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
			@Override
			public void imageStart(IImagePositionEvent event) {
				double[] position = event.getPosition();
				int[] imagePosition = event.getImagePosition();
				updateStatus((int) position[0], (int) position[1], imagePosition[0], imagePosition[1]);			
			}		
			@Override
			public void imageFinished(IImagePositionEvent event) {
				double[] position = event.getPosition();
				int[] imagePosition = event.getImagePosition();
				updateStatus((int) position[0], (int) position[1], imagePosition[0], imagePosition[1]);
			}			
			@Override
			public void imageDragged(IImagePositionEvent event) {
				double[] position = event.getPosition();
				int[] imagePosition = event.getImagePosition();
				updateStatus((int) position[0], (int) position[1], imagePosition[0], imagePosition[1]);
			}
		};
		viewer.getPositionTool().addImagePositionListener(newListener, null);		
	}
	private static void createListenersGroup(Composite composite) {
		listenersGroup = new Group (composite, SWT.NONE);
		listenersGroup.setLayout (new GridLayout (1, false));
		listenersGroup.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true, 2, 1));		
		listenersGroup.setText ("Listeners");
		
		/*
		 * Create the checkbox to add/remove listeners to/from the example widgets.
		 */
		final Button listenCheckbox = new Button (listenersGroup, SWT.CHECK);
		listenCheckbox.setText ("Listen");
		listenCheckbox.addSelectionListener (new SelectionAdapter () {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logging = listenCheckbox.getSelection ();
			}
		});	
		
		/*
		 * Create the button to clear the text.
		 */
		Button clearButton = new Button (listenersGroup, SWT.PUSH);
		clearButton.setText ("Clear");
		clearButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		clearButton.addSelectionListener (new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent e) {
				eventConsole.setText ("");
			}
		});	
		
		eventConsole = new Text (listenersGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData data = new GridData (GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		data.heightHint = 80;
		eventConsole.setLayoutData (data);		
	}
	
	private static void log(MouseEvent event, String tag){
		if (logging){
			String toString = tag + " " + (event.button == 1 ? "Left" : event.button == 3 ? "Right" : event.button) + " button x=" + event.x + " y=" + event.y + " count=" + event.count;
			if (eventConsole != null){
				eventConsole.append(toString);
				eventConsole.append("\n");
			}			
		}
	}	
	
	private static void log(Event event, String tag){
		if (logging){
			String toString = tag + " x=" + event.x + " y=" + event.y + " count=" + event.count;
			if (eventConsole != null){
				eventConsole.append(toString);
				eventConsole.append("\n");
			}			
		}
	}
	
	private static void updateStatus(int x, int y, int ix, int iy){
		statusLabel.setText("Mouse position at: (" + x + ", " + y + ")" );
		imageLabel.setText("Image position at: (" + ix + ", " + iy + ")");
	}

	/**
	 * Returns the IFigure of the image being drawn. The figure has an XYLayout and
	 * the layout manager should not be changed.
	 * @return top IFigure
	 */
	public IFigure getTopFigure() {
		return topFig;
	}
	ImageData defaultImageData;
	public void showDefaultImage() {
		if( 	defaultImageData == null){
			defaultImageData = new ImageData(getClass().getResourceAsStream("imageViewerDefaultImage.png"));
		}
		loadImage(defaultImageData);
		zoomFit();
		
	}
	
	public boolean isShowingDefault(){
		return imageData != null ? imageData == defaultImageData : false;
	}
}


class TestFigure extends Figure{
	private Polyline horz;
	private Polyline vert;
	private Dimension crossHairSize = new Dimension(100, 100);
	
	public TestFigure(){
		setLayoutManager(new XYLayout());

		horz = new Polyline();
		horz.setLineWidth(2);
		add(horz, new Rectangle(0, 0, -1, -1));
		
		vert = new Polyline();
		vert.setLineWidth(2);
		add(vert, new Rectangle(0, 0, -1, -1));
		
		update();
	}
	
	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
	
	/**
	 * Updates label contents and box size
	 * This needs to be called from UI thread
	 */
	private void update() {
		PointList horzPl = new PointList(2);
		horzPl.addPoint(new Point(0, crossHairSize.width/2));
		horzPl.addPoint(new Point(crossHairSize.width, crossHairSize.width/2));		
		horz.setPoints(horzPl);
		
		PointList vertPl = new PointList(2);
		vertPl.addPoint(new Point(crossHairSize.height/2, 0));
		vertPl.addPoint(new Point(crossHairSize.height/2, crossHairSize.height));		
		vert.setPoints(vertPl);
	}	
	
	public void setBeamSize(int size){
		this.crossHairSize.height = size;
		this.crossHairSize.width = size;
		update();
	}
	
	 public Dimension getCrossHairSize() {
		 return crossHairSize;
	 }	
	
}