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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
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
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.LEFT_PAGE;
import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView.RIGHT_PAGE;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.client.tomo.alignment.view.handlers.CrossWireMouseListener;
import uk.ac.gda.client.tomo.alignment.view.handlers.CrossWireMouseListener.CrosswireListener;
import uk.ac.gda.client.tomo.composites.FullImageComposite;
import uk.ac.gda.client.tomo.composites.OverlayImageFigure.MOVE_AXIS;
import uk.ac.gda.client.tomo.composites.SWT2Dutil;
import uk.ac.gda.ui.components.CameraControlComposite;
import uk.ac.gda.ui.components.CameraControlComposite.RESOLUTION;
import uk.ac.gda.ui.components.IMotionControlListener;
import uk.ac.gda.ui.components.CameraControlComposite.RESOLUTION;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.ui.components.MotionControlComposite;
import uk.ac.gda.ui.components.MotionControlComposite.MotionControlCentring;
import uk.ac.gda.ui.components.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.ui.components.ZoomButtonComposite.ZOOM_LEVEL;

/**
 *
 */
public class MotionControlListener implements IMotionControlListener {

	private static final IWorkbenchWindow ACTIVE_WORKBENCH_WINDOW = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();

	private static final Logger logger = LoggerFactory.getLogger(MotionControlListener.class);

	private final TomoAlignmentViewController tomoAlignmentViewController;

	private final MotionControlComposite motionControlComposite;

	private final FullImageComposite leftWindowImageViewer;

	private final CameraControlComposite cameraControls;

	private final TomoAlignmentView v;

	private boolean cameraPositionDoNotResetFlag = false;

	public MotionControlListener(TomoAlignmentView v, TomoAlignmentViewController tomoAlignmentViewController,
			MotionControlComposite motionControlComposite, CameraControlComposite cameraControls,
			FullImageComposite leftWindowImageViewer) {
		this.v = v;
		this.tomoAlignmentViewController = tomoAlignmentViewController;
		this.motionControlComposite = motionControlComposite;
		this.cameraControls = cameraControls;
		this.leftWindowImageViewer = leftWindowImageViewer;
	}

	@Override
	public void setSampleWeight(SAMPLE_WEIGHT sampleWeight) throws Exception {
		logger.debug("Sample weight now is {}", sampleWeight);
		tomoAlignmentViewController.setSampleWeight(sampleWeight);
	}

	@Override
	public void cameraDistanceChanged(final double cameraDistance) throws InvocationTargetException,
			InterruptedException, DeviceException {
		logger.debug("Camera distance changed to {}", cameraDistance);
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		cameraPositionDoNotResetFlag = true;
		final CAMERA_MODULE module = motionControlComposite.getSelectedCameraModule();
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				SubMonitor progress = SubMonitor.convert(monitor);
				try {
					tomoAlignmentViewController.moveCameraMotion(progress, module, cameraDistance);
				} catch (DeviceException e) {
					cameraPositionDoNotResetFlag = false;
					try {
						resetCameraDistance();
					} catch (DeviceException e1) {
						throw new InvocationTargetException(e, "Unable to get camera position");
					}
					throw new InvocationTargetException(e, "Unable to move to requested camera position");
				} finally {
					monitor.done();
				}
			}
		});
		double cameraMotionPosition = tomoAlignmentViewController.getCameraMotionMotorPosition();
		motionControlComposite.setCameraMotionPosition(cameraMotionPosition);
		cameraPositionDoNotResetFlag = false;
	}

	@Override
	public void resetXrayEnergy() {

	}

	@Override
	public void resetCameraDistance() throws DeviceException {
		if (!cameraPositionDoNotResetFlag) {
			double cameraMotionPosition = tomoAlignmentViewController.getCameraMotionMotorPosition();
			motionControlComposite.setCameraMotionPosition(cameraMotionPosition);
		}
	}

	@Override
	public void xRayEnergyChanged(double xRayEnergy) {

	}

	private void centringStarted(MOVE_AXIS axis) throws Exception {
		cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
		cameraControls.deSelectSaturation();
		v.disableCameraControls();

		try {
			if (!v.isStreamingSampleExposure()) {
				cameraControls.startSampleStreaming();

				final int expTimeInSeconds = (int) (cameraControls.getSampleExposureTime() * 1000);
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
			cameraControls.stopSampleStream();

			ImageData leftWindowMainImage = leftWindowImageViewer.getImageData();
			leftWindowImageViewer.loadOverlayImage(leftWindowMainImage);

			leftWindowImageViewer.addOverlayImageFigureListener(v.overlayImageFigureListener);
			leftWindowImageViewer.setOverLayImageMoveAxis(axis);
			leftWindowImageViewer.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					v.getViewSite().getActionBars().getStatusLineManager()
							.setMessage("Drag the image to the desired position to align");
					leftWindowImageViewer.setFeedbackCursor(SWT.CURSOR_HAND);
				}
			});

		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void moduleChanged(CAMERA_MODULE oldModule, final CAMERA_MODULE newModule) throws InterruptedException,
			InvocationTargetException {
		logger.debug("Module Changed to:" + newModule);

		try {
			ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor moduleChangeMonitor) throws InvocationTargetException,
						InterruptedException {
					try {
						if (moduleChangeMonitor.isCanceled()) {
							throw new InterruptedException();
						}
						tomoAlignmentViewController.setModule(newModule, moduleChangeMonitor);
						v.resetAmplifier();
						v.setResolution(RESOLUTION.FULL);
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
			motionControlComposite.deselectModule();
			throw e;
		} catch (InterruptedException e) {
			logger.error("Problem Chaning modules:", e);
			motionControlComposite.deselectModule();
			throw e;
		}
	}

	/**
	 * Mouse movement listener
	 */
	private CrossWireMouseListener crossWireMouseListener = new CrossWireMouseListener();

	/**
	 * Listener to do action once the cross-wire has completed moving.
	 */
	private CrosswireListener crossWireListener = new CrosswireListener() {

		@Override
		public void performAction(final int pixelMoved) {
			final CAMERA_MODULE selectedCameraModule = motionControlComposite.getSelectedCameraModule();
			logger.debug("Pixels moved {}", pixelMoved);
			try {
				ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							tomoAlignmentViewController.moveAxisOfRotation(monitor, selectedCameraModule, pixelMoved);

						} catch (Exception e) {
							throw new InvocationTargetException(e, e.getMessage());
						} finally {
							monitor.done();
						}
					}
				});
			} catch (InvocationTargetException e) {
				logger.error("Problem moving axis of rotation", e);
				loadErrorInDisplay("Problem moving axis of rotation", e.getMessage());
			} catch (InterruptedException e) {
				logger.error("Problem moving axis of rotation", e);
			} finally {
				v.enableCameraControls();
				try {
					motionControlComposite.switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
				} catch (Exception e) {
					loadErrorInDisplay("Cannot reset Motion control buttons", "Cannot reset Motion control buttons");
				}
			}
		}
	};

	@Override
	public void moveAxisOfRotation(boolean selected) throws Exception {
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			v.disableCameraControls();
			final int imageCentre = leftWindowImageViewer.getImageCenterX();// already accounted for offset

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
			v.enableCameraControls();
			crossWireMouseListener.removeCrossWireListener(crossWireListener);
			leftWindowImageViewer.getCrossWire1Vertical().setCursor(
					Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
			leftWindowImageViewer.getCrossWire1Vertical().removeMouseListener(crossWireMouseListener);
			leftWindowImageViewer.getCrossWire1Vertical().removeMouseMotionListener(crossWireMouseListener);
		}
	}

	@Override
	public void tilt(boolean selected) throws Exception {
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
			cameraControls.profileStopped();
			logger.debug("Switching off the zoom");
			final CAMERA_MODULE selectedCameraModule = motionControlComposite.getSelectedCameraModule();
			v.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
			v.setRightPage(RIGHT_PAGE.PLOT);
			final double exposureTime = cameraControls.getSampleExposureTime();

			ACTIVE_WORKBENCH_WINDOW.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					SubMonitor progress = SubMonitor.convert(monitor);
					try {

						progress.beginTask("Tilt", 50);
						TiltPlotPointsHolder tiltPoints = tomoAlignmentViewController.doTiltAlignment(progress,
								selectedCameraModule, exposureTime);
						if (tiltPoints != null) {
							logger.debug("Tilt points: {}", tiltPoints);
							v.tomoPlotComposite.updatePlotPoints(progress, tiltPoints);
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
			try {
				motionControlComposite.switchOff(MotionControlCentring.TILT);
			} catch (Exception e) {
				logger.error("Problem stopping switching off 'Tilt'", e);
			}
		} else {
			logger.debug("'Tilt' de-selected");
		}
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
	public void degreeMovedBy(double degree) throws Exception {
		moveRotationMotor(degree);
	}

	@Override
	public void degreeMovedTo(final double degree) throws Exception {
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				try {
					SubMonitor progress = SubMonitor.convert(monitor);
					tomoAlignmentViewController.moveRotationMotorTo(progress.newChild(1), degree);
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

	private void centringStopped() throws Exception {
		v.enableCameraControls();
		leftWindowImageViewer.removeOverlayImage();
		leftWindowImageViewer.resetFeedbackCursor();
		v.getViewSite().getActionBars().getStatusLineManager().setMessage(null);
	}

	@Override
	public void horizontal(boolean selected) throws Exception {
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			centringStarted(MOVE_AXIS.X_AXIS);
		} else {
			tomoAlignmentViewController.stopMotors();
			centringStopped();
		}
	}

	@Override
	public void findRotationAxis(boolean selected) throws Exception {
		if (!v.isModuleSelected()) {
			throw new IllegalArgumentException("Camera module must be selected");
		}
		if (selected) {
			cameraControls.setZoom(ZOOM_LEVEL.NO_ZOOM);
			cameraControls.deSelectSaturation();
			v.disableCameraControls();

			v.setLeftPage(LEFT_PAGE.IMAGE_VIEWER);
			cameraControls.startSampleStreaming();
			final double steppedAcqTime = v.getSteppedSampleExposureTime();

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

							tomoAlignmentViewController.moveRotationMotorBy(progress.newChild(1), -180);
							firstImageLocation = tomoAlignmentViewController.demandRaw(progress.newChild(2),
									steppedAcqTime, false);

							tomoAlignmentViewController.moveRotationMotorBy(progress.newChild(1), 180);

							secondImageLocation = tomoAlignmentViewController.demandRaw(progress.newChild(1),
									steppedAcqTime, false);

						} catch (Exception ex) {
							logger.debug("Exception while finding half rotation", ex);
							v.switchOffCentring(MotionControlCentring.FIND_AXIS_ROTATION);
							throw new InvocationTargetException(ex, "Exception while finding half rotation:"
									+ ex.getMessage());
						}
						try {
							// Loading the first image
							img = new Image(leftWindowImageViewer.getDisplay(), firstImageLocation);
							ImageData firstImgData = img.getImageData();
							img.dispose();

							// Loading the second image
							img = new Image(leftWindowImageViewer.getDisplay(), secondImageLocation);
							ImageData secondImgData = img.getImageData();
							img.dispose();

							v.loadImageInUIThread(leftWindowImageViewer, secondImgData.scaledTo(
									TomoAlignmentView.SCALED_TO_X, TomoAlignmentView.SCALED_TO_Y));

							ImageData horizontallyFlippedImageData = SWT2Dutil.flip(firstImgData, false);
							loadOverlayImgInUIThread(horizontallyFlippedImageData.scaledTo(
									TomoAlignmentView.SCALED_TO_X, TomoAlignmentView.SCALED_TO_Y));
							leftWindowImageViewer.addOverlayImageFigureListener(v.overlayImageFigureListener);
							leftWindowImageViewer.setOverLayImageMoveAxis(MOVE_AXIS.X_AXIS);
							leftWindowImageViewer.getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									leftWindowImageViewer.setFeedbackCursor(SWT.CURSOR_HAND);
								}
							});
						} catch (Exception ex) {
							logger.debug("Exception while finding half rotation", ex);
							v.switchOffCentring(MotionControlCentring.FIND_AXIS_ROTATION);
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
			tomoAlignmentViewController.stopMotors();
		}
	}

	@Override
	public void vertical(boolean selected) throws Exception {
		if (selected) {
			centringStarted(MOVE_AXIS.Y_AXIS);
		} else {
			centringStopped();
			tomoAlignmentViewController.stopMotors();
		}
	}

	private void moveRotationMotor(final double deg) throws Exception {
		ACTIVE_WORKBENCH_WINDOW.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					tomoAlignmentViewController.moveRotationMotorBy(monitor, deg);
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
				if (!motionControlComposite.isDisposed()) {
					motionControlComposite.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								motionControlComposite.moveRotationSliderTo(tomoAlignmentViewController
										.getRotationMotorDeg());
								motionControlComposite.showRotationButtonsDeselected();
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
		if (motionControlComposite != null && !motionControlComposite.isDisposed()) {
			motionControlComposite.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					motionControlComposite.showRotationButtonsDeselected();
				}
			});
		}
	}

	private void loadOverlayImgInUIThread(final ImageData image) {
		if (leftWindowImageViewer.getDisplay() != null) {
			leftWindowImageViewer.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						leftWindowImageViewer.loadOverlayImage(image);
					} catch (Exception ex) {
						logger.error("Error loading image :{}", ex);
						loadErrorInDisplay("Error loading image", ex.getMessage());
					}
				}
			});
		}
	}

	public void loadErrorInDisplay(final String dialogTitle, final String errorMsg) {
		leftWindowImageViewer.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openError(leftWindowImageViewer.getShell(), dialogTitle, errorMsg);
			}
		});
	}

}
