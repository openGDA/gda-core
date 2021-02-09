/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.Objects;
import java.util.Optional;

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
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Draws a {@link BeamCameraMappingEvent} as polygon on top of an
 * {@link IPlottingSystem}. The polygon represents the boundaries inside which
 * there is a valid mapping between the camera pixels and the beam motors range.
 *
 * @author Maurizio Nagni
 *
 * @See {@link BeamCameraCalibrationComposite}
 * @See {@link BeamMappingSupport}
 */
public class DrawCameraMappingArea {

	private final IPlottingSystem<Composite> plottingSystem;
	private final ICameraConfiguration iCameraConfiguration;

	private static final String BEAM_BOUNDARIES = "BeamBoundaries";

	private DrawCameraMappingArea(IPlottingSystem<Composite> plottingSystem, ICameraConfiguration cameraConfiguration) {
		this.plottingSystem = plottingSystem;
		this.iCameraConfiguration = cameraConfiguration;
	}

	/**
	 * Draws the camera boundaries
	 *
	 * @param plottingSystem      where draw the rectangle
	 * @param cameraConfiguration the camera for which the boundaries have to be
	 *                            drawn
	 * @throws GDAClientException if
	 * <ul>
	 * <li>
	 * any of the beam driver are unavailable or throw an exception, or
	 * </li>
	 * <li>
	 * no BeamCameraMap is available for the given {@code cameraConfiguration}, or
	 * </li>
	 * <li>
	 * cannot get the camera frame size
	 * </li>
	 * </ul>
	 */
	public static void drawBeamBoundaries(IPlottingSystem<Composite> plottingSystem,
			ICameraConfiguration cameraConfiguration) throws GDAClientException {
		if (Objects.isNull(cameraConfiguration.getBeamCameraMap())) {
			throw new GDAClientException("no mapping from camera to beam is available");
		}
		DrawCameraMappingArea instance = new DrawCameraMappingArea(plottingSystem, cameraConfiguration);
		instance.estimateBoundaries();
	}

	/**
	 * Removes the camera boundaries
	 *
	 * @param plottingSystem from where remove the boundaries
	 */
	public static void removeBeamBoundaries(IPlottingSystem<Composite> plottingSystem) {
		Optional.ofNullable(plottingSystem.getRegion(BEAM_BOUNDARIES))
			.ifPresent(plottingSystem::removeRegion);
	}

	private static final Logger logger = LoggerFactory.getLogger(CameraImageComposite.class);

	private void addRegion(double ptx, double pty, double width, double height) {
		try {
			removeBeamBoundaries(plottingSystem);
			IRegion boundary = plottingSystem.createRegion(BEAM_BOUNDARIES, RegionType.POLYLINE);
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

	private void estimateBoundaries() throws GDAClientException {
		int[] cameraSize = new int[2];
		try {
			if (iCameraConfiguration.getCameraControl().isPresent()) {
				cameraSize = iCameraConfiguration.getCameraControl().get().getFrameSize();
			}
		} catch (DeviceException e) {
			throw new GDAClientException("Cannot retrieve the camera frame size", e);
		}
		estimateBoundaries(cameraSize, iCameraConfiguration.getBeamCameraMap());
	}

	/**
	 * Calculates the camera region covered by the actual mapping
	 *
	 * @param cameraSize the mapped camera size
	 * @param beamCameraMap the calculated camera to beam driver mapping
	 * @throws GDAClientException if
	 * <ul>
	 * <li>
	 * any of the beam driver are unavailable or  throw an exception, or
	 * </li>
	 * <li>
	 * cameraSize is null or its length is not 2
	 * </li>
	 * </ul>
	 *
	 */
	private void estimateBoundaries(int[] cameraSize, CameraToBeamMap beamCameraMap) throws GDAClientException {
		if (cameraSize == null || cameraSize.length != 2) {
			throw new GDAClientException("Cannot retrieve the camera frame size");
		}

		IScannableMotor driverX = FinderHelper.getIScannableMotor(beamCameraMap.getDriver().get(0))
				.orElseThrow(() -> new GDAClientException("Cannot use beam driver X"));
		IScannableMotor driverY = FinderHelper.getIScannableMotor(beamCameraMap.getDriver().get(1))
				.orElseThrow(() -> new GDAClientException("Cannot use beam driver Y"));

		Optional<RealVector> solutionMin;
		Optional<RealVector> solutionMax;
		try {
			solutionMin = calculateSolution(driverX.getLowerInnerLimit(), driverY.getLowerInnerLimit());
			solutionMax = calculateSolution(driverX.getLowerInnerLimit(), driverY.getLowerInnerLimit());
		} catch (DeviceException e) {
			throw new GDAClientException("Cannnot estimate minMax solutions", e);
		}

		double pxMax = solutionMax
					.map(smax -> getMaxValue(smax.getEntry(0), cameraSize[0]))
					.orElseGet(() -> 0d);

		double pxMin = solutionMin
				.map(smin -> getMinValue(smin.getEntry(0), pxMax))
				.orElseGet(() -> 0d);

		double pyMax = solutionMax
				.map(smax -> getMaxValue(smax.getEntry(1), cameraSize[1]))
				.orElseGet(() -> 0d);

		double pyMin = solutionMin
				.map(smin -> getMinValue(smin.getEntry(1), pyMax))
				.orElseGet(() -> 0d);

		Display.getDefault().asyncExec(() -> addRegion(pxMin, pyMin, pxMax - pxMin, pyMax - pyMin));
	}

	private double getMaxValue(double vectorElement, double cameraElement) {
		if (vectorElement > 0) {
			return vectorElement >= cameraElement ? cameraElement : vectorElement;
		}
		return Double.MIN_VALUE;
	}

	private double getMinValue(double vectorElement, double axisMax) {
		if (vectorElement >= 0 && axisMax > 0) {
			return vectorElement;
		}
		return 0;
	}

	private Optional<RealVector> calculateSolution(double x, double y) {
		return iCameraConfiguration.getBeamCameraMapping().beamToPixel(iCameraConfiguration, x, y);
	}
}
