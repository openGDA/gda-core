/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import gda.device.DeviceException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ViewerDisplayMode;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.LEFT_PAGE;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.RIGHT_PAGE;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.RIGHT_INFO;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.client.tomo.alignment.view.handlers.IRoiHandler;
import uk.ac.gda.client.tomo.alignment.view.utils.ScaleDisplay;
import uk.ac.gda.client.tomo.composites.FullImageComposite;
import uk.ac.gda.client.tomo.composites.FullImageComposite.IRoiPointsListener;
import uk.ac.gda.ui.components.AmplifierStepperComposite.STEPPER;
import uk.ac.gda.ui.components.CameraControlComposite;
import uk.ac.gda.ui.components.CameraControlComposite.STREAM_STATE;
import uk.ac.gda.ui.components.ICameraControlListener;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.ui.components.ZoomButtonComposite.ZOOM_LEVEL;

/**
 * Camera control composite's listener - advices the Tomoalignment view of the actions it has to take according to the
 * user requests.
 */
public class CameraCompositeController implements ICameraControlListener {
	private IProgressMonitor monitor = new NullProgressMonitor();
	private TomoAlignmentView v;
	private static final IWorkbenchWindow ACTIVE_WORKBENCH_WINDOW = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	private static final Logger logger = LoggerFactory.getLogger(CameraCompositeController.class);
	private final CameraControlComposite cameraControls;
	private final TomoAlignmentViewController tomoAlignmentViewController;

	private static final String ERR_SATURATION_RAWIMG_shortdesc = "Saturation can only be applied to Raw images";
	private final FullImageComposite leftWindowImageViewer;

	public CameraCompositeController(TomoAlignmentView v, CameraControlComposite cameraControls,
			TomoAlignmentViewController tomoAlignmentViewController, FullImageComposite leftWindowImageViewer) {
		this.v = v;
		this.cameraControls = cameraControls;
		this.tomoAlignmentViewController = tomoAlignmentViewController;
		this.leftWindowImageViewer = leftWindowImageViewer;
	}

	@Override
	public void sampleHistogram(boolean selection) throws Exception {
		logger.debug("sample histogram selected {}", selection);
		double sampleExposureTime = cameraControls.getSampleExposureTime();

		if (selection) {
			// show the plot view
			ViewerDisplayMode leftWindowViewerDisplayMode = v.getLeftWindowViewerDisplayMode();
			if (ViewerDisplayMode.SAMPLE_STREAM_LIVE.equals(leftWindowViewerDisplayMode)) {

				cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
				cameraControls.stopFlatHistogram();
				cameraControls.deselectFlatStream();
				//
				v.resetAmplifier();
				cameraControls.deSelectSaturation();

				double cameraExposureTime = tomoAlignmentViewController.getCameraExposureTime();
				if (cameraExposureTime != sampleExposureTime) {
					tomoAlignmentViewController.setAmplifierUpdate(sampleExposureTime, 1);
				}

				v.setRightPage(RIGHT_PAGE.PLOT);
				v.addLeftWindowTomoImageListener();
				v.setRightInfoPage(RIGHT_INFO.HISTOGRAM);
			} else if (ViewerDisplayMode.SAMPLE_SINGLE.equals(leftWindowViewerDisplayMode)) {
				String fileName = leftWindowViewerDisplayMode.getFileName(tomoAlignmentViewController);
				cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
				cameraControls.stopFlatHistogram();
				v.tomoPlotComposite.updateHistogramData(v.getLeftWindowViewerDisplayMode(), new ImageData(fileName));
				v.setRightPage(RIGHT_PAGE.PLOT);
				v.setRightInfoPage(RIGHT_INFO.NONE);
			} else {
				MessageDialog.openError(cameraControls.getShell(), "Histogram cannot be displayed",
						"Histogram can only be displayed for Sample Stream or Single");
				cameraControls.stopSampleHistogram();
				v.setRightInfoPage(RIGHT_INFO.NONE);
			}
		} else {
			cameraControls.stopSampleStream();
			v.setRightPage(RIGHT_PAGE.NONE);
			v.setRightInfoPage(RIGHT_INFO.NONE);
		}
	}

	@Override
	public void flatHistogram(boolean selection) throws Exception {

		logger.debug("sample histogram selected {}", selection);
		double flatExposureTime = cameraControls.getFlatExposureTime();

		if (selection) {
			// show the plot view
			ViewerDisplayMode leftWindowViewerDisplayMode = v.getLeftWindowViewerDisplayMode();
			if (ViewerDisplayMode.FLAT_STREAM_LIVE.equals(leftWindowViewerDisplayMode)
					|| ViewerDisplayMode.FLAT_SINGLE.equals(leftWindowViewerDisplayMode)) {
				cameraControls.deselectSampleStream();
				cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
				cameraControls.stopSampleHistogram();
				//
				v.resetAmplifier();
				cameraControls.deSelectSaturation();
				double cameraExposureTime = tomoAlignmentViewController.getCameraExposureTime();
				if (cameraExposureTime != flatExposureTime) {
					tomoAlignmentViewController.setAmplifierUpdate(flatExposureTime, 1);
				}

				v.setRightPage(RIGHT_PAGE.PLOT);
				v.addLeftWindowTomoImageListener();
			} else {
				MessageDialog.openError(cameraControls.getShell(), "Histogram cannot be displayed",
						"Histogram can only be displayed for Flat Stream or Single");
				cameraControls.stopFlatHistogram();

			}
		} else {
			v.setRightPage(RIGHT_PAGE.NONE);
			v.removeLeftWindowTomoImageListener();
		}

	}

	@Override
	public void sampleDescriptionChanged(String sampleDescription) {
		logger.debug("The sample description has changed to: {}", sampleDescription);
	}

	@Override
	public void saveAlignmentConfiguration() throws InvocationTargetException, InterruptedException {
		logger.debug("save alignment configuration");
		v.saveConfiguration();
	}

	@Override
	public void showDark() throws Exception {

		// Stop streaming when showing 'Show Flat' is called.
		cameraControls.deSelectSaturation();
		v.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		cameraControls.stopSampleStream();

		String darkImageFileName = tomoAlignmentViewController.getDarkFieldImageFullFileName();
		if (darkImageFileName != null) {
			File checkFile = new File(darkImageFileName);
			if (checkFile.exists()) {
				try {
					Image img = new Image(leftWindowImageViewer.getDisplay(), darkImageFileName);
					ImageData imgData = img.getImageData();
					leftWindowImageViewer.loadMainImage(imgData.scaledTo(TomoAlignmentView.SCALED_TO_X,
							TomoAlignmentView.SCALED_TO_Y));
					img.dispose();
					displayFileDetails(ViewerDisplayMode.DARK_SINGLE);
				} catch (Exception e) {
					v.lblFileName.setText(TomoAlignmentView.BLANK_STR);
					v.lblFileTimeStamp.setText(TomoAlignmentView.BLANK_STR);
					throw e;
				} finally {
					logger.debug("Loaded Flat image File Name timestamp {}",
							getSimpleDateFormat(checkFile.lastModified()));
					v.setLeftWindowInfo(TomoAlignmentView.STATIC_DARK);
				}
			}
		}
	}

	@Override
	public void flatExposureTimeChanged(double flatExposureTime) throws Exception {
		v.resetAmplifier();
		tomoAlignmentViewController.setPreferredFlatExposureTime(flatExposureTime);
		if (STREAM_STATE.FLAT_STREAM.equals(cameraControls.getStreamState())) {
			tomoAlignmentViewController.setExposureTime(flatExposureTime, 1);
		}
	}

	@Override
	public void sampleExposureTimeChanged(double sampleExposureTime) throws Exception {
		try {
			v.resetAmplifier();
			tomoAlignmentViewController.setPreferredSampleExposureTime(sampleExposureTime);
			if (STREAM_STATE.SAMPLE_STREAM.equals(cameraControls.getStreamState())) {
				tomoAlignmentViewController.setExposureTime(sampleExposureTime, 1);
			}
		} catch (Exception e) {
			logger.error("Problem resetting amplifier dark or flat", e);
			throw e;
		}
	}

	@Override
	public void updatePreferredFlatExposureTime() throws Exception {
		try {
			cameraControls.setPreferredFlatExposureTime(tomoAlignmentViewController.getPreferredFlatExposureTime());
		} catch (Exception e) {
			logger.error("Problem getting exposure time", e);
			throw e;
		}
	}

	@Override
	public void flatSingle(final boolean flatCorrectionSelected) throws InvocationTargetException, Exception {
		logger.debug("Flat Single Called");
		v.stopProfiling();
		cameraControls.stopSampleHistogram();

		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		v.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		final double acqTime = cameraControls.getFlatExposureTime();
		final boolean isStreaming = STREAM_STATE.FLAT_STREAM.equals(cameraControls.getStreamState());

		final boolean isAmplifierAtOne = STEPPER.ONE.equals(v.getSelectedAmplifierStepper());
		try {

			cameraControls.deSelectSaturation();
			//
			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						SubMonitor progress = SubMonitor.convert(monitor);
						progress.beginTask("Demanding raw image", 10);
						String fileLocation = null;

						// Special case of demanding raw - when the amplifier is at "1" and the stream is switched
						// on. This is to prevent the arming/disarming of the camera in order to save a few seconds
						if (isAmplifierAtOne && isStreaming) {
							fileLocation = tomoAlignmentViewController.demandRawWithStreamOn(progress.newChild(2),
									flatCorrectionSelected);
							cameraControls.stopFlatStream();
						} else {
							cameraControls.stopFlatStream();
							//
							try {
								fileLocation = tomoAlignmentViewController.demandRaw(progress.newChild(2), acqTime,
										flatCorrectionSelected);
							} catch (Exception e) {
								logger.error("problem while demand raw", e);
								throw new InvocationTargetException(e);
							}

						}
						//
						if (!monitor.isCanceled() && fileLocation != null) {
							Image img = new Image(leftWindowImageViewer.getDisplay(), fileLocation);
							ImageData imgData = img.getImageData();

							logger.debug(String.format("demandRaw() imageData depth#%d  palete is direct# %s",
									imgData.depth, imgData.palette.isDirect));
							v.loadImageInUIThread(leftWindowImageViewer,
									imgData.scaledTo(TomoAlignmentView.SCALED_TO_X, TomoAlignmentView.SCALED_TO_Y));
							v.setHistogramAdjusterImageData((ImageData) imgData.clone());
							imgData.data = null;
							imgData = null;
							img.dispose();
							loadZoomImageInUI();
							//
						}
					} catch (Exception e1) {
						logger.error("problem while demand raw", e1);
						throw new InvocationTargetException(e1, "Problem while demanding raw");
					} finally {
						monitor.done();
						v.setLeftWindowInfo(TomoAlignmentView.FLAT_SINGLE);
					}
				}
			});
			ViewerDisplayMode viewDisplayMode = v.getLeftWindowViewerDisplayMode();
			v.updateScaleBars(v.getSelectedCameraModule());

			if (viewDisplayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
				displayFileDetails(viewDisplayMode);
			}
		} catch (InvocationTargetException e) {
			logger.error("demandRaw", e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("demandRaw", e);
		} catch (Exception e) {
			logger.error("Exception while demanding raw ", e);
			throw e;
		}

	}

	private class RunnableWithProgress implements IRunnableWithProgress {

		private boolean isJobComplete = false;
		private final boolean flatCorrectionSelected;
		private final boolean isAmplifierAtOne;
		private final boolean isStreaming;

		private final double acqTime;

		public RunnableWithProgress(double acqTime, boolean isAmplifierAtOne, boolean isStreaming,
				boolean flatCorrectionSelected) {
			this.acqTime = acqTime;
			this.isAmplifierAtOne = isAmplifierAtOne;
			this.isStreaming = isStreaming;
			this.flatCorrectionSelected = flatCorrectionSelected;
		}

		@Override
		public void run(final IProgressMonitor baseMonitor) throws InvocationTargetException, InterruptedException {
			Job checkProgressJob = null;
			isJobComplete = false;
			try {
				checkProgressJob = new Job("Check Progress") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						while (!isJobComplete) {
							if (baseMonitor.isCanceled()) {
								try {
									tomoAlignmentViewController.stopDemandRaw();
								} catch (Exception e) {
									logger.error("Problem stopping sample single");
									// MessageDialog.openError(cameraControls.getShell(), "User Stopped Operation",
									// "Problem with taking Single: User stopped operation");
								}
								break;
							}
						}
						return Status.OK_STATUS;
					}
				};
				checkProgressJob.schedule(50);

				SubMonitor progress = SubMonitor.convert(baseMonitor);
				progress.beginTask("Demanding raw image", 10);
				String fileLocation = null;

				// Special case of demanding raw - when the amplifier is at "1" and the stream is switched
				// on. This is to prevent the arming/disarming of the camera in order to save a few seconds
				if (isAmplifierAtOne && isStreaming) {
					fileLocation = tomoAlignmentViewController.demandRawWithStreamOn(progress.newChild(2),
							flatCorrectionSelected);
					cameraControls.stopSampleStream();
				} else {
					cameraControls.stopSampleStream();
					//
					try {
						fileLocation = tomoAlignmentViewController.demandRaw(progress.newChild(2), acqTime,
								flatCorrectionSelected);
					} catch (Exception e) {
						logger.error("problem while demand raw", e);
						throw new InvocationTargetException(e);
					}

				}
				//
				if (!baseMonitor.isCanceled() && fileLocation != null) {
					Image img = new Image(leftWindowImageViewer.getDisplay(), fileLocation);
					ImageData imgData = img.getImageData();

					logger.debug(String.format("demandRaw() imageData depth#%d  palete is direct# %s", imgData.depth,
							imgData.palette.isDirect));
					v.loadImageInUIThread(leftWindowImageViewer,
							imgData.scaledTo(TomoAlignmentView.SCALED_TO_X, TomoAlignmentView.SCALED_TO_Y));
					v.setHistogramAdjusterImageData((ImageData) imgData.clone());
					imgData.data = null;
					imgData = null;
					img.dispose();
					loadZoomImageInUI();
					//
				}
			} catch (Exception e1) {
				logger.error("problem while demand raw", e1);
				throw new InvocationTargetException(e1, "Problem while demanding raw");
			} finally {
				isJobComplete = true;
				baseMonitor.done();
				v.setLeftWindowInfo(TomoAlignmentView.SAMPLE_SINGLE);
				if (checkProgressJob != null) {
					checkProgressJob.cancel();
				}
			}
		}

	}

	@Override
	public void sampleSingle(final boolean flatCorrectionSelected) throws Exception {
		logger.debug("Sample Single Called");
		v.stopProfiling();
		cameraControls.stopFlatHistogram();

		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		v.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		final double acqTime = cameraControls.getSampleExposureTime();
		final boolean isStreaming = v.isStreamingSampleExposure();

		final boolean isAmplifierAtOne = STEPPER.ONE.equals(v.getSelectedAmplifierStepper());
		try {

			cameraControls.deSelectSaturation();
			//
			final RunnableWithProgress sampleSingleRunnable = new RunnableWithProgress(acqTime, isAmplifierAtOne,
					isStreaming, flatCorrectionSelected);

			ACTIVE_WORKBENCH_WINDOW.run(true, true, sampleSingleRunnable);

			ViewerDisplayMode viewDisplayMode = v.getLeftWindowViewerDisplayMode();
			v.updateScaleBars(v.getSelectedCameraModule());

			if (viewDisplayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
				displayFileDetails(viewDisplayMode);
			}
		} catch (InvocationTargetException e) {
			logger.error("demandRaw", e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("demandRaw", e);
		} catch (Exception e) {
			logger.error("Exception while demanding raw ", e);
			throw e;
		}

	}

	private void displayFileDetails(ViewerDisplayMode viewDisplayMode) throws Exception {
		String rawFileName = viewDisplayMode.getFileName(tomoAlignmentViewController);
		v.lblFileName.setText(String.format("%1$s %2$s", TomoAlignmentView.FILE_NAME, rawFileName));
		if (rawFileName != null) {
			File checkFile = new File(rawFileName);
			if (checkFile.exists()) {
				v.lblFileTimeStamp.setText(String.format("%1$s %2$s", TomoAlignmentView.TIMESTAMP,
						getSimpleDateFormat(checkFile.lastModified())));
			}
		} else {
			throw new IllegalArgumentException("Single image could not be loaded");
		}
	}

	@Override
	public void profile(boolean selected) throws Exception {
		if (selected) {
			logger.debug("'Profile' is selected");
			cameraControls.startDemandRaw();
			v.setRightPage(RIGHT_PAGE.PLOT);

			// According to the requirement only RAW images need to be profiled.
			ViewerDisplayMode staticSingleEnum = v.getLeftWindowViewerDisplayMode();

			if (ViewerDisplayMode.SAMPLE_SINGLE == staticSingleEnum) {
				String rawFileName = null;
				try {
					rawFileName = staticSingleEnum.getFileName(tomoAlignmentViewController);

					String darkImgFileName = null;
					if (tomoAlignmentViewController.isDarkImageSaved()) {
						darkImgFileName = tomoAlignmentViewController.getDarkImageFileName();
					}
					v.tomoPlotComposite.setImagesToPlot(rawFileName, darkImgFileName);

					ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							v.updatePlots(monitor, 0, 4008, 1);
							monitor.done();
						}
					});

				} catch (Exception e1) {
					logger.error("getting raw file problem.", e1);
				}

			}
			leftWindowImageViewer.showLineProfiler();
			v.setRightPage(RIGHT_PAGE.PLOT);
		} else {
			logger.debug("'Profile' is de-selected");
			v.stopProfiling();
		}
	}

	@Override
	public void sampleStream(final boolean selected) throws Exception {
		if (selected) {
			cameraControls.stopFlatHistogram();
			cameraControls.stopSampleHistogram();
			cameraControls.deselectFlatStream();
			if (!STREAM_STATE.NO_STREAM.equals(cameraControls.getStreamState())) {
				if (cameraControls.getSampleExposureTime() != cameraControls.getFlatExposureTime()) {
					setExposureTime(v.getSteppedSampleExposureTime());
					cameraControls.setStreamState(STREAM_STATE.SAMPLE_STREAM);
					v.setLeftWindowInfo(TomoAlignmentView.SAMPLE_LIVE_STREAM);
				}
			} else {
				final double steppedAcqTime = v.getSteppedSampleExposureTime();
				stream(selected, steppedAcqTime, TomoAlignmentView.SAMPLE_LIVE_STREAM);
			}
		} else {
			cameraControls.deselectSampleStream();
			cameraControls.stopSampleHistogram();
			stream(selected, Double.NaN, TomoAlignmentView.STREAM_STOPPED);
		}
	}

	@Override
	public void flatStream(boolean selected) throws Exception {
		if (selected) {
			cameraControls.deselectSampleStream();
			cameraControls.stopFlatHistogram();
			cameraControls.stopSampleHistogram();
			if (!STREAM_STATE.NO_STREAM.equals(cameraControls.getStreamState())) {
				if (cameraControls.getSampleExposureTime() != cameraControls.getFlatExposureTime()) {
					setExposureTime(v.getSteppedFlatExposureTime());
					cameraControls.setStreamState(STREAM_STATE.FLAT_STREAM);
					v.setLeftWindowInfo(TomoAlignmentView.FLAT_LIVE_STREAM);
				}
			} else {
				final double steppedAcqTime = v.getSteppedFlatExposureTime();
				cameraControls.deselectSampleStream();
				stream(selected, steppedAcqTime, TomoAlignmentView.FLAT_LIVE_STREAM);
			}
		} else {
			cameraControls.stopFlatHistogram();
			stream(selected, Double.NaN, TomoAlignmentView.STREAM_STOPPED);
		}
	}

	private void stream(boolean selected, final double exposureTime, final String displayMsg)
			throws InvocationTargetException {
		if (selected) {
			v.startStreaming(exposureTime);
			v.setLeftWindowInfo(displayMsg);
		} else {
			v.stopStreaming();
		}
	}

	private void unZoomInUI() {
		if (v.page_nonProfile_noZoom != null && !v.page_nonProfile_noZoom.isDisposed()) {
			cameraControls.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					leftWindowImageViewer.hideZoomRectangleFigure();
					v.setRightPage(RIGHT_PAGE.NO_ZOOM);

					v.rightScaleBar.setScaleWidth(0);
					v.lblRightWindowInfoNumPixels.setText(TomoAlignmentView.NO_ZOOM_lbl);
					if (cameraControls.isProfileSelected()) {
						Rectangle lineBounds = leftWindowImageViewer.getProfilerLineBounds();
						int y = lineBounds.y - leftWindowImageViewer.getImageBounds().y;
						v.updatePlots(monitor, 0, 4008, y * tomoAlignmentViewController.getLeftWindowBinValue());
					}
				}
			});
		}
	}

	@Override
	public void zoomButtonClicked(ZOOM_LEVEL zoomLevel) throws Exception {
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		if (ZOOM_LEVEL.NO_ZOOM.equals(zoomLevel)) {
			v.stopZoomVideoReceiver();
			v.clearZoomWindow();
			unZoomInUI();

		} else {
			ViewerDisplayMode viewerDisplayMode = v.getLeftWindowViewerDisplayMode();
			if (!ViewerDisplayMode.STREAM_STOPPED.equals(viewerDisplayMode)) {
				CAMERA_MODULE selectedCameraModule = v.getSelectedCameraModule();
				ZOOM_LEVEL selectedZoomLevel = cameraControls.getSelectedZoomLevel();
				cameraControls.stopSampleHistogram();
				switch (viewerDisplayMode) {
				case SAMPLE_STREAM_LIVE:
				case FLAT_STREAM_LIVE:
					v.setRightPage(RIGHT_PAGE.ZOOM_STREAM);
					leftWindowImageViewer.showZoomRectangleFigure(zoomLevel.getRectSize());
					try {
						tomoAlignmentViewController.handleZoom(zoomLevel, leftWindowImageViewer.getZoomFigureBounds());
					} catch (Exception e) {
						logger.error("Problem handling zoom", e);
						throw e;
					}

					ScaleDisplay rightBarLengthInPixel = tomoAlignmentViewController.getRightBarLengthInPixel(
							TomoAlignmentView.RIGHT_WINDOW_WIDTH / 2, selectedCameraModule, zoomLevel);
					if (rightBarLengthInPixel != null) {
						v.lblRightWindowInfoNumPixels.setText(rightBarLengthInPixel.toString());
						try {
							v.rightScaleBar.setScaleWidth(rightBarLengthInPixel.getBarLengthInPixel());
						} catch (Exception e) {
							logger.error("right scale bar problem", e);
							throw e;
						}
					}
					v.startZoomVideoReceiver();
					v.clearZoomWindow();
					break;
				default:
					// if PROFILE is ON
					if (cameraControls.isProfileSelected()) {
						leftWindowImageViewer.showZoomRectangleFigure(zoomLevel.getRectSize());
						Rectangle zoomFigureBounds = leftWindowImageViewer.getZoomFigureBounds();
						Rectangle lineBounds = leftWindowImageViewer.getProfilerLineBounds();

						if (zoomFigureBounds.intersects(lineBounds)) {
							Rectangle zoomFigureBoundsCopy = zoomFigureBounds.getCopy();
							Rectangle intersect = zoomFigureBoundsCopy.intersect(lineBounds);

							v.updatePlots(monitor, intersect.x, intersect.x
									+ (intersect.width * tomoAlignmentViewController.getLeftWindowBinValue()),
									intersect.y);
						} else {
							v.updatePlots(monitor, 0, 4008, lineBounds.y);
						}
					} else {
						// If profile is switched OFF
						v.setRightPage(RIGHT_PAGE.ZOOM_DEMAND_RAW);
						// In the case of 'Demand Raw' need to sub-image the 'hdf5' to display in the zoom window.
						String tiffFullFileName = null;
						try {
							tiffFullFileName = viewerDisplayMode.getFileName(tomoAlignmentViewController);
						} catch (Exception e) {
							logger.error("problem retrieving the tiff full file name", e);
							throw e;
						}
						if (tiffFullFileName != null) {
							leftWindowImageViewer.showZoomRectangleFigure(zoomLevel.getRectSize());
							// Based on SATURATION display the image.
							if (cameraControls.isSaturationSelected()) {
								Image image = new Image(leftWindowImageViewer.getDisplay(), tiffFullFileName);
								ImageData appliedSaturation = tomoAlignmentViewController.applySaturation(image
										.getImageData());
								image.dispose();
								v.loadDemandRawZoom(appliedSaturation, zoomLevel.getDemandRawScale(), true);
							} else {
								v.loadDemandRawZoom(tiffFullFileName, zoomLevel.getDemandRawScale(), true);
							}
						} else {
							v.clearZoomWindow();
						}

						try {

							rightBarLengthInPixel = tomoAlignmentViewController.getRightBarLengthInPixel(
									TomoAlignmentView.RIGHT_WINDOW_WIDTH / 2, selectedCameraModule, selectedZoomLevel);
							v.updateRightWindowNumPixelsLabel(rightBarLengthInPixel.toString(),
									rightBarLengthInPixel.getBarLengthInPixel());
						} catch (Exception e) {
							logger.error("Problem updating right scale bar", e);
							throw e;
						}
					}
				}
			} else {
				MessageDialog.openError(cameraControls.getShell(), "Zoom cannot be enabled",
						"Zoom can be enabled only when Live Stream or Single Image is displayed in the left window");
				cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
			}
		}
	}

	@Override
	public void saturation(boolean selected) throws java.lang.IllegalStateException {
		if (!v.isModuleSelected()) {
			throw new java.lang.IllegalStateException("Module should be selected");
		}
		if (selected) {
			logger.debug("Saturation switched on: Saturation only applicable for Demand Raw images");
			ViewerDisplayMode staticSingleEnum = v.getLeftWindowViewerDisplayMode();

			if (staticSingleEnum != ViewerDisplayMode.SAMPLE_SINGLE) {
				v.loadErrorInDisplay(ERR_SATURATION_RAWIMG_shortdesc, ERR_SATURATION_RAWIMG_shortdesc);
				cameraControls.saturationOff();
				return;
			}

			try {
				final String rawFileName = staticSingleEnum.getFileName(tomoAlignmentViewController);
				switchOnSaturation(rawFileName);
			} catch (Exception e) {
				logger.error("saturation", e);
			}
		} else {
			try {
				ViewerDisplayMode staticSingleEnum = v.getLeftWindowViewerDisplayMode();
				if (staticSingleEnum != null) {
					final String rawFileName = staticSingleEnum.getFileName(tomoAlignmentViewController);
					switchOffSaturation(rawFileName);
				}
				logger.debug("Saturation switched off");
			} catch (Exception e) {
				logger.error("saturation", e);
			}
		}
	}

	private void loadZoomImageInUI() {
		leftWindowImageViewer.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					zoomButtonClicked(cameraControls.getSelectedZoomLevel());
				} catch (IllegalArgumentException e) {
					logger.error("loadZoomImageInUI", e);
				} catch (Exception e) {
					logger.error("Problem loading zoomed image", e);
				}
			}
		});
	}

	@Override
	public void takeFlatAndDark() throws InterruptedException, InvocationTargetException {
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		final double expTime = cameraControls.getFlatExposureTime();
		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						SubMonitor progress = SubMonitor.convert(monitor);
						cameraControls.stopSampleStream();
						progress.beginTask("Taking dark images", 20);
						progress.worked(1);
						try {
							tomoAlignmentViewController.takeDark(progress.newChild(9), expTime);
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}

						if (!progress.isCanceled()) {
							// Taking flat images
							progress.setTaskName("Taking Flat images");
							cameraControls.stopSampleStream();
							progress.worked(1);
							int numFlat = tomoAlignmentViewController.getPreferredNumberFlatImages();
							tomoAlignmentViewController.takeFlat(progress.newChild(9), numFlat, expTime);
						} else {
							throw new InterruptedException("Operation was cancelled");
						}
					} catch (InterruptedException e) {
						throw new InvocationTargetException(e, "Operation Interrupted");
					} catch (Exception e1) {
						throw new InvocationTargetException(e1, e1.getMessage());
					} finally {
						monitor.done();
					}
				}
			});

			cameraControls.setFlatCaptured(true, cameraControls.getFlatExposureTime());
		} catch (InvocationTargetException e) {
			logger.error("Error in takeflat", e);
			cameraControls.setFlatCaptured(false, Double.NaN);
			throw e;
		} catch (InterruptedException e) {
			logger.error("Error in takeflat", e);
			cameraControls.setFlatCaptured(false, Double.NaN);
			throw e;
		}
	}

	@Override
	public void correctFlatAndDark(boolean selected) throws Exception {
		if (selected) {
			try {
				tomoAlignmentViewController.enableFlatCorrection();
				tomoAlignmentViewController.enableDarkSubtraction();
			} catch (Exception e) {
				logger.error("error enabling flat correction", e);
				throw e;
			}
		} else {
			try {
				tomoAlignmentViewController.disableFlatCorrection();
				tomoAlignmentViewController.disableDarkSubtraction();
			} catch (Exception e) {
				logger.error("error enabling flat correction", e);
				throw e;
			}
		}

	}

	@Override
	public void showFlat() throws Exception {
		// Stop streaming when showing 'Show Flat' is called.
		cameraControls.deSelectSaturation();
		v.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		cameraControls.stopSampleStream();

		String flatImageFileName = tomoAlignmentViewController.getFlatImageFullFileName();
		if (flatImageFileName != null) {
			File checkFile = new File(flatImageFileName);
			if (checkFile.exists()) {
				try {
					Image img = new Image(leftWindowImageViewer.getDisplay(), flatImageFileName);
					ImageData imgData = img.getImageData();
					leftWindowImageViewer.loadMainImage(imgData.scaledTo(TomoAlignmentView.SCALED_TO_X,
							TomoAlignmentView.SCALED_TO_Y));
					img.dispose();
					displayFileDetails(ViewerDisplayMode.FLAT_SINGLE);
				} catch (Exception e) {
					throw e;
				} finally {
					logger.debug("Loaded Flat image File Name timestamp {}",
							getSimpleDateFormat(checkFile.lastModified()));
					v.setLeftWindowInfo(TomoAlignmentView.STATIC_FLAT);
				}
			}
		}
	}

	private String getSimpleDateFormat(double epoch) {
		Date date = new Date((long) (epoch));
		SimpleDateFormat simpleDatef = new SimpleDateFormat("dd/MM/yy hh:mm:ss.SSS");
		return simpleDatef.format(date);
	}

	public void setExposureTime(double exposureTime) throws Exception {
		try {
			STEPPER selectedStepper = v.getSelectedAmplifierStepper();
			tomoAlignmentViewController.setExposureTime(exposureTime, selectedStepper.getValue());
		} catch (Exception e) {
			logger.error("Cannot set exposure time", e);
			throw e;
		}
	}

	@Override
	public void updatePreferredSampleExposureTime() throws Exception {
		try {
			cameraControls.setPreferredSampleExposureTime(tomoAlignmentViewController.getPreferredSampleExposureTime());
		} catch (Exception e) {
			logger.error("Problem getting exposure time", e);
			throw e;
		}
	}

	@Override
	public void sampleFlatTimeChanged() throws Exception {

		try {
			v.resetAmplifier();
		} catch (Exception e) {
			logger.error("Problem resetting amplifier dark or flat", e);
			throw e;
		}
	}

	protected void switchOffSaturation(final String rawFileName) {
		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						if (rawFileName != null) {
							Image img = new Image(cameraControls.getDisplay(), rawFileName);
							ImageData imageData = img.getImageData();
							img.dispose();
							v.loadImageInUIThread(leftWindowImageViewer,
									imageData.scaledTo(TomoAlignmentView.SCALED_TO_X, TomoAlignmentView.SCALED_TO_Y));
							imageData.data = null;
							imageData = null;
							loadZoomImageInUI();
						}
					} catch (Exception e1) {
						logger.error("Problem with taking saturation", e1);
						throw new InvocationTargetException(e1);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			logger.error("Problem switching off saturation", e);
		} catch (InterruptedException e) {
			logger.error("Problem switching off saturation", e);
		}
	}

	protected void switchOnSaturation(final String rawFileName) {
		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						Image img = new Image(cameraControls.getDisplay(), rawFileName);
						ImageData imageData = img.getImageData();
						img.dispose();
						ImageData id = tomoAlignmentViewController.applySaturation(imageData);
						v.loadImageInUIThread(leftWindowImageViewer,
								id.scaledTo(TomoAlignmentView.SCALED_TO_X, TomoAlignmentView.SCALED_TO_Y));
						loadZoomImageInUI();

					} catch (Exception e1) {
						throw new InvocationTargetException(e1);
					} finally {
						monitor.done();
					}
				}

			});
		} catch (InvocationTargetException e) {
			logger.error("saturation", e);
		} catch (InterruptedException e) {
			logger.error("saturation", e);
		}
	}

	private IRoiPointsListener roiPointsListener = new IRoiPointsListener() {

		@Override
		public void roiPointsChanged(int direction, int x1, int y1, int x2, int y2) {
			IRoiHandler roiHandler = tomoAlignmentViewController.getRoiHandler();
			int leftWindowBinValue = tomoAlignmentViewController.getLeftWindowBinValue();

			// check if left window bin value is not 0 expected to be 4;
			PointList validPoints = roiHandler.validatePoints(direction, x1 * leftWindowBinValue, y1
					* leftWindowBinValue, x2 * leftWindowBinValue, y2 * leftWindowBinValue);

			// check if valid points size is 2
			Point firstPoint = validPoints.getPoint(0);
			validPoints.setPoint(
					new Point(Math.round(firstPoint.x / leftWindowBinValue), Math.round(firstPoint.y
							/ leftWindowBinValue)), 0);

			Point secondPoint = validPoints.getPoint(1);
			validPoints.setPoint(
					new Point(Math.round(secondPoint.x / leftWindowBinValue), Math.round(secondPoint.y
							/ leftWindowBinValue)), 1);

			v.setValidRoi(validPoints);
		}
	};

	@Override
	public void defineRoi(boolean selection) {
		if (selection) {

			if (v.getLeftWindowImageData() == null) {
				throw new IllegalStateException("No image on the left window to define ROI");
			}
			// 1. display the GUI handles on the left window.
			v.enableRoiWidgets();
			v.addLeftWindowImageRoiPointsListener(roiPointsListener);
		} else {
			v.disableRoiWidget();
			v.removeLeftWindowImageRoiPointsListener(roiPointsListener);
		}
	}

	@Override
	public void resetRoi() {
		if (v.getLeftWindowImageData() == null) {
			throw new IllegalStateException("No image on the left window to reset ROI");
		}
		v.resetLeftWindowRoiBounds();
	}

	@Override
	public void moveSampleIn() throws InvocationTargetException, InterruptedException {
		logger.debug("Move sample stage in");
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					tomoAlignmentViewController.moveSampleStageIn(monitor);
				} catch (DeviceException e) {
					cameraControls.selectSampleOut();
					throw new InvocationTargetException(e, "Unable to move sample stage: " + e.getMessage());
				} finally {
					monitor.done();
				}

			}
		});
	}

	@Override
	public void moveSampleOut() throws InvocationTargetException, InterruptedException {
		logger.debug("Move sample stage ");
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					tomoAlignmentViewController.moveSampleStageOut(monitor);
				} catch (DeviceException e) {
					cameraControls.selectSampleOut();
					throw new InvocationTargetException(e, "Unable to move sample stage: " + e.getMessage());
				} finally {
					monitor.done();
				}

			}
		});
	}

}
