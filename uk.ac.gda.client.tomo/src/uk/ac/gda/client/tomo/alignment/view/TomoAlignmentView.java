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

package uk.ac.gda.client.tomo.alignment.view;

import static org.eclipse.swt.SWT.DOUBLE_BUFFERED;
import gda.images.camera.ImageListener;
import gda.images.camera.MotionJpegOverHttpReceiverSwt;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.PageBook;

import uk.ac.gda.client.tomo.ViewerDisplayMode;
import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController.SAMPLE_STAGE_STATE;
import uk.ac.gda.client.tomo.alignment.view.utils.HistogramAdjuster;
import uk.ac.gda.client.tomo.alignment.view.utils.ScaleDisplay;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite;
import uk.ac.gda.client.tomo.composites.FullImageComposite;
import uk.ac.gda.client.tomo.composites.FullImageComposite.IRoiPointsListener;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.client.tomo.composites.ScaleBarComposite;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.MotionControlCentring;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.TomoAlignmentLeftPanelComposite;
import uk.ac.gda.client.tomo.composites.TomoAlignmentLeftPanelComposite.SAMPLE_OR_FLAT;
import uk.ac.gda.client.tomo.composites.TomoPlotComposite;
import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;
import uk.ac.gda.client.tomo.composites.ZoomedImageComposite;
import uk.ac.gda.client.tomo.composites.ZoomedImgCanvas;
import uk.ac.gda.client.tomo.views.BaseTomographyView;
import uk.ac.gda.ui.components.ColourSliderComposite;
import uk.ac.gda.ui.components.PointInDouble;

/**
 * View for Tomography alignment, and scan
 */
public class TomoAlignmentView extends BaseTomographyView implements ITomoAlignmentView {
	private static final String IOC_RUNNING_CONTEXT = "uk.ac.gda.client.tomo.alignment.isDetectorIocRunningContext";
	public static final String FIND_ROTATION_AXIS_DISPLAY_INFO = "ROTATION AXIS";

	private boolean isScanRunning = Boolean.TRUE;

	private static final IWorkbenchWindow ACTIVE_WORKBENCH_WINDOW = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();

	private static Color BACKGROUND_COLOR = ColorConstants.white;

	public enum RIGHT_PAGE {
		NONE, PLOT, ZOOM_DEMAND_RAW, NO_ZOOM, ZOOM_STREAM
	}

	public enum LEFT_PAGE {
		IMAGE_VIEWER;
	}

	public enum RIGHT_INFO {
		NONE, PROFILE, HISTOGRAM;
	}

	public boolean isScanRunning() {
		return isScanRunning;
	}

	public void setScanRunning(boolean isScanRunning) {
		this.isScanRunning = isScanRunning;
	}

	private TomoAlignmentLeftPanelComposite leftPanelComposite;

	public static final int RIGHT_WINDOW_WIDTH = 300;
	public static final String STREAM_STOPPED = "";
	private static final String EMPTY_STRING_VALUE = "-----";
	private static final String LBL_INTENSITY = "Intensity";
	private static final String Y_lbl = "y";
	private static final String X_lbl = "x";
	public static final String TIMESTAMP = "Timestamp :";
	public static final String FILE_NAME = "FileName :";
	public static final String BLANK_STR = "";
	public static final String SAMPLE_SINGLE = "SINGLE";
	public static final String FLAT_SINGLE = SAMPLE_SINGLE;
	public static final String SAMPLE_LIVE_STREAM = "LIVE";
	public static final String FLAT_LIVE_STREAM = SAMPLE_LIVE_STREAM;

	public static final String STATIC_FLAT = "STATIC FLAT";
	public static final String STATIC_DARK = "STATIC DARK";
	private static final String DEFAULT_LEFT_WINDOW_INFO_SIZE = "27mm";
	private static final String BOLD_TEXT_11 = "bold-text_11";
	private static final String BOLD_TEXT_16 = "bold-text_16";

	private ViewerDisplayMode leftWindowDisplayMode = ViewerDisplayMode.STREAM_STOPPED;
	private static final int CONTROLLER_HEIGHT = 220;

	private static final RGB BACKGROUND_COLOUR_RGB = new RGB(242, 242, 242);

	private boolean isSaving;
	private HistogramAdjuster histogramAdjuster;
	private TomoAlignmentControlComposite tomoControlComposite;
	/**/
	private Label lblLeftWindowDisplayModeStatus;
	/**/
	private Label lblRightWindowInfoNumPixels;
	/* Right Window Page Book Composites */
	private Composite page_rightWindow_nonProfile;
	/**/
	private MotionJpegOverHttpReceiverSwt leftVideoReceiver;
	/**/
	private ZoomedImageComposite page_nonProfile_streamZoom;

	private ZoomedImgCanvas demandRawZoomCanvas;
	/**/
	private Composite page_leftWindow_imgViewer;
	private Composite page_nonProfile_demandRaw;
	/**/
	private PageBook pageBook_zoomImg;
	private PageBook pageBook_rightInfo;
	private PageBook pageBook_rightWindow;

	private ScaleBarComposite rightScaleBar;
	private Composite page_nonProfile_noZoom;
	private Label lblFileTimeStamp;
	private Label lblFileName;
	private Composite page_rightInfo_profile;
	private Composite page_rightInfo_histogram;

	/**/
	private FullImageComposite leftWindowImageViewer;

	/**/

	/* Labels and default values */
	private boolean zoomReceiverStarted;
	/**
	 * Size of the screen pixel in mm.
	 */
	private Double screenPixelSize;

	private TomoAlignmentController tomoAlignmentController;
	private String viewPartName;
	private boolean fullImgReceiverStarted;

	private VideoListener leftVideoListener;

	private MotionJpegOverHttpReceiverSwt rightVideoReceiver;
	private VideoListener rightVideoListener;

	//
	private FormToolkit toolkit;
	/* Left Window Info viewer */
	private Label lblLeftWindowInfoNumPixels;
	/**/
	private FontRegistry fontRegistry;
	/**/
	private Composite page_rightWindow_plot;
	/**/
	private ScaleBarComposite leftScaleBar;

	private TomoPlotComposite tomoPlotComposite;
	private Composite page_rightInfo_nonProfile;
	private ColourSliderComposite contrastSliderComposite;
	private Label lblPixelX;
	private Label lblPixelY;
	private Label lblPixelIntensityVal;

	private TomoAlignmentViewController tomoViewController;

	public void setRightInfoPage(RIGHT_INFO rightInfo) {
		switch (rightInfo) {
		case NONE:
			pageBook_rightInfo.showPage(page_rightInfo_nonProfile);
			break;
		case PROFILE:
			pageBook_rightInfo.showPage(page_rightInfo_profile);
			break;
		case HISTOGRAM:
			pageBook_rightInfo.showPage(page_rightInfo_histogram);
			break;
		}
	}

	protected void updatePlots(IProgressMonitor monitor, int y) {
		tomoPlotComposite.updateProfilePlots(monitor, y);
		leftWindowImageViewer.hideProfileHighlighter();
	}

	@Override
	public void setFocus() {
		// Do nothing
	}

	public TomoAlignmentController getTomoAlignmentController() {
		return tomoAlignmentController;
	}

	public void setTomoAlignmentController(TomoAlignmentController tomoAlignmentViewController) {
		this.tomoAlignmentController = tomoAlignmentViewController;
		if (tomoAlignmentController != null) {
			tomoAlignmentController.isScanRunning();
		}
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public String getPartName() {
		return viewPartName;
	}

	public void loadErrorInDisplay(final String dialogTitle, final String errorMsg) {
		leftWindowImageViewer.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openError(leftWindowImageViewer.getShell(), dialogTitle,
						"Problem with tomography alignment \n" + errorMsg);
			}
		});
	}

	protected void switchOffCentring(final MotionControlCentring centring) {
		leftWindowImageViewer.removeOverlayImageFigureListener(tomoViewController);
		if (!leftWindowImageViewer.isDisposed()) {
			leftWindowImageViewer.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						tomoControlComposite.switchOff(centring);
						// setLeftWindowInfo(String.format("%1$s COMPLETE", centring.toString()));
					} catch (Exception e) {
						logger.error(centring.toString() + " failed ", e);
					}
				}
			});
		}
	}

	protected void enableLeftPanelControls() {
		if (leftPanelComposite != null && !leftPanelComposite.isDisposed()) {
			leftPanelComposite.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					leftPanelComposite.enableAll();
				}
			});
		}
	}

	protected void disableCameraControls() {
		if (leftPanelComposite != null && !leftPanelComposite.isDisposed()) {
			leftPanelComposite.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					leftPanelComposite.disableAll();
				}
			});
		}
	}

	/**
	 * @param imgViewer
	 * @param image
	 */
	void loadImageInUIThread(final FixedImageViewerComposite imgViewer, final ImageData image) {
		if (imgViewer.getDisplay() != null) {
			imgViewer.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					// Check if there is already an existing image in the imageviewer
					if (image != null
							&& (isSingle() || ((fullImgReceiverStarted || zoomReceiverStarted)))) {
						try {
							imgViewer.loadMainImage(image);

							//
							if (ZOOM_LEVEL.NO_ZOOM.equals(leftPanelComposite.getSelectedZoomLevel())) {
								page_nonProfile_streamZoom.clearZoomWindow();
							}
						} catch (Exception ex) {
							logger.error("Error loading image :{}", ex);
							loadErrorInDisplay("Error loading image", ex.getMessage());
						}
					} else {
						try {
							imgViewer.loadMainImage(null);
							page_nonProfile_streamZoom.clearZoomWindow();
						} catch (Exception ex) {
							logger.error("Error loading image :{}", ex);
							loadErrorInDisplay("Error loading image", ex.getMessage());
						}

					}
				}

				private boolean isSingle() {
					ViewerDisplayMode leftWindowViewerDisplayMode = getLeftWindowViewerDisplayMode();
					switch (leftWindowViewerDisplayMode) {
					case DARK_SINGLE:
					case FLAT_SINGLE:
					case SAMPLE_SINGLE:
					case STATIC_FLAT:
						return true;
					case FLAT_STREAM_LIVE:
					case ROTATION_AXIS:
					case SAMPLE_STREAM_LIVE:
					case STREAM_STOPPED:
						return false;
					}
					return false;
				}
			});
		}
	}

	/**
	 * Constructor - initalizes the font registry
	 */
	public TomoAlignmentView() {
		tomoViewController = new TomoAlignmentViewController(this);
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
			fontRegistry.put(BOLD_TEXT_16, new FontData[] { new FontData(fontName, 16, SWT.BOLD) });
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			BACKGROUND_COLOR = new Color(getViewSite().getShell().getDisplay(), BACKGROUND_COLOUR_RGB);
			toolkit = new FormToolkit(parent.getDisplay());
			toolkit.setBorderStyle(SWT.BORDER);

			Composite cmpRoot = toolkit.createComposite(parent);
			GridLayout layout = new GridLayout();
			setDefaultLayoutSettings(layout);
			cmpRoot.setLayout(layout);
			//
			Composite cmpMainWindow = createMainComposite(cmpRoot);
			GridData gd = new GridData(GridData.FILL_BOTH);
			cmpMainWindow.setLayoutData(gd);
			//
			tomoControlComposite = new TomoAlignmentControlComposite(cmpRoot, toolkit, SWT.None);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = CONTROLLER_HEIGHT;
			tomoControlComposite.setLayoutData(gd);
			//
			tomoAlignmentController.registerTomoAlignmentView(this);
			/* Calls the update fields in a separate thread - so that the UI is not blocked. */
			Future<Boolean> isSuccessful = tomoAlignmentController.init();
			new Thread(new RunUpdateAllFields(isSuccessful)).start();
			/**/

			//

			tomoControlComposite.addMotionControlListener(tomoViewController);

			tomoAlignmentController.addScanControllerUpdateListener(tomoViewController);
			leftPanelComposite.addLeftPanelListener(tomoViewController);
			tomoAlignmentController.isScanRunning();

			// Initialise the duration of scan and number of projections.
			tomoControlComposite.setNumberOfProjections(Integer.toString(tomoAlignmentController
					.getNumberOfProjections(tomoControlComposite.getResolution().getResolutionNumber())));

			tomoControlComposite.setEstimatedDuration(tomoAlignmentController
					.getEstimatedDurationOfScan(tomoControlComposite.getResolution()));

		} catch (Exception ex) {
			throw new RuntimeException("Error opening view", ex);
		}
		addPartListener();
	}

	/**
	 * @param layout
	 */
	private void setDefaultLayoutSettings(GridLayout layout) {
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
	}

	/**
	 * @param root
	 * @return {@link Composite}
	 * @throws Exception
	 */
	private Composite createMainComposite(Composite root) throws Exception {
		Composite mainComposite = toolkit.createComposite(root);
		GridLayout layout = new GridLayout(3, false);
		setDefaultLayoutSettings(layout);
		mainComposite.setLayout(layout);

		// Left panel
		Composite leftPanel = createLeftPanel(mainComposite);
		GridData layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 105;
		leftPanel.setLayoutData(layoutData);

		// Left window
		Composite leftWindow = createLeftWindow(mainComposite);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = 530;
		leftWindow.setLayoutData(layoutData);
		// Right Window
		Composite rightWindow = createRightWindow(mainComposite);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = 200;
		rightWindow.setLayoutData(layoutData);

		return mainComposite;

	}

	private Composite createLeftPanel(Composite mainComposite) {
		leftPanelComposite = new TomoAlignmentLeftPanelComposite(mainComposite, SWT.None);
		return leftPanelComposite;
	}

	// This contains the right side of the main window
	private Composite createRightWindow(Composite mainComposite) throws Exception {
		Composite rightWindowComposite = toolkit.createComposite(mainComposite);

		GridLayout gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		rightWindowComposite.setLayout(gl);
		pageBook_rightWindow = new PageBook(rightWindowComposite, SWT.None);

		GridData ld1 = new GridData(GridData.FILL_BOTH);
		pageBook_rightWindow.setLayoutData(ld1);

		page_rightWindow_nonProfile = toolkit.createComposite(pageBook_rightWindow);
		GridLayout layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		page_rightWindow_nonProfile.setLayout(layout);
		page_rightWindow_nonProfile.setLayoutData(new GridData(GridData.FILL_BOTH));
		page_rightWindow_nonProfile.setBackground(new Color(page_rightWindow_nonProfile.getDisplay(), new RGB(242, 242,
				242)));
		/**/
		Composite imgViewerComposite = toolkit.createComposite(page_rightWindow_nonProfile);
		imgViewerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gridlayout = new GridLayout();
		setDefaultLayoutSettings(gridlayout);
		imgViewerComposite.setLayout(gridlayout);

		pageBook_zoomImg = new PageBook(imgViewerComposite, SWT.None);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		pageBook_zoomImg.setLayoutData(gd);

		page_nonProfile_streamZoom = new ZoomedImageComposite(pageBook_zoomImg, SWT.DOUBLE_BUFFERED);

		page_nonProfile_streamZoom.setBackground(BACKGROUND_COLOR);

		page_nonProfile_demandRaw = toolkit.createComposite(pageBook_zoomImg);
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		page_nonProfile_demandRaw.setLayout(gl);
		demandRawZoomCanvas = new ZoomedImgCanvas(page_nonProfile_demandRaw, DOUBLE_BUFFERED);
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		demandRawZoomCanvas.setLayoutData(layoutData2);
		//
		page_nonProfile_noZoom = new Composite(pageBook_zoomImg, SWT.None);
		page_nonProfile_noZoom.setLayout(new FillLayout());

		page_nonProfile_noZoom.setBackground(BACKGROUND_COLOR);
		pageBook_zoomImg.showPage(page_nonProfile_noZoom);

		//
		rightVideoReceiver = new MotionJpegOverHttpReceiverSwt();
		rightVideoListener = new VideoListener(page_nonProfile_streamZoom);

		//
		Composite nonProfileInfoViewerComposite = createRightWindowInfoViewComposite(rightWindowComposite);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		nonProfileInfoViewerComposite.setLayoutData(ld);

		/* Profile composite */

		page_rightWindow_plot = toolkit.createComposite(pageBook_rightWindow);
		layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		page_rightWindow_plot.setLayout(layout);
		page_rightWindow_plot.setLayoutData(new GridData(GridData.FILL_BOTH));

		tomoPlotComposite = new TomoPlotComposite(page_rightWindow_plot, SWT.None);
		tomoPlotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tomoPlotComposite.addOverlayLineListener(tomoViewController);
		tomoPlotComposite.addTomoPlotListener(tomoViewController);
		pageBook_rightWindow.showPage(page_rightWindow_nonProfile);
		return rightWindowComposite;
	}

	/**
	 * @param mainComposite
	 * @return {@link Composite} for the left window
	 */
	private Composite createLeftWindow(Composite mainComposite) {
		Composite leftWindowComposite = toolkit.createComposite(mainComposite);
		GridLayout layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		leftWindowComposite.setLayout(layout);

		page_leftWindow_imgViewer = toolkit.createComposite(leftWindowComposite);
		page_leftWindow_imgViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout(2, false);
		setDefaultLayoutSettings(layout);
		page_leftWindow_imgViewer.setLayout(layout);
		/**/
		Composite viewerComposite = createLeftWindowImageViewerComposite(page_leftWindow_imgViewer);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		viewerComposite.setLayoutData(layoutData);
		//
		histogramAdjuster = new HistogramAdjuster(getSite().getShell().getDisplay());
		//
		contrastSliderComposite = new ColourSliderComposite(page_leftWindow_imgViewer, SWT.None);
		layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 30;
		contrastSliderComposite.setLayoutData(layoutData);
		contrastSliderComposite.setMaximum(70000);
		contrastSliderComposite.setMaximumLimit(histogramAdjuster.getMaxIntensity());
		contrastSliderComposite.addColourSliderListener(tomoViewController);

		Composite infoComposite = createLeftWindowInfoViewComposite(page_leftWindow_imgViewer);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		infoComposite.setLayoutData(layoutData);

		return leftWindowComposite;
	}

	private Composite createLeftWindowImageViewerComposite(Composite leftWindowComposite) {
		Composite imageViewAndInfoBarComposite = toolkit.createComposite(leftWindowComposite);
		GridLayout gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		imageViewAndInfoBarComposite.setLayout(gridLayout);

		//
		leftWindowImageViewer = new FullImageComposite(imageViewAndInfoBarComposite, SWT.DOUBLE_BUFFERED, true);
		leftWindowImageViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		leftWindowImageViewer.addZoomRectListener(tomoViewController);
		leftWindowImageViewer.addProfileListener(tomoViewController);
		leftWindowImageViewer.getCanvas().addMouseTrackListener(mouseTrackAdapter);
		leftWindowImageViewer.getCanvas().setBackground(
				new Color(leftWindowImageViewer.getDisplay(), new RGB(242, 242, 242)));
		//
		leftVideoReceiver = new MotionJpegOverHttpReceiverSwt();
		leftVideoListener = new VideoListener(leftWindowImageViewer);
		//
		return imageViewAndInfoBarComposite;
	}

	private MouseTrackAdapter mouseTrackAdapter = new MouseTrackAdapter() {
		@Override
		public void mouseHover(final org.eclipse.swt.events.MouseEvent e) {
			int x = e.x;
			int y = e.y;

			Dimension locWrtImageStart = new Point(x, y).getDifference(leftWindowImageViewer.getImageBounds()
					.getLocation());

			Integer roi1BinX = 1;
			try {
				roi1BinX = tomoAlignmentController.getRoi1BinX();
			} catch (Exception e1) {
				logger.error("Problem getting Roi1 BinX", e1);
			}
			if (locWrtImageStart.width >= 0 && locWrtImageStart.height >= 0
					&& locWrtImageStart.width <= tomoAlignmentController.getScaledX()
					&& locWrtImageStart.height <= tomoAlignmentController.getScaledY()) {
				lblPixelX.setText(Integer.toString(locWrtImageStart.width * roi1BinX));
				lblPixelY.setText(Integer.toString(locWrtImageStart.height * roi1BinX));
				// Since the display is generally a 24 bit display and the image intensity values are understood in the
				// 16 bit format - shifting the pixel value right by 8 bits.
				lblPixelIntensityVal.setText(Integer.toString(leftWindowImageViewer.getImageData().getPixel(
						locWrtImageStart.width, locWrtImageStart.height) >> 8));
			} else {
				lblPixelX.setText(EMPTY_STRING_VALUE);
				lblPixelY.setText(EMPTY_STRING_VALUE);
				lblPixelIntensityVal.setText(EMPTY_STRING_VALUE);
			}

		}
	};

	private Label lblExpTime;

	private Composite createLeftWindowInfoViewComposite(final Composite imageViewAndInfoBarComposite) {

		// Border composite used to make the borders thick
		Composite borderComposite = toolkit.createComposite(imageViewAndInfoBarComposite, SWT.BORDER);
		borderComposite.setBackground(ColorConstants.black);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;

		borderComposite.setLayout(layout);

		Composite infoViewerComposite = toolkit.createComposite(borderComposite);
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		infoViewerComposite.setLayout(gridLayout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 55;
		infoViewerComposite.setLayoutData(layoutData);

		Composite scaleBarComposite = new Composite(infoViewerComposite, SWT.None);
		scaleBarComposite.setBackground(ColorConstants.white);
		gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		gridLayout.marginHeight = 10;
		scaleBarComposite.setLayout(gridLayout);

		GridData gd = new GridData(GridData.FILL_BOTH);
		scaleBarComposite.setLayoutData(gd);

		leftScaleBar = new ScaleBarComposite(scaleBarComposite, SWT.None);
		GridData layoutData2 = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		leftScaleBar.setLayoutData(layoutData2);
		leftScaleBar.setBackground(ColorConstants.black);

		Composite statusExpComposite = toolkit.createComposite(infoViewerComposite);
		statusExpComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl1 = new GridLayout(2, true);
		setDefaultLayoutSettings(gl1);
		statusExpComposite.setLayout(gl1);

		lblLeftWindowDisplayModeStatus = toolkit.createLabel(statusExpComposite, "", SWT.CENTER);
		lblLeftWindowDisplayModeStatus.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblLeftWindowDisplayModeStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite exposureTimeComposite = toolkit.createComposite(statusExpComposite);
		exposureTimeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl2 = new GridLayout(2, true);
		setDefaultLayoutSettings(gl2);
		exposureTimeComposite.setLayout(gl2);

		Label expTimeLbl = toolkit.createLabel(exposureTimeComposite, "Exposure Time = ", SWT.RIGHT);
		expTimeLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblExpTime = toolkit.createLabel(exposureTimeComposite, "", SWT.LEFT);
		lblExpTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblLeftWindowInfoNumPixels = toolkit.createLabel(infoViewerComposite, DEFAULT_LEFT_WINDOW_INFO_SIZE, SWT.LEFT);
		lblLeftWindowInfoNumPixels.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblLeftWindowInfoNumPixels.setLayoutData(new GridData());

		Composite imagePixelValueComposite = new Composite(infoViewerComposite, SWT.None);
		imagePixelValueComposite.setBackground(ColorConstants.white);
		gridLayout = new GridLayout(6, true);
		gridLayout.marginHeight = 2;
		gridLayout.marginWidth = 5;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 2;
		imagePixelValueComposite.setLayout(gridLayout);
		imagePixelValueComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblX = new Label(imagePixelValueComposite, SWT.RIGHT);
		lblX.setBackground(ColorConstants.white);
		lblX.setText(X_lbl);
		lblX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPixelX = new Label(imagePixelValueComposite, SWT.LEFT);
		lblPixelX.setBackground(ColorConstants.white);
		lblPixelX.setText("----");
		lblPixelX.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblPixelX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblY = new Label(imagePixelValueComposite, SWT.RIGHT);
		lblY.setBackground(ColorConstants.white);
		lblY.setText(Y_lbl);
		lblY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPixelY = new Label(imagePixelValueComposite, SWT.LEFT);
		lblPixelY.setBackground(ColorConstants.white);
		lblPixelY.setText(EMPTY_STRING_VALUE);
		lblPixelY.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblPixelY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblPixelIntensity = new Label(imagePixelValueComposite, SWT.RIGHT);
		lblPixelIntensity.setBackground(ColorConstants.white);
		lblPixelIntensity.setText(LBL_INTENSITY);
		lblPixelIntensity.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPixelIntensityVal = new Label(imagePixelValueComposite, SWT.LEFT);
		lblPixelIntensityVal.setBackground(ColorConstants.white);
		lblPixelIntensityVal.setText(EMPTY_STRING_VALUE);
		lblPixelIntensityVal.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblPixelIntensityVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return borderComposite;
	}

	private Composite createRightWindowInfoViewComposite(Composite imageViewAndInfoBarComposite) {
		// Border composite used to make the borders thick
		Composite borderComposite = toolkit.createComposite(imageViewAndInfoBarComposite, SWT.BORDER);
		borderComposite.setBackground(ColorConstants.black);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		borderComposite.setLayout(layout);

		Composite baseInfoViewerComposite = toolkit.createComposite(borderComposite);
		GridLayout gridLayout = new GridLayout(2, true);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		baseInfoViewerComposite.setLayout(gridLayout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 55;
		baseInfoViewerComposite.setLayoutData(layoutData);

		Composite fileLabelsComposite = toolkit.createComposite(baseInfoViewerComposite);
		fileLabelsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout fileLabelsCompositeLayout = new GridLayout();
		fileLabelsCompositeLayout.marginHeight = 2;
		fileLabelsCompositeLayout.marginWidth = 2;
		fileLabelsCompositeLayout.horizontalSpacing = 2;
		fileLabelsCompositeLayout.verticalSpacing = 2;
		fileLabelsComposite.setLayout(fileLabelsCompositeLayout);

		lblFileName = toolkit.createLabel(fileLabelsComposite, BLANK_STR, SWT.LEFT);
		lblFileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblFileTimeStamp = toolkit.createLabel(fileLabelsComposite, BLANK_STR, SWT.LEFT);
		lblFileTimeStamp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		pageBook_rightInfo = new PageBook(baseInfoViewerComposite, SWT.None);
		pageBook_rightInfo.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Non profile page -
		page_rightInfo_nonProfile = toolkit.createComposite(pageBook_rightInfo);
		GridLayout layout2 = new GridLayout();
		setDefaultLayoutSettings(layout2);
		page_rightInfo_nonProfile.setLayout(layout2);

		Composite scalebarComposite = toolkit.createComposite(page_rightInfo_nonProfile, SWT.RIGHT_TO_LEFT);
		GridData gd = new GridData(GridData.FILL_BOTH);
		scalebarComposite.setLayoutData(gd);
		//
		gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		gridLayout.marginHeight = 10;
		scalebarComposite.setLayout(gridLayout);

		rightScaleBar = new ScaleBarComposite(scalebarComposite, SWT.RIGHT);
		GridData gd2 = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		rightScaleBar.setLayoutData(gd2);

		lblRightWindowInfoNumPixels = toolkit.createLabel(page_rightInfo_nonProfile, BLANK_STR, SWT.RIGHT);
		lblRightWindowInfoNumPixels.setFont(fontRegistry.get(BOLD_TEXT_11));
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.verticalAlignment = SWT.BEGINNING;
		lblRightWindowInfoNumPixels.setLayoutData(layoutData);

		// Profile page.
		page_rightInfo_profile = toolkit.createComposite(pageBook_rightInfo);
		GridLayout layout3 = new GridLayout(4, true);
		layout3.marginHeight = 2;
		layout3.marginWidth = 2;
		layout3.horizontalSpacing = 2;
		layout3.verticalSpacing = 2;
		page_rightInfo_profile.setLayout(layout3);

		// Histogram Page
		page_rightInfo_histogram = toolkit.createComposite(pageBook_rightInfo);
		GridLayout gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		page_rightInfo_histogram.setLayout(gl);

		setRightInfoPage(RIGHT_INFO.NONE);

		return borderComposite;
	}

	@Override
	public void updateFullImgStreamUrl(String mjPegURL) {
		leftVideoReceiver.setUrl(mjPegURL);

	}

	@Override
	public void updateZoomImgStreamUrl(String zoomImgJpegURL) {
		rightVideoReceiver.setUrl(zoomImgJpegURL);
	}

	@Override
	public void updateStreamWidget(int acquisitionState) {
		if (fullImgReceiverStarted) {
			if (acquisitionState == 0) {
				leftPanelComposite.deselectStreamButton();
				// stopFullVideoReceiver();
			} else if (acquisitionState == 1) {
				leftPanelComposite.selectStreamButton();
			}
		}
	}

	/**
	 * Uses the {@link Callable} API to invoke the calls to updateFields.
	 */
	public class RunUpdateAllFields implements Runnable {
		private final Future<Boolean> isSuccess;

		public RunUpdateAllFields(Future<Boolean> isSuccess) {
			this.isSuccess = isSuccess;
		}

		@Override
		public void run() {
			try {
				if (isSuccess.get()) {
					try {
						if (tomoAlignmentController.isStreaming() && !isScanRunning) {
							logger.debug("run->Stopping stream while updating all fields");
							// leftPanelComposite.stopStream();
						}
					} catch (Exception e) {
						logger.error("Problem identifying whether the streaming is switched on", e);
					}
				} else {
					// TODO-Ravi - Need to do anything here?
				}
			} catch (InterruptedException e) {
				logger.error("IOC May be down", e);
			} catch (ExecutionException e) {
				logger.error("IOC May be down", e);
			}
		}
	}

	protected boolean isModuleSelected() {
		return tomoControlComposite.getSelectedCameraModule() != CAMERA_MODULE.NO_MODULE;
	}

	/**
	 * VideoListener class to listen to image updates from the MotionMJPegreceiver.
	 */
	private final class VideoListener implements ImageListener<ImageData> {
		private String name;
		private final FixedImageViewerComposite imgViewer;

		public VideoListener(FixedImageViewerComposite imageViewer) {
			imgViewer = imageViewer;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void processImage(final ImageData image) {
			if (image == null) {
				return;
			}

			// Other wise if there is an image data then show the image composite.
			if (!isSaving && imgViewer != null && !imgViewer.isDisposed()) {
				if (imgViewer != null) {
					loadImageInUIThread(imgViewer, image);
				}
			}
		}

	}

	/**
	 * @return Returns the screenPixelSize.
	 */
	@Override
	public Double getScreenPixelSize() {
		return screenPixelSize;
	}

	/**
	 * @param screenPixelSize
	 *            The screenPixelSize to set.
	 */
	public void setScreenPixelSize(Double screenPixelSize) {
		this.screenPixelSize = screenPixelSize;
	}

	@Override
	public void updateLeftWindowNumPixelsLabel(final String cameraScaleBarDisplayText, final int barLengthInPixel) {
		if (lblLeftWindowInfoNumPixels != null && !lblLeftWindowInfoNumPixels.isDisposed()) {
			lblLeftWindowInfoNumPixels.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					// lblLeftWindowInfoNumPixels.setText(tomoAlignmentViewController.getCameraScaleBarDisplayText(1,
					// motionControlComposite.getSelectedCameraModule()));
					lblLeftWindowInfoNumPixels.setText(cameraScaleBarDisplayText);

					try {
						// int barLengthInPixel = tomoAlignmentViewController.getLeftBarLengthInPixel(1,
						// motionControlComposite.getSelectedCameraModule());
						leftScaleBar.setScaleWidth(barLengthInPixel);
						lblLeftWindowInfoNumPixels.pack(true);
					} catch (Exception e) {
						logger.error("Error setting scale bar", e);
					}
				}
			});
		}
	}

	@Override
	public void updateRightWindowNumPixelsLabel(final String cameraScaleBarDisplayText, final int barLengthInPixel) {
		if (lblRightWindowInfoNumPixels != null && !lblRightWindowInfoNumPixels.isDisposed()) {
			lblRightWindowInfoNumPixels.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!ZOOM_LEVEL.NO_ZOOM.equals(leftPanelComposite.getSelectedZoomLevel())) {
						lblRightWindowInfoNumPixels.setText(cameraScaleBarDisplayText);

						try {
							logger.debug("Screen bar length in pixel:{}", barLengthInPixel);
							rightScaleBar.setScaleWidth(barLengthInPixel);
							lblRightWindowInfoNumPixels.redraw();
						} catch (Exception e) {
							logger.error("Error setting scale bar", e);
						}
					}
				}
			});
		}
	}

	@Override
	public void setRotationDeg(final Double rotationMotorDeg) {
		if (tomoControlComposite != null && !tomoControlComposite.isDisposed()) {
			tomoControlComposite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tomoControlComposite.moveRotationSliderTo(rotationMotorDeg);
				}
			});
		}
	}

	@Override
	public void setFlatFieldCorrection(boolean enabled) {
		leftPanelComposite.setFlatFieldCorrection(enabled);
	}

	@Override
	public void setPreferredSampleExposureTimeToWidget(double preferredExposureTime) {
		leftPanelComposite.setPreferredSampleExposureTime(preferredExposureTime);
		tomoControlComposite.setEstimatedDuration(tomoAlignmentController
				.getEstimatedDurationOfScan(tomoControlComposite.getResolution()));
	}

	@Override
	public void setPreferredFlatExposureTimeToWidget(double preferredExposureTime) {
		leftPanelComposite.setPreferredFlatExposureTime(preferredExposureTime);
	}

	@Override
	public void setCameraModule(final CAMERA_MODULE module) {
		tomoControlComposite.setCameraModule(module);
		updateScaleBars(module);
	}

	protected void updateScaleBars(final CAMERA_MODULE module) {
		if (leftWindowImageViewer != null && !leftWindowImageViewer.isDisposed()) {
			leftWindowImageViewer.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					ScaleDisplay leftBarLengthInPixel = tomoAlignmentController.getLeftBarLengthInPixel(
							leftWindowImageViewer.getBounds().width / 3, module);
					if (leftBarLengthInPixel != null) {
						updateLeftWindowNumPixelsLabel(leftBarLengthInPixel.toString(),
								leftBarLengthInPixel.getBarLengthInPixel());
					}
					//
					ScaleDisplay rightBarLengthInPixel = tomoAlignmentController.getRightBarLengthInPixel(
							page_rightWindow_nonProfile.getBounds().width / 2 - 10, module,
							leftPanelComposite.getSelectedZoomLevel());
					if (rightBarLengthInPixel != null) {
						updateRightWindowNumPixelsLabel(rightBarLengthInPixel.toString(),
								rightBarLengthInPixel.getBarLengthInPixel());
					}

				}
			});
		}
	}

	@Override
	public void dispose() {
		try {
			if (leftWindowImageViewer != null) {
				logger.debug("Removing zoom rect listener");
				leftWindowImageViewer.removeZoomRectListener(tomoViewController);
				leftWindowImageViewer.removeProfileListener(tomoViewController);
				leftWindowImageViewer.removeOverlayImageFigureListener(tomoViewController);
				leftWindowImageViewer.dispose();
			}
			contrastSliderComposite.removeColourSliderListener(tomoViewController);
			contrastSliderComposite.dispose();
			tomoPlotComposite.removeOverlayLineListener(tomoViewController);

			leftPanelComposite.removeLeftPanelListener(tomoViewController);
			stopFullVideoReceiver();
			leftVideoListener = null;
			rightVideoListener = null;

			leftVideoReceiver = null;
			rightVideoReceiver = null;

			tomoControlComposite.removeMotionControlListener(tomoViewController);
			logger.debug("Disposing tomoalignment viewer");

			page_nonProfile_streamZoom.dispose();
			demandRawZoomCanvas.dispose();

			pageBook_zoomImg.dispose();
			//
			page_rightWindow_plot.dispose();
			page_rightWindow_nonProfile.dispose();
			page_leftWindow_imgViewer.dispose();

			pageBook_rightWindow.dispose();
			//
			tomoControlComposite.dispose();
			//
			tomoPlotComposite.removeTomoPlotListener(tomoViewController);
			tomoPlotComposite.removeOverlayLineListener(tomoViewController);
			tomoPlotComposite.dispose();
			//
			tomoAlignmentController.unregisterTomoAlignmentView(this);
			tomoAlignmentController.dispose();
			toolkit.dispose();
			tomoAlignmentController.removeScanControllerUpdateListener(tomoViewController);

			super.dispose();
		} catch (Exception ex) {
			logger.error("Exception in dispose", ex);
		}
	}

	@Override
	public void updateRotationMotorBusy(boolean isBusy) {
		tomoControlComposite.setRotationMotorBusy(isBusy);
	}

	@Override
	public void updateErrorAligningTilt(String status) {
		loadErrorInDisplay("Error preparing TILT alignment", status);
	}

	@Override
	public void reset() {
		try {
			tomoAlignmentController.resetAll();
		} catch (Exception e) {
			logger.error("Problem resetting detector", e);
			loadErrorInDisplay("Error while reseting the camera", "Connection with the camera IOC may be disrupted.");
		}
	}

	@Override
	public void updateModuleButtonText(final String unit, final Map<Integer, String> moduleButtonText) {
		if (moduleButtonText != null && !(moduleButtonText.isEmpty())) {
			if (tomoControlComposite != null && !tomoControlComposite.isDisposed()) {
				tomoControlComposite.getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						tomoControlComposite.setModuleButtonText(unit, moduleButtonText.get(1),
								moduleButtonText.get(2), moduleButtonText.get(3), moduleButtonText.get(4));

					}
				});
			}
		}
	}

	@Override
	public void setCameraMotionMotorPosition(double cameraMotionMotorPosition) {
		tomoControlComposite.setCameraMotionPosition(cameraMotionMotorPosition);
	}

	private void startFullVideoReceiver() {
		if (!fullImgReceiverStarted) {
			leftVideoReceiver.addImageListener(leftVideoListener);
			try {
				leftVideoReceiver.createConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			fullImgReceiverStarted = true;
		}
	}

	/**
	 * Stops the video receiver.
	 */
	protected void stopFullVideoReceiver() {
		if (fullImgReceiverStarted) {
			leftVideoReceiver.closeConnection();

			leftVideoReceiver.removeImageListener(leftVideoListener);
			fullImgReceiverStarted = false;

			//
			leftPanelComposite.deselectStreamButton();
			setLeftWindowInfo(TomoAlignmentView.STREAM_STOPPED);
			//
			if (zoomReceiverStarted) {
				stopZoomVideoReceiver();
			}
		}
	}

	/**
	 * Starts the zoom receiver.
	 */
	protected void startZoomVideoReceiver() {
		if (!zoomReceiverStarted) {
			rightVideoReceiver.addImageListener(rightVideoListener);
			rightVideoReceiver.createConnection();
			zoomReceiverStarted = true;
		}
	}

	/**
	 * Stops the zoom receiver.
	 */
	protected void stopZoomVideoReceiver() {
		if (zoomReceiverStarted) {
			rightVideoReceiver.closeConnection();
			rightVideoReceiver.removeImageListener(rightVideoListener);
			zoomReceiverStarted = false;

		}
	}

	/**
	 * @param info
	 */
	protected void setLeftWindowInfo(final String info) {
		leftWindowDisplayMode = ViewerDisplayMode.getDisplayMode(info);
		setLeftWindowDisplayMode(leftWindowDisplayMode);
		if (info != null && lblLeftWindowDisplayModeStatus != null && !lblLeftWindowDisplayModeStatus.isDisposed()) {
			lblLeftWindowDisplayModeStatus.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					lblLeftWindowDisplayModeStatus.setText(info.toUpperCase());
					switchOffLeftWindowDisplayImage();
				}

			});
		}
	}

	private void switchOffLeftWindowDisplayImage() {
		if (tomoControlComposite.getSelectedCentring() == null) {
			if (leftWindowDisplayMode == ViewerDisplayMode.STREAM_STOPPED) {
				loadImageInUIThread(leftWindowImageViewer, null);
				leftPanelComposite.switchOffCrosshair();
				leftPanelComposite.setZoom(ZOOM_LEVEL.NO_ZOOM);
				lblPixelX.setText(EMPTY_STRING_VALUE);
				lblPixelY.setText(EMPTY_STRING_VALUE);
				lblPixelIntensityVal.setText(EMPTY_STRING_VALUE);
				// Stop the histogram
				leftPanelComposite.stopHistogram();
			}
		}
	}

	/**
	 * Set of procedures that need to be done when profiling is stopped.
	 */
	protected void stopProfiling() {
		if (leftPanelComposite.isProfileSelected()) {
			leftPanelComposite.deselectProfileButton();
			leftWindowImageViewer.hideLineProfiler();
			setRightPage(RIGHT_PAGE.NONE);
			setRightInfoPage(RIGHT_INFO.NONE);
			tomoPlotComposite.setImagesToPlot(null, null);
			ZOOM_LEVEL selectedZoomLevel = leftPanelComposite.getSelectedZoomLevel();
			if (!ZOOM_LEVEL.NO_ZOOM.equals(selectedZoomLevel)) {
				leftPanelComposite.setZoom(selectedZoomLevel);
			}
		}
	}

	/**
	 * Set of procedures that need to run when streaming is stopped this needs to be called sparingly and does not cause
	 * the Stream button on the screen to be toggled.<br>
	 * the {@link TomoAlignmentLeftPanelComposite#stopStream()} should be called for that purpose
	 */
	public void stopStreaming() {
		logger.debug("stopStreaming -> Stop video receiver and call stop acquiring");
		stopFullVideoReceiver();
		try {
			tomoAlignmentController.stopAcquiring();
		} catch (Exception e) {
			logger.error("stopStreaming -> Problem stop acquiring", e);
		} finally {
			setLeftWindowInfo(STREAM_STOPPED);
		}
	}

	/**
	 * see {@link TomoAlignmentLeftPanelComposite#startStreaming()} to get the Stream button enabled
	 * 
	 * @throws InvocationTargetException
	 */
	public void startStreaming(final double acquireTime) throws InvocationTargetException {
		try {

			logger.debug("startStreaming -> change the page book to image view -start acquiring.");
			// Start the acquisition on the detector.

			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					boolean isAmplified = leftPanelComposite.isAmplified();
					double lower = getContrastLower();
					double upper = getContrastUpper();
					tomoAlignmentController.startAcquiring(acquireTime, isAmplified, lower, upper);
				}
			});

			// Shouldn't be showing the file name and the file timestamp while streaming imgs.
			lblFileTimeStamp.setText(BLANK_STR);
			lblFileName.setText(BLANK_STR);
			// Need to stop profiling
			leftPanelComposite.deSelectSaturationButton();
			// If zoom is selected then update the zoomed window.
			if (leftPanelComposite.getSelectedZoomLevel() != ZOOM_LEVEL.NO_ZOOM) {
				setRightPage(RIGHT_PAGE.ZOOM_STREAM);
				leftPanelComposite.setZoom(leftPanelComposite.getSelectedZoomLevel());
			}
			stopProfiling();
			// Set the MJPeg Streamer URL
			setMJPegUrl();
			// Start the video receiver.
			logger.debug("startStreaming -> change the page book to image view -start videoReceiver");
			if (leftVideoReceiver.isUrlSet()) {
				startFullVideoReceiver();
			}
			updateScaleBars(tomoControlComposite.getSelectedCameraModule());
		} catch (InvocationTargetException e) {
			logger.error("startStreaming -> Problem acquiring FFMJpeg from the camera", e);
			throw new InvocationTargetException(e, "Cannot start streaming: ");
		} catch (InterruptedException e) {
			logger.error("Shouldn't flow through here - but stream operation was somehow interrupted", e);
		}
	}

	protected void setMJPegUrl() {
		Future<Boolean> isSuccessful = tomoAlignmentController.getStreamUrl();
		BusyIndicator.showWhile(leftWindowImageViewer.getDisplay(), new GetStreamURLThread(isSuccessful));
	}

	/**
	 * Uses the {@link Callable} API to invoke the calls to updateFields.
	 */
	public class GetStreamURLThread implements Runnable {
		private static final String ERROR_STREAM_START_shortdesc = "The camera MJPeg stream cannot be started. The IOC may be down";
		private static final String ERROR_STARTING_STREAM_label = "Error Starting Stream";
		private final Future<Boolean> isSuccess;

		public GetStreamURLThread(Future<Boolean> isSuccess) {
			this.isSuccess = isSuccess;
		}

		@Override
		public void run() {
			try {
				if (!isSuccess.get()) {
					stopFullVideoReceiver();
				}
			} catch (InterruptedException e) {
				logger.error("IOC May be down", e);
				MessageDialog.openError(getViewSite().getShell(), ERROR_STARTING_STREAM_label,
						ERROR_STREAM_START_shortdesc);
				//
				if (!isScanRunning) {
					leftPanelComposite.stopStream();
				}
				//
			} catch (ExecutionException e) {
				logger.error("IOC May be down", e);
				MessageDialog.openError(getViewSite().getShell(), ERROR_STARTING_STREAM_label,
						ERROR_STREAM_START_shortdesc);
				//
				leftPanelComposite.stopStream();
				stopFullVideoReceiver();
			}

		}
	}

	public boolean isStreamingSampleExposure() {
		return leftPanelComposite.isStreamButtonSelected()
				&& SAMPLE_OR_FLAT.SAMPLE.equals(leftPanelComposite.getStreamState());
	}

	public boolean isStreamingFlatExposure() {
		return leftPanelComposite.isStreamButtonSelected()
				&& SAMPLE_OR_FLAT.FLAT.equals(leftPanelComposite.getStreamState());
	}

	public void setHistogramAdjusterMainImageData(ImageData imgData) {
		histogramAdjuster.setMainImageData(imgData);
	}

	public void setHistogramAdjusterOverlayImageData(ImageData imgData) {
		histogramAdjuster.setOverlayImageData(imgData);
	}

	public CAMERA_MODULE getSelectedCameraModule() {
		return tomoControlComposite.getSelectedCameraModule();
	}

	public void setRightPage(final RIGHT_PAGE page) {
		if (pageBook_rightWindow.getDisplay() != null && !pageBook_rightWindow.getDisplay().isDisposed()) {
			pageBook_rightWindow.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					switch (page) {
					case NONE:
						pageBook_rightWindow.showPage(page_rightWindow_nonProfile);
						break;
					case PLOT:
						pageBook_rightWindow.showPage(page_rightWindow_plot);
						break;
					case ZOOM_DEMAND_RAW:
						pageBook_rightWindow.showPage(page_rightWindow_nonProfile);
						pageBook_zoomImg.showPage(page_nonProfile_demandRaw);
						break;
					case NO_ZOOM:
						pageBook_rightWindow.showPage(page_rightWindow_nonProfile);
						pageBook_zoomImg.showPage(page_nonProfile_noZoom);
						break;
					case ZOOM_STREAM:
						pageBook_rightWindow.showPage(page_rightWindow_nonProfile);
						pageBook_zoomImg.showPage(page_nonProfile_streamZoom);
					}
				}
			});
		}
	}

	public void loadDemandRawZoom(ImageData appliedSaturation, PointInDouble demandRawScale, boolean b) {
		demandRawZoomCanvas.loadImage(appliedSaturation, demandRawScale, b);
	}

	public void loadDemandRawZoom(String fileName, PointInDouble demandRawScale, boolean b) {
		demandRawZoomCanvas.loadImage(fileName, demandRawScale, b);
	}

	public void clearZoomWindow() {
		demandRawZoomCanvas.clearZoomWindow();
		page_nonProfile_streamZoom.clearZoomWindow();
	}

	private synchronized void setLeftWindowDisplayMode(ViewerDisplayMode viewerDisplayMode) {
		leftWindowDisplayMode = viewerDisplayMode;
	}

	public ViewerDisplayMode getLeftWindowViewerDisplayMode() {
		return leftWindowDisplayMode;
	}

	public void enableRoiWidgets() {
		leftWindowImageViewer.enableRoiWidgets();
	}

	public void disableRoiWidget() {
		leftWindowImageViewer.disableRoiWidget();

	}

	public void setValidRoi(PointList validPoints) {
		leftWindowImageViewer.setValidRoiPoints(validPoints);
	}

	public void addLeftWindowImageRoiPointsListener(IRoiPointsListener roiPointsListener) {
		leftWindowImageViewer.addRoiPointsListener(roiPointsListener);
	}

	public void removeLeftWindowImageRoiPointsListener(IRoiPointsListener roiPointsListener) {
		leftWindowImageViewer.removeRoiPointsListener(roiPointsListener);
	}

	public ImageData getLeftWindowImageData() {
		return leftWindowImageViewer.getImageData();
	}

	public void resetLeftWindowRoiBounds() {
		leftWindowImageViewer.resetRoiWidgets();
	}

	@Override
	public void setSampleInOutState(SAMPLE_STAGE_STATE state) {
		switch (state) {
		case IN:
			leftPanelComposite.selectSampleInButton();
			break;
		case OUT:
			leftPanelComposite.selectSampleOutButton();
			break;
		}
	}

	public String saveConfiguration() throws Exception {
		final String[] configId = new String[1];
		try {
			isSaving = true;
			leftPanelComposite.startStreaming();
			leftPanelComposite.setZoom(ZOOM_LEVEL.NO_ZOOM);
			// rsr31645 - Commented below code which opens the save dialog to show images at 0 and +90. This would be
			// used for stitching images in the configuration view, however, the stitch feature will not be used for
			// sometime now.
			// AlignmentConfigSaveDialog configSaveDialog = new AlignmentConfigSaveDialog(getViewSite().getShell(),
			// tomoAlignmentViewController, leftVideoReceiver);
			// configSaveDialog.open();
			//
			// int returnCode = configSaveDialog.getReturnCode();
			// final ImageLocationRelTheta viewerBtnSelected = configSaveDialog.getViewerButtonSelected();

			// if (IDialogConstants.OK_ID == returnCode) {

			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {
				private final DecimalFormat threePrecision = new DecimalFormat("#.###");

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.setTaskName("Saving configuration...");
						SaveableConfiguration configuration = new SaveableConfiguration();
						// Module number
						configuration.setModuleNumber(tomoControlComposite.getSelectedCameraModule().getValue());
						// Sample Acquisition time
						configuration.setSampleAcquisitonTime(Double.valueOf(threePrecision.format(leftPanelComposite
								.getSampleExposureTime())));
						// Flat Acquisition time
						configuration.setFlatAcquisitionTime(Double.valueOf(threePrecision.format(leftPanelComposite
								.getFlatExposureTime())));
						// Sample description
						configuration.setSampleDescription(tomoControlComposite.getSampleDescription());
						// ROI points
						configuration.setRoiPoints(leftWindowImageViewer.getRoiPoints());
						// Energy
						configuration.setEnergy(tomoControlComposite.getEnergy());
						// resolution
						configuration.setResolution3D(tomoControlComposite.getResolution());
						// sample weight
						configuration.setSampleWeight(tomoControlComposite.getSampleWeight());
						// number of projections
						configuration.setNumProjections(tomoControlComposite.getFramesPerProjection());
						//
						
						//if(not present)
						configuration.setTomoRotationAxis(leftWindowImageViewer.getCrossWire1XRelativeToImage()
								* tomoAlignmentController.getLeftWindowBinValue());

						// String imgAtTheta = null;
						// double theta = 0;
						// String imgAtThetaPlus90 = null;
						// try {
						// switch (viewerBtnSelected) {
						// case THETA:
						// theta = tomoAlignmentViewController.getRotationMotorDeg();
						// imgAtTheta = tomoAlignmentViewController.demandRawWithStreamOn(monitor, false);
						// tomoAlignmentViewController.moveRotationMotorBy(monitor, 90);
						// imgAtThetaPlus90 = tomoAlignmentViewController
						// .demandRawWithStreamOn(monitor, false);
						// tomoAlignmentViewController.moveRotationMotorBy(monitor, -90);
						// break;
						// case THETA_PLUS_90:
						// theta = tomoAlignmentViewController.getRotationMotorDeg() - 90;
						// imgAtThetaPlus90 = tomoAlignmentViewController
						// .demandRawWithStreamOn(monitor, false);
						// tomoAlignmentViewController.moveRotationMotorBy(monitor, -90);
						// imgAtTheta = tomoAlignmentViewController.demandRawWithStreamOn(monitor, false);
						// break;
						// }
						// } catch (Exception ex) {
						// logger.error("Unable to save images at theta:{}", ex);
						// throw new InvocationTargetException(ex, "Cannot save images at theta");
						// }
						// // stitching angle
						// configuration.setStitchingAngle(theta);
						// // image at theta
						// configuration.setImageAtTheta(imgAtTheta);
						// // image at theta+90
						// configuration.setImageAtThetaPlus90(imgAtThetaPlus90);

						if (leftWindowImageViewer.getCrossWire1Vertical().isVisible()) {
							int x = leftWindowImageViewer.getCrossWire1Vertical().getPoints().getFirstPoint().x
									- leftWindowImageViewer.getImageBounds().x;
							logger.debug("Tomo rotation axis:{}", x);
							configuration.setTomoRotationAxis(x * tomoAlignmentController.getLeftWindowBinValue());
						}
						try {
							configId[0] = tomoAlignmentController.saveConfiguration(monitor, configuration);
						} catch (Exception e) {
							logger.error("Unable to save configuration", e);
							throw new InvocationTargetException(e, "Cannot save alignment configuration");
						}
					} catch (InvocationTargetException e) {
						throw e;
					} finally {
						monitor.done();
					}
				}
			});
			tomoControlComposite.clearSampleDescription();
			// }
		} finally {
			isSaving = false;
		}
		return configId[0];
	}

	@Override
	public void setEnergy(double energy) {
		tomoControlComposite.setEnergyValue(energy);
	}

	@Override
	public void setResolutionPixelSize(String resolutionPixelSize) {
		tomoControlComposite.setResolutionPixelSize(resolutionPixelSize);
	}

	@Override
	public void setResolution(RESOLUTION res) {
		tomoControlComposite.setResolution(res);
	}

	@Override
	public void setAdjustedPreferredExposureTimeToWidget(double preferredExposureTime) {
		if (leftWindowDisplayMode == ViewerDisplayMode.SAMPLE_STREAM_LIVE
				|| leftWindowDisplayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
			setPreferredSampleExposureTimeToWidget(preferredExposureTime);
			tomoAlignmentController.setPreferredSampleExposureTime(preferredExposureTime);

			// FIXME - Only updating the estimated duration based on the sample exposure time and not the flat exposure
			// time - this needs to be modified once the flat exposure time is used in the experiment scan.

			tomoControlComposite.setEstimatedDuration(tomoAlignmentController
					.getEstimatedDurationOfScan(tomoControlComposite.getResolution()));
		} else if (leftWindowDisplayMode == ViewerDisplayMode.FLAT_STREAM_LIVE
				|| leftWindowDisplayMode == ViewerDisplayMode.FLAT_SINGLE) {
			setPreferredFlatExposureTimeToWidget(preferredExposureTime);
			tomoAlignmentController.setPreferredFlatExposureTime(preferredExposureTime);
		} else {
			tomoAlignmentController.setPreferredSampleExposureTime(preferredExposureTime);
		}
	}

	public TomoAlignmentLeftPanelComposite getLeftPanelComposite() {
		return leftPanelComposite;
	}

	public TomoAlignmentControlComposite getTomoControlComposite() {
		return tomoControlComposite;
	}

	public FullImageComposite getLeftWindowImageViewer() {
		return leftWindowImageViewer;
	}

	public TomoPlotComposite getTomoPlotComposite() {
		return tomoPlotComposite;
	}

	protected void unZoomInUI() {
		if (page_nonProfile_noZoom != null && !page_nonProfile_noZoom.isDisposed()) {
			leftPanelComposite.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					leftWindowImageViewer.hideZoomRectangleFigure();
					setRightPage(RIGHT_PAGE.NO_ZOOM);

					rightScaleBar.setScaleWidth(0);
					lblRightWindowInfoNumPixels.setText(BLANK_STR);
					if (leftPanelComposite.isProfileSelected()) {
						Rectangle lineBounds = leftWindowImageViewer.getProfilerLineBounds();
						int y = lineBounds.y - leftWindowImageViewer.getImageBounds().y;
						updatePlots(new NullProgressMonitor(), y * tomoAlignmentController.getLeftWindowBinValue());
					}
				}
			});
		}
	}

	protected void displayFileDetails(ViewerDisplayMode viewDisplayMode) throws Exception {
		String rawFileName = viewDisplayMode.getFileName(getTomoAlignmentController());
		lblFileName.setText(String.format("%1$s %2$s", TomoAlignmentView.FILE_NAME, rawFileName));
		if (rawFileName != null) {
			File checkFile = new File(rawFileName);
			if (checkFile.exists()) {
				lblFileTimeStamp.setText(String.format("%1$s %2$s", TomoAlignmentView.TIMESTAMP,
						getSimpleDateFormat(checkFile.lastModified())));
			}
		} else {
			throw new IllegalArgumentException("Single image could not be loaded");
		}
	}

	private String getSimpleDateFormat(double epoch) {
		Date date = new Date((long) (epoch));
		SimpleDateFormat simpleDatef = new SimpleDateFormat("dd/MM/yy hh:mm:ss.SSS");
		return simpleDatef.format(date);
	}

	public ZoomedImgCanvas getDemandRawZoomCanvas() {
		return demandRawZoomCanvas;
	}

	public HistogramAdjuster getHistogramAdjuster() {
		return histogramAdjuster;
	}

	public void setRightWindowInfoNumPixels(String numPixels) {
		lblRightWindowInfoNumPixels.setText(numPixels);
	}

	public void setRightScaleBarWidth(int barLengthInPixel) {
		rightScaleBar.setScaleWidth(barLengthInPixel);
	}

	public void setYLabelValue(String yLblValue) {
		tomoPlotComposite.setYLabelValue(yLblValue);
	}

	public void setXLabelValue(String formattedXVal) {
		tomoPlotComposite.setXLabelValue(formattedXVal);
	}

	public void setProfileIntensityValue(String profileIntensityValue) {
		tomoPlotComposite.setProfileIntensityValue(profileIntensityValue);
	}

	protected int getContrastLower() {
		return (int) contrastSliderComposite.getLowerValue();
	}

	protected int getContrastUpper() {
		return (int) contrastSliderComposite.getUpperValue();
	}

	protected void setTiltLastSaveDateTime() {
		String tiltLastSaved = getSimpleDateFormat(System.currentTimeMillis());
		tomoControlComposite.setTiltLastDoneLabel(tiltLastSaved);
	}

	public void moveHigherContrastSliderTo(final int value) {
		if (getViewSite().getShell().getDisplay() != null && !getViewSite().getShell().getDisplay().isDisposed()) {
			getViewSite().getShell().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {

					contrastSliderComposite.moveTopSliderTo(value);
				}
			});
		}
	}

	public void moveLowerContrastSliderTo(final int value) {
		if (getViewSite().getShell().getDisplay() != null && !getViewSite().getShell().getDisplay().isDisposed()) {
			getViewSite().getShell().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					contrastSliderComposite.moveBottomSliderTo(value);
				}
			});
		}
	}

	@Override
	protected void doPartDeactivated() {
		// check if the scan is on.
		if (!isScanRunning()) {
			stopFullVideoReceiver();
		} else {
			// disable all the controls and inform the user that the scan is running.
		}
	}

	@Override
	protected String getDetectorPortName() throws Exception {
		return tomoAlignmentController.getDetectorPortName();
	}

	@Override
	protected String getIocRunningContext() {
		return IOC_RUNNING_CONTEXT;
	}

	@Override
	public void updateExposureTimeToWidget(final double acqExposure) {
		if (lblExpTime != null && !lblExpTime.isDisposed()) {
			lblExpTime.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					lblExpTime.setText(String.format("%.3g (s)", acqExposure));
				}
			});
		}
	}

}
