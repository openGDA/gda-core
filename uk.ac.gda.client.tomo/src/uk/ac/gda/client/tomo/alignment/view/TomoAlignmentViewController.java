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
import gda.util.Sleep;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.ViewerDisplayMode;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.LEFT_PAGE;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.RIGHT_INFO;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.RIGHT_PAGE;
import uk.ac.gda.client.tomo.alignment.view.handlers.CrossWireMouseListener;
import uk.ac.gda.client.tomo.alignment.view.handlers.CrossWireMouseListener.CrosswireListener;
import uk.ac.gda.client.tomo.alignment.view.handlers.IRoiHandler;
import uk.ac.gda.client.tomo.alignment.view.utils.ScaleDisplay;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite.ProfilePointListener;
import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite.ZoomRectangleListener;
import uk.ac.gda.client.tomo.composites.FullImageComposite;
import uk.ac.gda.client.tomo.composites.FullImageComposite.IRoiPointsListener;
import uk.ac.gda.client.tomo.composites.ITomoAlignmentControlListener;
import uk.ac.gda.client.tomo.composites.ITomoAlignmentLeftPanelListener;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.client.tomo.composites.OverlayImageFigure.MOVE_AXIS;
import uk.ac.gda.client.tomo.composites.OverlayImageFigure.OverlayImgFigureListener;
import uk.ac.gda.client.tomo.composites.SWT2Dutil;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.MotionControlCentring;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.STREAM_STATE;
import uk.ac.gda.client.tomo.composites.TomoPlotComposite.PlottingSystemActionListener;
import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;
import uk.ac.gda.client.tomo.configuration.view.handlers.IScanControllerUpdateListener;
import uk.ac.gda.ui.components.AmplifierStepperComposite.STEPPER;
import uk.ac.gda.ui.components.ColourSliderComposite.IColourSliderListener;

public class TomoAlignmentViewController implements ITomoAlignmentLeftPanelListener, ITomoAlignmentControlListener,
		PlottingSystemActionListener, ZoomRectangleListener, ProfilePointListener, OverlayImgFigureListener,
		IColourSliderListener, IScanControllerUpdateListener {
	private static final String ERR_WHILE_SATURATION = "Error while saturation";
	private static final IWorkbenchWindow ACTIVE_WORKBENCH_WINDOW = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentViewController.class);
	private final TomoAlignmentView tomoAlignmentView;
	private boolean cameraPositionDoNotResetFlag;
	private final static DecimalFormat lblXDecimalFormat = new DecimalFormat("###");
	public static final String BLANK_STR = "";

	public TomoAlignmentViewController(TomoAlignmentView tomoAlignmentView) {
		this.tomoAlignmentView = tomoAlignmentView;
	}

	@Override
	public void moduleChanged(CAMERA_MODULE oldModule, final CAMERA_MODULE newModule) throws InterruptedException,
			InvocationTargetException {
		logger.debug("Module Changed to:" + newModule);

		// start the stream button if not already streaming
		ViewerDisplayMode leftWindowViewerDisplayMode = tomoAlignmentView.getLeftWindowViewerDisplayMode();
		if (ViewerDisplayMode.SAMPLE_STREAM_LIVE != leftWindowViewerDisplayMode) {
			try {
				tomoAlignmentView.getLeftPanelComposite().startStreaming();
			} catch (Exception ex) {
				throw new InvocationTargetException(ex, "Unable to start streaming while module change");
			}
		}

		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor moduleChangeMonitor) throws InvocationTargetException,
						InterruptedException {
					try {
						if (moduleChangeMonitor.isCanceled()) {
							throw new InterruptedException();
						}
						tomoAlignmentView.getTomoAlignmentController().setModule(newModule, moduleChangeMonitor);
						tomoAlignmentView.resetAmplifier();
						tomoAlignmentView.setResolution(RESOLUTION.FULL);
					} catch (InterruptedException ie) {
						throw ie;
					} catch (InvocationTargetException ite) {
						logger.error("Problem changing modules", ite);
						throw ite;
					} catch (Exception e) {
						logger.error("Problem changing modules", e);
						throw new InvocationTargetException(e);
					} finally {
						moduleChangeMonitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			logger.error("Problem Chaning modules:", e);
			tomoAlignmentView.getTomoControlComposite().deselectModule();
			throw e;
		} catch (InterruptedException e) {
			logger.error("Problem Chaning modules:", e);
			tomoAlignmentView.getTomoControlComposite().deselectModule();
			throw e;
		}
	}

	private void centringStarted(MOVE_AXIS axis) throws Exception {
		tomoAlignmentView.getLeftPanelComposite().setZoom(ZOOM_LEVEL.NO_ZOOM);
		tomoAlignmentView.getLeftPanelComposite().deSelectSaturationButton();
		tomoAlignmentView.disableCameraControls();

		try {
			if (!tomoAlignmentView.isStreamingSampleExposure()) {
				tomoAlignmentView.getLeftPanelComposite().startStreaming();

				final int expTimeInSeconds = (int) (tomoAlignmentView.getTomoControlComposite().getSampleExposureTime() * 1000);
				// Sleeping in progress to get the right streamed image in the left window.
				ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						SubMonitor progress = SubMonitor.convert(monitor);
						progress.beginTask("Preparing view for centring", 10);

						progress.subTask("After this dialog closes, please drag the image to the desired location");
						long timeBeforePrep = System.currentTimeMillis();
						for (int i = 0; i < 2; i++) {
							Sleep.sleep((expTimeInSeconds * 3) + 10);
							progress.worked(1);
						}
						logger.debug("Prep time waited for  {} milli seconds",
								(System.currentTimeMillis() - timeBeforePrep));
						progress.done();
						monitor.done();
					}
				});
			}
			tomoAlignmentView.getLeftPanelComposite().stopStream();

			ImageData leftWindowMainImage = tomoAlignmentView.getLeftWindowImageData();
			tomoAlignmentView.getLeftWindowImageViewer().loadOverlayImage(leftWindowMainImage);

			tomoAlignmentView.getLeftWindowImageViewer().addOverlayImageFigureListener(this);
			tomoAlignmentView.getLeftWindowImageViewer().setOverLayImageMoveAxis(axis);
			tomoAlignmentView.getLeftWindowImageViewer().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					tomoAlignmentView.getViewSite().getActionBars().getStatusLineManager()
							.setMessage("Drag the image to the desired position to align");
					tomoAlignmentView.getLeftWindowImageViewer().setFeedbackCursor(SWT.CURSOR_HAND);
				}
			});

		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void xRayEnergyChanged(double xRayEnergy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vertical(boolean selected) throws Exception {
		if (selected) {
			centringStarted(MOVE_AXIS.Y_AXIS);
		} else {
			centringStopped();
			tomoAlignmentView.getTomoAlignmentController().stopMotors();
		}
	}

	@Override
	public void updatePreferredSampleExposureTime() throws Exception {
		try {
			tomoAlignmentView.getTomoControlComposite().setPreferredSampleExposureTime(
					tomoAlignmentView.getTomoAlignmentController().getPreferredSampleExposureTime());
		} catch (Exception e) {
			logger.error("Problem getting exposure time", e);
			throw e;
		}

	}

	@Override
	public void updatePreferredFlatExposureTime() throws Exception {
		try {
			tomoAlignmentView.getTomoControlComposite().setPreferredFlatExposureTime(
					tomoAlignmentView.getTomoAlignmentController().getPreferredFlatExposureTime());
		} catch (Exception e) {
			logger.error("Problem getting exposure time", e);
			throw e;
		}
	}

	@Override
	public void tilt(boolean selected) throws Exception {

		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			tomoAlignmentView.getLeftPanelComposite().setZoom(ZOOM_LEVEL.NO_ZOOM);
			tomoAlignmentView.getLeftPanelComposite().deselectProfileButton();
			logger.debug("Switching off the zoom");
			tomoAlignmentView.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);

			// start the stream button if not already streaming
			ViewerDisplayMode leftWindowViewerDisplayMode = tomoAlignmentView.getLeftWindowViewerDisplayMode();
			if (ViewerDisplayMode.SAMPLE_STREAM_LIVE != leftWindowViewerDisplayMode) {
				tomoAlignmentView.getLeftPanelComposite().startStreaming();
			}
			try {
				ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException {
						final CAMERA_MODULE selectedCameraModule = tomoAlignmentView.getTomoControlComposite()
								.getSelectedCameraModule();
						final double exposureTime = tomoAlignmentView.getTomoControlComposite().getSampleExposureTime();
						SubMonitor progress = SubMonitor.convert(monitor);
						try {
							progress.beginTask("Tilt", 50);
							TiltPlotPointsHolder tiltPoints = tomoAlignmentView.getTomoAlignmentController()
									.doTiltAlignment(progress, selectedCameraModule, exposureTime);
							if (tiltPoints != null) {
								logger.debug("Tilt points: {}", tiltPoints);
								tomoAlignmentView.getTomoPlotComposite().updatePlotPoints(progress, tiltPoints);
							}
						} catch (Exception ex) {
							logger.error("Error while preparing for Tilt alignment", ex);
							throw new InvocationTargetException(ex, "Error while preparing for Tilt alignment:"
									+ ex.getMessage());
						} finally {
							progress.done();
							monitor.done();
						}
					}
				});
			} finally {
				// in order to push the right size to the mjpeg
				tomoAlignmentView.reset();
			}
			try {
				tomoAlignmentView.setRightPage(RIGHT_PAGE.PLOT);
				tomoAlignmentView.getTomoControlComposite().switchOff(MotionControlCentring.TILT);
			} catch (Exception e) {
				logger.error("Problem stopping switching off 'Tilt'", e);
			}
		} else {
			logger.debug("'Tilt' de-selected");
		}

	}

	@Override
	public void setSampleWeight(SAMPLE_WEIGHT sampleWeight) throws Exception {
		logger.debug("Sample weight now is {}", sampleWeight);
		tomoAlignmentView.getTomoAlignmentController().setSampleWeight(sampleWeight);
	}

	@Override
	public void saveAlignmentConfiguration() throws InvocationTargetException, InterruptedException, Exception {
		logger.debug("save alignment configuration");
		tomoAlignmentView.saveConfiguration();
	}

	@Override
	public void sampleFlatTimeChanged() throws Exception {
		try {
			tomoAlignmentView.resetAmplifier();
		} catch (Exception e) {
			logger.error("Problem resetting amplifier dark or flat", e);
			throw e;
		}
	}

	@Override
	public void sampleExposureTimeChanged(double sampleExposureTime) throws Exception {
		try {
			tomoAlignmentView.resetAmplifier();
			tomoAlignmentView.getTomoAlignmentController().setPreferredSampleExposureTime(sampleExposureTime);
			if (STREAM_STATE.SAMPLE_STREAM.equals(tomoAlignmentView.getTomoControlComposite().getStreamState())) {
				tomoAlignmentView.getTomoAlignmentController().setExposureTime(sampleExposureTime, 1);
			}
		} catch (Exception e) {
			logger.error("Problem resetting amplifier dark or flat", e);
			throw e;
		}
	}

	@Override
	public void sampleDescriptionChanged(String sampleDescription) {
		logger.debug("The sample description has changed to: {}", sampleDescription);
	}

	@Override
	public void rotateRight90() throws Exception {
		logger.debug("Rotating the motor right by 90°");
		moveRotationMotor(90);
	}

	@Override
	public void rotateLeft90() throws Exception {
		logger.debug("Rotating the motor left by 90°");
		moveRotationMotor(-90);
	}

	@Override
	public void resetXrayEnergy() {
		// Do nothing for the moment
	}

	@Override
	public void resetRoi() {
		if (tomoAlignmentView.getLeftWindowImageData() == null) {
			throw new IllegalStateException("No image on the left window to reset ROI");
		}
		tomoAlignmentView.resetLeftWindowRoiBounds();
	}

	@Override
	public void resetCameraDistance() throws Exception {
		if (!cameraPositionDoNotResetFlag) {
			Double cameraMotionPosition = tomoAlignmentView.getTomoAlignmentController().getCameraMotionMotorPosition();
			if (cameraMotionPosition != null) {
				tomoAlignmentView.getTomoControlComposite().setCameraMotionPosition(cameraMotionPosition);
			}
		}
	}

	@Override
	public void moveAxisOfRotation(boolean selected) throws Exception {
		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		FullImageComposite leftWindowImageViewer = tomoAlignmentView.getLeftWindowImageViewer();
		if (selected) {
			tomoAlignmentView.disableCameraControls();
			final int imageCentre = leftWindowImageViewer.getImageCenterX();// already accounted
																			// for offset

			leftWindowImageViewer.getCrossWire1Vertical().setCursor(
					leftWindowImageViewer.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
			leftWindowImageViewer.getCrossWire1Vertical().addMouseListener(crossWireMouseListener);
			leftWindowImageViewer.getCrossWire1Vertical().addMouseMotionListener(crossWireMouseListener);
			crossWireMouseListener.setImageCentre(imageCentre);
			crossWireMouseListener.setMin(leftWindowImageViewer.getImageBounds().x);
			crossWireMouseListener.setMax(leftWindowImageViewer.getImageBounds().x
					+ leftWindowImageViewer.getImageBounds().width);
			crossWireMouseListener.addCrossWireListener(crossWireListener);

		} else {
			tomoAlignmentView.enableLeftPanelControls();
			crossWireMouseListener.removeCrossWireListener(crossWireListener);
			leftWindowImageViewer.getCrossWire1Vertical().setCursor(
					Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
			leftWindowImageViewer.getCrossWire1Vertical().removeMouseListener(crossWireMouseListener);
			leftWindowImageViewer.getCrossWire1Vertical().removeMouseMotionListener(crossWireMouseListener);
		}
	}

	@Override
	public void horizontal(boolean selected) throws Exception {
		if (tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			centringStarted(MOVE_AXIS.X_AXIS);
		} else {
			tomoAlignmentView.getTomoAlignmentController().stopMotors();
			centringStopped();
		}

	}

	@Override
	public void flatExposureTimeChanged(double flatExposureTime) throws Exception {
		tomoAlignmentView.resetAmplifier();
		tomoAlignmentView.getTomoAlignmentController().setPreferredFlatExposureTime(flatExposureTime);
		if (STREAM_STATE.FLAT_STREAM.equals(tomoAlignmentView.getTomoControlComposite().getStreamState())) {
			tomoAlignmentView.getTomoAlignmentController().setExposureTime(flatExposureTime, 1);
		}
	}

	@Override
	public void findRotationAxis(boolean selected) throws Exception {

		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			tomoAlignmentView.getLeftPanelComposite().setZoom(ZOOM_LEVEL.NO_ZOOM);
			tomoAlignmentView.getLeftPanelComposite().deSelectSaturationButton();
			tomoAlignmentView.disableCameraControls();

			tomoAlignmentView.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
			tomoAlignmentView.getLeftPanelComposite().startStreaming();
			final double steppedAcqTime = tomoAlignmentView.getTomoControlComposite().getSampleExposureTime();

			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Image img = null;
					String secondImageLocation = null;
					String firstImageLocation = null;
					try {
						try {
							SubMonitor progress = SubMonitor.convert(monitor);
							progress.beginTask("Preparing to find axis of Tomo rotation", 10);

							tomoAlignmentView.getTomoAlignmentController().moveRotationMotorBy(progress.newChild(1),
									-180);
							firstImageLocation = tomoAlignmentView.getTomoAlignmentController().demandRaw(
									progress.newChild(2), steppedAcqTime, false);

							tomoAlignmentView.getTomoAlignmentController().moveRotationMotorBy(progress.newChild(1),
									180);

							secondImageLocation = tomoAlignmentView.getTomoAlignmentController().demandRaw(
									progress.newChild(1), steppedAcqTime, false);

						} catch (Exception ex) {
							logger.debug("Exception while finding half rotation", ex);
							tomoAlignmentView.switchOffCentring(MotionControlCentring.FIND_AXIS_ROTATION);
							throw new InvocationTargetException(ex, "Exception while finding half rotation:"
									+ ex.getMessage());
						}
						try {
							// Loading the first image
							img = new Image(tomoAlignmentView.getLeftWindowImageViewer().getDisplay(),
									firstImageLocation);
							ImageData firstImgData = img.getImageData();
							img.dispose();

							// Loading the second image
							img = new Image(tomoAlignmentView.getLeftWindowImageViewer().getDisplay(),
									secondImageLocation);
							ImageData secondImgData = img.getImageData();
							img.dispose();

							tomoAlignmentView.loadImageInUIThread(tomoAlignmentView.getLeftWindowImageViewer(),
									secondImgData.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(),
											tomoAlignmentView.getTomoAlignmentController().getScaledX()));

							ImageData horizontallyFlippedImageData = SWT2Dutil.flip(firstImgData, false);
							loadOverlayImgInUIThread(horizontallyFlippedImageData.scaledTo(tomoAlignmentView
									.getTomoAlignmentController().getScaledX(), tomoAlignmentView
									.getTomoAlignmentController().getScaledX()));
							tomoAlignmentView.getLeftWindowImageViewer().addOverlayImageFigureListener(
									TomoAlignmentViewController.this);
							tomoAlignmentView.getLeftWindowImageViewer().setOverLayImageMoveAxis(MOVE_AXIS.X_AXIS);
							tomoAlignmentView.getLeftWindowImageViewer().getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									tomoAlignmentView.getLeftWindowImageViewer().setFeedbackCursor(SWT.CURSOR_HAND);
								}
							});
						} catch (Exception ex) {
							logger.debug("Exception while finding half rotation", ex);
							tomoAlignmentView.switchOffCentring(MotionControlCentring.FIND_AXIS_ROTATION);
							throw new InvocationTargetException(ex,
									"Exception while finding half rotation:Problem loading images.");
						}
					} finally {
						monitor.done();
					}
				}
			});
		} else {
			centringStopped();
			tomoAlignmentView.getTomoAlignmentController().stopMotors();
		}

	}

	private void loadOverlayImgInUIThread(final ImageData image) {
		if (tomoAlignmentView.getLeftWindowImageViewer().getDisplay() != null) {
			tomoAlignmentView.getLeftWindowImageViewer().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						tomoAlignmentView.getLeftWindowImageViewer().loadOverlayImage(image);
					} catch (Exception ex) {
						logger.error("Error loading image :{}", ex);
						tomoAlignmentView.loadErrorInDisplay("Error loading image", ex.getMessage());
					}
				}
			});
		}
	}

	@Override
	public void degreeMovedTo(final double degree) throws Exception {
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				try {
					SubMonitor progress = SubMonitor.convert(monitor);
					tomoAlignmentView.getTomoAlignmentController().moveRotationMotorTo(progress.newChild(1), degree);
					rotationCompleted();
				} catch (InterruptedException iex) {
					logger.error("User stopped motor");
					rotationCompleted();
				} catch (DeviceException e) {
					logger.error("error moving rotation motor", e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		});

	}

	@Override
	public void degreeMovedBy(double degree) throws Exception {
		moveRotationMotor(degree);
	}

	@Override
	public void defineRoi(boolean selection) {
		if (selection) {

			if (tomoAlignmentView.getLeftWindowImageData() == null) {
				throw new IllegalStateException("No image on the left window to define ROI");
			}
			// 1. display the GUI handles on the left window.
			tomoAlignmentView.enableRoiWidgets();
			tomoAlignmentView.addLeftWindowImageRoiPointsListener(roiPointsListener);
		} else {
			tomoAlignmentView.disableRoiWidget();
			tomoAlignmentView.removeLeftWindowImageRoiPointsListener(roiPointsListener);
		}

	}

	@Override
	public void cameraDistanceChanged(final double cameraDistance) throws InvocationTargetException,
			InterruptedException, Exception {

		logger.debug("Camera distance changed to {}", cameraDistance);
		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		cameraPositionDoNotResetFlag = true;
		final CAMERA_MODULE module = tomoAlignmentView.getTomoControlComposite().getSelectedCameraModule();
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				SubMonitor progress = SubMonitor.convert(monitor);
				try {
					tomoAlignmentView.getTomoAlignmentController().moveCameraMotion(progress, module, cameraDistance);
				} catch (DeviceException e) {
					cameraPositionDoNotResetFlag = false;
					try {
						resetCameraDistance();
					} catch (Exception ex) {
						logger.error("TODO put description of error here", e);
						throw new InvocationTargetException(e, "Unable to get camera position");
					}
					throw new InvocationTargetException(e, "Unable to move to requested camera position");
				} finally {
					monitor.done();
				}
			}
		});

		Double cameraMotionPosition = tomoAlignmentView.getTomoAlignmentController().getCameraMotionMotorPosition();
		if (cameraMotionPosition != null) {
			tomoAlignmentView.getTomoControlComposite().setCameraMotionPosition(cameraMotionPosition);
		}

		cameraPositionDoNotResetFlag = false;

	}

	@Override
	public void autoFocus(boolean selected) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void takeFlatAndDark() throws InterruptedException, InvocationTargetException {
		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		final double expTime = tomoAlignmentView.getTomoControlComposite().getFlatExposureTime();
		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						SubMonitor progress = SubMonitor.convert(monitor);
						tomoAlignmentView.getLeftPanelComposite().stopStream();
						progress.beginTask("Taking dark images", 20);
						progress.worked(1);
						try {
							tomoAlignmentView.getTomoAlignmentController().takeDark(progress.newChild(9), expTime);
						} catch (Exception e) {
							throw new InvocationTargetException(e, "Problem taking flat and dark images");
						}

						if (!progress.isCanceled()) {
							// Taking flat images
							progress.setTaskName("Taking Flat images");
							tomoAlignmentView.getLeftPanelComposite().stopStream();
							progress.worked(1);
							int numFlat = tomoAlignmentView.getTomoAlignmentController().getPreferredNumberFlatImages();
							// Just so that the recursive average is for a minimum of 2 images. it is better to do it
							// this way rather than
							// limit the user to enter numbers greater than or equal to 2
							if (numFlat == 1) {
								numFlat = numFlat + 1;
							}
							tomoAlignmentView.getTomoAlignmentController().takeFlat(progress.newChild(9), numFlat,
									expTime);
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

			tomoAlignmentView.getLeftPanelComposite().setFlatCaptured(true,
					tomoAlignmentView.getTomoControlComposite().getFlatExposureTime());
		} catch (InvocationTargetException e) {
			logger.error("Error in takeflat", e);
			tomoAlignmentView.getLeftPanelComposite().setFlatCaptured(false, Double.NaN);
			throw e;
		} catch (InterruptedException e) {
			logger.error("Error in takeflat", e);
			tomoAlignmentView.getLeftPanelComposite().setFlatCaptured(false, Double.NaN);
			throw e;
		}
	}

	@Override
	public void stream(boolean selected) throws Exception {
		if (selected) {
			tomoAlignmentView.getLeftPanelComposite().stopHistogram();
			tomoAlignmentView.getLeftPanelComposite().deselectStream();
			if (!STREAM_STATE.NO_STREAM.equals(tomoAlignmentView.getTomoControlComposite().getStreamState())) {
				if (tomoAlignmentView.getTomoControlComposite().getSampleExposureTime() != tomoAlignmentView
						.getTomoControlComposite().getFlatExposureTime()) {
					setExposureTime(tomoAlignmentView.getTomoControlComposite().getSampleExposureTime());
					tomoAlignmentView.getTomoControlComposite().setStreamState(STREAM_STATE.SAMPLE_STREAM);
					tomoAlignmentView.setLeftWindowInfo(TomoAlignmentView.SAMPLE_LIVE_STREAM);
				}
			} else {
				final double steppedAcqTime = tomoAlignmentView.getTomoControlComposite().getSampleExposureTime();
				stream(selected, steppedAcqTime, TomoAlignmentView.SAMPLE_LIVE_STREAM);
			}
		} else {
			tomoAlignmentView.getLeftPanelComposite().deselectStream();
			tomoAlignmentView.getLeftPanelComposite().stopHistogram();
			stream(selected, Double.NaN, TomoAlignmentView.STREAM_STOPPED);
		}
	}

	private void stream(boolean selected, final double exposureTime, final String displayMsg)
			throws InvocationTargetException {
		if (selected) {
			tomoAlignmentView.startStreaming(exposureTime);
			tomoAlignmentView.setLeftWindowInfo(displayMsg);
		} else {
			tomoAlignmentView.stopStreaming();
		}
	}

	@Override
	public void single(boolean isFlatCorrectionRequired) throws InvocationTargetException, Exception {
		logger.debug("Sample Single Called");
		tomoAlignmentView.stopProfiling();
		tomoAlignmentView.getLeftPanelComposite().stopHistogram();

		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		tomoAlignmentView.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		final double acqTime = tomoAlignmentView.getTomoControlComposite().getSampleExposureTime();
		final boolean isStreaming = tomoAlignmentView.isStreamingSampleExposure();

		try {

			tomoAlignmentView.getLeftPanelComposite().deSelectSaturationButton();
			// TODO - Ravi fix

			final RunnableWithProgress sampleSingleRunnable = new RunnableWithProgress(acqTime, true, isStreaming,
					false);

			ACTIVE_WORKBENCH_WINDOW.run(true, true, sampleSingleRunnable);

			ViewerDisplayMode viewDisplayMode = tomoAlignmentView.getLeftWindowViewerDisplayMode();
			tomoAlignmentView.updateScaleBars(tomoAlignmentView.getSelectedCameraModule());

			if (viewDisplayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
				tomoAlignmentView.displayFileDetails(viewDisplayMode);
			}
		} catch (InvocationTargetException e) {
			logger.error("demandRaw", e);
			throw e;
		} catch (InterruptedException e) {
			logger.error("demandRaw", e);
		} catch (Exception e) {
			logger.error("Exception while taking single ", e);
			throw e;
		}

	}

	@Override
	public void showFlat() throws Exception {
		// Stop streaming when showing 'Show Flat' is called.
		tomoAlignmentView.getLeftPanelComposite().deSelectSaturationButton();
		tomoAlignmentView.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		tomoAlignmentView.getLeftPanelComposite().stopStream();

		String flatImageFileName = tomoAlignmentView.getTomoAlignmentController().getFlatImageFullFileName();
		if (flatImageFileName != null) {
			File checkFile = new File(flatImageFileName);
			if (checkFile.exists()) {
				try {
					Image img = new Image(tomoAlignmentView.getLeftWindowImageViewer().getDisplay(), flatImageFileName);
					ImageData imgData = img.getImageData();
					tomoAlignmentView.getLeftWindowImageViewer().loadMainImage(
							imgData.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(),
									tomoAlignmentView.getTomoAlignmentController().getScaledX()));
					img.dispose();
					tomoAlignmentView.displayFileDetails(ViewerDisplayMode.STATIC_FLAT);
				} catch (Exception e) {
					throw e;
				} finally {
					logger.debug("Loaded Flat image File Name timestamp ");
					tomoAlignmentView.setLeftWindowInfo(TomoAlignmentView.FLAT_SINGLE);
				}
			}
		}

	}

	@Override
	public void showDark() throws Exception {
		// Stop streaming when showing 'Show Flat' is called.
		tomoAlignmentView.getLeftPanelComposite().deSelectSaturationButton();
		tomoAlignmentView.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
		tomoAlignmentView.getLeftPanelComposite().stopStream();

		String darkImageFileName = tomoAlignmentView.getTomoAlignmentController().getDarkFieldImageFullFileName();
		if (darkImageFileName != null) {
			File checkFile = new File(darkImageFileName);
			if (checkFile.exists()) {
				try {
					Image img = new Image(tomoAlignmentView.getLeftWindowImageViewer().getDisplay(), darkImageFileName);
					ImageData imgData = img.getImageData();
					tomoAlignmentView.getLeftWindowImageViewer().loadMainImage(
							imgData.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(),
									tomoAlignmentView.getTomoAlignmentController().getScaledX()));
					img.dispose();
					tomoAlignmentView.displayFileDetails(ViewerDisplayMode.DARK_SINGLE);
				} catch (Exception e) {
					// lblFileName.setText(TomoAlignmentView.BLANK_STR);
					// lblFileTimeStamp.setText(TomoAlignmentView.BLANK_STR);
					throw e;
				} finally {
					logger.debug("Loaded Flat image File Name timestamp ");
					tomoAlignmentView.setLeftWindowInfo(TomoAlignmentView.STATIC_DARK);
				}
			}
		}
	}

	@Override
	public void saturation(boolean selected) throws IllegalArgumentException {
		if (!tomoAlignmentView.isModuleSelected()) {
			throw new java.lang.IllegalStateException("Module should be selected");
		}
		if (selected) {
			logger.debug("Saturation switched on: Saturation only applicable for Demand Raw images");
			ViewerDisplayMode staticSingleEnum = tomoAlignmentView.getLeftWindowViewerDisplayMode();

			if (staticSingleEnum != ViewerDisplayMode.SAMPLE_SINGLE) {
				tomoAlignmentView.loadErrorInDisplay("Error saturation", "Error saturation");
				tomoAlignmentView.getLeftPanelComposite().saturationOff();
				return;
			}

			try {
				final String rawFileName = staticSingleEnum.getFileName(tomoAlignmentView.getTomoAlignmentController());
				switchOnSaturation(rawFileName);
			} catch (Exception e) {
				logger.error("saturation", e);
			}
		} else {
			try {
				ViewerDisplayMode staticSingleEnum = tomoAlignmentView.getLeftWindowViewerDisplayMode();
				if (staticSingleEnum != null) {
					final String rawFileName = staticSingleEnum.getFileName(tomoAlignmentView
							.getTomoAlignmentController());
					switchOffSaturation(rawFileName);
				}
				logger.debug("Saturation switched off");
			} catch (Exception e) {
				logger.error("saturation", e);
			}
		}

	}

	@Override
	public void profile(boolean selected) throws Exception {
		if (selected) {
			logger.debug("'Profile' is selected");
			tomoAlignmentView.setRightPage(RIGHT_PAGE.PLOT);
			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						final Exception[] es = new Exception[1];
						tomoAlignmentView.getLeftPanelComposite().getDisplay().syncExec(new Runnable() {

							@Override
							public void run() {

								try {
									tomoAlignmentView.getLeftPanelComposite().startSingle();
								} catch (Exception e) {
									logger.error("TODO put description of error here", e);
									es[0] = e;
								}
							}
						});
						if (es[0] != null) {
							throw es[0];
						}

						// According to the requirement only RAW images need to be profiled.
						ViewerDisplayMode staticSingleEnum = tomoAlignmentView.getLeftWindowViewerDisplayMode();

						if (ViewerDisplayMode.SAMPLE_SINGLE == staticSingleEnum) {
							String rawFileName = null;
							rawFileName = staticSingleEnum.getFileName(tomoAlignmentView.getTomoAlignmentController());

							String darkImgFileName = null;
							if (tomoAlignmentView.getTomoAlignmentController().isDarkImageSaved()) {
								darkImgFileName = tomoAlignmentView.getTomoAlignmentController().getDarkImageFileName();
							}
							tomoAlignmentView.tomoPlotComposite.setImagesToPlot(rawFileName, darkImgFileName);

							tomoAlignmentView.updatePlots(monitor, 0, 4008, 1);
							monitor.done();

						}
					} catch (Exception e1) {
						logger.error("getting raw file problem.", e1);
						throw new InvocationTargetException(e1, "Problem starting to profile");
					}
				}

			});
			tomoAlignmentView.getLeftWindowImageViewer().showLineProfiler();
		} else {
			logger.debug("'Profile' is de-selected");
			tomoAlignmentView.stopProfiling();
		}
	}

	@Override
	public void moveSampleOut() throws InvocationTargetException, InterruptedException {
		logger.debug("Move sample stage ");
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					tomoAlignmentView.getTomoAlignmentController().moveSampleStageOut(monitor);
				} catch (DeviceException e) {
					tomoAlignmentView.getLeftPanelComposite().selectSampleOutButton();
					throw new InvocationTargetException(e, "Unable to move sample stage: " + e.getMessage());
				} finally {
					monitor.done();
				}

			}
		});
	}

	@Override
	public void moveSampleIn() throws InvocationTargetException, InterruptedException {
		logger.debug("Move sample stage in");
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					tomoAlignmentView.getTomoAlignmentController().moveSampleStageIn(monitor);
				} catch (DeviceException e) {
					tomoAlignmentView.getLeftPanelComposite().selectSampleOutButton();
					throw new InvocationTargetException(e, "Unable to move sample stage: " + e.getMessage());
				} finally {
					monitor.done();
				}

			}
		});

	}

	@Override
	public void histogram(boolean selection) throws Exception {
		logger.debug("sample histogram selected {}", selection);
		double sampleExposureTime = tomoAlignmentView.getTomoControlComposite().getSampleExposureTime();

		ViewerDisplayMode leftWindowViewerDisplayMode = tomoAlignmentView.getLeftWindowViewerDisplayMode();
		if (selection) {
			// show the plot view
			if (ViewerDisplayMode.SAMPLE_STREAM_LIVE.equals(leftWindowViewerDisplayMode)) {

				tomoAlignmentView.getLeftPanelComposite().setZoom(ZOOM_LEVEL.NO_ZOOM);
				tomoAlignmentView.getLeftPanelComposite().stopHistogram();
				tomoAlignmentView.getLeftPanelComposite().deselectStream();
				//
				tomoAlignmentView.resetAmplifier();
				tomoAlignmentView.getLeftPanelComposite().deSelectSaturationButton();

				double cameraExposureTime = tomoAlignmentView.getTomoAlignmentController().getCameraExposureTime();
				if (cameraExposureTime != sampleExposureTime) {
					tomoAlignmentView.getTomoAlignmentController().setAmplifierUpdate(sampleExposureTime, 1);
				}

				tomoAlignmentView.setRightPage(RIGHT_PAGE.PLOT);
				tomoAlignmentView.addLeftWindowTomoImageListener();
				tomoAlignmentView.setRightInfoPage(RIGHT_INFO.HISTOGRAM);
			} else if (ViewerDisplayMode.SAMPLE_SINGLE.equals(leftWindowViewerDisplayMode)) {
				String fileName = leftWindowViewerDisplayMode.getFileName(tomoAlignmentView
						.getTomoAlignmentController());
				tomoAlignmentView.getLeftPanelComposite().setZoom(ZOOM_LEVEL.NO_ZOOM);
				tomoAlignmentView.getLeftPanelComposite().stopHistogram();
				tomoAlignmentView.getTomoPlotComposite().updateHistogramData(
						tomoAlignmentView.getLeftWindowViewerDisplayMode(), new ImageData(fileName));
				tomoAlignmentView.setRightPage(RIGHT_PAGE.PLOT);
				tomoAlignmentView.setRightInfoPage(RIGHT_INFO.NONE);
			} else {
				MessageDialog.openError(tomoAlignmentView.getLeftPanelComposite().getShell(),
						"Histogram cannot be displayed", "Histogram can only be displayed for Sample Stream or Single");
				tomoAlignmentView.getLeftPanelComposite().stopHistogram();
				tomoAlignmentView.setRightInfoPage(RIGHT_INFO.NONE);
			}
		} else {
			tomoAlignmentView.removeLeftWindowTomoImageListener();
			tomoAlignmentView.setRightPage(RIGHT_PAGE.NONE);
			tomoAlignmentView.setRightInfoPage(RIGHT_INFO.NONE);
			if (ViewerDisplayMode.SAMPLE_STREAM_LIVE.equals(leftWindowViewerDisplayMode)) {
				tomoAlignmentView.getTomoAlignmentController().setProc1ScaleValue(1);
			}
		}
	}

	@Override
	public void fastPreview(boolean isFlatCorrectionRequired) throws InvocationTargetException, Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void crosshair(boolean selection) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void correctFlatAndDark(boolean selected) throws Exception {
		if (selected) {
			try {
				tomoAlignmentView.getTomoAlignmentController().enableFlatCorrection();
				tomoAlignmentView.getTomoAlignmentController().enableDarkSubtraction();
			} catch (Exception e) {
				logger.error("error enabling flat correction", e);
				throw e;
			}
		} else {
			try {
				tomoAlignmentView.getTomoAlignmentController().disableFlatCorrection();
				tomoAlignmentView.getTomoAlignmentController().disableDarkSubtraction();
			} catch (Exception e) {
				logger.error("error enabling flat correction", e);
				throw e;
			}
		}

	}

	private void moveRotationMotor(final double deg) throws Exception {
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					tomoAlignmentView.getTomoAlignmentController().moveRotationMotorBy(monitor, deg);
					// this will be returned only after the rotation is complete
					rotationCompleted();
				} catch (InterruptedException ex) {
					logger.error("User stopped motor");
					resetSlider();
				} catch (DeviceException e) {
					logger.error("error moving rotation motor", e);
					// Attempting to reset the motor position
					resetSlider();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}

			protected void resetSlider() {
				if (!tomoAlignmentView.getTomoControlComposite().isDisposed()) {
					tomoAlignmentView.getTomoControlComposite().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								tomoAlignmentView.getTomoControlComposite().moveRotationSliderTo(
										tomoAlignmentView.getTomoAlignmentController().getRotationMotorDeg());
								tomoAlignmentView.getTomoControlComposite().showRotationButtonsDeselected();
							} catch (DeviceException e1) {
								logger.error("Problem with move rotation slider", e1);
							}
						}
					});

				}
			}
		});
	}

	/**
	 * 
	 */
	private void rotationCompleted() {
		if (tomoAlignmentView.getTomoControlComposite() != null
				&& !tomoAlignmentView.getTomoControlComposite().isDisposed()) {
			tomoAlignmentView.getTomoControlComposite().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					tomoAlignmentView.getTomoControlComposite().showRotationButtonsDeselected();
				}
			});
		}
	}

	/**
	 * Mouse movement listener
	 */
	private CrossWireMouseListener crossWireMouseListener = new CrossWireMouseListener();

	private void centringStopped() throws Exception {
		tomoAlignmentView.enableLeftPanelControls();
		tomoAlignmentView.getLeftWindowImageViewer().removeOverlayImage();
		tomoAlignmentView.getLeftWindowImageViewer().resetFeedbackCursor();
		tomoAlignmentView.getViewSite().getActionBars().getStatusLineManager().setMessage(null);
	}

	/**
	 * Listener to do action once the cross-wire has completed moving.
	 */
	private CrosswireListener crossWireListener = new CrosswireListener() {

		@Override
		public void performAction(final int pixelMoved) {
			final CAMERA_MODULE selectedCameraModule = tomoAlignmentView.getTomoControlComposite()
					.getSelectedCameraModule();
			logger.debug("Pixels moved {}", pixelMoved);
			try {
				ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							tomoAlignmentView.getTomoAlignmentController().moveAxisOfRotation(monitor,
									selectedCameraModule, pixelMoved);

						} catch (Exception e) {
							throw new InvocationTargetException(e, e.getMessage());
						} finally {
							monitor.done();
						}
					}
				});
			} catch (InvocationTargetException e) {
				logger.error("Problem moving axis of rotation", e);
				tomoAlignmentView.loadErrorInDisplay("Problem moving axis of rotation", e.getMessage());
			} catch (InterruptedException e) {
				logger.error("Problem moving axis of rotation", e);
			} finally {
				tomoAlignmentView.enableLeftPanelControls();
				try {
					tomoAlignmentView.getTomoControlComposite().switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
				} catch (Exception e) {
					tomoAlignmentView.loadErrorInDisplay("Cannot reset Motion control buttons",
							"Cannot reset Motion control buttons");
				}
			}
		}
	};

	@Override
	public void zoomButtonClicked(ZOOM_LEVEL zoomLevel) throws Exception {

		if (!tomoAlignmentView.isModuleSelected()) {
			throw new IllegalArgumentException("Module should be selected");
		}
		if (ZOOM_LEVEL.NO_ZOOM.equals(zoomLevel)) {
			tomoAlignmentView.stopZoomVideoReceiver();
			tomoAlignmentView.clearZoomWindow();
			tomoAlignmentView.unZoomInUI();

		} else {
			ViewerDisplayMode viewerDisplayMode = tomoAlignmentView.getLeftWindowViewerDisplayMode();
			if (!ViewerDisplayMode.STREAM_STOPPED.equals(viewerDisplayMode)) {
				CAMERA_MODULE selectedCameraModule = tomoAlignmentView.getSelectedCameraModule();
				ZOOM_LEVEL selectedZoomLevel = tomoAlignmentView.getLeftPanelComposite().getSelectedZoomLevel();
				tomoAlignmentView.getLeftPanelComposite().stopHistogram();
				switch (viewerDisplayMode) {
				case SAMPLE_STREAM_LIVE:
				case FLAT_STREAM_LIVE:
					tomoAlignmentView.setRightPage(RIGHT_PAGE.ZOOM_STREAM);
					tomoAlignmentView.getLeftWindowImageViewer().showZoomRectangleFigure(zoomLevel.getRectSize());
					try {
						tomoAlignmentView.getTomoAlignmentController().handleZoom(zoomLevel,
								tomoAlignmentView.getLeftWindowImageViewer().getZoomFigureBounds());
					} catch (Exception e) {
						logger.error("Problem handling zoom", e);
						throw e;
					}

					ScaleDisplay rightBarLengthInPixel = tomoAlignmentView.getTomoAlignmentController()
							.getRightBarLengthInPixel(TomoAlignmentView.RIGHT_WINDOW_WIDTH / 2, selectedCameraModule,
									zoomLevel);
					if (rightBarLengthInPixel != null) {
						tomoAlignmentView.setRightWindowInfoNumPixels(rightBarLengthInPixel.toString());
						try {
							tomoAlignmentView.setRightScaleBarWidth(rightBarLengthInPixel.getBarLengthInPixel());
						} catch (Exception e) {
							logger.error("right scale bar problem", e);
							throw e;
						}
					}
					tomoAlignmentView.startZoomVideoReceiver();
					tomoAlignmentView.clearZoomWindow();
					break;
				default:
					// if PROFILE is ON
					if (tomoAlignmentView.getLeftPanelComposite().isProfileSelected()) {
						tomoAlignmentView.getLeftWindowImageViewer().showZoomRectangleFigure(zoomLevel.getRectSize());
						Rectangle zoomFigureBounds = tomoAlignmentView.getLeftWindowImageViewer().getZoomFigureBounds();
						Rectangle lineBounds = tomoAlignmentView.getLeftWindowImageViewer().getProfilerLineBounds();

						if (zoomFigureBounds.intersects(lineBounds)) {
							Rectangle zoomFigureBoundsCopy = zoomFigureBounds.getCopy();
							Rectangle intersect = zoomFigureBoundsCopy.intersect(lineBounds);

							tomoAlignmentView.updatePlots(new NullProgressMonitor(), intersect.x, intersect.x
									+ (intersect.width * tomoAlignmentView.getTomoAlignmentController()
											.getLeftWindowBinValue()), intersect.y);
						} else {
							tomoAlignmentView.updatePlots(new NullProgressMonitor(), 0, 4008, lineBounds.y);
						}
					} else {
						// If profile is switched OFF
						tomoAlignmentView.setRightPage(RIGHT_PAGE.ZOOM_DEMAND_RAW);
						// In the case of 'Demand Raw' need to sub-image the 'hdf5' to display in the zoom window.
						String tiffFullFileName = null;
						try {
							tiffFullFileName = viewerDisplayMode.getFileName(tomoAlignmentView
									.getTomoAlignmentController());
						} catch (Exception e) {
							logger.error("problem retrieving the tiff full file name", e);
							throw e;
						}
						if (tiffFullFileName != null) {
							tomoAlignmentView.getLeftWindowImageViewer().showZoomRectangleFigure(
									zoomLevel.getRectSize());
							// Based on SATURATION display the image.
							if (tomoAlignmentView.getLeftPanelComposite().isSaturationSelected()) {
								Image image = new Image(tomoAlignmentView.getLeftWindowImageViewer().getDisplay(),
										tiffFullFileName);
								ImageData appliedSaturation = tomoAlignmentView.getTomoAlignmentController()
										.applySaturation(image.getImageData());
								image.dispose();
								tomoAlignmentView.loadDemandRawZoom(appliedSaturation, zoomLevel.getDemandRawScale(),
										true);
							} else {
								tomoAlignmentView.loadDemandRawZoom(tiffFullFileName, zoomLevel.getDemandRawScale(),
										true);
							}
						} else {
							tomoAlignmentView.clearZoomWindow();
						}

						try {

							rightBarLengthInPixel = tomoAlignmentView.getTomoAlignmentController()
									.getRightBarLengthInPixel(TomoAlignmentView.RIGHT_WINDOW_WIDTH / 2,
											selectedCameraModule, selectedZoomLevel);
							tomoAlignmentView.updateRightWindowNumPixelsLabel(rightBarLengthInPixel.toString(),
									rightBarLengthInPixel.getBarLengthInPixel());
						} catch (Exception e) {
							logger.error("Problem updating right scale bar", e);
							throw e;
						}
					}
				}
			} else {
				MessageDialog.openError(tomoAlignmentView.getLeftPanelComposite().getShell(), "Zoom cannot be enabled",
						"Zoom can be enabled only when Live Stream or Single Image is displayed in the left window");
				tomoAlignmentView.getLeftPanelComposite().setZoom(ZOOM_LEVEL.NO_ZOOM);
			}
		}

	}

	private void loadZoomImageInUI() {
		tomoAlignmentView.getLeftWindowImageViewer().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					zoomButtonClicked(tomoAlignmentView.getLeftPanelComposite().getSelectedZoomLevel());
				} catch (IllegalArgumentException e) {
					logger.error("loadZoomImageInUI", e);
				} catch (Exception e) {
					logger.error("Problem loading zoomed image", e);
				}
			}
		});
	}

	public void setExposureTime(double exposureTime) throws Exception {
		try {
			STEPPER selectedStepper = tomoAlignmentView.getSelectedAmplifierStepper();
			tomoAlignmentView.getTomoAlignmentController().setExposureTime(exposureTime, selectedStepper.getValue());
		} catch (Exception e) {
			logger.error("Cannot set exposure time", e);
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
									tomoAlignmentView.getTomoAlignmentController().stopDemandRaw();
								} catch (Exception e) {
									logger.error("Problem stopping sample single");
									// MessageDialog.openError(tomoAlignmentView.getLeftPanelComposite().getShell(),
									// "User Stopped Operation",
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
				progress.beginTask("Taking single", 10);
				String fileLocation = null;

				// Special case of taking single - when the amplifier is at "1" and the stream is switched
				// on. This is to prevent the arming/disarming of the camera in order to save a few seconds
				if (isAmplifierAtOne && isStreaming) {
					fileLocation = tomoAlignmentView.getTomoAlignmentController().demandRawWithStreamOn(
							progress.newChild(2), flatCorrectionSelected);
					tomoAlignmentView.getLeftPanelComposite().stopStream();
				} else {
					tomoAlignmentView.getLeftPanelComposite().stopStream();
					//
					try {
						fileLocation = tomoAlignmentView.getTomoAlignmentController().demandRaw(progress.newChild(2),
								acqTime, flatCorrectionSelected);
					} catch (Exception e) {
						logger.error("problem while demand raw", e);
						throw new InvocationTargetException(e);
					}

				}
				//
				if (!baseMonitor.isCanceled() && fileLocation != null) {
					Image img = new Image(tomoAlignmentView.getLeftWindowImageViewer().getDisplay(), fileLocation);
					ImageData imgData = img.getImageData();

					logger.debug(String.format("demandRaw() imageData depth#%d  palete is direct# %s", imgData.depth,
							imgData.palette.isDirect));
					tomoAlignmentView.loadImageInUIThread(tomoAlignmentView.getLeftWindowImageViewer(), imgData
							.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(), tomoAlignmentView
									.getTomoAlignmentController().getScaledX()));
					tomoAlignmentView.setHistogramAdjusterImageData((ImageData) imgData.clone());
					imgData.data = null;
					imgData = null;
					img.dispose();
					loadZoomImageInUI();
					//
				}
			} catch (Exception e1) {
				logger.error("problem while demand raw", e1);
				throw new InvocationTargetException(e1, "Problem while taking single");
			} finally {
				isJobComplete = true;
				baseMonitor.done();
				tomoAlignmentView.setLeftWindowInfo(TomoAlignmentView.SAMPLE_SINGLE);
				if (checkProgressJob != null) {
					checkProgressJob.cancel();
				}
			}
		}

	}

	private IRoiPointsListener roiPointsListener = new IRoiPointsListener() {

		@Override
		public void roiPointsChanged(int direction, int x1, int y1, int x2, int y2) {
			IRoiHandler roiHandler = tomoAlignmentView.getTomoAlignmentController().getRoiHandler();
			int leftWindowBinValue = tomoAlignmentView.getTomoAlignmentController().getLeftWindowBinValue();

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

			tomoAlignmentView.setValidRoi(validPoints);
		}
	};

	protected void switchOnSaturation(final String rawFileName) {
		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						Image img = new Image(tomoAlignmentView.getLeftPanelComposite().getDisplay(), rawFileName);
						ImageData imageData = img.getImageData();
						img.dispose();
						ImageData id = tomoAlignmentView.getTomoAlignmentController().applySaturation(imageData);
						tomoAlignmentView.loadImageInUIThread(tomoAlignmentView.getLeftWindowImageViewer(), id
								.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(),
										tomoAlignmentView.getTomoAlignmentController().getScaledX()));
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

	protected void switchOffSaturation(final String rawFileName) {
		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						if (rawFileName != null) {
							Image img = new Image(tomoAlignmentView.getLeftPanelComposite().getDisplay(), rawFileName);
							ImageData imageData = img.getImageData();
							img.dispose();
							tomoAlignmentView.loadImageInUIThread(tomoAlignmentView.getLeftWindowImageViewer(),
									imageData.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(),
											tomoAlignmentView.getTomoAlignmentController().getScaledX()));
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

	@Override
	public void profileLineMovedTo(final double xVal, final long intensity) {
		if (tomoAlignmentView != null && !tomoAlignmentView.getLeftWindowImageViewer().isDisposed()) {
			tomoAlignmentView.getLeftWindowImageViewer().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					tomoAlignmentView.setRightInfoPage(RIGHT_INFO.PROFILE);
					String formattedXVal = lblXDecimalFormat.format(xVal);
					tomoAlignmentView.setXLabelValue(formattedXVal);
					tomoAlignmentView.setProfileIntensityValue(Long.toString(intensity));
					tomoAlignmentView.getLeftWindowImageViewer().showProfileHighlighter();
					int leftWindowBinValue = tomoAlignmentView.getTomoAlignmentController().getLeftWindowBinValue();
					// FIXME - potential divide by zero problem
					tomoAlignmentView.getLeftWindowImageViewer().moveProfileHighlighter(xVal / leftWindowBinValue);
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

		switch (tomoAlignmentView.getLeftWindowViewerDisplayMode()) {
		case SAMPLE_STREAM_LIVE:
		case FLAT_STREAM_LIVE:
			try {
				tomoAlignmentView.getTomoAlignmentController().setAdjustedProc1ScaleValue(from, to);
			} catch (Exception e) {
				tomoAlignmentView.loadErrorInDisplay("Problem updating scale on the detector",
						"Problem updating scale on the detector:" + e.getMessage());
			}
			break;
		case SAMPLE_SINGLE:
			try {
				tomoAlignmentView.getTomoAlignmentController().setAdjustedExposureTime(from, to);
				tomoAlignmentView.getLeftPanelComposite().startSingle();
				tomoAlignmentView.getLeftPanelComposite().startHistogram();
			} catch (Exception e) {
				tomoAlignmentView.loadErrorInDisplay("Problem updating scale on the detector",
						"Problem updating scale on the detector:" + e.getMessage());
				logger.error("TODO put description of error here", e);
			}
			break;
		case FLAT_SINGLE:
			try {
				tomoAlignmentView.getTomoAlignmentController().setAdjustedExposureTime(from, to);
				tomoAlignmentView.getLeftPanelComposite().startSingle();
				tomoAlignmentView.getLeftPanelComposite().startHistogram();
			} catch (Exception e) {
				tomoAlignmentView.loadErrorInDisplay("Problem updating scale on the detector",
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
			tomoAlignmentView.getTomoAlignmentController().applyHistogramToAdjustExposureTime();
		} catch (Exception e) {
			logger.error("TODO put description of error here", e);
			tomoAlignmentView.loadErrorInDisplay("Cannot apply calculated exposure time",
					"Cannot apply calculated exposure time:" + e.getMessage());
		}
	}

	@Override
	public void zoomRectMoved(Rectangle bounds, Dimension figureTopLeftRelativeImgBounds, Dimension distanceMoved) {
		logger.debug("BoundX :" + bounds.x + " BoundY:" + bounds.y);
		IProgressMonitor monitor = new NullProgressMonitor();
		if (tomoAlignmentView.isStreamingSampleExposure() || tomoAlignmentView.isStreamingFlatExposure()) {
			// change the ROI start only if 'Stream' is switched 'ON'
			try {
				tomoAlignmentView.getTomoAlignmentController().handleZoomStartMoved(figureTopLeftRelativeImgBounds);
			} catch (Exception e) {
				logger.error("moving zoomed rectangle problem:", e);
				tomoAlignmentView.loadErrorInDisplay("Problem moving zoom rectangle", e.getMessage());
			}
		} else {
			ViewerDisplayMode displayMode = tomoAlignmentView.getLeftWindowViewerDisplayMode();
			if (displayMode == ViewerDisplayMode.FLAT_SINGLE || displayMode == ViewerDisplayMode.SAMPLE_SINGLE) {
				int leftWindowBinValue = tomoAlignmentView.getTomoAlignmentController().getLeftWindowBinValue();
				if (tomoAlignmentView.getLeftPanelComposite().isProfileSelected()) {
					Rectangle leftWindowImgBounds = tomoAlignmentView.getLeftWindowImageViewer().getImageBounds();
					Rectangle zoomFigureBounds = tomoAlignmentView.getLeftWindowImageViewer().getZoomFigureBounds()
							.getTranslated(-1, 0);
					Rectangle lineBounds = tomoAlignmentView.getLeftWindowImageViewer().getProfilerLineBounds()
							.getTranslated(leftWindowImgBounds.x + 1, 0);

					if (zoomFigureBounds.intersects(lineBounds)) {
						Rectangle zoomFigureBoundsCopy = zoomFigureBounds.getCopy();
						Rectangle intersect = zoomFigureBoundsCopy.intersect(lineBounds);
						Rectangle translatedIntersect = intersect.getTranslated(-leftWindowImgBounds.x,
								-leftWindowImgBounds.y);

						tomoAlignmentView.updatePlots(monitor, translatedIntersect.x * leftWindowBinValue,
								(translatedIntersect.x + translatedIntersect.width) * leftWindowBinValue, lineBounds.y);
					} else {
						tomoAlignmentView.updatePlots(monitor, 0, 4008, lineBounds.y);
					}
				} else {
					ZOOM_LEVEL selectedZoomLevel = tomoAlignmentView.getLeftPanelComposite().getSelectedZoomLevel();
					double zoomDemandRawScaleX = selectedZoomLevel.getDemandRawScale().x;
					logger.debug("Zoom demand Raw scale X:{}", zoomDemandRawScaleX);
					logger.debug("dx:{}", distanceMoved.width);
					logger.debug("dy:{}", distanceMoved.height);

					Dimension scaled = distanceMoved.getCopy().getScaled(leftWindowBinValue * zoomDemandRawScaleX);
					logger.debug("Scaled dx:{}", scaled.width);
					logger.debug("Scaled dy:{}", scaled.height);

					tomoAlignmentView.getDemandRawZoomCanvas().scroll(scaled);
				}
			}
		}
	}

	@Override
	public void profileLineMoved(int y) {
		if (tomoAlignmentView.getLeftPanelComposite().isProfileSelected()) {
			tomoAlignmentView
					.updatePlots(new NullProgressMonitor(), 0, tomoAlignmentView.getTomoAlignmentController()
							.getDetectorFullWidth(), y
							* tomoAlignmentView.getTomoAlignmentController().getLeftWindowBinValue());
			tomoAlignmentView.setYLabelValue(Integer.toString(y
					* tomoAlignmentView.getTomoAlignmentController().getLeftWindowBinValue()));
			tomoAlignmentView.setXLabelValue(BLANK_STR);
			tomoAlignmentView.setProfileIntensityValue(BLANK_STR);
		}
	}

	/**
	 * Need to persist this here for the "Center Axis of Rotation operation" - This is recorded when the axis of
	 * rotation is calculated using the half rotation tool
	 */
	private int calcOffset = -1;

	@Override
	public void performOverlayImgMoved(final Point initialPoint, final Point finalPoint, final Dimension difference) {
		logger.debug("image figure overlay difference" + difference);
		tomoAlignmentView.getLeftWindowImageViewer().setFeedbackCursor(SWT.CURSOR_NO);

		try {
			final MotionControlCentring selectedCentring = tomoAlignmentView.getTomoControlComposite()
					.getSelectedCentring();
			tomoAlignmentView.getLeftWindowImageViewer().removeOverlayImage();
			// issue request to move the motor to the difference position
			final CAMERA_MODULE cameraModule = tomoAlignmentView.getTomoControlComposite().getSelectedCameraModule();
			tomoAlignmentView.getLeftPanelComposite().startStreaming();

			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					switch (selectedCentring) {
					case VERTICAL:
						try {
							tomoAlignmentView.getTomoAlignmentController().moveVertical(monitor, cameraModule,
									difference);
						} catch (InterruptedException e) {
							logger.error("Action stopped by user");
						} catch (Exception ex) {
							logger.error("Problem with vertical centring", ex);
							throw new InvocationTargetException(ex);
						} finally {
							try {
								tomoAlignmentView.getTomoControlComposite().switchOff(selectedCentring);
							} catch (Exception e) {
								logger.error("TODO put description of error here", e);
								tomoAlignmentView.loadErrorInDisplay("Problem switching off centring", e.getMessage());
							}
							monitor.done();
						}
						break;
					case FIND_AXIS_ROTATION:
						try {
							tomoAlignmentView.getLeftWindowImageViewer().getDisplay().asyncExec(new Runnable() {

								@Override
								public void run() {
									calcOffset = ((initialPoint.x - finalPoint.x) / 2);
									int imageStart = (tomoAlignmentView.getLeftWindowImageViewer().getImageBounds().x);
									int imageCenter = (tomoAlignmentView.getLeftWindowImageViewer().getImageBounds().width / 2);
									int calcTomoAxis = imageCenter - calcOffset;
									int newCrosshair = (tomoAlignmentView.getLeftWindowImageViewer().getImageBounds().x + calcTomoAxis);

									logger.debug("Image start x {}", imageStart);
									logger.debug("A = finalPoint.x = {}", finalPoint.x);
									logger.debug("C = initialPoint.x = {}", initialPoint.x);
									logger.debug("M = Image center =  {}", imageCenter);
									logger.debug("T = {}", calcTomoAxis);
									logger.debug("Setting crosshair to {}", newCrosshair);

									tomoAlignmentView.getLeftWindowImageViewer().moveCrossHairTo(newCrosshair);
									tomoAlignmentView.getTomoControlComposite().setTomoAxisFound(true);
								}
							});
							//
						} catch (Exception e) {
							logger.error("Problem with Half Rotation tool", e);
							throw new InvocationTargetException(e, "Problem with Half Rotation tool:" + e.getMessage());
						} finally {
							tomoAlignmentView.enableLeftPanelControls();
							try {
								tomoAlignmentView.getTomoControlComposite().switchOff(selectedCentring);
							} catch (Exception e) {
								logger.error("TODO put description of error here", e);
								tomoAlignmentView.loadErrorInDisplay("Problem switching off centring", e.getMessage());
							}
						}
						break;
					case MOVE_AXIS_OF_ROTATION:
						// Do nothing
						break;
					case HORIZONTAL:
						try {
							tomoAlignmentView.getTomoAlignmentController().moveHorizontal(monitor, cameraModule,
									difference);
						} catch (InterruptedException e) {
							logger.error("Action stopped by user");
						} catch (Exception e) {
							logger.error("Problem with Center Current Position", e);
							throw new InvocationTargetException(e, "Problem with Center Current Position:"
									+ e.getMessage());
						} finally {
							try {
								tomoAlignmentView.getTomoControlComposite().switchOff(selectedCentring);
							} catch (Exception e) {
								logger.error("TODO put description of error here", e);
								tomoAlignmentView.loadErrorInDisplay("Problem switching off centring", e.getMessage());
							}
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
			tomoAlignmentView.loadErrorInDisplay("Error while performing motor movement", e.getMessage());
		}
	}

	@Override
	public void cancelMove() {
		final MotionControlCentring selectedCentring = tomoAlignmentView.getTomoControlComposite()
				.getSelectedCentring();
		try {
			tomoAlignmentView.getTomoControlComposite().switchOff(selectedCentring);
		} catch (Exception e) {
			logger.error("TODO put description of error here", e);
		}
	}

	@Override
	public void mouseClicked() {
		tomoAlignmentView.getViewSite().getActionBars().getStatusLineManager().setMessage(null);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void colourSliderRegion(int upperLimit, int lowerLimit) {
		logger.debug("Lower Limit:{}", lowerLimit);
		logger.debug("Upper Limit:{}", upperLimit);
		switch (tomoAlignmentView.getLeftWindowViewerDisplayMode()) {
		case SAMPLE_SINGLE:
			ImageData histAppliedImgData = tomoAlignmentView.getHistogramAdjuster().updateHistogramValues(lowerLimit,
					upperLimit);
			tomoAlignmentView.loadImageInUIThread(tomoAlignmentView.getLeftWindowImageViewer(), histAppliedImgData
					.scaledTo(tomoAlignmentView.getTomoAlignmentController().getScaledX(), tomoAlignmentView
							.getTomoAlignmentController().getScaledY()));
			break;
		case SAMPLE_STREAM_LIVE:
			double scale = ((tomoAlignmentView.getHistogramAdjuster().getMaxIntensity() - tomoAlignmentView
					.getHistogramAdjuster().getMinIntensity()) / (upperLimit - lowerLimit));
			try {
				tomoAlignmentView.getTomoAlignmentController().applyScalingContrast(-lowerLimit, scale);
			} catch (Exception e) {
				logger.error("TODO put description of error here", e);
				tomoAlignmentView.loadErrorInDisplay("Problem applying contrast",
						"Problem applying contrast:" + e.getMessage());
			}
			break;
		}
	}

	@Override
	public void updateScanProgress(double progress) {

	}

	@Override
	public void updateMessage(String message) {

	}

	@Override
	public void updateExposureTime(double exposureTime) {
		tomoAlignmentView.setPreferredSampleExposureTimeToWidget(exposureTime);
	}

	@Override
	public void updateError(Exception exception) {

	}
}