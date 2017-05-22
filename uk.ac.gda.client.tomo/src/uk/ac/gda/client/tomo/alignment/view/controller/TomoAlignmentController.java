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

package uk.ac.gda.client.tomo.alignment.view.controller;

import gda.device.DeviceException;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gov.aps.jca.TimeoutException;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.IScanResolutionLookupProvider;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.TomoViewController;
import uk.ac.gda.client.tomo.alignment.view.IRotationMotorListener;
import uk.ac.gda.client.tomo.alignment.view.ITomoAlignmentView;
import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration.AlignmentScanMode;
import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration.MotorPosition;
import uk.ac.gda.client.tomo.alignment.view.handlers.IAutofocusController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraStageMotorHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IRoiHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleStageMotorHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleWeightRotationHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IShutterHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITomoConfigResourceHandler;
import uk.ac.gda.client.tomo.alignment.view.utils.ScaleDisplay;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;

/**
 * The Tomography alignment view controller - this controller communicates with the EPICS model and updates the relevant
 * views.
 */
public class TomoAlignmentController extends TomoViewController {

	private ISampleStageMotorHandler sampleStageMotorHandler;

	private IShutterHandler cameraShutterHandler;

	private ICameraStageMotorHandler cameraStageMotorHandler;

	private ICameraHandler cameraHandler;

	private ITiltController tiltController;

	private ICameraMotionController cameraMotionController;

	private IRoiHandler roiHandler;

	private ICameraModuleController cameraModuleController;

	private ISampleWeightRotationHandler sampleWeightRotationHandler;

	private ITomoConfigResourceHandler saveHandler;

	private IScanResolutionLookupProvider scanResolutionLookupProvider;

	private Exception iocDownException = null;

	private IAutofocusController autofocusController;

	public enum SAMPLE_STAGE_STATE {
		IN, OUT;
	}

	public void setCameraShutterHandler(IShutterHandler cameraShutterHandler) {
		this.cameraShutterHandler = cameraShutterHandler;
	}

	public void setCameraStageMotorHandler(ICameraStageMotorHandler cameraStageMotorHandler) {
		this.cameraStageMotorHandler = cameraStageMotorHandler;
	}

	private boolean darkImageSaved;

	private Set<ITomoAlignmentView> tomoalignmentViews = new HashSet<ITomoAlignmentView>();

	private Set<IRotationMotorListener> rotationMotorListeners = new HashSet<IRotationMotorListener>();

	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentController.class);

	private Thread prepareAlignmentThread;

	private Double samplePositionBeforeMovingOut;

	public void setCameraMotionController(ICameraMotionController cameraMotionController) {
		this.cameraMotionController = cameraMotionController;
	}

	public void setCameraHandler(ICameraHandler cameraHandler) {

		this.cameraHandler = cameraHandler;
		this.cameraHandler.setViewController(this);
	}

	public void setSampleStageMotorHandler(ISampleStageMotorHandler motorHandler) {
		this.sampleStageMotorHandler = motorHandler;
		this.sampleStageMotorHandler.setTomoAlignmentViewController(this);
	}

	public boolean addRotationMotorListener(IRotationMotorListener rotationMotorListener) {
		return this.rotationMotorListeners.add(rotationMotorListener);
	}

	public boolean removeRotationMotorListener(IRotationMotorListener rotationMotorListener) {
		return this.rotationMotorListeners.remove(rotationMotorListener);
	}

	/**
	 * @param tomoAlignmentView
	 * @return <code>true</code> if the view is registered with the controller successfully.
	 */
	public boolean registerTomoAlignmentView(ITomoAlignmentView tomoAlignmentView) {
		return this.tomoalignmentViews.add(tomoAlignmentView);
	}

	/**
	 * @param tomoAlignmentView
	 * @return <code>true</code> if the view is unregistered with the controller successfully
	 */
	public boolean unregisterTomoAlignmentView(ITomoAlignmentView tomoAlignmentView) {
		return this.tomoalignmentViews.remove(tomoAlignmentView);
	}

	/**
	 * This method finds in EPICS the ROI1 MJpeg streaming URL.
	 *
	 * @return the URL of the MJpeg streamer
	 * @throws Exception
	 */
	private String getFullImgMJPegURL() throws Exception {
		return cameraHandler.getFullMJpegURL();
	}

	/**
	 * This method finds in EPICS the ROI2 MJpeg streaming URL.
	 *
	 * @return the URL of the MJpeg streamer
	 * @throws Exception
	 */
	private String getZoomImgMJPegURL() throws Exception {
		return cameraHandler.getZoomImgMJPegURL();
	}

	private Callable<Boolean> streamUrlCallable = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			try {
				if (getFullImgMJPegURL() == null) {
					throw new MalformedURLException("Full image MJpeg Url inappropriate");
				}
				if (getZoomImgMJPegURL() == null) {
					throw new MalformedURLException("Zoomed image MJpeg Url inappropriate");
				}

				for (ITomoAlignmentView av : tomoalignmentViews) {
					av.updateFullImgStreamUrl(getFullImgMJPegURL());
					av.updateZoomImgStreamUrl(getZoomImgMJPegURL());
				}
			} catch (TimeoutException tme) {
				logger.error("IOC doesn't seem to be running", tme);
				throw tme;
			} catch (Exception ex) {
				logger.error("Problem with loading the channel", ex);
				throw ex;
			}
			return Boolean.TRUE;

		}
	};

	/**
	 * Executes the procedure in a new {@link Callable}.
	 *
	 * @return {@link Future}
	 */
	public Future<Boolean> getStreamUrl() {
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		return executorService.submit(streamUrlCallable);
	}

	/**
	 * @param monitor
	 * @throws Exception
	 */
	public void takeFlat(IProgressMonitor monitor, int numFlat, double acqTime) throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Taking flat images", numFlat + 5);
		// double distanceToMoveForFlat = motorHandler.getDistanceToMoveSampleForTakeFlat();
		double initialMotorPos = sampleStageMotorHandler.getSampleBaseMotorPosition();
		try {
			sampleStageMotorHandler.moveSampleScannableBy(progress,
					sampleStageMotorHandler.getDistanceToMoveSampleOut());
			try {
				logger.debug("Requesting for open shutter");
				// 1. Open Experimental shutter
				cameraShutterHandler.openShutter(progress.newChild(1));
			} catch (DeviceException e) {
				logger.error("Failed to open shutter", e);
				throw new InvocationTargetException(e, e.getMessage());
			}

			try {
				cameraHandler.takeFlat(progress.newChild(numFlat), numFlat, acqTime);
			} catch (Exception e) {
				logger.error("Problem with taking flat", e);
				throw new InvocationTargetException(e, e.getMessage());
			}

			cameraHandler.resetFileFormat();
		} finally {
			sampleStageMotorHandler.moveSampleScannable(progress, initialMotorPos);
		}
	}

	public int getPreferredNumberFlatImages() {
		return cameraHandler.getTakeFlatNumImages();
	}

	public void setPreferredSampleExposureTime(final double exposureTime) {
		cameraHandler.setPreferredSampleExposureTime(exposureTime);
	}

	public double getCameraExposureTime() throws Exception {
		double acqExposureRBV = cameraHandler.getAcqExposureRBV();
		logger.debug("getting camera exposure time to -" + acqExposureRBV);
		return acqExposureRBV;
	}

	public void startAcquiring(final double acqTime, boolean isAmplified, double lower, double upper)
			throws InvocationTargetException {
		logger.debug("Start acquisition request");
		try {
			isAmplified = isAmplified && acqTime > getFastPreviewExposureThreshold();
			cameraHandler.startAcquiring(acqTime, isAmplified, lower, upper);
			iocDownException = null;
		} catch (Exception e) {
			iocDownException = e;
			throw new InvocationTargetException(e, "Problem with starting stream - Detector IOC may be be down");
		}

	}

	public void stopAcquiring() {
		logger.debug("Stop acquiring");

		if (iocDownException == null) {
			Thread stopAcqThread = new Thread(new Runnable() {
				@Override
				public void run() {
					logger.debug("Stop acquisition request");
					try {
						cameraHandler.stopAcquiring();
					} catch (Exception e) {
						logger.error("Error stopping acquire", e);
					}
				}
			});
			stopAcqThread.setPriority(Thread.MAX_PRIORITY);
			stopAcqThread.start();
		}
	}

	public void updateAcquireState(int acquisitionState) {
		logger.debug("Updating acquire state");
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateStreamWidget(acquisitionState);
		}
	}

	public Future<Boolean> init() {
		super.initialize();
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		return executorService.submit(initThread);
	}

	public CAMERA_MODULE getModule() throws DeviceException {
		return cameraModuleController.getModule();
	}

	private Callable<Boolean> initThread = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			try {
				updateModuleButtonText(cameraModuleController.lookupMagnificationUnit(),
						getModuleButtonTextFromModuleLookupTable());
				// adbase model values update
				logger.debug("Set up all data fields - expected to be called only during initialization");
				cameraHandler.init();
				try {
					updateModuleSelected(getModule());
				} catch (Exception ex) {
					logger.error("Exception while updating module selection", ex);
				}

				updatePreferredSampleExposureTime(cameraHandler.getPreferredSampleExposureTime());
				cameraHandler.setPreferredFlatExposureTime(cameraHandler.getPreferredSampleExposureTime());
				updatePreferredFlatExposureTime(cameraHandler.getPreferredFlatExposureTime());
				updateAcquireState(cameraHandler.getAcquireState());
				cameraHandler.disableFlatCorrection();

				updateRotationDegree(sampleStageMotorHandler.getRotationMotorDeg());
				updateProc1FlatFieldCorrection(cameraHandler.getProc1FlatFieldCorrection());

				updateSampleInOutState(sampleStageMotorHandler.getSampleBaseMotorPosition());

				updateEnergy(getEnergy());

				updateResolutionPixelSize(getResolutionPixelSize(getModule()));

				updateAcqExposure(getCameraExposureTime());

				Double cameraMotionMotorPosition = getCameraMotionMotorPosition();
				if (cameraMotionMotorPosition != null) {
					updateCameraMotionPosition(cameraMotionMotorPosition);
				}
			} catch (Exception ex) {
				logger.error("Problem with loading the channel", ex);
				throw ex;
			}
			return Boolean.TRUE;
		}

	};

	public boolean isStreaming() throws Exception {
		boolean isStreaming = (cameraHandler.getAcquireState() == 1);
		logger.debug("checking the acquire state of the detector:" + isStreaming);
		return isStreaming;
	}

	protected void updateResolutionPixelSize(String resolutionPixelSize) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setResolutionPixelSize(resolutionPixelSize);
		}
	}

	protected String getResolutionPixelSize(CAMERA_MODULE module) throws DeviceException {
		Double lookupObjectPixelSize = cameraModuleController.lookupObjectPixelSize(module);
		String objPixelSizeUnits = cameraModuleController.lookupObjectPixelSizeUnits();
		if (lookupObjectPixelSize != null) {
			return String.format("%.3g %s", lookupObjectPixelSize, objPixelSizeUnits);
		}
		return null;
	}

	protected void updateEnergy(double energy) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setEnergy(energy);
		}
	}

	protected double getEnergy() {
		// logger.debug(arg0)
		return 112;
	}

	protected void updateSampleInOutState(double samplePosition) {

		Double defaultSampleInPosition = sampleStageMotorHandler.getDefaultSampleInPosition();
		double sampleAbsoluteDifference = Math.abs(defaultSampleInPosition - samplePosition);
		SAMPLE_STAGE_STATE sampleState = SAMPLE_STAGE_STATE.IN;

		if (sampleAbsoluteDifference >= Math.abs(sampleStageMotorHandler.getDistanceToMoveSampleOut())) {
			sampleState = SAMPLE_STAGE_STATE.OUT;
		}
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setSampleInOutState(sampleState);
		}
	}

	protected void updateCameraMotionPosition(double cameraMotionMotorPosition) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setCameraMotionMotorPosition(cameraMotionMotorPosition);
		}
	}

	protected Map<Integer, String> getModuleButtonTextFromModuleLookupTable() throws DeviceException {
		if (cameraModuleController != null) {
			HashMap<Integer, String> moduleBtnTextMap = new HashMap<Integer, String>();
			for (CAMERA_MODULE module : CAMERA_MODULE.values()) {
				if (module != CAMERA_MODULE.NO_MODULE) {
					double lookupMagnification = cameraModuleController.lookupMagnification(module);
					moduleBtnTextMap.put(module.getValue(), String.format("%.3g", lookupMagnification));
				}
			}
			return moduleBtnTextMap;
		}
		return null;
	}

	private void updateModuleButtonText(String unitsToBeDisplayed, Map<Integer, String> moduleButtonTextMap) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateModuleButtonText(unitsToBeDisplayed, moduleButtonTextMap);
		}

	}

	protected void updateModuleSelected(CAMERA_MODULE module) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setCameraModule(module);
		}

	}

	protected void updatePreferredSampleExposureTime(double preferredExposureTime) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setPreferredSampleExposureTimeToWidget(preferredExposureTime);
		}
	}

	protected void updatePreferredFlatExposureTime(double preferredExposureTime) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setPreferredFlatExposureTimeToWidget(preferredExposureTime);
		}
	}

	protected void updateProc1FlatFieldCorrection(Boolean proc1FlatFieldCorrection) {
		logger.debug("updateProc1FlatFieldCorrection");
		if (proc1FlatFieldCorrection != null) {
			for (ITomoAlignmentView av : tomoalignmentViews) {
				av.setFlatFieldCorrection(proc1FlatFieldCorrection);
			}
		}
	}

	public void updateRotationDegree(Double rotationMotorDeg) {
		for (IRotationMotorListener rml : rotationMotorListeners) {
			rml.setRotationDeg(rotationMotorDeg);
		}
		//
		for (IRotationMotorListener av : tomoalignmentViews) {
			av.setRotationDeg(rotationMotorDeg);
		}
	}

	public void setIsRotationMotorBusy(boolean busy) {
		for (IRotationMotorListener rml : rotationMotorListeners) {
			rml.updateRotationMotorBusy(busy);
		}
		//
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateRotationMotorBusy(busy);
		}

	}

	@Override
	public void dispose() {
		if (sampleStageMotorHandler != null) {
			sampleStageMotorHandler.dispose();
		}

		if (cameraHandler != null) {
			cameraHandler.dispose();
		}

		if (tiltController != null) {
			tiltController.dispose();
		}

		if (cameraMotionController != null) {
			cameraMotionController.dispose();
		}

		if (roiHandler != null) {
			roiHandler.dispose();
		}

		if (cameraModuleController != null) {
			cameraModuleController.dispose();
		}

		if (sampleWeightRotationHandler != null) {
			sampleWeightRotationHandler.dispose();
		}
		super.dispose();
	}

	/**
	 * This method invokes a jython command using the {@link JythonServerFacade}. In fact, this is the only one that
	 * does that.
	 *
	 * @param newModule
	 * @param monitor
	 * @throws Exception
	 */
	/**
	 * @param newModule
	 * @param monitor
	 * @throws Exception
	 */
	public void setModule(final CAMERA_MODULE newModule, IProgressMonitor monitor) throws Exception {
		logger.debug("Setting module on the camera using the camera modules lookuptable:" + newModule);
		try {
			monitor.subTask("Move motors");
			cameraModuleController.moveModuleTo(newModule, monitor);
			if (!monitor.isCanceled()) {
				updateModuleSelected(newModule);
				updateResolutionPixelSize(getResolutionPixelSize(newModule));
				updateResolution(RESOLUTION.FULL);
			}
		} catch (Exception ex) {
			logger.error("Exc:", ex);
			throw new InvocationTargetException(ex, "Problem setting module");
		}
	}

	private void updateResolution(RESOLUTION res) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setResolution(res);
		}
	}

	public String demandRaw(IProgressMonitor monitor, double acqTime, boolean flatCorrected) throws Exception {
		try {
			String fileLocation = cameraHandler.demandRaw(monitor, acqTime, flatCorrected);
			logger.debug("demandRaw -> fileLocation:" + fileLocation);
			return fileLocation;
		} catch (Exception ex) {
			throw new InvocationTargetException(ex, "Problem with demanding raw");
		}
	}

	public String demandRawWithStreamOn(IProgressMonitor monitor, boolean flatCorrectionSelected) throws Exception {
		String fileLocation = cameraHandler.demandRawWithStreamOn(SubMonitor.convert(monitor), flatCorrectionSelected);
		logger.debug("demandRaw -> fileLocation:" + fileLocation);
		return fileLocation;
	}

	public void moveVertical(IProgressMonitor monitor, CAMERA_MODULE cameraModule, Dimension difference)
			throws Exception {
		try {
			logger.debug("Moving vertical motor:" + difference);
			double objectPixelSize = cameraModuleController.getObjectPixelSizeInMM(cameraModule);
			// ObjectPixelSize * binValue gives the distance to move per pixel on the screen
			double scale = objectPixelSize * cameraHandler.getRoi1BinValue();
			double movement = -(difference.height) * scale;
			sampleStageMotorHandler.moveVerticalBy(monitor, movement);
		} catch (DeviceException e) {
			logger.error("Exception when moving y2 motor", e.getMessage());
			throw e;
		}
	}

	public void moveHorizontal(IProgressMonitor monitor, CAMERA_MODULE cameraModule, Dimension difference)
			throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Moving Center Current Position", 2);
		double objectPixelSize = cameraModuleController.getObjectPixelSizeInMM(cameraModule);
		Integer roi1BinX = cameraHandler.getRoi1BinX();

		double scaleNumber = objectPixelSize * roi1BinX;

		double rotationDiff = sampleStageMotorHandler.getRotationMotorDeg() - sampleStageMotorHandler.getThethaOffset();
		double ss1TxMoveToPos = difference.width * scaleNumber * Math.cos(getRadianFromDegree(rotationDiff));
		sampleStageMotorHandler.moveSs1TxBy(progress.newChild(1), ss1TxMoveToPos);
		progress.worked(1);

		if (!progress.isCanceled()) {
			double ss1TzMoveToPos = (difference.width) * scaleNumber * Math.sin(getRadianFromDegree(rotationDiff));
			sampleStageMotorHandler.moveSs1TzBy(progress.newChild(1), ss1TzMoveToPos);
			progress.worked(1);
		}
		progress.done();
	}

	private double getRadianFromDegree(double rotationAngle) {
		return rotationAngle * Math.PI / 180;
	}

	public void moveAxisOfRotation(IProgressMonitor monitor, CAMERA_MODULE cameraModule, int differenceWidth)
			throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Moving to align Center of Axis rotation", 5);

		double objectPixelSize = cameraModuleController.getObjectPixelSizeInMM(cameraModule);
		Integer roi1BinX = cameraHandler.getRoi1BinX();
		double scaleNumber = objectPixelSize * roi1BinX;

		double distanceToMove = differenceWidth * scaleNumber;
		sampleStageMotorHandler.moveSampleScannableBy(monitor, distanceToMove);
		logger.debug("moveCenterAxisOfRotation: differenceWidth:{}", differenceWidth);
		logger.debug("moveCenterAxisOfRotation: distanceToMove:{}", distanceToMove);

		progress.done();

	}

	@SuppressWarnings("unused")
	public void moveTilt(IProgressMonitor monitor, CAMERA_MODULE cameraModule, Dimension difference) throws Exception {
		try {
			logger.debug("Moving tilt:" + difference);
			double objectPixelSize = cameraModuleController.getObjectPixelSizeInMM(cameraModule);
			Integer roi1BinX = cameraHandler.getRoi1BinX();
			double scaleNumber = objectPixelSize * roi1BinX;
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			logger.error("Sleep interrupted:", e);
		}
	}

	public void handleZoomStartMoved(Dimension figureTopLeftRelativeImgBounds) throws Exception {
		int roiX = figureTopLeftRelativeImgBounds.width * 4;
		int roiY = figureTopLeftRelativeImgBounds.height * 4;
		Point roiStart = new Point(roiX, roiY);
		cameraHandler.setZoomRoiLocation(roiStart);
	}

	@SuppressWarnings("unused")
	public void handleZoom(ZOOM_LEVEL zoomLevel, Rectangle zoomFigureBounds) throws Exception {
		cameraHandler.setupZoom(zoomLevel);
	}

	public String getDemandRawTiffFullFileName() throws Exception {
		return cameraHandler.getTiffFullFileName();
	}

	public Integer getRoi1BinX() throws Exception {
		return cameraHandler.getRoi1BinX();
	}

	public void moveRotationMotorBy(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);
		sampleStageMotorHandler.moveRotationMotorBy(progress.newChild(1), deg);
	}

	public void moveRotationMotorTo(IProgressMonitor monitor, double degree) throws DeviceException,
			InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);
		sampleStageMotorHandler.moveRotationMotorTo(progress.newChild(100), degree);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (sampleStageMotorHandler == null) {
			throw new IllegalArgumentException("'motorHandler' should be provided");
		}
		if (cameraHandler == null) {
			throw new IllegalArgumentException("'cameraHandler' should be provided");
		}
		if (cameraModuleController == null) {
			throw new IllegalArgumentException("'cameraModuleController' should be provided");
		}
		if (tiltController == null) {
			throw new IllegalArgumentException("'tiltController' should be provided");
		}
		if (saveHandler == null) {
			throw new IllegalArgumentException("'saveHandler' should be provided");
		}
	}

	public void enableFlatCorrection() throws Exception {
		cameraHandler.enableFlatCorrection();
	}

	public void disableFlatCorrection() throws Exception {
		cameraHandler.disableFlatCorrection();
	}

	public String getFlatImageFullFileName() {
		return cameraHandler.getFlatImageFullFileName();
	}

	public void takeDark(final IProgressMonitor monitor, double acqTime) throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Taking dark images", 10);
		logger.debug("Requesting for close shutter");
		// 1. Close Experimental shutter
		cameraShutterHandler.closeShutter(progress.newChild(2));

		try {
			cameraHandler.takeDark(progress.newChild(8), acqTime);
			darkImageSaved = true;
		} catch (Exception e) {
			darkImageSaved = false;
			throw e;
		}

		logger.debug("Requesting for open shutter");
		// . Open Experimental shutter
		cameraShutterHandler.openShutter(progress.newChild(2));
		progress.done();
		cameraHandler.resetFileFormat();
	}

	public void updateEnableFlatField(boolean enabled) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setFlatFieldCorrection(enabled);
		}
	}

	public double getPreferredSampleExposureTime() {
		return cameraHandler.getPreferredSampleExposureTime();
	}

	public void stopMotors() throws DeviceException {
		sampleStageMotorHandler.stopMotors();
	}

	public ScaleDisplay getLeftBarLengthInPixel(int maxNumberOfPixels, CAMERA_MODULE selectedCameraModule) {
		double objectPixelSize = cameraModuleController.getObjectPixelSizeInMM(selectedCameraModule);
		return ScaleDisplay.getScaleDisplay(maxNumberOfPixels, objectPixelSize, cameraHandler.getRoi1BinValue(), 1);
	}

	public ScaleDisplay getRightBarLengthInPixel(int maxNumberOfPixels, CAMERA_MODULE selectedCameraModule,
			ZOOM_LEVEL zoomLevel) {
		double objectPixelSize = cameraModuleController.getObjectPixelSizeInMM(selectedCameraModule);
		if (objectPixelSize != Double.NaN) {
			return ScaleDisplay.getScaleDisplay(maxNumberOfPixels, objectPixelSize, 1, zoomLevel.getValue());
		}
		return null;
	}

	public ImageData applySaturation(ImageData imgData) {
		Integer saturationThreshold = cameraHandler.getSaturationThreshold();
		PaletteData palette = imgData.palette;
		RGB red = new RGB(255, 0, 0);
		int redPix = palette.getPixel(red);
		for (int h = 0; h < imgData.height; h++) {
			for (int w = 0; w < imgData.width; w++) {
				if (imgData.getPixel(w, h) > saturationThreshold) {
					imgData.setPixel(w, h, redPix);
				}
			}
		}
		return imgData;

	}

	public TiltPlotPointsHolder doTiltAlignment(final IProgressMonitor monitor,
			final CAMERA_MODULE selectedCameraModule, double exposureTime) throws Exception {
		double ss1RzPosition = sampleStageMotorHandler.getSs1RzPosition();
		double ss1RxPosition = sampleStageMotorHandler.getSs1RxPosition();

		TiltPlotPointsHolder tiltPlotPointsHolder = tiltController.doTilt(monitor, selectedCameraModule, exposureTime);

		double newSs1RzPosition = sampleStageMotorHandler.getSs1RzPosition();
		double newSs1RxPosition = sampleStageMotorHandler.getSs1RxPosition();

		String tiltPointsTitle = String.format("Tilt alignment (%s moved by %.3g and %s moved by %.3g)",
				sampleStageMotorHandler.getSs1RxMotorName(), (ss1RxPosition - newSs1RxPosition),
				sampleStageMotorHandler.getSs1RzMotorName(), (ss1RzPosition - newSs1RzPosition));
		logger.debug("Tilt title :{}", tiltPointsTitle);
		tiltPlotPointsHolder.setTiltPointsTitle(tiltPointsTitle);
		return tiltPlotPointsHolder;
	}

	public void stopTiltPreparation() {
		if (JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING) {
			InterfaceProvider.getCommandAborter().abortCommands();
		}
	}

	public void updateErrorAligning(final String status) {
		// TODO - need to thorough test this.
		// Interrupt the prepare thread and block further
		if (prepareAlignmentThread != null) {
			prepareAlignmentThread.interrupt();
		}
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateErrorAligningTilt(status);
		}
	}

	public void setCameraModuleController(ICameraModuleController cameraModuleController) {
		this.cameraModuleController = cameraModuleController;
	}

	public void stopModuleChange() throws DeviceException {
		cameraModuleController.stopModuleChange();
	}

	public void stopDemandRaw() throws Exception {
		cameraHandler.stopDemandRaw();
	}

	public void resetAll() throws Exception {
		cameraHandler.reset();
	}

	public int getLeftWindowBinValue() {
		return cameraHandler.getRoi1BinValue();
	}

	public double getRotationMotorDeg() throws DeviceException {
		return sampleStageMotorHandler.getRotationMotorDeg();
	}

	public ITiltController getTiltController() {
		return tiltController;
	}

	public void setTiltController(ITiltController tiltController) {
		this.tiltController = tiltController;
	}

	public void setAutofocusController(IAutofocusController autofocusController) {
		this.autofocusController = autofocusController;
	}

	public String getDarkImageFileName() {
		return cameraHandler.getDarkImageFullFileName();
	}

	public double getPreferredFlatExposureTime() {
		return cameraHandler.getPreferredFlatExposureTime();
	}

	public void setPreferredFlatExposureTime(double flatExposureTime) {
		cameraHandler.setPreferredFlatExposureTime(flatExposureTime);
	}

	public boolean isDarkImageSaved() {
		return darkImageSaved;
	}

	public void enableDarkSubtraction() throws Exception {
		cameraHandler.enableDarkSubtraction();
	}

	public void disableDarkSubtraction() throws Exception {
		cameraHandler.disableDarkSubtraction();
	}

	public String getDarkFieldImageFullFileName() {
		return cameraHandler.getDarkFieldImageFullFileName();
	}

	public void moveCameraMotion(IProgressMonitor monitor, CAMERA_MODULE module, double t3m1zValue)
			throws DeviceException, InterruptedException {
		cameraMotionController.moveT3m1ZTo(monitor, module, t3m1zValue);
	}

	public Double getCameraMotionMotorPosition() throws DeviceException {
		if (cameraStageMotorHandler != null) {
			return cameraStageMotorHandler.getT3M1ZPosition();
		}
		return null;
	}

	public void setSampleWeight(SAMPLE_WEIGHT sampleWeight) throws Exception {
		logger.debug("Sample weight in the tomoalignment view controller:{}", sampleWeight);
		if (sampleWeightRotationHandler != null) {
			sampleWeightRotationHandler.handleSampleWeight(sampleWeight);
		}
	}

	public IRoiHandler getRoiHandler() {
		return roiHandler;
	}

	public void setRoiHandler(IRoiHandler roiHandler) {
		this.roiHandler = roiHandler;
	}

	public void moveSampleStageIn(IProgressMonitor monitor) throws DeviceException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);
		if (samplePositionBeforeMovingOut != null) {
			sampleStageMotorHandler.moveSampleScannable(progress, samplePositionBeforeMovingOut);
		} else {
			sampleStageMotorHandler.moveSampleScannable(progress, sampleStageMotorHandler.getDefaultSampleInPosition());
		}
	}

	public void moveSampleStageOut(IProgressMonitor monitor) throws DeviceException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);

		progress.beginTask("Move Sample Stage out", 4);
		samplePositionBeforeMovingOut = sampleStageMotorHandler.getSampleBaseMotorPosition();

		double distanceToMoveSampleOut = sampleStageMotorHandler.getDistanceToMoveSampleOut();

		sampleStageMotorHandler.moveSampleScannableBy(progress, distanceToMoveSampleOut);
	}

	public void setSampleWeightRotationHandler(ISampleWeightRotationHandler sampleWeightRotationHandler) {
		this.sampleWeightRotationHandler = sampleWeightRotationHandler;
	}

	public void setHistogramScaleOffsetValue(double acqTime, int lower, int upper, boolean isAmplified,
			double histogramFactor) throws Exception {

		cameraHandler.setAmplifiedValue(acqTime, isAmplified, lower, upper, histogramFactor);
	}

	public void setAdjustedExposureTime(boolean isAmplified, int lower, int upper, double histogramFactor)
			throws Exception {
		double adjustedExposureTime = getCameraExposureTime();
		adjustedExposureTime = adjustedExposureTime * histogramFactor;
		updateAdjustedPreferredExposureTime(adjustedExposureTime);
		// FIXME -
		setExposureTime(adjustedExposureTime, isAmplified, lower, upper, histogramFactor);
	}

	protected void updateAdjustedPreferredExposureTime(double preferredExposureTime) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setAdjustedPreferredExposureTimeToWidget(preferredExposureTime);
		}
	}

	public ITomoConfigResourceHandler getSaveHandler() {
		return saveHandler;
	}

	public void setSaveHandler(ITomoConfigResourceHandler saveHandler) {
		this.saveHandler = saveHandler;
	}

	/**
	 * @param monitor
	 * @param saveableConfiguration
	 * @throws Exception
	 */
	public String saveConfiguration(IProgressMonitor monitor, final SaveableConfiguration saveableConfiguration)
			throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Saving Configuration", 20);

		// energy - provided by view
		// number of projections - provided by view
		// description - provided by view
		// Detector properties
		// 3d resolution - provided by view
		// number of frames per projection - provided by view
		// roi
		int[] roiPoints = saveableConfiguration.getRoiPoints();
		int count = 0;
		if (roiPoints != null) {
			for (int i : roiPoints) {
				roiPoints[count++] = i * cameraHandler.getRoi1BinValue();
			}
		} else {
			roiPoints = new int[] { 0, 0, cameraHandler.getFullImageWidth(), cameraHandler.getFullImageHeight() };
		}
		// ROI adjusted to bin value
		saveableConfiguration.setRoiPoints(roiPoints);
		// module
		// module number - provided by view

		// horizontalFieldOfView
		Double cameraMagnification = cameraModuleController.lookupMagnification(CAMERA_MODULE
				.getEnum(saveableConfiguration.getModuleNumber()));
		saveableConfiguration.setCameraMagnification(cameraMagnification);

		saveableConfiguration.setInBeamPosition(sampleStageMotorHandler.getSampleBaseMotorPosition());

		saveableConfiguration.setOutOfBeamPosition(sampleStageMotorHandler.getSampleBaseMotorPosition()
				+ sampleStageMotorHandler.getDistanceToMoveSampleOut());
		// sample stage parameters
		// basex
		ArrayList<MotorPosition> motorPositions = saveableConfiguration.getMotorPositions();
		// ss1_x
		motorPositions.add(new MotorPosition(sampleStageMotorHandler.getSampleBaseMotorName(), sampleStageMotorHandler
				.getSampleBaseMotorPosition()));
		// Vertical Positions
		Map<String, Double> verticalMotorPositions = sampleStageMotorHandler.getVerticalMotorPositions();

		for (String motorName : verticalMotorPositions.keySet()) {
			motorPositions.add(new MotorPosition(motorName, verticalMotorPositions.get(motorName)));
		}

		// ss1_tx
		motorPositions.add(new MotorPosition(sampleStageMotorHandler.getCentreXMotorName(), sampleStageMotorHandler
				.getSs1TxPosition()));
		// ss1_tz
		motorPositions.add(new MotorPosition(sampleStageMotorHandler.getCentreZMotorName(), sampleStageMotorHandler
				.getSs1TzPosition()));
		// tilt x
		motorPositions.add(new MotorPosition(sampleStageMotorHandler.getTiltXMotorName(), sampleStageMotorHandler
				.getSs1RxPosition()));
		// tilt z
		motorPositions.add(new MotorPosition(sampleStageMotorHandler.getTiltZMotorName(), sampleStageMotorHandler
				.getSs1RzPosition()));
		//
		// sample exposure time - provided by view
		// flat exposure time - provided by view
		// sampleWeight - provided by view

		if (cameraStageMotorHandler != null) {
			// detector stage parameters
			// x
			motorPositions.add(new MotorPosition(cameraStageMotorHandler.getT3XMotorName(), cameraStageMotorHandler
					.getT3XPosition()));
			// y
			motorPositions.add(new MotorPosition(cameraStageMotorHandler.getT3m1YMotorName(), cameraStageMotorHandler
					.getT3M1YPosition()));
			// z
			motorPositions.add(new MotorPosition(cameraStageMotorHandler.getT3m1ZMotorName(), cameraStageMotorHandler
					.getT3M1ZPosition()));
		}

		// image location at theta - provided by view
		// Image location at theta +90 - provided by view

		// Scan mode
		saveableConfiguration.setScanMode(AlignmentScanMode.Step);

		return saveHandler.saveConfiguration(monitor, saveableConfiguration);
	}

	public Integer getDetectorFullWidth() {
		return cameraHandler.getFullImageWidth();
	}

	public int getScaledX() {
		return getDetectorFullWidth() / getLeftWindowBinValue();
	}

	public int getScaledY() {
		return cameraHandler.getFullImageHeight() / getLeftWindowBinValue();
	}

	public void setExposureTime(double actualExpTimeBeforeFactoring, boolean isAmplified, double lower, double upper,
			double histogramFactor) throws Exception {
		cameraHandler.setAmplifiedValue(actualExpTimeBeforeFactoring, isAmplified, (int) lower, (int) upper,
				histogramFactor);
	}

	public double[] getHistogramFromStats() throws Exception {
		return cameraHandler.getHistogramData();
	}

	public void setupHistoStatCollection() throws Exception {
		cameraHandler.setupHistoStatCollection();
	}

	public Double getDefaultExpTimeForModule(CAMERA_MODULE newModule) throws DeviceException {
		return cameraModuleController.lookupDefaultExposureTime(newModule);
	}

	public int getNumberOfProjections(int resolutionNumber) throws Exception {
		if (scanResolutionLookupProvider != null) {
			return scanResolutionLookupProvider.getNumberOfProjections(resolutionNumber);
		}
		return 0;
	}

	public void setScanResolutionLookupProvider(IScanResolutionLookupProvider scanResolutionLookupProvider) {
		this.scanResolutionLookupProvider = scanResolutionLookupProvider;
	}

	public String getEstimatedDurationOfScan(RESOLUTION resolution) {
		double sampleExposureTime = getPreferredSampleExposureTime();
		double binning = 1;
		int resolutionNumber = resolution.getResolutionNumber();
		try {
			binning = scanResolutionLookupProvider.getBinX(resolutionNumber);
		} catch (Exception e) {
			logger.error("Cannot access lookup table to retrieve Bin X", e);
		}
		int projections = 0;
		try {
			projections = scanResolutionLookupProvider.getNumberOfProjections(resolutionNumber);
		} catch (Exception e) {
			logger.error("Cannot access lookup table to retrieve projections", e);
		}
		double runTime = projections * ((sampleExposureTime / binning) + 0.5);// +OVERHEAD TIME);

		int hours = (int) (runTime / 3600); // since both are ints, you get an int
		int minutes = (int) ((runTime / 60) % 60);
		int seconds = (int) (runTime % 60);
		return String.format("%dh %02dm %02ds", hours, minutes, seconds);
	}

	public String getDetectorPortName() throws Exception {
		return cameraHandler.getPortName();
	}

	public double getFastPreviewExposureThreshold() {
		return cameraHandler.getFastPreviewExposureThreshold();
	}

	public String doAutoFocus(SubMonitor progress, double exposureTime) throws Exception {
		try {
			return autofocusController.doAutoFocus(progress, exposureTime);
		} catch (Exception ex) {
			logger.error("Problem with autofocus", ex);
			throw ex;
		}
	}

	public void updateAcqExposure(double acqExposure) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateExposureTimeToWidget(acqExposure);
		}
	}
}
