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

package uk.ac.gda.client.tomo.alignment.view;

import static org.eclipse.swt.SWT.DOUBLE_BUFFERED;
import gda.images.camera.ImageListener;
import gda.images.camera.MotionJpegOverHttpReceiverSwt;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.StatInfo;
import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.ViewerDisplayMode;
import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController.SAMPLE_STAGE_STATE;
import uk.ac.gda.client.tomo.alignment.view.utils.HistogramAdjuster;
import uk.ac.gda.client.tomo.alignment.view.utils.ScaleDisplay;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite.ProfilePointListener;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite.ZoomRectangleListener;
import uk.ac.gda.client.tomo.composites.FullImageComposite;
import uk.ac.gda.client.tomo.composites.FullImageComposite.IRoiPointsListener;
import uk.ac.gda.client.tomo.composites.OverlayImageFigure.OverlayImgFigureListener;
import uk.ac.gda.client.tomo.composites.ScaleBarComposite;
import uk.ac.gda.client.tomo.composites.StatInfoComposite;
import uk.ac.gda.client.tomo.composites.TomoPlotComposite;
import uk.ac.gda.client.tomo.composites.TomoPlotComposite.PlottingSystemActionListener;
import uk.ac.gda.client.tomo.composites.ZoomedImageComposite;
import uk.ac.gda.client.tomo.composites.ZoomedImgCanvas;
import uk.ac.gda.epics.client.EPICSClientActivator;
import uk.ac.gda.ui.components.AmplifierStepperComposite;
import uk.ac.gda.ui.components.AmplifierStepperComposite.AmplifierStepperListener;
import uk.ac.gda.ui.components.AmplifierStepperComposite.STEPPER;
import uk.ac.gda.ui.components.CameraControlComposite;
import uk.ac.gda.ui.components.CameraControlComposite.RESOLUTION;
import uk.ac.gda.ui.components.CameraControlComposite.STREAM_STATE;
import uk.ac.gda.ui.components.ColourSliderComposite;
import uk.ac.gda.ui.components.ColourSliderComposite.IColourSliderListener;
import uk.ac.gda.ui.components.ICameraControlListener;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.ui.components.MotionControlComposite;
import uk.ac.gda.ui.components.MotionControlComposite.MotionControlCentring;
import uk.ac.gda.ui.components.PointInDouble;
import uk.ac.gda.ui.components.ZoomButtonComposite.ZOOM_LEVEL;
import uk.ac.gda.ui.event.PartAdapter2;

/**
 * View for Tomography alignment, and scan
 */
public class TomoAlignmentView extends ViewPart implements ITomoAlignmentView {

	public enum RIGHT_PAGE {
		NONE, PLOT, ZOOM_DEMAND_RAW, NO_ZOOM, ZOOM_STREAM
	}

	public enum LEFT_PAGE {
		IMAGE_VIEWER;
	}

	public enum RIGHT_INFO {
		NONE, PROFILE, HISTOGRAM;
	}

	private boolean isSaving;
	public static final String STREAM_STOPPED = "STREAM STOPPED";
	private ViewerDisplayMode leftWindowDisplayMode = ViewerDisplayMode.STREAM_STOPPED;
	public static final int RIGHT_WINDOW_WIDTH = 320;
	private HistogramAdjuster histogramAdjuster;
	private AmplifierStepperComposite amplifierStepper;
	private MotionControlComposite motionControlComposite;
	/**/
	Label lblLeftWindowDisplayModeStatus;
	/**/
	Label lblRightWindowInfoNumPixels;
	/* Right Window Page Book Composites */
	private Composite page_rightWindow_nonProfile;
	/**/
	MotionJpegOverHttpReceiverSwt leftVideoReceiver;
	/**/
	/**/
	private ZoomedImageComposite page_nonProfile_streamZoom;

	private ZoomedImgCanvas demandRawZoomCanvas;
	/**/
	private Composite page_leftWindow_imgViewer;
	private Composite page_nonProfile_demandRaw;
	/**/
	private PageBook pageBook_leftWindow;
	private PageBook pageBook_nonProfile_zoomImg;
	ScaleBarComposite rightScaleBar;
	Composite page_nonProfile_noZoom;
	Label lblFileTimeStamp;
	Label lblFileName;
	private Composite page_rightInfo_profile;

	private Composite page_rightInfo_histogram;

	private PageBook pageBook_rightInfo;
	/**/
	private FullImageComposite leftWindowImageViewer;
	private PageBook pageBook_rightWindow;

	private static final String EMPTY_STRING_VALUE = "-----";
	public static final String NO_ZOOM_lbl = "NO ZOOM";
	private static final int LEFT_WINDOW_WIDTH = 530;
	private static final int MOTION_COMPOSITE_HEIGHT = 140;
	private static final int CONTROL_COMPOSITE_HEIGHT = 80;
	private static final String RESET_DETECTOR = "Reset Detector";
	private static final int IMAGE_FULL_WIDTH = 4008;
	private static final String LBL_INTENSITY = "Intensity";
	private static final String LBL_y = "y";
	private static final String LBL_x = "x";
	private static final IWorkbenchWindow ACTIVE_WORKBENCH_WINDOW = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	private static final String LBL_X = "X :";
	public static final String TIMESTAMP = "Timestamp :";
	public static final String FILE_NAME = "FileName :";
	public static final String BLANK_STR = "";
	private static final String ZOOM_NOT_SELECTED_shortdesc = "ZOOM NOT SELECTED";

	private static final String SET_EXPOSURE_TIME = "Apply Exposure Time";

	//
	private ICameraControlListener cameraControlListener;
	private MotionControlListener motionControlListener;
	/**
	 * 
	 */
	public static final int SCALED_TO_Y = 668;
	public static final int SCALED_TO_X = 1002;
	/**/

	private static final String PLAY_STREAM = "Play Stream";

	public static final String SAMPLE_SINGLE = "SINGLE (SAMPLE)";

	public static final String FLAT_SINGLE = "SINGLE (FLAT)";

	public static final String SAMPLE_LIVE_STREAM = "LIVE (SAMPLE)";

	public static final String FLAT_LIVE_STREAM = "LIVE (FLAT)";

	public static final String STATIC_FLAT = "STATIC FLAT";

	public static final String STATIC_DARK = "STATIC DARK";

	/* Labels and default values */
	private static final String DEFAULT_LEFT_WINDOW_INFO_SIZE = "27mm";
	private boolean zoomReceiverStarted;
	/**
	 * Size of the screen pixel in mm.
	 */
	private Double screenPixelSize;
	/**
	 * listens to the moves on the overlay image. Overlay image movements are translated to motor movements.
	 */
	protected OverlayImageFigureListenerImpl overlayImageFigureListener = new OverlayImageFigureListenerImpl();

	private TomoAlignmentViewController tomoAlignmentViewController;
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentView.class);
	private String viewPartName;
	private boolean fullImgReceiverStarted;

	private VideoListener leftVideoListener;

	private MotionJpegOverHttpReceiverSwt rightVideoReceiver;
	private VideoListener rightVideoListener;

	private CameraControlComposite cameraControls;
	private static final String BOLD_TEXT_11 = "bold-text_11";
	private static final String BOLD_TEXT_16 = "bold-text_16";
	//
	private FormToolkit toolkit;
	/* Left Window Info viewer */
	private Label lblLeftWindowInfoNumPixels;
	/**/
	private FontRegistry fontRegistry;
	/**/
	private Composite page_rightWindow_plot;
	/**/
	private Composite page_leftWindow_introComposite;
	/**/
	private ScaleBarComposite leftScaleBar;

	protected TomoPlotComposite tomoPlotComposite;
	private StatInfoComposite statInfo;

	private Label lblYValue;
	private Label lblXValue;
	private Composite page_rightInfo_nonProfile;
	private Label lblProfileIntensityValue;
	private final static DecimalFormat lblXDecimalFormat = new DecimalFormat("###");
	private ColourSliderComposite histogramSliderComposite;
	private Label lblPixelX;
	private Label lblPixelY;
	private Label lblPixelIntensityVal;

	private IPartListener tomoPartAdapter = new PartAdapter2() {
		@Override
		public void partDeactivated(org.eclipse.ui.IWorkbenchPart part) {
			stopStreamByCheckingIfOn();
		}

		private void stopStreamByCheckingIfOn() {
			if (isStreamingSampleExposure()) {
				cameraControls.stopSampleStream();
			} else if (isStreamingFlatExposure()) {
				cameraControls.stopFlatStream();
			}
		}

		@Override
		public void partHidden(org.eclipse.ui.IWorkbenchPartReference partRef) {
			stopStreamByCheckingIfOn();
		}

		@Override
		public void partClosed(org.eclipse.ui.IWorkbenchPart part) {
			stopStreamByCheckingIfOn();
		}
	};

	private IColourSliderListener histogramSliderListener = new IColourSliderListener() {

		@SuppressWarnings("incomplete-switch")
		@Override
		public void colourSliderRegion(int upperLimit, int lowerLimit) {
			logger.debug("Lower Limit:{}", lowerLimit);
			logger.debug("Upper Limit:{}", upperLimit);
			switch (leftWindowDisplayMode) {
			case SAMPLE_SINGLE:
				ImageData histAppliedImgData = histogramAdjuster.updateHistogramValues(lowerLimit, upperLimit);
				loadImageInUIThread(leftWindowImageViewer, histAppliedImgData.scaledTo(SCALED_TO_X, SCALED_TO_Y));
				break;
			case SAMPLE_STREAM_LIVE:
				double scale = ((histogramAdjuster.getMaxIntensity() - histogramAdjuster.getMinIntensity()) / (upperLimit - lowerLimit));
				try {
					tomoAlignmentViewController.applyScalingContrast(-lowerLimit, scale);
				} catch (Exception e) {
					logger.error("TODO put description of error here", e);
					loadErrorInDisplay("Problem applying contrast", "Problem applying contrast:" + e.getMessage());
				}
				break;
			}
		}

	};

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

	private PlottingSystemActionListener profileLineListener = new PlottingSystemActionListener() {
		@Override
		public void profileLineMovedTo(final double xVal, final long intensity) {
			if (page_rightInfo_nonProfile != null && !page_rightInfo_nonProfile.isDisposed()) {
				page_rightInfo_nonProfile.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						setRightInfoPage(RIGHT_INFO.PROFILE);
						String formattedXVal = lblXDecimalFormat.format(xVal);
						lblXValue.setText(formattedXVal);
						lblProfileIntensityValue.setText(Long.toString(intensity));
						leftWindowImageViewer.showProfileHighlighter();
						int leftWindowBinValue = tomoAlignmentViewController.getLeftWindowBinValue();
						// FIXME - potential divide by zero problem
						leftWindowImageViewer.moveProfileHighlighter(xVal / leftWindowBinValue);
					}

				});
			}
		}

		@Override
		public void histogramChangedRoi(double minValue, double maxValue, double from, double to) {
			logger.debug("minValue:{}", minValue);
			logger.debug("maxValue:{}", maxValue);
			logger.debug("from:{}", from);
			logger.debug("to:{}", to);

			switch (leftWindowDisplayMode) {
			case SAMPLE_STREAM_LIVE:
			case FLAT_STREAM_LIVE:
				try {
					tomoAlignmentViewController.setAdjustedProc1ScaleValue(from, to);
				} catch (Exception e) {
					loadErrorInDisplay("Problem updating scale on the detector",
							"Problem updating scale on the detector:" + e.getMessage());
				}
				break;
			case SAMPLE_SINGLE:
				try {
					tomoAlignmentViewController.setAdjustedExposureTime(from, to);
					cameraControls.startSampleSingle();
					cameraControls.startSampleHistogram();
				} catch (Exception e) {
					loadErrorInDisplay("Problem updating scale on the detector",
							"Problem updating scale on the detector:" + e.getMessage());
					logger.error("TODO put description of error here", e);
				}
				break;
			case FLAT_SINGLE:
				try {
					tomoAlignmentViewController.setAdjustedExposureTime(from, to);
					cameraControls.startFlatSingle();
					cameraControls.startFlatHistogram();
				} catch (Exception e) {
					loadErrorInDisplay("Problem updating scale on the detector",
							"Problem updating scale on the detector:" + e.getMessage());
					logger.error("TODO put description of error here", e);
				}
				break;
			case DARK_SINGLE:
			case STATIC_FLAT:
			case STREAM_STOPPED:
				// Do nothing
				// Wont be applicable as the histogram only applies to single or stream
				break;
			}
		}

		@Override
		public void applyExposureTimeButtonClicked() {
			logger.debug("Apply exposure time button clicked:");
			try {
				tomoAlignmentViewController.applyHistogramToAdjustExposureTime();
			} catch (Exception e) {
				logger.error("TODO put description of error here", e);
				loadErrorInDisplay("Cannot apply calculated exposure time", "Cannot apply calculated exposure time:"
						+ e.getMessage());
			}
		}
	};

	private ZoomRectangleListener zoomRectListener = new ZoomRectangleListener() {

		@Override
		public void zoomRectMoved(Rectangle bounds, Dimension figureTopLeftRelativeImgBounds, Dimension distanceMoved) {
			logger.debug("BoundX :" + bounds.x + " BoundY:" + bounds.y);
			IProgressMonitor monitor = new NullProgressMonitor();
			if (isStreamingSampleExposure() || isStreamingFlatExposure()) {
				// change the ROI start only if 'Stream' is switched 'ON'
				try {
					tomoAlignmentViewController.handleZoomStartMoved(figureTopLeftRelativeImgBounds);
				} catch (Exception e) {
					logger.error("moving zoomed rectangle problem:", e);
					loadErrorInDisplay("Problem moving zoom rectangle", e.getMessage());
				}
			} else {
				ViewerDisplayMode displayMode = leftWindowDisplayMode;
				if (displayMode == ViewerDisplayMode.FLAT_SINGLE || displayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
					int leftWindowBinValue = tomoAlignmentViewController.getLeftWindowBinValue();
					if (cameraControls.isProfileSelected()) {
						Rectangle leftWindowImgBounds = leftWindowImageViewer.getImageBounds();
						Rectangle zoomFigureBounds = leftWindowImageViewer.getZoomFigureBounds().getTranslated(-1, 0);
						Rectangle lineBounds = leftWindowImageViewer.getProfilerLineBounds().getTranslated(
								leftWindowImgBounds.x + 1, 0);

						if (zoomFigureBounds.intersects(lineBounds)) {
							Rectangle zoomFigureBoundsCopy = zoomFigureBounds.getCopy();
							Rectangle intersect = zoomFigureBoundsCopy.intersect(lineBounds);
							Rectangle translatedIntersect = intersect.getTranslated(-leftWindowImgBounds.x,
									-leftWindowImgBounds.y);

							updatePlots(monitor, translatedIntersect.x * leftWindowBinValue,
									(translatedIntersect.x + translatedIntersect.width) * leftWindowBinValue,
									lineBounds.y);
						} else {
							updatePlots(monitor, 0, 4008, lineBounds.y);
						}
					} else {
						ZOOM_LEVEL selectedZoomLevel = cameraControls.getSelectedZoomLevel();
						double zoomDemandRawScaleX = selectedZoomLevel.getDemandRawScale().x;
						logger.debug("Zoom demand Raw scale X:{}", zoomDemandRawScaleX);
						logger.debug("dx:{}", distanceMoved.width);
						logger.debug("dy:{}", distanceMoved.height);

						Dimension scaled = distanceMoved.getCopy().getScaled(leftWindowBinValue * zoomDemandRawScaleX);
						logger.debug("Scaled dx:{}", scaled.width);
						logger.debug("Scaled dy:{}", scaled.height);

						demandRawZoomCanvas.scroll(scaled);
					}
				}
			}
		}
	};

	void updatePlots(IProgressMonitor monitor, int xStart, int xEnd, int y) {
		tomoPlotComposite.updateProfilePlots(monitor, xStart, xEnd, y);
		leftWindowImageViewer.hideProfileHighlighter();
	}

	private ProfilePointListener profilePointListener = new ProfilePointListener() {

		IProgressMonitor monitor = new NullProgressMonitor();

		@Override
		public void profileLineMoved(int y) {
			if (cameraControls.isProfileSelected()) {
				updatePlots(monitor, 0, IMAGE_FULL_WIDTH, y * tomoAlignmentViewController.getLeftWindowBinValue());
				lblYValue.setText(Integer.toString(y * tomoAlignmentViewController.getLeftWindowBinValue()));
				lblXValue.setText(BLANK_STR);
				lblProfileIntensityValue.setText(BLANK_STR);
			}
		}
	};

	@Override
	public void setFocus() {
		// Do nothing
	}

	public TomoAlignmentViewController getTomoAlignmentViewController() {
		return tomoAlignmentViewController;
	}

	public void setTomoAlignmentViewController(TomoAlignmentViewController tomoAlignmentViewController) {
		this.tomoAlignmentViewController = tomoAlignmentViewController;
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
				MessageDialog.openError(leftWindowImageViewer.getShell(), dialogTitle, errorMsg);
			}
		});
	}

	void switchOffCentring(final MotionControlCentring centring) {
		leftWindowImageViewer.removeOverlayImageFigureListener(overlayImageFigureListener);
		if (!leftWindowImageViewer.isDisposed()) {
			leftWindowImageViewer.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						motionControlComposite.switchOff(centring);
						// setLeftWindowInfo(String.format("%1$s COMPLETE", centring.toString()));
					} catch (Exception e) {
						logger.error(centring.toString() + " failed ", e);
					}
				}
			});
		}
	}

	protected class OverlayImageFigureListenerImpl implements OverlayImgFigureListener {

		/**
		 * Need to persist this here for the "Center Axis of Rotation operation" - This is recorded when the axis of
		 * rotation is calculated using the half rotation tool
		 */
		private int calcOffset = -1;

		public int getCalcOffset() {
			return calcOffset;
		}

		@Override
		public void performOverlayImgMoved(final Point initialPoint, final Point finalPoint, final Dimension difference) {
			logger.debug("image figure overlay difference" + difference);
			leftWindowImageViewer.setFeedbackCursor(SWT.CURSOR_NO);

			try {
				final MotionControlCentring selectedCentring = motionControlComposite.getSelectedCentring();
				leftWindowImageViewer.removeOverlayImage();
				// issue request to move the motor to the difference position
				final CAMERA_MODULE cameraModule = motionControlComposite.getSelectedCameraModule();
				cameraControls.startSampleStreaming();

				ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						switch (selectedCentring) {
						case VERTICAL:
							try {
								tomoAlignmentViewController.moveVertical(monitor, cameraModule, difference);
							} catch (InterruptedException e) {
								logger.error("Action stopped by user");
							} catch (Exception ex) {
								logger.error("Problem with vertical centring", ex);
								throw new InvocationTargetException(ex);
							} finally {
								switchOffCentring(selectedCentring);
								monitor.done();
							}
							break;
						case FIND_AXIS_ROTATION:
							try {
								leftWindowImageViewer.getDisplay().asyncExec(new Runnable() {

									@Override
									public void run() {
										calcOffset = ((initialPoint.x - finalPoint.x) / 2);
										int imageStart = (leftWindowImageViewer.getImageBounds().x);
										int imageCenter = (leftWindowImageViewer.getImageBounds().width / 2);
										int calcTomoAxis = imageCenter - calcOffset;
										int newCrosshair = (leftWindowImageViewer.getImageBounds().x + calcTomoAxis);

										logger.debug("Image start x {}", imageStart);
										logger.debug("A = finalPoint.x = {}", finalPoint.x);
										logger.debug("C = initialPoint.x = {}", initialPoint.x);
										logger.debug("M = Image center =  {}", imageCenter);
										logger.debug("T = {}", calcTomoAxis);
										logger.debug("Setting crosshair to {}", newCrosshair);

										leftWindowImageViewer.moveCrossHairTo(newCrosshair);
										motionControlComposite.setTomoAxisFound(true);
									}
								});
								//
							} catch (Exception e) {
								logger.error("Problem with Half Rotation tool", e);
								throw new InvocationTargetException(e, "Problem with Half Rotation tool:"
										+ e.getMessage());
							} finally {
								enableCameraControls();
								switchOffCentring(selectedCentring);
							}
							break;
						case MOVE_AXIS_OF_ROTATION:
							// Do nothing
							break;
						case HORIZONTAL:
							try {
								tomoAlignmentViewController.moveHorizontal(monitor, cameraModule, difference);
							} catch (InterruptedException e) {
								logger.error("Action stopped by user");
							} catch (Exception e) {
								logger.error("Problem with Center Current Position", e);
								throw new InvocationTargetException(e, "Problem with Center Current Position:"
										+ e.getMessage());
							} finally {
								switchOffCentring(selectedCentring);
								monitor.done();
							}
							break;

						case TILT:
							// Tilt does not have overlay layer added on top
							break;
						}
					}
				});
			} catch (Exception e) {
				logger.error("Problem streaming when overlay is removed.", e);
				loadErrorInDisplay("Error while performing motor movement", e.getMessage());
			}
		}

		@Override
		public void cancelMove() {
			final MotionControlCentring selectedCentring = motionControlComposite.getSelectedCentring();
			switchOffCentring(selectedCentring);
		}

		@Override
		public void mouseClicked() {
			TomoAlignmentView.this.getViewSite().getActionBars().getStatusLineManager().setMessage(null);
		}
	}

	protected void enableCameraControls() {
		if (cameraControls != null && !cameraControls.isDisposed()) {
			cameraControls.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					cameraControls.enableAll();
				}
			});
		}
	}

	protected void disableCameraControls() {
		if (cameraControls != null && !cameraControls.isDisposed()) {
			cameraControls.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					cameraControls.disableAll();
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
					try {
						imgViewer.loadMainImage(image);

						//
						if (ZOOM_LEVEL.NO_ZOOM.equals(cameraControls.getSelectedZoomLevel())) {
							page_nonProfile_streamZoom.clearZoomWindow();
						}
					} catch (Exception ex) {
						logger.error("Error loading image :{}", ex);
						loadErrorInDisplay("Error loading image", ex.getMessage());
					}
				}
			});
		}
	}

	/**
	 * Constructor - initalizes the font registry
	 */
	public TomoAlignmentView() {
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
			fontRegistry.put(BOLD_TEXT_16, new FontData[] { new FontData(fontName, 16, SWT.BOLD) });
		}
	}

	@Override
	public Image getTitleImage() {
		return TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_TOMO_ALIGNMENT);
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
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
			Composite cmpControlBox = createControlBoxComposite(cmpRoot);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = CONTROL_COMPOSITE_HEIGHT;
			cmpControlBox.setLayoutData(gd);
			//
			Composite cmpMotionBars = createMotionControlComposite(cmpRoot);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = MOTION_COMPOSITE_HEIGHT;
			cmpMotionBars.setLayoutData(gd);
			//
			tomoAlignmentViewController.registerTomoAlignmentView(this);
			/* Calls the update fields in a separate thread - so that the UI is not blocked. */
			Future<Boolean> isSuccessful = tomoAlignmentViewController.init();
			new Thread(new RunUpdateAllFields(isSuccessful)).start();
			// Create toolbar actions
			createActions();
			/**/
			cameraControlListener = new CameraCompositeController(this, cameraControls, tomoAlignmentViewController,
					leftWindowImageViewer);
			cameraControls.addCamerControlListener(cameraControlListener);

			motionControlListener = new MotionControlListener(this, tomoAlignmentViewController,
					motionControlComposite, cameraControls, leftWindowImageViewer);
			motionControlComposite.addMotionControlListener(motionControlListener);

			getSite().getPage().addPartListener(tomoPartAdapter);
		} catch (Exception ex) {
			throw new RuntimeException("Error opening view", ex);
		}
	}

	private void createActions() {
		//
		Action resetDetectorAction = new Action(RESET_DETECTOR) {
			@Override
			public void runWithEvent(Event event) {
				reset();
			}
		};
		resetDetectorAction.setImageDescriptor(TomoClientActivator.getDefault().getImageRegistry()
				.getDescriptor(ImageConstants.ICON_RESET_DETECTOR));
		ActionContributionItem resetDetectorActionContributionItem = new ActionContributionItem(resetDetectorAction);
		resetDetectorActionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		getViewSite().getActionBars().getToolBarManager().add(resetDetectorActionContributionItem);

	}

	/**
	 * @param root
	 * @return {@link Composite} motion controls
	 */
	private Composite createMotionControlComposite(Composite root) {
		motionControlComposite = new MotionControlComposite(root, toolkit, SWT.None);
		return motionControlComposite;
	}

	/**
	 * @param root
	 * @return {@link Composite} that creates Control box
	 */
	private Composite createControlBoxComposite(Composite root) {
		cameraControls = new CameraControlComposite(root, toolkit, SWT.None);
		return cameraControls;
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
		GridLayout layout = new GridLayout(2, false);
		setDefaultLayoutSettings(layout);
		mainComposite.setLayout(layout);

		// Left window
		Composite leftWindow = createLeftWindow(mainComposite);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = LEFT_WINDOW_WIDTH;
		leftWindow.setLayoutData(layoutData);
		// Right Window
		Composite rightWindow = createRightWindow(mainComposite);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.widthHint = RIGHT_WINDOW_WIDTH;
		rightWindow.setLayoutData(layoutData);

		return mainComposite;

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
		GridLayout layout = new GridLayout(2, false);
		setDefaultLayoutSettings(layout);
		page_rightWindow_nonProfile.setLayout(layout);
		page_rightWindow_nonProfile.setLayoutData(new GridData(GridData.FILL_BOTH));

		amplifierStepper = new AmplifierStepperComposite(page_rightWindow_nonProfile, SWT.None);
		GridData layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 35;
		amplifierStepper.setLayoutData(layoutData);
		// Setting default to ONE
		amplifierStepper.moveStepperTo(STEPPER.ONE);

		amplifierStepper.addAmplifierStepperListener(amplifierStepperListener);

		/**/
		Composite imgViewerComposite = toolkit.createComposite(page_rightWindow_nonProfile);
		imgViewerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gridlayout = new GridLayout();
		setDefaultLayoutSettings(gridlayout);
		imgViewerComposite.setLayout(gridlayout);

		pageBook_nonProfile_zoomImg = new PageBook(imgViewerComposite, SWT.None);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		pageBook_nonProfile_zoomImg.setLayoutData(gd);

		page_nonProfile_streamZoom = new ZoomedImageComposite(pageBook_nonProfile_zoomImg, SWT.DOUBLE_BUFFERED);

		page_nonProfile_demandRaw = toolkit.createComposite(pageBook_nonProfile_zoomImg);
		gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		page_nonProfile_demandRaw.setLayout(gl);
		demandRawZoomCanvas = new ZoomedImgCanvas(page_nonProfile_demandRaw, DOUBLE_BUFFERED);
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		demandRawZoomCanvas.setLayoutData(layoutData2);
		//
		page_nonProfile_noZoom = new Composite(pageBook_nonProfile_zoomImg, SWT.None);
		page_nonProfile_noZoom.setLayout(new FillLayout());

		toolkit.createLabel(page_nonProfile_noZoom, ZOOM_NOT_SELECTED_shortdesc)
				.setFont(fontRegistry.get(BOLD_TEXT_16));

		//
		rightVideoReceiver = new MotionJpegOverHttpReceiverSwt();
		rightVideoListener = new VideoListener(page_nonProfile_streamZoom);

		//
		Composite nonProfileInfoViewerComposite = createRightWindowNonProfileInfoViewComposite(rightWindowComposite);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		// ld.horizontalSpan = 2;
		nonProfileInfoViewerComposite.setLayoutData(ld);

		/* Profile composite */

		page_rightWindow_plot = toolkit.createComposite(pageBook_rightWindow);
		layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		page_rightWindow_plot.setLayout(layout);
		page_rightWindow_plot.setLayoutData(new GridData(GridData.FILL_BOTH));

		tomoPlotComposite = new TomoPlotComposite(page_rightWindow_plot, SWT.None);
		tomoPlotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tomoPlotComposite.addOverlayLineListener(profileLineListener);
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

		pageBook_leftWindow = new PageBook(leftWindowComposite, SWT.None);
		pageBook_leftWindow.setLayoutData(new GridData(GridData.FILL_BOTH));

		page_leftWindow_introComposite = createIntroComposite(pageBook_leftWindow);

		page_leftWindow_imgViewer = toolkit.createComposite(pageBook_leftWindow);
		layout = new GridLayout(2, false);
		setDefaultLayoutSettings(layout);
		page_leftWindow_imgViewer.setLayout(layout);
		/**/
		Composite viewerComposite = createLeftWindowImageViewerComposite(page_leftWindow_imgViewer);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		viewerComposite.setLayoutData(layoutData);
		//
		histogramAdjuster = new HistogramAdjuster();
		//
		histogramSliderComposite = new ColourSliderComposite(page_leftWindow_imgViewer, SWT.None);
		layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 30;
		histogramSliderComposite.setLayoutData(layoutData);
		histogramSliderComposite.setMaximum(70000);
		histogramSliderComposite.setMarkerInterval(10000);
		histogramSliderComposite.setMaximumLimit(histogramAdjuster.getMaxIntensity());
		histogramSliderComposite.addColourSliderListener(histogramSliderListener);

		Composite infoComposite = createLeftWindowInfoViewComposite(page_leftWindow_imgViewer);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		infoComposite.setLayoutData(layoutData);

		/**/
		pageBook_leftWindow.showPage(page_leftWindow_introComposite);
		/**/
		return leftWindowComposite;
	}

	/**
	 * Creates an intro(dashboard) kind of composite - providing options to either start a video stream or demand raw
	 * 
	 * @param leftWindowPageBook
	 * @return {@link Composite}
	 */
	private Composite createIntroComposite(final PageBook leftWindowPageBook) {
		Composite introComposite = toolkit.createComposite(leftWindowPageBook, SWT.BORDER);

		GridLayout layout = new GridLayout();
		introComposite.setLayout(layout);

		Button btnPlayStream = toolkit.createButton(introComposite, PLAY_STREAM, SWT.PUSH);
		btnPlayStream.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 40;
		btnPlayStream.setLayoutData(layoutData);
		btnPlayStream.setImage(EPICSClientActivator.getDefault().getImageRegistry()
				.get(uk.ac.gda.epics.client.ImageConstants.IMG_PLAY_STREAM));
		btnPlayStream.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (!isModuleSelected()) {
						loadErrorInDisplay("Module needs to be selected", "Please select a module before streaming");
						return;
					}
					cameraControls.startSampleStreaming();
				} catch (Exception e1) {
					logger.error("error selecting play button", e1);
				}
			}
		});

		Button btnDemandRaw = toolkit.createButton(introComposite, "Take Single", SWT.PUSH);
		btnDemandRaw.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.heightHint = 40;
		btnDemandRaw.setLayoutData(layoutData2);
		btnDemandRaw.setImage(TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_RAW_IMAGE));
		btnDemandRaw.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (!isModuleSelected()) {
						loadErrorInDisplay("Module needs to be selected",
								"Please select a module before a raw image can be captured.");
						return;
					}
					cameraControls.startSampleSingle();
					pageBook_leftWindow.showPage(page_leftWindow_imgViewer);
				} catch (Exception e1) {
					logger.error("Demand Raw problems", e1);
				}
			}
		});
		return introComposite;
	}

	private Composite createLeftWindowImageViewerComposite(Composite leftWindowComposite) {
		Composite imageViewAndInfoBarComposite = toolkit.createComposite(leftWindowComposite);
		GridLayout gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		imageViewAndInfoBarComposite.setLayout(gridLayout);

		//
		leftWindowImageViewer = new FullImageComposite(imageViewAndInfoBarComposite, SWT.DOUBLE_BUFFERED, true);
		leftWindowImageViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		leftWindowImageViewer.addZoomRectListener(zoomRectListener);
		leftWindowImageViewer.addProfileListener(profilePointListener);
		leftWindowImageViewer.getCanvas().addMouseTrackListener(mouseTrackAdapter);
		leftWindowImageViewer.getCanvas().setBackground(
				new Color(leftWindowImageViewer.getDisplay(), new RGB(255, 255, 240)));
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
				roi1BinX = tomoAlignmentViewController.getRoi1BinX();
			} catch (Exception e1) {
				logger.error("Problem getting Roi1 BinX", e1);
			}
			if (locWrtImageStart.width >= 0 && locWrtImageStart.height >= 0 && locWrtImageStart.width <= SCALED_TO_X
					&& locWrtImageStart.height <= SCALED_TO_Y) {
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
		GridLayout gridLayout = new GridLayout(7, true);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		infoViewerComposite.setLayout(gridLayout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 55;
		infoViewerComposite.setLayoutData(layoutData);

		lblLeftWindowInfoNumPixels = toolkit.createLabel(infoViewerComposite, DEFAULT_LEFT_WINDOW_INFO_SIZE, SWT.LEFT);
		lblLeftWindowInfoNumPixels.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblLeftWindowInfoNumPixels.setLayoutData(new GridData());
		//
		statInfo = new StatInfoComposite(infoViewerComposite, SWT.None);
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.horizontalSpan = 5;
		statInfo.setLayoutData(layoutData2);
		//
		lblLeftWindowDisplayModeStatus = toolkit.createLabel(infoViewerComposite, STREAM_STOPPED, SWT.None);
		lblLeftWindowDisplayModeStatus.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblLeftWindowDisplayModeStatus.setForeground(ColorConstants.darkBlue);
		lblLeftWindowDisplayModeStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite lowerBarComposite = toolkit.createComposite(infoViewerComposite);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 7;
		lowerBarComposite.setLayoutData(gd);
		//

		lowerBarComposite.setLayout(new FillLayout());

		Composite scaleBarComposite = new Composite(lowerBarComposite, SWT.None);
		scaleBarComposite.setBackground(ColorConstants.white);
		gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		gridLayout.marginHeight = 10;
		scaleBarComposite.setLayout(gridLayout);
		leftScaleBar = new ScaleBarComposite(scaleBarComposite, SWT.None);
		leftScaleBar.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		leftScaleBar.setBackground(ColorConstants.blue);

		Composite imagePixelValueComposite = new Composite(lowerBarComposite, SWT.None);
		imagePixelValueComposite.setBackground(ColorConstants.white);
		gridLayout = new GridLayout(6, true);
		gridLayout.marginHeight = 2;
		gridLayout.marginWidth = 5;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 2;
		imagePixelValueComposite.setLayout(gridLayout);

		Label lblX = new Label(imagePixelValueComposite, SWT.RIGHT);
		lblX.setBackground(ColorConstants.white);
		lblX.setText(LBL_x);
		lblX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPixelX = new Label(imagePixelValueComposite, SWT.LEFT);
		lblPixelX.setBackground(ColorConstants.white);
		lblPixelX.setText("----");
		lblPixelX.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblPixelX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblY = new Label(imagePixelValueComposite, SWT.RIGHT);
		lblY.setBackground(ColorConstants.white);
		lblY.setText(LBL_y);
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

	private Composite createRightWindowNonProfileInfoViewComposite(Composite imageViewAndInfoBarComposite) {
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
		pageBook_rightInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Non profile page -
		page_rightInfo_nonProfile = toolkit.createComposite(pageBook_rightInfo);
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 2;
		layout2.marginWidth = 2;
		layout2.horizontalSpacing = 2;
		layout2.verticalSpacing = 2;
		page_rightInfo_nonProfile.setLayout(layout2);

		lblRightWindowInfoNumPixels = toolkit.createLabel(page_rightInfo_nonProfile, NO_ZOOM_lbl, SWT.RIGHT);
		lblRightWindowInfoNumPixels.setFont(fontRegistry.get(BOLD_TEXT_11));
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		lblRightWindowInfoNumPixels.setLayoutData(layoutData);

		/* TODO-Ravi */
		Composite scalebarComposite = toolkit.createComposite(page_rightInfo_nonProfile, SWT.RIGHT_TO_LEFT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		scalebarComposite.setLayoutData(gd);
		//
		gridLayout = new GridLayout();
		setDefaultLayoutSettings(gridLayout);
		gridLayout.marginHeight = 10;
		scalebarComposite.setLayout(gridLayout);

		rightScaleBar = new ScaleBarComposite(scalebarComposite, SWT.RIGHT);
		rightScaleBar.setLayoutData(new GridData());

		// Profile page.
		page_rightInfo_profile = toolkit.createComposite(pageBook_rightInfo);
		GridLayout layout3 = new GridLayout(4, true);
		layout3.marginHeight = 2;
		layout3.marginWidth = 2;
		layout3.horizontalSpacing = 2;
		layout3.verticalSpacing = 2;
		page_rightInfo_profile.setLayout(layout3);

		Label lblY = toolkit.createLabel(page_rightInfo_profile, LBL_y, SWT.RIGHT);
		lblY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblYValue = toolkit.createLabel(page_rightInfo_profile, "0", SWT.LEFT);
		lblYValue.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblYValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblX = toolkit.createLabel(page_rightInfo_profile, LBL_X, SWT.RIGHT);
		lblX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblXValue = toolkit.createLabel(page_rightInfo_profile, "0", SWT.LEFT);
		lblXValue.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblXValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblIntensity = toolkit.createLabel(page_rightInfo_profile, "Intensity:", SWT.RIGHT);
		lblIntensity.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lblProfileIntensityValue = toolkit.createLabel(page_rightInfo_profile, "0", SWT.LEFT);
		lblProfileIntensityValue.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData ld2 = new GridData(GridData.FILL_HORIZONTAL);
		ld2.horizontalSpan = 3;
		lblProfileIntensityValue.setLayoutData(ld2);

		// Histogram Page
		page_rightInfo_histogram = toolkit.createComposite(pageBook_rightInfo);
		GridLayout gl = new GridLayout();
		setDefaultLayoutSettings(gl);
		page_rightInfo_histogram.setLayout(gl);

		Button btnApplyExposureSettings = new Button(page_rightInfo_histogram, SWT.PUSH);
		btnApplyExposureSettings.setText(SET_EXPOSURE_TIME);
		btnApplyExposureSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					tomoAlignmentViewController.applyHistogramToAdjustExposureTime();
				} catch (Exception e1) {
					logger.error("TODO put description of error here", e1);
					loadErrorInDisplay("Problem applying histogram value to exposure time",
							"Problem applying histogram value to exposure time:" + e1.getMessage());
				}
			}
		});
		layoutData = new GridData(GridData.FILL_BOTH);
		btnApplyExposureSettings.setLayoutData(layoutData);

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
				cameraControls.deselectSampleAndFlatStream();
				// stopFullVideoReceiver();
			} else if (acquisitionState == 1) {
				STREAM_STATE streamState = cameraControls.getStreamState();
				if (STREAM_STATE.SAMPLE_STREAM.equals(streamState)) {
					cameraControls.selectStreamButton();
				} else if (STREAM_STATE.FLAT_STREAM.equals(streamState)) {
					cameraControls.enableFlatStreamButton();
				}
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
						if (tomoAlignmentViewController.isStreaming()) {
							logger.debug("run->Stopping stream while updating all fields");
							cameraControls.stopSampleStream();
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

	@Override
	public void updateExposureTimeToWidget(final double acqExposure) {
		statInfo.updateExposureTime(acqExposure);
	}

	private double getSteppedExposureTime(double acquisitionTime) {
		int stepperVal = amplifierStepper.getSelectedStepper().getValue();
		final double steppedAcqTime = acquisitionTime / stepperVal;
		return steppedAcqTime;
	}

	double getSteppedSampleExposureTime() {
		double acqTime = cameraControls.getSampleExposureTime();
		return getSteppedExposureTime(acqTime);
	}

	double getSteppedFlatExposureTime() {
		double acqTime = cameraControls.getFlatExposureTime();
		return getSteppedExposureTime(acqTime);
	}

	boolean isModuleSelected() {
		return motionControlComposite.getSelectedCameraModule() != CAMERA_MODULE.NO_MODULE;
	}

	@Override
	public void resetAmplifier() throws Exception {
		amplifierStepper.moveStepperTo(STEPPER.ONE);
	}

	/**
	 * Amplifier stepper listener.
	 */
	private AmplifierStepperListener amplifierStepperListener = new AmplifierStepperListener() {

		@Override
		public void performAction(STEPPER stepper) throws Exception {
			try {
				STREAM_STATE streamState = cameraControls.getStreamState();
				double exposureTime = Double.NaN;
				if (streamState.equals(STREAM_STATE.SAMPLE_STREAM)) {
					exposureTime = cameraControls.getSampleExposureTime();
				} else if (streamState.equals(STREAM_STATE.FLAT_STREAM)) {
					exposureTime = cameraControls.getFlatExposureTime();
				}
				if (!streamState.equals(STREAM_STATE.NO_STREAM)) {
					tomoAlignmentViewController.setAmplifierUpdate(exposureTime, stepper.getValue());
				}
			} catch (Exception e) {
				logger.error("Unable to apply stepper value:", e);
				throw e;
			}
		}
	};

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
					if (!ZOOM_LEVEL.NO_ZOOM.equals(cameraControls.getSelectedZoomLevel())) {
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
		if (motionControlComposite != null && !motionControlComposite.isDisposed()) {
			motionControlComposite.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					motionControlComposite.moveRotationSliderTo(rotationMotorDeg);
				}
			});
		}
	}

	@Override
	public void setFlatFieldCorrection(boolean enabled) {
		cameraControls.setFlatFieldCorrection(enabled);
	}

	@Override
	public void setPreferredSampleExposureTimeToWidget(double preferredExposureTime) {
		cameraControls.setPreferredSampleExposureTime(preferredExposureTime);
	}

	@Override
	public void setPreferredFlatExposureTimeToWidget(double preferredExposureTime) {
		cameraControls.setPreferredFlatExposureTime(preferredExposureTime);
	}

	@Override
	public void setCameraModule(final CAMERA_MODULE module) {
		motionControlComposite.setCameraModule(module);
		updateScaleBars(module);
	}

	protected void updateScaleBars(final CAMERA_MODULE module) {
		if (leftWindowImageViewer != null && !leftWindowImageViewer.isDisposed()) {
			leftWindowImageViewer.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					ScaleDisplay leftBarLengthInPixel = tomoAlignmentViewController.getLeftBarLengthInPixel(
							leftWindowImageViewer.getBounds().width / 3, module);
					if (leftBarLengthInPixel != null) {
						updateLeftWindowNumPixelsLabel(leftBarLengthInPixel.toString(),
								leftBarLengthInPixel.getBarLengthInPixel());
					}
					//
					ScaleDisplay rightBarLengthInPixel = tomoAlignmentViewController.getRightBarLengthInPixel(
							page_rightWindow_nonProfile.getBounds().width / 2 - 10, module,
							cameraControls.getSelectedZoomLevel());
					if (rightBarLengthInPixel != null) {
						updateRightWindowNumPixelsLabel(rightBarLengthInPixel.toString(),
								rightBarLengthInPixel.getBarLengthInPixel());
					}

				}
			});
		}
	}

	private ImageListener<ImageData> tomoImageListener = new ImageListener<ImageData>() {

		private String name;

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void processImage(final ImageData imageData) {
			try {
				tomoPlotComposite.updateHistogramData(getLeftWindowViewerDisplayMode(), imageData);
			} catch (Exception ex) {
				cameraControls.stopSampleHistogram();
			}
		}
	};

	@Override
	public void dispose() {

		try {
			getSite().getPage().removePartListener(tomoPartAdapter);
			if (leftWindowImageViewer != null) {
				logger.debug("Removing zoom rect listener");
				leftWindowImageViewer.removeZoomRectListener(zoomRectListener);
				leftWindowImageViewer.removeProfileListener(profilePointListener);
				leftWindowImageViewer.removeOverlayImageFigureListener(overlayImageFigureListener);
				leftWindowImageViewer.dispose();
			}
			histogramSliderComposite.removeColourSliderListener(histogramSliderListener);
			histogramSliderComposite.dispose();
			tomoPlotComposite.removeOverlayLineListener(profileLineListener);
			zoomRectListener = null;

			stopFullVideoReceiver();
			// leftVideoReceiver.removeImageListener(leftVideoListener);
			// leftVideoReceiver.removeImageListener(tomoImageListener);
			// rightVideoReceiver.removeImageListener(rightVideoListener);
			leftVideoListener = null;
			rightVideoListener = null;

			leftVideoReceiver = null;
			rightVideoReceiver = null;

			overlayImageFigureListener = null;

			motionControlComposite.removeMotionControlListener(motionControlListener);
			motionControlListener = null;

			cameraControls.removeCamerControlListener(cameraControlListener);
			cameraControlListener = null;

			logger.debug("Disposing tomoalignment viewer");
			amplifierStepper.removeAmplifierStepperListener(amplifierStepperListener);
			amplifierStepperListener = null;
			amplifierStepper.dispose();
			// FIXME-Ravi
			// fullImgProvider.removeJpegImageListener(fullImgListener);
			// zoomedImgProvider.removeJpegImageListener(zoomImgListener);

			page_nonProfile_streamZoom.dispose();
			demandRawZoomCanvas.dispose();

			pageBook_nonProfile_zoomImg.dispose();
			//
			page_rightWindow_plot.dispose();
			page_rightWindow_nonProfile.dispose();
			page_leftWindow_imgViewer.dispose();
			page_leftWindow_introComposite.dispose();

			pageBook_leftWindow.dispose();
			pageBook_rightWindow.dispose();
			//
			cameraControls.dispose();
			//
			motionControlComposite.dispose();
			tomoPlotComposite.dispose();
			//
			tomoAlignmentViewController.unregisterTomoAlignmentView(this);
			tomoAlignmentViewController.dispose();
			toolkit.dispose();
			// ACTIVE_WORKBENCH_WINDOW.getPartService().removePartListener(partListener);
			histogramAdjuster.dispose();
			super.dispose();
		} catch (Exception ex) {
			logger.error("Exception in dispose", ex);
		}
	}

	@Override
	public void updateStatInfo(StatInfo statInfoEnum, String val) {
		this.statInfo.updateValue(statInfoEnum, val);
	}

	@Override
	public void updateRotationMotorBusy(boolean isBusy) {
		motionControlComposite.setRotationMotorBusy(isBusy);
	}

	@Override
	public void updateErrorAligningTilt(String status) {
		loadErrorInDisplay("Error preparing TILT alignment", status);
	}

	@Override
	public void reset() {
		try {
			tomoAlignmentViewController.resetAll();
		} catch (Exception e) {
			logger.error("TODO put description of error here", e);
			loadErrorInDisplay("Error while reseting the camera", "Connection with the camera IOC may be disrupted.");
		}
	}

	@Override
	public void updateModuleButtonText(final String unit, final Map<Integer, String> moduleButtonText) {
		if (moduleButtonText != null && !(moduleButtonText.isEmpty())) {
			if (motionControlComposite != null && !motionControlComposite.isDisposed()) {
				motionControlComposite.getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						motionControlComposite.setModuleButtonText(unit, moduleButtonText.get(1),
								moduleButtonText.get(2), moduleButtonText.get(3), moduleButtonText.get(4));

					}
				});
			}
		}
	}

	@Override
	public void setCameraMotionMotorPosition(double cameraMotionMotorPosition) {
		motionControlComposite.setCameraMotionPosition(cameraMotionMotorPosition);
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
	void stopFullVideoReceiver() {
		if (fullImgReceiverStarted) {
			leftVideoReceiver.closeConnection();

			leftVideoReceiver.removeImageListener(leftVideoListener);
			leftVideoReceiver.removeImageListener(tomoImageListener);
			fullImgReceiverStarted = false;

			if (zoomReceiverStarted) {
				stopZoomVideoReceiver();
			}
		}
	}

	/**
	 * Starts the zoom receiver.
	 */
	void startZoomVideoReceiver() {
		if (!zoomReceiverStarted) {
			rightVideoReceiver.addImageListener(rightVideoListener);
			rightVideoReceiver.createConnection();
			zoomReceiverStarted = true;
		}
	}

	/**
	 * Stops the zoom receiver.
	 */
	void stopZoomVideoReceiver() {
		if (zoomReceiverStarted) {
			rightVideoReceiver.closeConnection();
			rightVideoReceiver.removeImageListener(rightVideoListener);
			zoomReceiverStarted = false;

		}
	}

	/**
	 * @param info
	 */
	void setLeftWindowInfo(final String info) {
		leftWindowDisplayMode = ViewerDisplayMode.getDisplayMode(info);
		setLeftWindowDisplayMode(leftWindowDisplayMode);
		if (info != null && lblLeftWindowDisplayModeStatus != null && !lblLeftWindowDisplayModeStatus.isDisposed()) {
			lblLeftWindowDisplayModeStatus.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					lblLeftWindowDisplayModeStatus.setText(info.toUpperCase());
				}
			});
		}
	}

	/**
	 * Set of procedures that need to be done when profiling is stopped.
	 */
	void stopProfiling() {
		if (cameraControls.isProfileSelected()) {
			cameraControls.profileStopped();
			leftWindowImageViewer.hideLineProfiler();
			pageBook_rightWindow.showPage(page_rightWindow_nonProfile);
			setRightInfoPage(RIGHT_INFO.NONE);
			tomoPlotComposite.setImagesToPlot(null, null);
			ZOOM_LEVEL selectedZoomLevel = cameraControls.getSelectedZoomLevel();
			if (!ZOOM_LEVEL.NO_ZOOM.equals(selectedZoomLevel)) {
				cameraControls.setZoom(selectedZoomLevel);
			}
		}
	}

	/**
	 * Set of procedures that need to run when streaming is stopped this needs to be called sparingly and does not cause
	 * the Stream button on the screen to be toggled.<br>
	 * the {@link CameraControlComposite#stopSampleStream()} should be called for that purpose
	 */
	public void stopStreaming() {
		logger.debug("stopStreaming -> Stop video receiver and call stop acquiring");
		stopFullVideoReceiver();
		try {
			tomoAlignmentViewController.stopAcquiring();
		} catch (Exception e) {
			logger.error("stopStreaming -> Problem stop acquiring", e);
		} finally {
			setLeftWindowInfo(STREAM_STOPPED);
		}
	}

	/**
	 * see {@link CameraControlComposite#startSampleStreaming()} to get the Stream button enabled
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
					tomoAlignmentViewController.startAcquiring(acquireTime, amplifierStepper.getSelectedStepper()
							.getValue());
				}
			});

			pageBook_leftWindow.showPage(page_leftWindow_imgViewer);

			// Shouldn't be showing the file name and the file timestamp while streaming imgs.
			lblFileTimeStamp.setText(BLANK_STR);
			lblFileName.setText(BLANK_STR);
			// Need to stop profiling
			cameraControls.deSelectSaturation();
			// If zoom is selected then update the zoomed window.
			if (cameraControls.getSelectedZoomLevel() != ZOOM_LEVEL.NO_ZOOM) {
				setRightPage(RIGHT_PAGE.ZOOM_STREAM);
				cameraControls.setZoom(cameraControls.getSelectedZoomLevel());
			}
			stopProfiling();
			// Set the MJPeg Streamer URL
			setMJPegUrl();
			// Start the video receiver.
			logger.debug("startStreaming -> change the page book to image view -start videoReceiver");
			if (leftVideoReceiver.isUrlSet()) {
				startFullVideoReceiver();
			}
			updateScaleBars(motionControlComposite.getSelectedCameraModule());
		} catch (InvocationTargetException e) {
			logger.error("startStreaming -> Problem acquiring FFMJpeg from the camera", e);
			throw new InvocationTargetException(e, "Cannot start streaming: ");
		} catch (InterruptedException e) {
			logger.error("Shouldn't flow through here - but stream operation was somehow interrupted", e);
		}
	}

	protected void setMJPegUrl() {
		Future<Boolean> isSuccessful = tomoAlignmentViewController.getStreamUrl();
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
				cameraControls.stopSampleStream();
				cameraControls.stopFlatStream();
				//
			} catch (ExecutionException e) {
				logger.error("IOC May be down", e);
				MessageDialog.openError(getViewSite().getShell(), ERROR_STARTING_STREAM_label,
						ERROR_STREAM_START_shortdesc);
				//
				cameraControls.stopSampleStream();
				cameraControls.stopFlatStream();
				stopFullVideoReceiver();
			}

		}
	}

	public boolean isStreamingSampleExposure() {
		return STREAM_STATE.SAMPLE_STREAM.equals(cameraControls.getStreamState());
	}

	public boolean isStreamingFlatExposure() {
		return STREAM_STATE.FLAT_STREAM.equals(cameraControls.getStreamState());
	}

	public STEPPER getSelectedAmplifierStepper() {
		return amplifierStepper.getSelectedStepper();
	}

	public void setHistogramAdjusterImageData(ImageData imgData) {
		histogramAdjuster.setImageData(imgData);
	}

	public CAMERA_MODULE getSelectedCameraModule() {
		return motionControlComposite.getSelectedCameraModule();
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
						pageBook_rightWindow.showPage(page_rightInfo_nonProfile);
						pageBook_nonProfile_zoomImg.showPage(page_nonProfile_demandRaw);
						break;
					case NO_ZOOM:
						pageBook_rightWindow.showPage(page_rightInfo_nonProfile);
						pageBook_nonProfile_zoomImg.showPage(page_nonProfile_noZoom);
						break;
					case ZOOM_STREAM:
						pageBook_rightWindow.showPage(page_rightInfo_nonProfile);
						pageBook_nonProfile_zoomImg.showPage(page_nonProfile_streamZoom);
					}
				}
			});
		}
	}

	public void setLeftPage(final LEFT_PAGE page) {
		if (pageBook_leftWindow.getDisplay() != null && !pageBook_leftWindow.getDisplay().isDisposed()) {
			pageBook_leftWindow.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					switch (page) {
					case IMAGE_VIEWER:
						pageBook_leftWindow.showPage(page_leftWindow_imgViewer);
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

	private void setLeftWindowDisplayMode(ViewerDisplayMode viewerDisplayMode) {
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
			cameraControls.selectSampleIn();
			break;
		case OUT:
			cameraControls.selectSampleOut();
			break;
		}
	}

	public void saveConfiguration() throws Exception {
		try {
			isSaving = true;
			cameraControls.startSampleStreaming();
			cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);

			AlignmentConfigSaveDialog configSaveDialog = new AlignmentConfigSaveDialog(getViewSite().getShell(),
					tomoAlignmentViewController, leftVideoReceiver);
			configSaveDialog.open();

			int returnCode = configSaveDialog.getReturnCode();
			final ImageLocationRelTheta viewerBtnSelected = configSaveDialog.getViewerButtonSelected();

			if (IDialogConstants.OK_ID == returnCode) {

				ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {
					private final DecimalFormat threePrecision = new DecimalFormat("#.###");

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							monitor.setTaskName("Saving configuration...");
							SaveableConfiguration configuration = new SaveableConfiguration();
							// Module number
							configuration.setModuleNumber(motionControlComposite.getSelectedCameraModule().getValue());
							// Sample Acquisition time
							configuration.setSampleAcquisitonTime(Double.valueOf(threePrecision.format(cameraControls
									.getSampleExposureTime())));
							// Flat Acquisition time
							configuration.setFlatAcquisitionTime(Double.valueOf(threePrecision.format(cameraControls
									.getFlatExposureTime())));
							// Sample description
							configuration.setSampleDescription(cameraControls.getSampleDescription());
							// ROI points
							configuration.setRoiPoints(leftWindowImageViewer.getRoiPoints());
							// Energy
							configuration.setEnergy(motionControlComposite.getEnergy());
							// resolution
							configuration.setResolution3D(cameraControls.getResolution());
							// sample weight
							configuration.setSampleWeight(motionControlComposite.getSampleWeight());
							// number of projections
							configuration.setNumProjections(cameraControls.getFramesPerProjection());

							String imgAtTheta = null;
							double theta = 0;
							String imgAtThetaPlus90 = null;
							try {
								switch (viewerBtnSelected) {
								case THETA:
									theta = tomoAlignmentViewController.getRotationMotorDeg();
									imgAtTheta = tomoAlignmentViewController.demandRawWithStreamOn(monitor, false);
									tomoAlignmentViewController.moveRotationMotorBy(monitor, 90);
									imgAtThetaPlus90 = tomoAlignmentViewController
											.demandRawWithStreamOn(monitor, false);
									tomoAlignmentViewController.moveRotationMotorBy(monitor, -90);
									break;
								case THETA_PLUS_90:
									theta = tomoAlignmentViewController.getRotationMotorDeg() - 90;
									imgAtThetaPlus90 = tomoAlignmentViewController
											.demandRawWithStreamOn(monitor, false);
									tomoAlignmentViewController.moveRotationMotorBy(monitor, -90);
									imgAtTheta = tomoAlignmentViewController.demandRawWithStreamOn(monitor, false);
									break;
								}
							} catch (Exception ex) {
								logger.error("Unable to save images at theta:{}", ex);
								throw new InvocationTargetException(ex, "Cannot save images at theta");
							}
							// stitching angle
							configuration.setStitchingAngle(theta);
							// image at theta
							configuration.setImageAtTheta(imgAtTheta);
							// image at theta+90
							configuration.setImageAtThetaPlus90(imgAtThetaPlus90);
							if (leftWindowImageViewer.getCrossWireVertical().isVisible()) {
								int x = leftWindowImageViewer.getCrossWireVertical().getPoints().getFirstPoint().x - leftWindowImageViewer.getImageBounds().x;
								logger.debug("Tomo rotation axis:{}", x);
								configuration.setTomoRotationAxis(x * tomoAlignmentViewController.getLeftWindowBinValue());
							}
							try {
								tomoAlignmentViewController.saveConfiguration(monitor, configuration);
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
				cameraControls.clearSampleDescription();
			}
		} finally {
			isSaving = false;
		}
	}

	@Override
	public void setEnergy(double energy) {
		motionControlComposite.setEnergyValue(energy);
	}

	@Override
	public void setResolutionPixelSize(String resolutionPixelSize) {
		cameraControls.setResolutionPixelSize(resolutionPixelSize);
	}

	@Override
	public void setResolution(RESOLUTION res) {
		cameraControls.setResolution(res);
	}

	public void addLeftWindowTomoImageListener() {
		leftVideoReceiver.addImageListener(tomoImageListener);
	}

	public void removeLeftWindowTomoImageListener() {
		leftVideoReceiver.removeImageListener(tomoImageListener);
	}

	@Override
	public void setAdjustedPreferredExposureTimeToWidget(double preferredExposureTime) {
		if (leftWindowDisplayMode == ViewerDisplayMode.SAMPLE_STREAM_LIVE
				|| leftWindowDisplayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
			setPreferredSampleExposureTimeToWidget(preferredExposureTime);
			tomoAlignmentViewController.setPreferredSampleExposureTime(preferredExposureTime);
		} else if (leftWindowDisplayMode == ViewerDisplayMode.FLAT_STREAM_LIVE
				|| leftWindowDisplayMode == ViewerDisplayMode.FLAT_SINGLE) {
			setPreferredFlatExposureTimeToWidget(preferredExposureTime);
			tomoAlignmentViewController.setPreferredFlatExposureTime(preferredExposureTime);
		}
	}
}
