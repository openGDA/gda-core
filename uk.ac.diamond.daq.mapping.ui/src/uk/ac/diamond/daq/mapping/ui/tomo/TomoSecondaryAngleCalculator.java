/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.eclipse.scanning.sequencer.ServiceHolder;

import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo.StepSizes;

/**
 * A calculator that can calculate the secondary angle (phi) steps sizes and positions for
 * a tensor tomo scan, given a path model for that angle and the primary angle positions.
 */
public abstract class TomoSecondaryAngleCalculator<T extends IAxialModel> {

	private static final class StepModelCalculator extends TomoSecondaryAngleCalculator<AxialStepModel> {

		@Override
		protected StepSizes calculateAngle2StepSizes(double[] angle1Positions, AxialStepModel angle2Model) {
			final double range = angle2Model.getStop() - angle2Model.getStart();
			final int numPoints = IBoundsToFit.getPointsOnLine(range, angle2Model.getStep(), angle2Model.isBoundsToFit());
			final double[] stepSizes = calculateAngle2StepSizes(angle1Positions, range, numPoints);
			return StepSizes.forSingleStepSizes(stepSizes);
		}

		@Override
		protected double[][] calculateAngle2Positions(double[] angle1Positions, AxialStepModel angle2Model,
				StepSizes angle2StepSizes) {
			if (angle1Positions.length != angle2StepSizes.getOneDStepSizes().length)
				throw new IllegalArgumentException(ERROR_MESSAGE_ARRAY_LENGTH_MISMATCH); // sanity check
			return calculateAngle2Positions(angle2Model.getStart(),
					angle2Model.getStop(), angle2StepSizes.getOneDStepSizes());
		}

		@Override
		public Class<AxialStepModel> getModelClass() {
			return AxialStepModel.class;
		}

	}

	private static final class PointsModelCalculator extends TomoSecondaryAngleCalculator<AxialPointsModel> {

		@Override
		public Class<AxialPointsModel> getModelClass() {
			return AxialPointsModel.class;
		}

		@Override
		protected StepSizes calculateAngle2StepSizes(double[] angle1Positions, AxialPointsModel angle2Model) {
			final int numPoints = angle2Model.getPoints();
			final double range = angle2Model.getStop() - angle2Model.getStart();
			double[] stepSizes = calculateAngle2StepSizes(angle1Positions, range, numPoints);
			return StepSizes.forSingleStepSizes(stepSizes);
		}

		@Override
		protected double[][] calculateAngle2Positions(double[] angle1Positions, AxialPointsModel angle2Model,
				StepSizes angle2StepSizes) {
			if (angle1Positions.length != angle2StepSizes.getLength()) // sanity check
				throw new IllegalArgumentException(ERROR_MESSAGE_ARRAY_LENGTH_MISMATCH);
			return calculateAngle2Positions(angle2Model.getStart(), angle2Model.getStop(), angle2StepSizes.getOneDStepSizes());
		}

	}

	private static final class ArrayModelCalculator extends TomoSecondaryAngleCalculator<AxialArrayModel> {

		@Override
		public Class<AxialArrayModel> getModelClass() {
			return AxialArrayModel.class;
		}

		@Override
		protected StepSizes calculateAngle2StepSizes(double[] angle1Positions, AxialArrayModel angle2Model) {
			return StepSizes.none();
		}

		@Override
		protected double[][] calculateAngle2Positions(double[] angle1Positions, AxialArrayModel angle2Model,
				StepSizes angle2StepSizes) {
//			// use the specified angle2 positions for all angle 1 positions
			final double[] angle2Positions = angle2Model.getPositions();
			return IntStream.range(0, angle1Positions.length).mapToObj(i -> angle2Positions.clone()).toArray(double[][]::new);
		}

	}

	private static final class MultiStepModelCalculator extends TomoSecondaryAngleCalculator<AxialMultiStepModel> {

		@Override
		public Class<AxialMultiStepModel> getModelClass() {
			return AxialMultiStepModel.class;
		}

		@Override
		protected StepSizes calculateAngle2StepSizes(double[] angle1Positions, AxialMultiStepModel angle2Model) {
			final double[][] stepSizesByModel = angle2Model.getModels().stream()
					.map(model -> forModel(model).calculateAngle2StepSizes(angle1Positions, model))
					.map(StepSizes::getOneDStepSizes)
					.toArray(double[][]::new);
			// flip the dimensions of the array
			final double[][] stepSizes = IntStream.range(0, angle1Positions.length)
					.mapToObj(angle1PosIndex -> Arrays.stream(stepSizesByModel).mapToDouble(arr -> arr[angle1PosIndex]).toArray())
					.toArray(double[][]::new);

			return StepSizes.forMultiStepSizes(stepSizes);
		}

		@Override
		protected double[][] calculateAngle2Positions(double[] angle1Positions, AxialMultiStepModel angle2Model,
				StepSizes angle2StepSizes) {
			if (angle1Positions.length != angle2StepSizes.getLength())
				throw new IllegalArgumentException(ERROR_MESSAGE_ARRAY_LENGTH_MISMATCH); // sanity check
			return calculateAngle2PositionsForMultiStep(angle2Model, angle2StepSizes.getTwoDStepSizes());
		}

		private double[][] calculateAngle2PositionsForMultiStep(AxialMultiStepModel angle2Model, double[][] angle2StepSizes) {
			return Arrays.stream(angle2StepSizes)
					.map(stepSizesForAngle1Pos -> createMultiStepModelForStepSizes(angle2Model, stepSizesForAngle1Pos))
					.map(this::getPositions)
					.toArray(double[][]::new);
		}

		private AxialMultiStepModel createMultiStepModelForStepSizes(AxialMultiStepModel initialMultiStepModel, double[] angle2StepSizesForAngle1Pos) {
			final List<AxialStepModel> stepModels = initialMultiStepModel.getModels();
			final List<AxialStepModel> newStepModels = IntStream.range(0, stepModels.size())
					.mapToObj(modelIndex -> createAxialStepModel(stepModels.get(modelIndex), angle2StepSizesForAngle1Pos[modelIndex]))
					.toList();
			final AxialMultiStepModel newMultiStepModel = new AxialMultiStepModel(initialMultiStepModel.getAxisName(), newStepModels);
			newMultiStepModel.setContinuous(false);
			return newMultiStepModel;
		}

		private AxialStepModel createAxialStepModel(AxialStepModel oldModel, double newStepSize) {
			return new AxialStepModel(oldModel.getAxisName(), oldModel.getStart(), oldModel.getStop(), newStepSize);
		}

	}

	private static final Map<Class<? extends IAxialModel>, Class<? extends TomoSecondaryAngleCalculator<?>>> MODEL_CALCULATOR_CLASS_MAP =
			Map.of(AxialStepModel.class, StepModelCalculator.class,
					AxialPointsModel.class, PointsModelCalculator.class,
					AxialArrayModel.class, ArrayModelCalculator.class,
					AxialMultiStepModel.class, MultiStepModelCalculator.class);

	private static final String ERROR_MESSAGE_ARRAY_LENGTH_MISMATCH = "angle1Positions and angle2StepSizes must have same length";

	private TomoSecondaryAngleCalculator() {
		// private constructor to prevent external instantiation
	}

	/**
	 * Returns the appropriate secondary angle calculator for the given model
	 * @param <T> model type
	 * @param angle2Model the model
	 * @return calculator for secondary angle
	 */
	@SuppressWarnings("unchecked")
	protected static <T extends IAxialModel> TomoSecondaryAngleCalculator<T> forModel(T angle2Model) {
		final Class<T> modelClass = (Class<T>) angle2Model.getClass();
		final Class<? extends TomoSecondaryAngleCalculator<T>> calculatorClass =
				(Class<? extends TomoSecondaryAngleCalculator<T>>) MODEL_CALCULATOR_CLASS_MAP.get(modelClass);
		try {
			return calculatorClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Could not create calculator for model: " + angle2Model); // NOSONAR runtime exception for use with streams
		}
	}

	/**
	 * @return the secondary angle model class for this calculator
	 */
	public abstract Class<T> getModelClass();

	/**
	 * Calculates the secondary angle (phi) step sizes for each the given primary angle (omega) positions.
	 * This method returns a {@link StepSizes} object to encapsulate either a one dimensional double array
	 * (in most cases), or a two dimensional double array (in the case of a multi-step model)
	 *
	 * @param angle1Positions primary angle positions
	 * @param angle2Model secondary angle model
	 * @return primary angle step sizes
	 */
	protected abstract StepSizes calculateAngle2StepSizes(double[] angle1Positions, T angle2Model);

	/**
	 * Calculates the secondary angle (phi) positions for each the given primary angle (omega) positions
	 * and step sizes.
	 *
	 * @param angle1Positions primary angle positions
	 * @param angle2Model secondary angle model
	 * @param angle2StepSizes secondary angle step sizes
	 * @return secondary angle positions, a two-dimensional array
	 */
	protected abstract double[][] calculateAngle2Positions(double[] angle1Positions, T angle2Model, StepSizes angle2StepSizes);

	/**
	 * Calculates the secondary angle (phi) step sizes for the given primary angle (omega) range and number of points
	 * @param angle1Positions
	 * @param range
	 * @param numPoints
	 * @return secondary angle step sizes
	 */
	protected double[] calculateAngle2StepSizes(double[] angle1Positions, double range, int numPoints) {
		return Arrays.stream(angle1Positions).map(x -> calculateStepSize(x, range, numPoints)).toArray();
	}

	/**
	 * Applies the formula to calculate the secondary angle step size:
	 * <p>
	 * <code>secondary_angle_step = secondary_angle_range /
	 *    floor(secondary_angle_points * cos(primary_angle_position * (pi / 180)))</code>
	 * <p>
	 * If the result of the formula is infinite, range * 2 is returned instead.
	 * If the result of the formula is negative, the absolute value is returned instead.
	 *
	 * @param angle1Pos primary angle position
	 * @param angle2Range secondary angle range
	 * @param numPoints number at secondary angle points
	 * @return secondary angle step size
	 */
	protected double calculateStepSize(double angle1Pos, final double angle2Range, final int numPoints) {
		// the formula to get the secondary angle (phi) step size for a particular primary angle (omega) position and step size.
		// note the formula actually uses the number of steps, which is number of points - 1.
		final int numSteps = numPoints - 1;
		final double result = Math.abs(angle2Range) / Math.floor(numSteps * Math.cos(angle1Pos * (Math.PI / 180.0)));
		return Double.isInfinite(result) ? angle2Range * 2 : Math.abs(result); // if an infinite step size is produced, use 2 * range. This will produce 1 point
	}

	/**
	 * Calculates the secondary angle (phi) positions for the given start and stop positions and array of step sizes.
	 * @param angle2Start secondary angle start pos
	 * @param angle2Stop secondary angle stop pos
	 * @param angle2StepSizes array of step sizes
	 * @return secondary angle positions
	 */
	protected double[][] calculateAngle2Positions(double angle2Start,
			double angle2Stop, double[] angle2StepSizes) {
		return Arrays.stream(angle2StepSizes)
				.mapToObj(angle2Step -> createAngle2Model(angle2Start, angle2Stop, angle2Step))
				.map(this::getPositions)
				.toArray(double[][]::new);
	}

	private IAxialModel createAngle2Model(double start, double stop, double stepSize) {
		if (stepSize <= 0 || Double.isNaN(stepSize)) {
			throw new IllegalArgumentException("step size not allowed: " + stepSize);
		}

		// note, step sizes larger than the range are allowed, and will result in a single point
		return new AxialStepModel("phi", start, stop, stepSize);
	}

	protected double[] getPositions(final IAxialModel pathModel) {
		try {
			return getPositions(ServiceHolder.getGeneratorService().createGenerator(pathModel));
		} catch (GeneratorException e) {
			throw new RuntimeException(e); // NOSONAR wrap in runtime exception so this method can be used with streams
		}
	}

	private double[] getPositions(IPointGenerator<? extends IAxialModel> pointGen) {
		final String axisName = pointGen.getModel().getAxisName();
		return pointGen.createPoints().stream()
				.map(p -> p.get(axisName))
				.map(Double.class::cast)
				.mapToDouble(Double::doubleValue)
				.toArray();
	}

}