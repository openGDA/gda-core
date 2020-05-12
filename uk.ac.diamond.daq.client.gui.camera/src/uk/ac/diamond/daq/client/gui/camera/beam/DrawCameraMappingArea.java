package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.Optional;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Draws a {@link BeamCameraMappingEvent} as polygon on top of the
 * {@link IPlottingSystem}
 * 
 * @author Maurizio Nagni
 */
public class DrawCameraMappingArea {

	private final IPlottingSystem<Composite> plottingSystem;
	private final BeamCameraMappingEvent event;
	private IScannableMotor driverX;
	private IScannableMotor driverY;

	private DrawCameraMappingArea(IPlottingSystem<Composite> plottingSystem, BeamCameraMappingEvent event) {
		super();
		this.plottingSystem = plottingSystem;
		this.event = event;
	}

	public static void handleEvent(final IPlottingSystem<Composite> plottingSystem,
			final BeamCameraMappingEvent event) {
		DrawCameraMappingArea instance = new DrawCameraMappingArea(plottingSystem, event);
		instance.estimateScaling();
	}

	public void estimateScaling() {
		ICameraConfiguration cameraConfiguraiton = CameraHelper.createICameraConfiguration(event.getCameraIndex());
		cameraConfiguraiton.getBeamCameraMap().ifPresent(this::estimateBoundaries);
	}

	private static final Logger logger = LoggerFactory.getLogger(CameraImageComposite.class);

	private void addRegion(double ptx, double pty, double width, double height) {
		try {
			Optional.ofNullable(plottingSystem.getRegion("Boundary")).ifPresent(plottingSystem::removeRegion);
			IRegion boundary = plottingSystem.createRegion("Boundary", RegionType.POLYLINE);
			PolylineROI poly = new PolylineROI(ptx, pty);
			poly.insertPoint(ptx + width, pty);
			poly.insertPoint(ptx + width, pty + height);
			poly.insertPoint(ptx, pty + height);
			poly.insertPoint(ptx, pty);
			boundary.setMobile(false);
			boundary.setROI(poly);
			plottingSystem.addRegion(boundary);
		} catch (Exception e) {
			UIHelper.showWarning(ClientMessages.CANNOT_DRAW_REGION, e);
			logger.error("Cannot draw camera mapping region", e);
		}
	}

	private void estimateBoundaries(BeamCameraMap beamCameraMap) {
		FinderHelper.getIScannableMotor(beamCameraMap.getDriverX()).ifPresent(d -> driverX = d);
		FinderHelper.getIScannableMotor(beamCameraMap.getDriverY()).ifPresent(d -> driverY = d);
		int[] cameraSize = null;
		try {
			cameraSize = cameraSize(beamCameraMap.getCameraConfiguration().getCameraControl());
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if ((driverX == null && driverY == null) || cameraSize == null) {
			return;
		}

		RealVector solutionMin;
		RealVector solutionMax;
		RealMatrix transformation = beamCameraMap.getAffineTransformation();
		LUDecomposition luDecompositionCameraToBeam = new LUDecomposition(transformation);
		try {
			RealVector constantsMin = new ArrayRealVector(
					new double[] { driverX.getLowerInnerLimit(), driverY.getLowerInnerLimit() }, false);
			RealVector constantsMax = new ArrayRealVector(
					new double[] { driverX.getUpperInnerLimit(), driverY.getUpperInnerLimit() }, false);
			solutionMin = luDecompositionCameraToBeam.getSolver().solve(constantsMin);
			solutionMax = luDecompositionCameraToBeam.getSolver().solve(constantsMax);
		} catch (DeviceException e) {
			logger.error("Cannnot estimate minMax solutions", e);
			return;
		}

		logger.debug("transformationDet: {} ", luDecompositionCameraToBeam.getDeterminant());
		double pxMin = 0;
		double pyMin = 0;
		double pxMax = 0;
		double pyMax = 0;

		if (solutionMax.getEntry(0) > 0) {
			pxMax = solutionMax.getEntry(0) >= cameraSize[0] ? cameraSize[0] : solutionMax.getEntry(0);
		}
		if (solutionMax.getEntry(0) < 0) {
			pxMax = Double.MIN_VALUE;
		}
		if (solutionMin.getEntry(0) < 0 && pxMax > 0) {
			pxMin = 0;
		}
		if (solutionMin.getEntry(0) >= 0 && pxMax > 0) {
			pxMin = solutionMin.getEntry(0);
		}

		if (solutionMax.getEntry(1) > 0) {
			pyMax = solutionMax.getEntry(1) >= cameraSize[1] ? cameraSize[1] : solutionMax.getEntry(1);
		}
		if (solutionMax.getEntry(1) < 0) {
			pyMax = Double.MIN_VALUE;
		}
		if (solutionMin.getEntry(1) < 0 && pyMax > 0) {
			pyMin = 0;
		}
		if (solutionMin.getEntry(1) >= 0 && pyMax > 0) {
			pyMin = solutionMin.getEntry(1);
		}

		double pxMn = pxMin;
		double pyMn = pyMin;

		double pxMx = pxMax;
		double pyMx = pyMax;

		Display.getDefault().asyncExec(() -> addRegion(pxMn, pyMn, pxMx - pxMn, pyMx - pyMn));
	}

	private int[] cameraSize(Optional<CameraControl> cameraControl) throws DeviceException {
		return cameraControl.isPresent() ? cameraControl.get().getFrameSize() : null;
	}

}
