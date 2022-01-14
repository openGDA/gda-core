/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.element;

import static gda.mscan.element.Mutator.ALTERNATING;
import static gda.mscan.element.Mutator.CONTINUOUS;
import static gda.mscan.element.Mutator.RANDOM_OFFSET;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.eclipse.scanning.api.points.models.AbstractBoundingBoxModel;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.AbstractMapModel;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;

import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

/**
 * Links the revised mscan syntax to corresponding scanning model factory functions in a typed way for both paths
 * that occupy a two dimensional area and axial paths. The abbreviation to specify a path is linked to the model via
 * the constructor along with the number of parameters required to specify the path (value count). The factory
 * functions need to be a nested static class so that they are in scope when the constructor is called.
 *
 * @since GDA 9.9
 */
public enum Scanpath implements IMScanDimensionalElementEnum {
	GRID_POINTS("grid", asList(), 2, 2, true, TwoAxisGridPointsModel.class, Factory::createTwoAxisGridPointsModel),
	GRID_STEP("rast", asList("raster"), 2, 2, true, TwoAxisGridStepModel.class, Factory::createTwoAxisGridStepModel),
	SPIRAL("spir", asList("spiral"), 2, 1, true, TwoAxisSpiralModel.class, Factory::createTwoAxisSpiralModel),
	LISSAJOUS("liss", asList("lissajous"), 2, 3, true, TwoAxisLissajousModel.class, Factory::createTwoAxisLissajousModel),
	LINE_STEP("step", asList("angl", "angle"), 2, 1, true, TwoAxisLineStepModel.class, Factory::createTwoAxisLineStepModel),
	LINE_POINTS("nopt", asList("pts", "noofpoints", "points", "proj", "projections"), 2, 1, true, TwoAxisLinePointsModel.class, Factory::createTwoAxisLinePointsModel),
	SINGLE_POINT("poin", asList(), 2, 2, true, TwoAxisPointSingleModel.class, Factory::createSinglePointModel),
	AXIS_STEP("axst", asList("axisstep"), 1, 1, true, AxialStepModel.class, Factory::createAxialStepModel),
	AXIS_POINTS("axno", asList("axispoints"), 1, 1, true, AxialPointsModel.class, Factory::createAxialPointsModel),
	AXIS_ARRAY("axar", asList("arr", "array", "axisarray"), 1, 2, false, AxialArrayModel.class, Factory::createAxialArrayModel),
	STATIC("stat", asList("static"), 0, 1, true, StaticModel.class, Factory::createStaticModel);

	private static final int BOUNDS_REQUIRED_PARAMS = 4;
	private static final String PREFIX = "Invalid Scan clause: ";
	private static final Map<Mutator, Function<Class<? extends AbstractPointsModel>, Boolean>> SUPPORT_LOOKUP =
							Map.of(RANDOM_OFFSET, AbstractPointsModel::supportsRandomOffset,
											ALTERNATING, AbstractPointsModel::supportsAlternating,
											CONTINUOUS, AbstractPointsModel::supportsContinuous);

	private static final String ALL_POSITIVE_ERROR = " path requires all positive parameters";
	private static final String ALL_INTEGER_ERROR = " path requires all integer parameters";
	private static final String ONE_POSITIVE_ERROR = " path requires that parameter %s is positive";
	private static final String ONE_INTEGER_ERROR = " path requires that parameter %s is an integer";
	private static final Map<String, Scanpath> termsMap;
	private final List<String> terms = new ArrayList<>();
	private final int axisCount;
	/** The number of parameters required to generate the path **/
	private final int valueCount;
	private final boolean hasFixedValueCount;
	private final Class<? extends AbstractPointsModel> modelType;
	private final ScanpathModelFactoryFunction factory;

	private Scanpath(final String text, final List<String> aliases, final int axisCount, final int valueCount,  final boolean hasFixedValueCount,
						final Class<? extends AbstractPointsModel> type,
						final ScanpathModelFactoryFunction factoryFunction) {
		this.terms.add(text);
		this.terms.addAll(aliases);
		this.axisCount = axisCount;
		this.valueCount = valueCount;
		this.hasFixedValueCount = hasFixedValueCount;
		this.modelType = type;
		this.factory= factoryFunction;
	}

	/**
	 * Initialise the {@link java.util.Map} of text terms (including aliases) to {@link Scanpath} instance
	 */
	static {
		termsMap = stream(values())
			.map(path -> path.terms().stream()
					.map(term -> new Pair<String, Scanpath>(term, path)))
			.flatMap(Function.identity())
			.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	/**
	 * Get the map of text terms (including aliases) to instances of Scanpath
	 *
	 * @return 		A {@link java.util.Map} of the terms to {@link Scanpath} instance
	 */
	public static Map<String, Scanpath> termsMap() {
		return termsMap;
	}
	/**
	 * The number of values required to construct the path
	 *
	 * @return		The number of values required to construct the path
	 */
	public int valueCount() {
		return valueCount;
	}

	public boolean hasFixedValueCount() {
		return hasFixedValueCount;
	}

	/**
	 * The default instance value to be used if one is not specified in the scan command
	 *
	 * @return		The {@link #GRID_POINTS} instance
	 */
	public static Scanpath defaultValue() {
		return GRID_STEP;
	}

	/**
	 * The default text values that correspond to the instances of Scanpath
	 *
	 * @return		List of default text for the instances
	 */
	public static List<String> strValues() {
		return stream(values()).map(val -> val.terms.get(0)).collect(toList());
	}

	/**
	 * Return the number of axes associated with this instance
	 */
	@Override
	public int getAxisCount() {
		return axisCount;
	}

	/**
	 * Get the full list of text terms that can be used to refer to this instance in mscan commands (default plus aliases)
	 *
	 * @return 		A {@link java.util.List} of the terms with the default text term at position 0
	 */
	public List<String> terms() {
		return terms;
	}

	/**
	 * Get a list of the aliases that can be used to refer to this instance in mscan commands
	 *
	 * @return 		A {@link java.util.List} of the aliases
	 */
	public List<String> aliases() {
		return terms.subList(1, terms.size() - 1);
	}

	/**
	 * The type of model that a particular instance's {@link #createModel} method will construct
	 *
	 * @return		The {@link AbstractBoundingBoxModel} based model type associated with the instance.
	 */
	public Class<? extends AbstractPointsModel> modelType() {
		return modelType;
	}

	/**
	 * Checks whether the supplied {@link Mutator} is supported by this {@link Scanpath} instance
	 *
	 * @param mutator The {@link Mutator} whose support to be checked
	 * @return		  true if the supplied {@link Mutator} can be applied to this {@link Scanpath}, otherwise false
	 */
	public boolean supports(Mutator mutator) {
		return SUPPORT_LOOKUP.get(mutator).apply(this.modelType);
	}

	/**
	 * Creates the correct {@link AbstractPointsModel} based object for this instance of Scanpath
	 * The number of supplied {@link Scannable}s, path parameters and bounding box parameters will be validated
	 * against their respective required numbers.
	 *
	 * @param scannables		The {@link List} of scannables associated with the path
	 * @param pathParams		The {@link List} of numeric parameters used to generate the path
	 * @param bboxParams		The {@link List} of bounding box parametes that define the extent of the path
	 * @param mutatorUses		The {@link Map} of mutators to their parameters to be applied to the path
	 *
	 * @return					The {@link IScanPathModel} of the constructed model object
	 *
	 * @throws 					IllegalArgumentException if the validation of parameters or {@link Mutator}s fails
	 */
	public IScanPointGeneratorModel createModel(final List<Scannable> scannables, final List<Number> pathParams,
										final List<Number> bboxParams, final Map<Mutator, List<Number>> mutatorUses) {
		validateInputs(scannables, pathParams, bboxParams);
		mutatorUses.keySet().forEach(mutator -> {
			if (!supports(mutator)) {
				throw new IllegalArgumentException(String.format(
						"%s The %s mutator is not supported by %s",
							PREFIX,  mutator, this.modelType.getSimpleName()));
			}
		});
		return factory.createScanpathModel(scannables, pathParams, bboxParams, mutatorUses);
	}

	/**
	 * Check that the correct number of all required parameters has been supplied for the required Scanpath
	 * covering Scannables, path parameters and bounding box parameters using the appropriate sub-method.
	 *
	 * @param scannables		The scannables involved in the scan
	 * @param pathParams		The point coordinates associated with the path
	 * @param bboxParams		The bounding box coordinates associated with the path
	 */
	private void validateInputs(final List<Scannable> scannables, final List<Number> pathParams,
			final List<Number> bboxParams) {
		if (this == STATIC) validateStatic(scannables, pathParams);
		else if(this == AXIS_ARRAY) validateAxisArray(scannables, pathParams);
		else validateInputs(Map.of(scannables, axisCount,
										pathParams, axisCount == 1 ? valueCount + 2 : valueCount,
										bboxParams, BOUNDS_REQUIRED_PARAMS));
	}

	/**
	 * Check that the correct number of all required parameters has been supplied for the required Scanpath
	 * for non-special case paths.
	 *
	 * @param inputs			An {@link Map} of lists to their expected sizes
	 * @param pathName			The name of the required path for Exception message purposes
	 *
	 * @throws					IllegalArgument exception if the required number of parameters is not present.
	 */
	private void validateInputs(final Map<List<? extends Object>, Integer> inputs) {
		inputs.entrySet().forEach(entry -> {
			if (entry.getKey().size() != entry.getValue()) {
				throw new IllegalArgumentException(String.format(
						"%s%s requires %s numeric values to be specified",
							PREFIX, modelType.getSimpleName(), entry.getValue()));
			}
		});
	}

	/**
	 * Check that no Scannables are associated with the path and that no more than one optional param (no of exposures)
	 * was supplied
	 *
	 * @param empty				Should be empty list of Scannables
	 * @param optionalArg		Other supplied params (max 1)
	 * 	 *
	 * @throws					IllegalArgument exception if requirements exceeded.
	 */
	private void validateStatic(List<Scannable> empty, List<Number> optionalArg) {
		if (!empty.isEmpty()) throw new IllegalArgumentException(String.format(
				"%s%s requires 0 scannables to be specified",
				PREFIX, modelType.getSimpleName()));
		if (optionalArg.size() > 1) throw new IllegalArgumentException(String.format(
				"%s%s requires exactly 0 or 1 arguments to be specified",
				PREFIX, modelType.getSimpleName()));
	}

	/**
	 * Check that the correct number of Scannable and position values have been specified
	 *
	 * @param scannables		The specified Scannables
	 * @param params			The specified positions
	 *
	 * @throws					IllegalArgument exception if requirements exceeded.
	 */
	private void validateAxisArray(List<Scannable> scannables, List<Number> params) {
		if (scannables.size() != axisCount) throw new IllegalArgumentException(String.format(
				"%sAxial Array requires %s scannable to be specified",
				PREFIX, axisCount));
		if (params.size() < valueCount) {
			throw new IllegalArgumentException(String.format(
				"%sAxial Array requires at least %s numeric value to be specified",
					PREFIX, valueCount));
		}
	}

	@Override
	public String toString() {
		return this.name().substring(0,1).concat(this.name().substring(1).toLowerCase());
	}

	/**
	 *  This Class contains factory methods for each of the required {@link AbstractPointsModel} based types
	 *  mapped by the instance constructor. It also defines constants to allow the various parameters required for
	 *  construction of the required {@link IScanPathModel} to be expressed in a more meaningful way.
	 */
	private static class Factory {
		// Constants to reference the axes of the various models via the supplied {@link List} of {@link Scannables}
		private static final int X_AXIS_INDEX = 0;
		private static final int Y_AXIS_INDEX = 1;

		// Constants to reference the available parameters for a {@link RandomOffsetTwoAxisGridPointsModel}
		private static final int OFFSET = 0;
		private static final int SEED = 1;

		// Constants to reference the bounding box coordinates via the supplied {@link List} of {@link Number}s
		// (bboxParameters)
		private static final int X_START_PARAM_INDEX = 0;
		private static final int Y_START_PARAM_INDEX = 1;
		private static final int X_LENGTH_PARAM_INDEX = 2;
		private static final int Y_LENGTH_PARAM_INDEX = 3;

		/** Constant to reference the available parameter of a {@link TwoAxisSpiralModel} **/
		private static final int SCALE = 0;

		/** Constant to reference the available parameter of a {@link TwoAxisLineAxialStepModel} **/
		private static final int STEP = 0;

		/** Constant to reference the parameter of a {@link TwoAxisLinePointsModel}/{@link TwoAxisLissajousModel} **/
		private static final int POINTS = 0;

		/** Constants to reference the available parameters of {@link AxialStepModel}/{@link AxialPointsModel} **/
		private static final int START = 0;
		private static final int STOP = 1;
		private static final int AX_STEP = 2;
		private static final int AX_POINTS = 2;

		/** Constant to reference the available parameter of a {@link TwoAxisLissajousModel} **/
		private static final int A = 1;
		private static final int B = 2;

		/** **/
		private static final int COUNT = 0;

		/**
		 * Creates a {@link TwoAxisGridPointsModel} using the supplied params. If the RandomOffset {@link Mutator} is
		 * specified, a {@link RandomOffsetTwoAxisGridPointsModel} is created instead.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the grid as a {@link List} in the
		 * 							order: fastScannable, slowScannable
		 * @param scanParameters	The number of points in each direction of the grid as a {@link List} in the order:
		 * 							nFast, nSlow
		 * @param bboxParameters	The coordinates of one corner of the rectangular bounding box that	encloses the
		 * 							grid plus its width and height as a {@link List} in the order x, y, width, height
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		private static IScanPointGeneratorModel createTwoAxisGridPointsModel ( final List<Scannable> scannables,
				 										final List<Number> scanParameters,
				 										final List<Number> bboxParameters,
				 										final Map<Mutator, List<Number>> mutatorUses) {

			for (Number param : scanParameters) {
				checkParamPositive(param, TwoAxisGridPointsModel.class.getSimpleName());
				checkParamInteger(param, TwoAxisGridPointsModel.class.getSimpleName());
			}
			TwoAxisGridPointsModel model;
			if (mutatorUses.containsKey(RANDOM_OFFSET)) {
				TwoAxisGridPointsRandomOffsetModel roModel = initBoxBasedModel(
						new TwoAxisGridPointsRandomOffsetModel(), scannables, bboxParameters);
				List<Number> params = mutatorUses.get(RANDOM_OFFSET);
				roModel.setOffset(params.get(OFFSET).doubleValue());
				if (params.size() > 1) {
					roModel.setSeed(mutatorUses.get(RANDOM_OFFSET).get(SEED).intValue());
				}
				model = roModel;
			} else {
				model = initBoxBasedModel(new TwoAxisGridPointsModel(), scannables, bboxParameters);
			}
			model.setxAxisPoints(scanParameters.get(X_AXIS_INDEX).intValue());
			model.setyAxisPoints(scanParameters.get(Y_AXIS_INDEX).intValue());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			return model;
		}

		/**
		 * Creates a {@link TwoAxisGridAxialStepModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the raster as a {@link List} in
		 * 							the order: fastScannable, slowScannable
		 * @param scanParameters	The step size in each direction of the raster as a {@link List} in the order:
		 * 							fastStep, slowStep
		 * @param bboxParameters	The coordinates of one corner of the rectangular bounding box that	encloses the
		 * 							raster plus its width and height as a {@link List} in the order x, y, width, height
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		private static IScanPointGeneratorModel createTwoAxisGridStepModel (final List<Scannable> scannables,
														 final List<Number> scanParameters,
														 final List<Number> bboxParameters,
														 final Map<Mutator, List<Number>> mutatorUses) {

			for (Number param : scanParameters) {
				checkParamPositive(param, TwoAxisGridStepModel.class.getSimpleName());
			}
			TwoAxisGridStepModel model = initBoxBasedModel(new TwoAxisGridStepModel(), scannables, bboxParameters);
			model.setxAxisStep(scanParameters.get(X_AXIS_INDEX).doubleValue());
			model.setyAxisStep(scanParameters.get(Y_AXIS_INDEX).doubleValue());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			return model;
		}

		/**
		 * Creates a {@link TwoAxisSpiralModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the spiral as a {@link List} in
		 * 							the order: fastScannable, slowScannable
		 * @param scanParameters	The required scale factor for the spiral as a single element {@link List}
		 * @param bboxParameters	The coordinates of one corner of the rectangular bounding box that	encloses the
		 * 							spiral plus its width and height as a {@link List} in the order x, y, width, height
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		private static IScanPointGeneratorModel createTwoAxisSpiralModel (final List<Scannable> scannables,
														 final List<Number> scanParameters,
														 final List<Number> bboxParameters,
														 final Map<Mutator, List<Number>> mutatorUses) {

			TwoAxisSpiralModel model = initBoxBasedModel(new TwoAxisSpiralModel(), scannables, bboxParameters);
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setScale(scanParameters.get(SCALE).doubleValue());
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			return model;
		}

		/**
		 * Creates a {@link TwoAxisLissajousModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the lissajous as a {@link List} in
		 * 							the order: fastScannable, slowScannable
		 * @param scanParameters	The parameters that define the lissajous path as a {@link List} in the order:
		 * 							numberOfPoints, A, B
		 * @param bboxParameters	The coordinates of diagonally opposite corners of the rectangular bounding box that
		 * 							encloses the lissajous as a {@link List} in the order x1, y1, x2, y2
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		private static IScanPointGeneratorModel createTwoAxisLissajousModel (final List<Scannable> scannables,
															final List<Number> scanParameters,
															final List<Number> bboxParameters,
															final Map<Mutator, List<Number>> mutatorUses) {

			TwoAxisLissajousModel model = initBoxBasedModel(new TwoAxisLissajousModel(), scannables, bboxParameters);
			model.setA(scanParameters.get(A).doubleValue());
			model.setB(scanParameters.get(B).doubleValue());
			model.setPoints(scanParameters.get(POINTS).intValue());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			return model;
		}

		/**
		 * Creates a {@link TwoAxisLineAxialStepModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the step path as a {@link List} in
		 * 							the order: fastScannable, slowScannable
		 * @param scanParameters	The parameter that defines the step size as a single element {@link List}.
		 * @param blineParameters	The coordinates of the start and end points of the bounding line that
		 * 							encloses the step path as a {@link List} in the order x1, y1, x2, y2
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		private static IScanPointGeneratorModel createTwoAxisLineStepModel (final List<Scannable> scannables,
														   final List<Number> scanParameters,
														   final List<Number> blineParameters,
														   final Map<Mutator, List<Number>> mutatorUses) {
			for (Number param : scanParameters) {
				checkParamPositive(param, TwoAxisLineStepModel.class.getSimpleName());
			}
			TwoAxisLineStepModel model = initLineBasedModel(new TwoAxisLineStepModel(), scannables, blineParameters);
			model.setStep(scanParameters.get(STEP).doubleValue());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			return model;
		}

		/**
		 * Creates a {@link TwoAxisLinePointsModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the path as a {@link List} in
		 * 							the order: fastScannable, slowScannable
		 * @param scanParameters	The parameter that defines the numberof points as a single element {@link List}.
		 * @param blineParameters	The coordinates of the start and end points of the bounding line that
		 * 							encloses the step path as a {@link List} in the order x1, y1, x2, y2
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		private static IScanPointGeneratorModel createTwoAxisLinePointsModel (final List<Scannable> scannables,
				   													final List<Number> scanParameters,
				   													final List<Number> blineParameters,
				   													final Map<Mutator, List<Number>> mutatorUses) {
			for (Number param : scanParameters) {
				checkParamPositive(param, TwoAxisLinePointsModel.class.getSimpleName());
				checkParamInteger(param, TwoAxisLinePointsModel.class.getSimpleName());
			}
			TwoAxisLinePointsModel model = initLineBasedModel(new TwoAxisLinePointsModel(), scannables, blineParameters);
			model.setPoints(scanParameters.get(POINTS).intValue());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			return model;
		}

		/**
		 * Creates a {@link PointModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable}s that relate to the axes of the point as a {@link List} in
		 * 							the order: fastScannable, slowScannable
		 * @param scanParameters	The parameters that define the point location as a single element {@link List}.
		 * @param notUsed			An empty {@link List} not used for this path
		 * @param notUsedMap		An empty {@link Map} not used for this path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		@SuppressWarnings("unused")
		private static IScanPointGeneratorModel createSinglePointModel (final List<Scannable> scannables,
																final List<Number> scanParameters,
																final List<Number> notUsed,
																final Map<Mutator, List<Number>> notUsedMap) {
			TwoAxisPointSingleModel model = new TwoAxisPointSingleModel();
			setAxisNames(model, scannables);
			model.setX(scanParameters.get(X_AXIS_INDEX).doubleValue());
			model.setY(scanParameters.get(Y_AXIS_INDEX).doubleValue());
			return model;
		}

		/**
		 * Creates a {@link AxialStepModel} using the supplied params.
		 *
		 * @param scannables		The {@link Scannable} that relates to the axis of the step path as a {@link List}
		 * 							of 1
		 * @param scanParameters	The parameters that defines the start stop and step size as a {@link List}.
		 * @param notUsed			Not used by {@link AxialStepModel}s
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		@SuppressWarnings("unused")
		private static IScanPointGeneratorModel createAxialStepModel (final List<Scannable> scannables,
														   final List<Number> scanParameters,
														   final List<Number> notUsed,
														   final Map<Mutator, List<Number>> mutatorUses) {

			// This is actually inconsistent as the underlying AxialStepModel requires steps in the negative direction
			// to be negative (see below) but for consistency with classic scanning and the Mapping UI negative steps
			// are disallowed as part of a valid mscan string
			checkOneParameterPositive(scanParameters.get(AX_STEP), AxialStepModel.class.getSimpleName(), AX_STEP);

			// Multiplier to adjust the step value direction
			double sign = scanParameters.get(STOP).doubleValue() < scanParameters.get(START).doubleValue() ? -1	: 1;

			AxialStepModel model = new AxialStepModel(scannables.get(0).getName(),
											scanParameters.get(START).doubleValue(),
											scanParameters.get(STOP).doubleValue(),
											scanParameters.get(AX_STEP).doubleValue() * sign);
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			model.setUnits(List.of(getUnit(scannables.get(0))));
			return model;
		}

		/**
		 * Creates a {@link AxialPointsModel} with a defined number of points using the supplied params.
		 *
		 * @param scannables		The {@link Scannable} that relates to the axis of the step path as a {@link List}
		 * 							of 1
		 * @param scanParameters	The parameters that defines the start stop and no of points as a {@link List}.
		 * @param notUsed			Not used by {@link AxialStepModel}s
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		@SuppressWarnings("unused")
		private static IScanPointGeneratorModel createAxialPointsModel (final List<Scannable> scannables,
														   final List<Number> scanParameters,
														   final List<Number> notUsed,
														   final Map<Mutator, List<Number>> mutatorUses) {
			checkOneParameterPositiveInteger(scanParameters.get(AX_POINTS), "AxialPointsModel", AX_POINTS);

			AxialPointsModel model = new AxialPointsModel(scannables.get(0).getName(),
											scanParameters.get(START).doubleValue(),
											scanParameters.get(STOP).doubleValue(),
											scanParameters.get(AX_POINTS).intValue());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			model.setUnits(List.of(getUnit(scannables.get(0))));
			return model;
		}

		/**
		 * Creates a {@link AxialArrayModel} with the specified positions using the supplied params.
		 *
		 * @param scannables		The {@link Scannable} that relates to the axis of the step path as a {@link List}
		 * 							of 1
		 * @param scanParameters	The parameters that defines the positions as a {@link List}.
		 * @param notUsed			Not used by {@link AxialArrayModel}s
		 * @param mutatorUses		A {@link Map} of mutators to their parameters to be applied to the path
		 * @return					An {@link IScanPathModel} of the requested path and features
		 */
		@SuppressWarnings("unused")
		private static IScanPointGeneratorModel createAxialArrayModel (final List<Scannable> scannables,
				   final List<Number> scanParameters,
				   final List<Number> notUsed,
				   final Map<Mutator, List<Number>> mutatorUses) {
			if (scanParameters.size() < AXIS_ARRAY.valueCount) {
				throw new IllegalArgumentException(PREFIX + "Array model must specify at least" + AXIS_ARRAY.valueCount + "positions");
			}

			AxialArrayModel model = new AxialArrayModel(scannables.get(0).getName(),
				scanParameters.stream().mapToDouble(Number::doubleValue).toArray());
			model.setAlternating(mutatorUses.containsKey(ALTERNATING));
			model.setContinuous(mutatorUses.containsKey(CONTINUOUS));
			model.setUnits(List.of(getUnit(scannables.get(0))));
			return model;
		}

		@SuppressWarnings("unused")
		private static IScanPointGeneratorModel createStaticModel(final List<Scannable> dimensionless,
				   final List<Number> scanParameters,
				   final List<Number> notUsed,
				   final Map<Mutator, List<Number>> notUsedMap) {
			final int size = scanParameters.isEmpty() ? 1 : scanParameters.get(COUNT).intValue();
			checkOneParameterPositiveInteger(size, StaticModel.class.getSimpleName(), COUNT);
			return new StaticModel(size);

		}

		/**
		 * Initialises the  bounds of models based on {@link AbstractBoundingBoxModel}
		 *
		 * @param model				The model to be initialised
		 * @param scannables		The scannables associated with the model in the order: fastScannable, slowScannable
		 * @param bBoxParameters	The parameters of the bounding box in the order x1, y1, x2, y2
		 * @return					The initalised model object
		 */
		private static <T extends AbstractBoundingBoxModel> T initBoxBasedModel(final T model,
				  																final List<Scannable> scannables,
				  																final List<Number> bBoxParameters) {
			model.setBoundingBox(new BoundingBox(
					bBoxParameters.get(X_START_PARAM_INDEX).doubleValue(),
					bBoxParameters.get(Y_START_PARAM_INDEX).doubleValue(),
					bBoxParameters.get(X_LENGTH_PARAM_INDEX).doubleValue(),
					bBoxParameters.get(Y_LENGTH_PARAM_INDEX).doubleValue()));
			setAxisNames(model, scannables);
			return model;
		}

		/**
		 * Initialises the  bounds of models based on {@link AbstractBoundingLineModel}
		 *
		 * @param model				The model to be initialised
		 * @param scannables		The scannables associated with the model in the order: xScannable, yScannable
		 * @param bBoxParameters	The parameters of the bounding line in the order x1, y1, x2, y2
		 * @return					The initalised model object
		 */
		private static <T extends AbstractBoundingLineModel> T initLineBasedModel(final T model,
																				  final List<Scannable> scannables,
																				  final List<Number> blineParameters) {
			model.setBoundingLine(new BoundingLine(
					blineParameters.get(X_START_PARAM_INDEX).doubleValue(),
					blineParameters.get(Y_START_PARAM_INDEX).doubleValue(),
					blineParameters.get(X_LENGTH_PARAM_INDEX).doubleValue(),
					blineParameters.get(Y_LENGTH_PARAM_INDEX).doubleValue()));
			setAxisNames(model, scannables);
			return model;
		}

		/**
		 * Sets the names of the axes on the model from the names of the supplied {@link Scannable}s
		 *
		 * @param model			The model to be initialised
		 * @param scannables	The {@link Scannable}s from which the names will be derived in the order: fastScannable,
		 * 						slowScannable.
		 */
		private static void setAxisNames(final AbstractMapModel model, final List<Scannable> scannables) {
			model.setxAxisName(scannables.get(X_AXIS_INDEX).getName());
			model.setyAxisName(scannables.get(Y_AXIS_INDEX).getName());
			model.setxAxisUnits(getUnit(scannables.get(X_AXIS_INDEX)));
			model.setyAxisUnits(getUnit(scannables.get(Y_AXIS_INDEX)));
		}

		private static String getUnit(Scannable scannable) {
			// If Scannable has units, we get them.
			if (scannable instanceof ScannableMotionUnits) {
				return ((ScannableMotionUnits) scannable).getUserUnits();
			}
			// If it does not, we return "mm"- we don't know the units, but it's consistent with Mapping and the default
			return "mm";
		}

		private static void checkParamInteger(Number param, String className) {
			if (!(param instanceof Integer)) {
				throw new IllegalArgumentException(PREFIX + className + ALL_INTEGER_ERROR);
			}
		}

		private static void checkParamPositive(Number param, String className) {
			if (param.doubleValue() <= 0) {
				throw new IllegalArgumentException(PREFIX + className + ALL_POSITIVE_ERROR);
			}
		}

		private static void checkOneParameterPositive(Number param, String className, int paramOrder) {
			if (param.doubleValue() <= 0) {
				throw new IllegalArgumentException(PREFIX + className + String.format(ONE_POSITIVE_ERROR, paramOrder));
			}
		}

		private static void checkOneParameterPositiveInteger(Number param, String className, int paramOrder) {
			if (!(param instanceof Integer)) {
				throw new IllegalArgumentException(PREFIX + className + String.format(ONE_INTEGER_ERROR, paramOrder));
			}
			checkOneParameterPositive(param, className, paramOrder);
		}
	}
}
