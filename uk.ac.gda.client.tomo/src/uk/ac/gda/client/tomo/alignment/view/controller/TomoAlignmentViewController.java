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
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gov.aps.jca.TimeoutException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.StatInfo;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.TomoAlignmentConfigurationHolder;
import uk.ac.gda.client.tomo.alignment.view.ITomoAlignmentView;
import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration.AlignmentScanMode;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionController;
import uk.ac.gda.client.tomo.alignment.view.handlers.IModuleLookupTableHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IMotorHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IRoiHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleWeightRotationHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITiltController;
import uk.ac.gda.client.tomo.alignment.view.utils.ScaleDisplay;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.ui.components.CameraControlComposite.RESOLUTION;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.ui.components.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.ui.components.ZoomButtonComposite.ZOOM_LEVEL;

/**
 * The Tomography alignment view controller - this controller communicates with the EPICS model and updates the relevant
 * views.
 */
public class TomoAlignmentViewController implements InitializingBean {

	private IMotorHandler motorHandler;

	private ICameraHandler cameraHandler;

	private IModuleLookupTableHandler moduleLookupTableHandler;

	private ITiltController tiltController;

	private ICameraMotionController cameraMotionController;

	private IRoiHandler roiHandler;

	private ICameraModuleController cameraModuleController;

	private ISampleWeightRotationHandler sampleWeightRotationHandler;

	private Exception iocDownException = null;

	public enum SAMPLE_STAGE_STATE {
		IN, OUT;
	}

	private boolean darkImageSaved;

	private final static DecimalFormat df = new DecimalFormat("#.##");

	public static double MAX_INTENSITY = 65535;

	private Set<ITomoAlignmentView> tomoalignmentViews = new HashSet<ITomoAlignmentView>();
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentViewController.class);

	private Thread prepareAlignmentThread;

	private Double samplePositionBeforeMovingOut;

	public void setCameraMotionController(ICameraMotionController cameraMotionController) {
		this.cameraMotionController = cameraMotionController;
	}

	public void setCameraHandler(ICameraHandler cameraHandler) {

		this.cameraHandler = cameraHandler;
		this.cameraHandler.setViewController(this);
	}

	public void setMotorHandler(IMotorHandler motorHandler) {
		this.motorHandler = motorHandler;
		this.motorHandler.setTomoAlignmentViewController(this);
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
		try {
			logger.debug("Requesting for open shutter");
			// 1. Open Experimental shutter
			motorHandler.openShutter(progress.newChild(1));
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
	}

	public int getPreferredNumberFlatImages() {
		return cameraHandler.getTakeFlatNumImages();
	}

	/**
	 * Flat fields to be reset - this may be invoked when the camera modules change on anything of the sort.
	 * 
	 * @throws Exception
	 */
	public void invalidateFlatField() throws Exception {
		logger.debug("Flat fields needs to be reset");
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.resetAmplifier();
		}
	}

	/**
	 * @param exposureTime
	 *            - the amplified exposure time
	 * @param amplifiedValue
	 * @throws Exception
	 */
	public void setExposureTime(final double exposureTime, final int amplifiedValue) throws Exception {
		logger.debug("setting camera exposure time to -" + exposureTime);
		cameraHandler.setExposureTime(exposureTime, amplifiedValue);
	}

	public void setPreferredSampleExposureTime(final double exposureTime) {
		cameraHandler.setPreferredSampleExposureTime(exposureTime);
	}

	public double getCameraExposureTime() throws Exception {
		double acqExposureRBV = cameraHandler.getAcqExposureRBV();
		logger.debug("getting camera exposure time to -" + acqExposureRBV);
		return acqExposureRBV;
	}

	public void startAcquiring(final double acqTime, final int amplifiedValue) throws InvocationTargetException {
		logger.debug("Start acquisition request");
		try {
			cameraHandler.startAcquiring(acqTime, amplifiedValue);
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

	public void updateAcqExposure(double acqExposure) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateExposureTimeToWidget(acqExposure);
		}
	}

	public Future<Boolean> init() {
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
				updateModuleButtonText(moduleLookupTableHandler.lookupHFOVUnit(),
						getModuleButtonTextFromModuleLookupTable());
				// adbase model values update
				logger.debug("Set up all data fields - expected to be called only during initialization");
				cameraHandler.init();
				updateModuleSelected(getModule());

				updateAcqExposure(cameraHandler.getAcqExposureRBV());
				updatePreferredSampleExposureTime(cameraHandler.getPreferredSampleExposureTime());
				updatePreferredFlatExposureTime(cameraHandler.getPreferredFlatExposureTime());
				updateAcquireState(cameraHandler.getAcquireState());
				cameraHandler.disableFlatCorrection();

				updateRotationDegree(motorHandler.getRotationMotorDeg());
				updateProc1FlatFieldCorrection(cameraHandler.getProc1FlatFieldCorrection());
				updateCameraMotionPosition(getCameraMotionMotorPosition());

				updateStatInfo();

				updateSampleInOutState(motorHandler.getSamplePosition());

				updateEnergy(getEnergy());

				updateResolutionPixelSize(getResolutionPixelSize(getModule()));

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
		Double lookupObjectPixelSize = moduleLookupTableHandler.lookupObjectPixelSize(module);
		String objPixelSizeUnits = moduleLookupTableHandler.lookupObjectPixelSizeUnits();
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

		Double defaultSampleInPosition = motorHandler.getDefaultSampleInPosition();
		double sampleAbsoluteDifference = Math.abs(defaultSampleInPosition - samplePosition);
		SAMPLE_STAGE_STATE sampleState = SAMPLE_STAGE_STATE.IN;

		if (sampleAbsoluteDifference >= Math.abs(motorHandler.getDistanceToMoveSampleOut())) {
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
		if (moduleLookupTableHandler != null) {
			HashMap<Integer, String> moduleBtnTextMap = new HashMap<Integer, String>();
			for (CAMERA_MODULE module : CAMERA_MODULE.values()) {
				if (module != CAMERA_MODULE.NO_MODULE) {
					double lookupHFOV = moduleLookupTableHandler.lookupHFOV(module);
					moduleBtnTextMap.put(module.getValue(), String.format("%.3g", lookupHFOV));
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

	protected void updateStatInfo() throws Exception {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateStatInfo(StatInfo.MAX, df.format(cameraHandler.getStatMax()));
			av.updateStatInfo(StatInfo.MIN, df.format(cameraHandler.getStatMin()));
			av.updateStatInfo(StatInfo.MEAN, df.format(cameraHandler.getStatMean()));
			av.updateStatInfo(StatInfo.SIGMA, df.format(cameraHandler.getStatSigma()));
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
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.setRotationDeg(rotationMotorDeg);
		}
	}

	public void dispose() {
		motorHandler.dispose();

		cameraHandler.dispose();

		moduleLookupTableHandler.dispose();

		tiltController.dispose();

		cameraMotionController.dispose();

		roiHandler.dispose();

		cameraModuleController.dispose();

		sampleWeightRotationHandler.dispose();
	}

	/**
	 * This method invokes a jython command using the {@link JythonServerFacade}. In fact, this is the only one that
	 * does that.
	 * 
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
			throw new InvocationTargetException(ex, ex.getMessage());
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
			double objectPixelSize = moduleLookupTableHandler.getObjectPixelSizeInMM(cameraModule);
			// ObjectPixelSize * binValue gives the distance to move per pixel on the screen
			double scale = objectPixelSize * cameraHandler.getRoi1BinValue();
			double movement = -(difference.height) * scale;
			double finalPosition = motorHandler.getSs1Y2Position() + movement;
			motorHandler.moveSs1Y2To(monitor, finalPosition);
		} catch (DeviceException e) {
			logger.error("Exception when moving y2 motor", e.getMessage());
			throw e;
		}
	}

	public void moveCenterCurrentPosition(IProgressMonitor monitor, CAMERA_MODULE cameraModule, Dimension difference)
			throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Moving Center Current Position", 2);
		double objectPixelSize = moduleLookupTableHandler.getObjectPixelSizeInMM(cameraModule);
		Integer roi1BinX = cameraHandler.getRoi1BinX();

		double scaleNumber = objectPixelSize * roi1BinX;

		double rotationDiff = motorHandler.getRotationMotorDeg() - motorHandler.getThethaOffset();
		double ss1TxMoveToPos = difference.width * scaleNumber * Math.cos(getRadianFromDegree(rotationDiff));
		motorHandler.moveSs1TxBy(progress.newChild(1), ss1TxMoveToPos);
		progress.worked(1);

		if (!progress.isCanceled()) {
			double ss1TzMoveToPos = (difference.width) * scaleNumber * Math.sin(getRadianFromDegree(rotationDiff));
			motorHandler.moveSs1TzBy(progress.newChild(1), ss1TzMoveToPos);
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

		double objectPixelSize = moduleLookupTableHandler.getObjectPixelSizeInMM(cameraModule);
		Integer roi1BinX = cameraHandler.getRoi1BinX();
		double scaleNumber = objectPixelSize * roi1BinX;

		double distanceToMove = differenceWidth * scaleNumber;
		motorHandler.moveSampleScannableBy(monitor, distanceToMove);
		logger.debug("moveCenterAxisOfRotation: differenceWidth:{}", differenceWidth);
		logger.debug("moveCenterAxisOfRotation: distanceToMove:{}", distanceToMove);

		progress.done();

	}

	@SuppressWarnings("unused")
	public void moveTilt(IProgressMonitor monitor, CAMERA_MODULE cameraModule, Dimension difference) throws Exception {
		try {
			logger.debug("Moving tilt:" + difference);
			double objectPixelSize = moduleLookupTableHandler.getObjectPixelSizeInMM(cameraModule);
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

	public void moveRotationMotor(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);
		motorHandler.moveRotationMotorBy(progress.newChild(1), deg);
	}

	public void moveRotationMotorTo(IProgressMonitor monitor, double degree) throws DeviceException,
			InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);
		motorHandler.moveRotationMotorTo(progress.newChild(100), degree);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (motorHandler == null) {
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
		if (moduleLookupTableHandler == null) {
			throw new IllegalArgumentException("'lookupTableHandler' should be provided");
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
		motorHandler.closeShutter(progress.newChild(2));

		cameraHandler.takeDark(progress.newChild(8), acqTime);

		logger.debug("Requesting for open shutter");
		// . Open Experimental shutter
		motorHandler.openShutter(progress.newChild(2));
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

	public void setAmplifierUpdate(double exposureTime, int factor) throws Exception {
		double newExpTime = exposureTime;
		cameraHandler.setAmplifiedValue(newExpTime, factor);
	}

	public void stopMotors() throws DeviceException {
		motorHandler.stopMotors();
	}

	public ScaleDisplay getLeftBarLengthInPixel(int maxNumberOfPixels, CAMERA_MODULE selectedCameraModule) {
		double objectPixelSize = moduleLookupTableHandler.getObjectPixelSizeInMM(selectedCameraModule);
		return ScaleDisplay.getScaleDisplay(maxNumberOfPixels, objectPixelSize, cameraHandler.getRoi1BinValue(), 1);
	}

	public ScaleDisplay getRightBarLengthInPixel(int maxNumberOfPixels, CAMERA_MODULE selectedCameraModule,
			ZOOM_LEVEL zoomLevel) {
		double objectPixelSize = moduleLookupTableHandler.getObjectPixelSizeInMM(selectedCameraModule);
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

	public void updateStatMax(double max) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateStatInfo(StatInfo.MAX, df.format(max));
		}
	}

	public void updateStatMin(double min) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateStatInfo(StatInfo.MIN, df.format(min));
		}
	}

	public void updateStatMean(double mean) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateStatInfo(StatInfo.MEAN, df.format(mean));
		}
	}

	public void updateStatSigma(double sigma) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateStatInfo(StatInfo.SIGMA, df.format(sigma));
		}
	}

	public void setIsRotationMotorBusy(boolean b) {
		for (ITomoAlignmentView av : tomoalignmentViews) {
			av.updateRotationMotorBusy(b);
		}
	}

	public TiltPlotPointsHolder doTiltAlignment(final IProgressMonitor monitor,
			final CAMERA_MODULE selectedCameraModule, double exposureTime) throws Exception {
		return tiltController.doTilt(monitor, selectedCameraModule, exposureTime);
		// return tiltController.getPlottablePoint(firstScanFolder, secondScanFolder);
	}

	public void stopTiltPreparation() {
		if (JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING) {
			JythonServerFacade.getInstance().panicStop();
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

	public void setModuleLookupTableHandler(IModuleLookupTableHandler lookupTableHandler) {
		this.moduleLookupTableHandler = lookupTableHandler;
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
		return motorHandler.getRotationMotorDeg();
	}

	public ITiltController getTiltController() {
		return tiltController;
	}

	public void setTiltController(ITiltController tiltController) {
		this.tiltController = tiltController;
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

	public double getCameraMotionMotorPosition() throws DeviceException {
		return motorHandler.getT3M1ZPosition();
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
			motorHandler.moveSampleScannable(progress, samplePositionBeforeMovingOut);
		} else {
			motorHandler.moveSampleScannable(progress, motorHandler.getDefaultSampleInPosition());
		}
	}

	public void moveSampleStageOut(IProgressMonitor monitor) throws DeviceException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor);

		progress.beginTask("Move Sample Stage out", 4);
		samplePositionBeforeMovingOut = motorHandler.getSamplePosition();

		double distanceToMoveSampleOut = motorHandler.getDistanceToMoveSampleOut();

		motorHandler.moveSampleScannableBy(progress, distanceToMoveSampleOut);
	}

	/**
	 * @param monitor
	 * @param saveableConfiguration
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 * @throws DeviceException
	 */
	public void saveConfiguration(IProgressMonitor monitor, final SaveableConfiguration saveableConfiguration)
			throws InvocationTargetException, InterruptedException, DeviceException {
		SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Saving Configuration", 20);

		int[] roiPoints = saveableConfiguration.getRoiPoints();
		int count = 0;
		if (roiPoints != null) {
			for (int i : roiPoints) {
				roiPoints[count++] = i * getLeftWindowBinValue();
			}
		} else {
			roiPoints = new int[] { 0, 0, cameraHandler.getFullImageWidth(), cameraHandler.getFullImageHeight() };
		}
		saveableConfiguration.setRoiPoints(roiPoints);

		saveableConfiguration.setSampleVerticalPosition(motorHandler.getSs1Y2Position());

		saveableConfiguration.setScanMode(AlignmentScanMode.Step);

		saveableConfiguration.setSampleDetectorDistance(motorHandler.getT3M1ZPosition());

		saveToConfigurationFile(monitor, saveableConfiguration);

		// List of data items to be saved
		// 1. Module number

		// 2. Vertical sample position
		// 3. Sample centre position (x and z)
		// 4. Tomo tilt angles
		// 5. Sample Acquisition time
		// 6. Flat field acquisition time
		// 7. Energy
		// 8. Recorded Camera ROI
		// 9. Sample weight
		// 10. Desired 3D resolution
		// 11. Frames per projection
		// 12. Sample description
		// 13. Snap shot of 0 and 90 deg
		// 14. Acquisition time divider
		// 15. Camera exposure
		// 16. Camera ROI
		// 17. Camera binning
		// 18. 1. projections
		// 19. Total run time
	}

	private void saveToConfigurationFile(IProgressMonitor monitor, final SaveableConfiguration saveableConfiguration)
			throws InvocationTargetException, InterruptedException {
		WorkspaceModifyOperation saveConfigurationOperation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {

				Resource res = TomoAlignmentConfigurationHolder.getAlignmentConfigResource(monitor, true);
				EObject eObject = res.getContents().get(0);
				if (eObject instanceof TomoExperiment) {
					TomoExperiment experiment = (TomoExperiment) eObject;

					AlignmentConfiguration expConfiguration = TomoParametersFactory.eINSTANCE
							.createAlignmentConfiguration();
					logger.debug("sample description:{}", saveableConfiguration.getSampleDescription());
					expConfiguration.setDescription(saveableConfiguration.getSampleDescription());

					DetectorProperties detectorProperties = TomoParametersFactory.eINSTANCE.createDetectorProperties();
					detectorProperties.setModule(saveableConfiguration.getModuleNumber());

					DetectorRoi detectorRoi = TomoParametersFactory.eINSTANCE.createDetectorRoi();
					int[] roiPoints = saveableConfiguration.getRoiPoints();
					detectorRoi.setMinX(roiPoints[0]);
					detectorRoi.setMinY(roiPoints[1]);
					detectorRoi.setMaxX(roiPoints[2]);
					detectorRoi.setMaxY(roiPoints[3]);

					detectorProperties.setDetectorRoi(detectorRoi);
					logger.warn("Need to know about the acquisition time divider");
					logger.warn("Need to know about the 3d resolution");
					logger.warn("Need to know about the detector bin value");
					logger.warn("Need to know about the number of frames per projection");

					expConfiguration.setDetectorProperties(detectorProperties);

					expConfiguration.setSampleExposureTime(saveableConfiguration.getSampleAcquisitonTime());
					expConfiguration.setFlatExposureTime(saveableConfiguration.getFlatAcquisitionTime());

					expConfiguration.setEnergy(saveableConfiguration.getEnergy());

					experiment.getParameters().getConfigurationSet().add(expConfiguration);
				}

				Map<Object, Object> options = new HashMap<Object, Object>();
				options.put(XMLResource.OPTION_ENCODING, "UTF-8");
				try {
					res.save(options);
				} catch (IOException e) {
					logger.error("Exception saving the configuration model", e);
				}

			}
		};
		saveConfigurationOperation.run(monitor);
	}

	public void setSampleWeightRotationHandler(ISampleWeightRotationHandler sampleWeightRotationHandler) {
		this.sampleWeightRotationHandler = sampleWeightRotationHandler;
	}
}