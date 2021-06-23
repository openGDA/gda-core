/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.calibration;

import java.util.Optional;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;

/**
 * Utility methods to transform either Pixel-to-Beam or Beam-to-Pixel spaces
 *
 * @author Maurizio Nagni
 */
public class MappedCalibrationUtils {

	/**
	 * Transforms a vector from the camera space to the beam space.
	 *
	 * <p>
	 * {@link CameraToBeamMap#getOffset()}, if not {@code null}, is added to the transformation result
	 * </p>
	 * @param cameraToBeam object with the transformation information
	 * @param vector the vector in the camera space
	 * @return the vector in the beam space
	 */
	public static Optional<RealVector> pixelToBeam(CameraToBeamMap cameraToBeam, RealVector vector) {
		return pixelToBeamSolver(cameraToBeam)
				.map(s -> transformCoordinates(s, vector))
				.map(t -> addOffset(t, cameraToBeam.getOffset()));
	}

	/**
	 * Transforms a vector from the beam space to the camera space.
	 *
	 * <p>
	 * {@link CameraToBeamMap#getOffset()}, if not {@code null}, is subtracted from the transformation result
	 * </p>
	 * @param cameraToBeam object with the transformation information
	 * @param vector the vector in the beam space
	 * @return the vector in the camera space
	 */
	public static Optional<RealVector> beamToPixel(CameraToBeamMap cameraToBeam, RealVector vector) {
		return beamToPixelSolver(cameraToBeam)
				.map(s -> transformCoordinates(s, vector))
				.map(t -> subtractOffset(t, cameraToBeam.getOffset()));
	}

	private static RealVector transformCoordinates(DecompositionSolver solver, RealVector cameraVector) {
		try {
			return solver.solve(cameraVector);
		} catch (SingularMatrixException e) {
			UIHelper.showError("error in pixel to beam conversion", e);
		}
		return null;
	}

	public static final Optional<RealVector> beamToPixel(CameraToBeamMap cameraToBeam, double x, double y) {
		RealVector cameraVector = new ArrayRealVector(new double[] { x, y }, false);
		return beamToPixel(cameraToBeam, cameraVector);
	}

	public static final Optional<RealVector> pixelToBeam(CameraToBeamMap cameraToBeam, double x, double y) {
		RealVector cameraVector = new ArrayRealVector(new double[] { x, y }, false);
		return MappedCalibrationUtils.pixelToBeam(cameraToBeam, cameraVector);
	}

	public static final RealVector getRealVector(double... elements) {
		return new ArrayRealVector(elements);
	}

	private static Optional<DecompositionSolver> pixelToBeamSolver(CameraToBeamMap beamCameraMap) {
		return beamToPixelSolver(beamCameraMap)
			.map(DecompositionSolver::getInverse)
			.map(LUDecomposition::new)
			.map(LUDecomposition::getSolver);
	}

	private static Optional<DecompositionSolver> beamToPixelSolver(CameraToBeamMap beamCameraMap) {
		return Optional.ofNullable(beamCameraMap.getMap())
				.map(LUDecomposition::new)
				.map(LUDecomposition::getSolver);
	}

	/**
	 * Adds an offset vector to a given one
	 *
	 * @param vector the primary vector
	 * @param offset the offset to add
	 * @return the amended vector
	 */
	private static RealVector addOffset(RealVector vector, RealVector offset) {
		return Optional.ofNullable(offset)
				.map(vector::add)
				.orElse(vector);
	}

	/**
	 * Adds an offset vector to a given one
	 *
	 * @param vector the primary vector
	 * @param offset the offset to add
	 * @return the amended vector
	 */
	private static RealVector subtractOffset(RealVector vector, RealVector offset) {
		return Optional.ofNullable(offset)
				.map(vector::subtract)
				.orElse(vector);
	}
}
