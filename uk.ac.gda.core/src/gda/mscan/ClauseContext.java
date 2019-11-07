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

package gda.mscan;

import static gda.mscan.element.AreaScanpath.GRID;
import static gda.mscan.element.AreaScanpath.LISSAJOUS;
import static gda.mscan.element.AreaScanpath.ONE_AXIS_NO_OF_POINTS;
import static gda.mscan.element.AreaScanpath.ONE_AXIS_STEP;
import static gda.mscan.element.AreaScanpath.RASTER;
import static gda.mscan.element.AreaScanpath.SINGLE_POINT;
import static gda.mscan.element.AreaScanpath.SPIRAL;
import static gda.mscan.element.AreaScanpath.TWO_AXIS_NO_OF_POINTS;
import static gda.mscan.element.AreaScanpath.TWO_AXIS_STEP;
import static gda.mscan.element.RegionShape.AXIAL;
import static gda.mscan.element.RegionShape.CENTRED_RECTANGLE;
import static gda.mscan.element.RegionShape.CIRCLE;
import static gda.mscan.element.RegionShape.LINE;
import static gda.mscan.element.RegionShape.POINT;
import static gda.mscan.element.RegionShape.POLYGON;
import static gda.mscan.element.RegionShape.RECTANGLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.util.MathUtils;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;

import com.google.common.collect.ImmutableMap;

import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.IMScanDimensionalElementEnum;
import gda.mscan.element.Mutator;
import gda.mscan.element.RegionShape;
import gda.mscan.processor.IClauseElementProcessor;

/**
 * This class is a semi-smart form that is filled in by the MScan parsing process based on the types of the various
 * elements of a Scan Clause and then used to create the {@link ScanRequest} object. It sets and maintains the metadata
 * associated with each of the element types as their corresponding methods are called so the that the
 * {@link IClauseElementProcessor#process} methods that drive them can be kept very simple.
 *
 * @since GDA 9.10
 */
public class ClauseContext {

	// The grammar of allowed next types used to validate the scan clause keyed on the type of the current element
	private static final ImmutableMap<Class<?>, List<Class<?>>> grammar = ImmutableMap.of(
			Scannable.class,    Arrays.asList(Scannable.class, RegionShape.class, Number.class),
			RegionShape.class,  Arrays.asList(Number.class),
			AreaScanpath.class, Arrays.asList(Number.class),
			Number.class,       Arrays.asList(Number.class, AreaScanpath.class, Mutator.class),
			Mutator.class,      Arrays.asList(Number.class, Mutator.class));

	private static final ImmutableMap<RegionShape, List<AreaScanpath>> VALID_COMBINATIONS =
			new ImmutableMap.Builder<RegionShape, List<AreaScanpath>>()
			.put(RECTANGLE,         Arrays.asList(GRID, RASTER ,SPIRAL, LISSAJOUS))
			.put(CENTRED_RECTANGLE, Arrays.asList(GRID, RASTER ,SPIRAL, LISSAJOUS))
			.put(CIRCLE,            Arrays.asList(GRID, RASTER ,SPIRAL, LISSAJOUS))
			.put(POLYGON,           Arrays.asList(GRID, RASTER ,SPIRAL, LISSAJOUS))
			.put(LINE,              Arrays.asList(TWO_AXIS_NO_OF_POINTS, TWO_AXIS_STEP))
			.put(AXIAL,             Arrays.asList(ONE_AXIS_NO_OF_POINTS, ONE_AXIS_STEP))
			.put(POINT,             Arrays.asList(SINGLE_POINT)).build();

	private static final int REQUIRED_SCANNABLES_FOR_AREA = 2;
	private static final List<Class<?>> INVALID_SCANNABLE_SUBTYPES = Arrays.asList(Detector.class, Monitor.class);

	// 'Output' lists which will be read in by the ScanRequest constructor
	private final List<Scannable> scannables = new ArrayList<>();
	private final List<Number> pathParams = new ArrayList<>();
	private final List<Number> shapeParams = new ArrayList<>();

	/**
	 * Map of {@link Mutator} to its {@link List} of parameters (which may be empty)
	 */
	private final Map<Mutator, List<Number>> mutatorUses = new EnumMap<>(Mutator.class);

	private Class<?> previousType = Scannable.class; // Initial value; every Scan Clause starts with a Scannable

	// Metadata with default values in case their corresponding typed element is never processed
	private int requiredParamCount = 0;
	private Optional<RegionShape> regionShape = Optional.empty();
	private boolean regionShapeDefaulted = false;
	private Optional<AreaScanpath> areaScanpath = Optional.empty();
	private boolean areaScanpathDefaulted = false;
	private boolean paramNullCheckValid = true;
	protected boolean validated = false;

	private List<Number> paramsToFill;  // Re-pointable reference used to select whether RegionShape or Scanpath
										// parameters are being stored

	// Update methods that fill in the context. Each of these will (at least) set the previousType field to the type
	// associated with them to be used as the grammar key next time round. They will also either reset or adjust
	// the paramsToFill.

	/**
	 * Adds the supplied {@link Scannable} to the internal list providing it is not full and the supplied instance is
	 * not null.The appropriate corresponding metadata is also set
	 *
	 * @param supplied		The {@link Scannable} to be added to the context
	 *
	 * @return 				The output of the {@link List#add} operation
	 */
	public boolean addScannable(final Scannable supplied) {  // block scannablegroup?
		if (scannables.size() >= REQUIRED_SCANNABLES_FOR_AREA) {
			throw new UnsupportedOperationException(String.format(
					"Too many scannables in scan clause, maximum amount is %d", REQUIRED_SCANNABLES_FOR_AREA));
		}
		// Scannables must be added before regionshape and scanpath
		rejectIfAlreadySet(areaScanpathDefaulted, areaScanpath);
		rejectIfAlreadySet(regionShapeDefaulted, regionShape);
		rejectIfAnyParamsWritten(Scannable.class.getSimpleName());

		nullCheck(supplied, Scannable.class.getSimpleName());
		rejectInvalidSubTypes(supplied);                         // Detectors/Monitors don't belong in scan clauses
		resetParamList();
		paramsToFill = null;
		requiredParamCount = 0;
		previousType = Scannable.class;
		return scannables.add(supplied);
	}

	/**
	 * Store the selected {@link RegionShape} and switch the filler reference to the corresponding param list. Also initialise
	 * other {@link RegionShape} related metadata. The {@link RegionShape} class is used for scans bounded in 2 axes, consequently there
	 * must be 2 {@Scannables} defined for the call to be valid. As {@Scannables} are the first entries in the clause,
	 * these must therefore have already been set. Checks are also made that the {@link RegionShape} selection hasn't already
	 * been defaulted or explicitly set.
	 *
	 * N.B. RegionShapes without a fixed value count will never be 'full' but if the scanpath is defaulted, this will be
	 * picked up in the validateAndAdjust method
	 *
	 * @param supplied		The {@link RegionShape} instance to be stored
	 */
	public void setRegionShape(final RegionShape supplied) {
		// RegionShape must be set before scanpath
		rejectIfAlreadySet(areaScanpathDefaulted, areaScanpath);
		rejectIfAnyParamsWritten(RegionShape.class.getSimpleName());
		// If the default regionShape has already been set or defaulted the reject the supplied one
		rejectIfAlreadySet(regionShapeDefaulted, regionShape);
		nullCheck(supplied, RegionShape.class.getSimpleName());
		rejectIncorrectNumberOfScannables(supplied);
		regionShape = Optional.of(supplied);
		paramsToFill = shapeParams;
		requiredParamCount = supplied.valueCount();
		previousType = RegionShape.class;
		resetParamList();
	}

	/**
	 * Store the selected {@link AreaScanpath} and reset the filler reference to point at the pathParams list. Also
	 * initialise other scanpath related metadata
	 *
	 * @param supplied		The {@link AreaScanpath} instance to be stored
	 */
	public void setAreaScanpath(final AreaScanpath supplied) {
		if (!pathParams.isEmpty()) {
			throw new IllegalStateException(
					"AreaScanpath must be specified before its parameters");
		}
		nullCheck(supplied, AreaScanpath.class.getSimpleName());
		rejectIncorrectNumberOfScannables(supplied);

		// If the default scanpath has already been set or defaulted the reject the supplied one
		rejectIfAlreadySet(areaScanpathDefaulted, areaScanpath);
		rejectIfNoRegionShapeOrNotEnoughRegionShapeParams();
		rejectIfInvalidCombintationOfShapeAndPath(supplied);
		areaScanpath = Optional.of(supplied);
		paramsToFill = pathParams;
		requiredParamCount = supplied.valueCount();
		previousType = AreaScanpath.class;
		resetParamList();
	}

	/**
	 * Adds the supplied {@link Mutator} and an empty parameters {@link ArrayList} to the internal Map providing the
	 * supplied instance is not null.The appropriate corresponding metadata is also set
	 *
	 * @param supplied		The {@link Mutator} to be added to the context
	 */
	public void addMutator(final Mutator supplied) {
		areaScanMustHaveRegionShapePlusAreaScanpath();
		nullCheck(supplied, Mutator.class.getSimpleName());
		if (!areaScanpath.get().supports(supplied)) {
			throw new UnsupportedOperationException(String.format("%s is not supported by the current scan path", supplied.toString()));
		}
		paramNullCheckValid = supplied.shouldParamsBeNullChecked();
		paramsToFill = new ArrayList<>();
		mutatorUses.put(supplied, paramsToFill);
		requiredParamCount = supplied.maxValueCount();
		previousType = Mutator.class;
	}

	/**
	 * Add the supplied number to the param list that has been selected by the setting of a {@link RegionShape}, {@link Mutator}
	 * or {@link AreaScanpath}. If either or both of {@link RegionShape} or {@link AreaScanpath} has not been set,  the default
	 * value is used and the list selection is  made based on the order ({@link RegionShape} is the first thing that can have
	 * parameters in the clause). If too many params are supplied this will also be rejected for all {@link RegionShape}s and
	 * bounded {@link AreaScanpath}s. Multiple {@link Mutator}s may be specified, so contextual validation of their
	 * parameters is done for each.
	 *
	 * @param supplied		The numeric param to be added to the list
	 *
	 * @return The outcome of the {@link List} add operation
	 *
	 * @throws IllegalStateException			if no param list has yet been selected or too many parameters have been
	 * 											supplied for the current list
	 * @throws IllegalArgumentException			if the supplied param is null or has an invalid value.
	 */
	public boolean addParam(final Number supplied) {
		if (scannables.isEmpty()) {
			throw new IllegalStateException("Invalid Scan clause: must contain at least 1 scannable");
		}
		nullCheck(supplied, Number.class.getSimpleName());
		/**
		 * The required order for a scan clause is <scannable(s)><regionShape (if required)><regionShape params> so if
		 * this method is called with two scannables but no regionShape that mans the default regionShape is required.
		 * In the old style SPEC case, there would be no regionShape but only one scannable would be specified.
		 */
		if (!regionShape.isPresent() && REQUIRED_SCANNABLES_FOR_AREA == scannables.size()) {
			// we are doing a 2D map scan with a bounding box with the default RegionShape so set this for future comparisons
			regionShape = Optional.of(RegionShape.defaultValue());
			regionShapeDefaulted = true;
			paramsToFill = shapeParams;
			requiredParamCount = regionShape.get().valueCount();
			resetParamList();
		} else if (paramsToFill == shapeParams && paramsFull() && !areaScanpath.isPresent()) {
			// we are doing a 2D map scan with a bounding box with the default path so set this for future comparisons
			areaScanpath = Optional.of(AreaScanpath.defaultValue());
			areaScanpathDefaulted = true;
			paramsToFill = pathParams;
			requiredParamCount = areaScanpath.get().valueCount();
			resetParamList();
		} else {
			int indexOfParamToAdd = paramsToFill.size();
			// can't use contains here as need to compare just the List objects not their content.
			for (Entry<Mutator, List<Number>> entry : mutatorUses.entrySet()) {
				if(entry.getValue() == paramsToFill) {
					try {
						if (entry.getKey().positiveValuesOnlyFor(indexOfParamToAdd) && supplied.floatValue() < 0) {
							throw new IllegalArgumentException(String.format(
									"%s parameter %d value must be positive", entry.getKey().name(), indexOfParamToAdd));
						}
					} catch(IndexOutOfBoundsException e) {
						throw new IllegalStateException(String.format(
									"Too many parameters supplied for %s", entry.getKey().name()));
					}
				}
			}
		}
		if (paramsToFill == null && paramNullCheckValid) {
			throw new IllegalStateException(
					"Parameters may not be added until either a RegionShape or a Scanpath has been specified");
		}
		// Because of defaulting behaviour, this next condition can only be true for AreaScanpaths
		if (paramsFull()) {
			throw new IllegalStateException(String.format(
					"The required number of params for the %s has already been supplied",
							StringUtils.capitalize(areaScanpath.get().name().toLowerCase())));
		}
		previousType = Number.class;
		return paramsToFill.add(supplied);
	}

	// Utility methods relating to parameter management

	private void resetParamList() {
		if (paramsToFill != null) {
			paramsToFill.clear();
		}
	}

	/**
	 * Indicates whether the currently pointed to parameter list contains the required valueCount
	 * of parameters for {@link RegionShape} or {@link AreaScanpath}, or more (more should not be possible).
	 * N.B.parameter lists for unbounded {@link RegionShape}s e.g. polygons can never be full.
	 *
	 * @return true if the currently reference param list has the required count of params or more
	 */
	public boolean paramsFull() {
		if (paramsToFill == null) {
			return false;
		}
		boolean isBounded = (paramsToFill == shapeParams) ? regionShape.get().hasFixedValueCount() : true;
		return (isBounded && paramsToFill.size() >= requiredParamCount);
	}

	/**
	 * Validates that the context is consistent.
	 */
	public boolean validateAndAdjust() {
		if (scannables.isEmpty() || scannables.size() > REQUIRED_SCANNABLES_FOR_AREA) {
			throw new IllegalStateException("Invalid Scan clause: scan must have the required number of Scannables");
		}
		areaScanMustHaveRegionShapePlusAreaScanpath();
		areaScanMustHaveCorrectNumberOfParametersForRegionShapeAndAreaScanpath();
		forPointRegionShapeScanpathMustBePointAlsoAndParamsMustMatch();
		checkRequiredMutatorParameters();

		//post validate the default case
		validated = true;
		return validated;
	}

	// Output methods to be used once the context has been populated and validated

	/**
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Scannable> getScannables() {
		throwIfNotValidated();
		return Collections.unmodifiableList(scannables);
	}

	/**
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public Map<Mutator, List<Number>> getMutatorUses() {
		throwIfNotValidated();
		return Collections.unmodifiableMap(mutatorUses);
	}

	/**
	 * Returns the list of path params as set
	 *
	 * @return	An unmodifiable list of the path params set.
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Number> getPathParams() {
		throwIfNotValidated();
		return Collections.unmodifiableList(pathParams);
	}

	/**
	 * For single axis scans (e.g. those based on {@link StepModel}), the start and stop values are required
	 * as well as the step size when creating the {@link IScanPathModel}. If validated, these are in the
	 * shapeParams, so these must be added in to the returned list.
	 *
	 * @return	An unmodifiable list  of the path params required to create {@link IScanPathModel} that
	 * 			 corresponds to the previously set {@link AreaScanpath}
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Number> getModelPathParams() {
		throwIfNotValidated();
		List<Number> output;
		if (regionShape.get().getAxisCount() == 1) {
			output = new ArrayList<>(shapeParams);
			output.addAll(pathParams);
		} else {
			output = pathParams;
		}

		return Collections.unmodifiableList(output);
	}

	/**
	 * @return	An unmodifiable list of the shape params set.
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Number> getShapeParams() {
		throwIfNotValidated();
		return Collections.unmodifiableList(shapeParams);
	}

	/**
	 * Mostly returns what {{@link #getShapeParams()} does except for axial scans which ignore the bounding box.
	 * In this case a synthetic bounding box is returned based on the start and stop values of the scan. This
	 * allows the {@link IROI} validation step to complete.
	 *
	 * @return	A List of numbers defining the bounds of the scan
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Number> getBounds() {
		throwIfNotValidated();
		/** For Axial Scans (which only have two (@link shapeParams} we need bounds to satisfy {@link IROI} validation
		 so just construct artificial ones, otherwise return the {@link shapeParams}. **/
		return regionShape.get().equals(AXIAL)
				? Arrays.asList(shapeParams.get(0), shapeParams.get(0), shapeParams.get(1), shapeParams.get(1))
				: getShapeParams();
	}

	/**
	 * @return	The specified {@link AreaScanpath} if set or defaulted, throwing otherwise
	 *
	 * @throws	NoSuchElementException if the  {@link ClauseContext} is not complete and valid.
	 */
	public AreaScanpath getAreaScanpath() {
		throwIfNotValidated();
		return areaScanpath.get();
	}

	/**
	 * @return	The specified {@link RegionShape} if set or defaulted, throwing otherwise
	 *
	 * @throws	NoSuchElementException if the  {@link ClauseContext} is not complete and valid.
	 */
	public RegionShape getRegionShape() {
		throwIfNotValidated();
		return regionShape.get();
	}

	// Status values to be used during completion of the context

	/**
	 * @return  The number of entries in the parameter list currently being filled
	 */
	public int getParamCount() {
		return paramsToFill.size();
	}

	/**
	 * @return  The number of entries required for the current parameter list to be full or
	 * 			in the case of unbounded {@link RegionShape}s the minimum required number
	 */
	public int getRequiredParamCount() {
		return requiredParamCount;
	}

	/**
	 * @return   The type of the last element to be set on this {@link ClauseContext}
	 */
	public Class<?> getPreviousType() {
		return previousType;
	}

	/**
	 * @return   A Map of the allowed sequence of element types based on the previous one
	 */
	public ImmutableMap<Class<?>, List<Class<?>>> grammar() {
		return grammar;
	}

	// input validation methods

	/**
	 * Provides uniform Null checking for all types
	 *
	 * @param obj	The object instance to be checked
	 * @param name	The type name to be used in the exception message
	 *
	 * @throws IllegalArgumentException if the supplied object is null
	 */
	private void nullCheck(final Object obj, final String name) {
		if (obj == null) {
			throw new IllegalArgumentException(String.format("The supplied %s was null", name));
		}
	}

	/**
	 *Checks that both {@link RegionShape} and {@link AreaScanpath} are set for an area based scan
	 *
	 *@throws IllegalStateException if either {@link AreaScanpath} or {@link RegionShape} are not set
	 */
	private void areaScanMustHaveRegionShapePlusAreaScanpath() {
		if (scannables.size() == REQUIRED_SCANNABLES_FOR_AREA && (!regionShape.isPresent() || !areaScanpath.isPresent())) {
			throw new IllegalStateException("Invalid Scan clause: area scan must have both RegionShape and AreaScanpath");
		}
	}

	/**
	 * Checks that the correct number of parameters have been set for the specified {@link AreaScanpath}
	 * and {@link RegionShape} whether bounded or not.
	 *
	 * @throws  IllegalStateException if and incorrect number of parameters has be supplied for either the
	 * 			{@link AreaScanpath} or {@link RegionShape}
	 */
	private void areaScanMustHaveCorrectNumberOfParametersForRegionShapeAndAreaScanpath() {
		if (scannables.size() == REQUIRED_SCANNABLES_FOR_AREA && regionShape.isPresent() && areaScanpath.isPresent()) {
			if (regionShape.get().hasFixedValueCount()) {
				if (regionShape.get().valueCount() != shapeParams.size() || areaScanpath.get().valueCount() != pathParams.size()) {
					throw new IllegalStateException(
							"Invalid Scan clause: clause must have correct no of params for RegionShape and Scanpath");
				}
			} else if (regionShape.get().valueCount() > shapeParams.size() || areaScanpath.get().valueCount() != pathParams.size()) {
				throw new IllegalStateException(
						"Invalid Scan clause: clause must have correct no of params for RegionShape and Scanpath");
			} else if (regionShape.get() == POLYGON && (shapeParams.size() & 1) > 0) {
				throw new IllegalStateException("Invalid Scan clause: Polygon requires an even number of params");
			}
		}
	}

	/**
	 * Checks that the parameter values of the {@link AreaScanpath} and {@link RegiomShape} match and that they both
	 * specify POINT if a point scan has been selected.
	 *
	 * @throws IllegalStateException if there is inconsistency between the @link AreaScanpath} and {@link RegiomShape}
	 * 								 specifications
	 */
	private void forPointRegionShapeScanpathMustBePointAlsoAndParamsMustMatch() {
		if (regionShape.get().equals(POINT)) {
			if (!areaScanpath.get().equals(SINGLE_POINT)) {
				throw new IllegalStateException(
						"Invalid Scan clause: POINT RegionShape can only be used with POINT Scanpath");
			}
			if (!MathUtils.equals(shapeParams.get(0).doubleValue(), pathParams.get(0).doubleValue(), 1e-10)
					|| !MathUtils.equals(shapeParams.get(1).doubleValue(), pathParams.get(1).doubleValue(), 1e-10)) {
				throw new IllegalStateException(
						"Invalid Scan clause: for POINT scan RegionShape and Scanpath parameters must match");
			}
		}
	}

	/**
	 * Confirm that the minimum required number of parameters have been supplied for the specified set of
	 * {@link Mutator}s; for use in the validation stage.
	 *
	 * @throws UnsupportedOperationException if too few parameters have been supplied for any of the {@link Mutator}s
	 */
	private void checkRequiredMutatorParameters() {
		for (Entry<Mutator, List<Number>> entry : mutatorUses.entrySet()) {
			if (entry.getValue().size() < entry.getKey().minValueCount()) {
				throw new UnsupportedOperationException(String.format(
							"Too few parameters supplied for %s", entry.getKey().name()));
			}
		}
	}

	/**
	 * Checks that {@link ClauseContext#validateAndAdjust()} has been successfully called
	 *
	 * @throws UnsupportedOperationException if an attempt is made to read the claus ebefore it has been validated
	 */
	private void throwIfNotValidated() {
		if(!validated) {
			throw new UnsupportedOperationException(
					"The clause context must be validated before output values can be read from it");
		}
	}

	/**
	 * Prevents {@link Detector}s and {@link Monitor}s being specified in scan clauses
	 *
	 * @param supplied	The {@link Scannable} interface of the object being set
	 * @throws UnsupportedOperationException if {@link Detector}s or {@link Monitor}s have been specified
	 */
	private void rejectInvalidSubTypes(final Scannable supplied) {
		for (Class<?> cls : INVALID_SCANNABLE_SUBTYPES) {
			if (cls.isInstance(supplied))
				throw new UnsupportedOperationException(String.format("%s cannot be present in a Scan clause",
						supplied.getClass().getSimpleName()));
		}
	}

	/**
	 * Prevents elements that follow {@link Scannable}s in the clause being set if the required number of
	 * {@link Scannable}s for the scan clause have not already been set
	 *
	 * @param elementName	The type name of element currently being set
	 * @throws UnsupportedOperationException if not enough {@link SCannable}s have been specified to set the element
	 */
	private void rejectIncorrectNumberOfScannables(final IMScanDimensionalElementEnum element) {
		if (scannables.size() != element.getAxisCount()) {
			throw new UnsupportedOperationException(String.format(
				"Invalid Scan clause: Scan with %s requires %d scannables",
				element.getClass().getSimpleName(), element.getAxisCount()));
		}
	}

	/**
	 * Prevents a {@link RegionShape} or {@link AreaScanpath} being set when this has already happened in this clause
	 *
	 * @param defaulted		Indicates the element being tested has been set by defaulting
	 * @param element		An {@link Optional} of the {@link RegionShape} or {@link Area Scanpath} being set
	 * @throws UnsupportedOperationException if an attempt to set {@link RegionShape} or {@link AreaScanpath} happens
	 * 						when these have already been set for this clause
	 */
	private void rejectIfAlreadySet(final boolean defaulted, final Optional<?> element) {
		if (defaulted || element.isPresent()) {
			String elementName = element.get().getClass().getSimpleName();
			String setMessage = String.format("set; Scan clause can only specify one %s", elementName);
			String defaultedMessage = String.format(
					"defaulted, it must be specified straight after the %s", ("RegionShape".equals(elementName))
							? "Scannables"
							: "RegionShape parameters (do you have too many for your specified RegionShape?");

			throw new UnsupportedOperationException(String.format(
					"Invalid Scan clause: %s already %s", elementName, (defaulted) ? defaultedMessage : setMessage));
		}
	}

	/**
	 * Prevents out of order specification of a elements when some parameters have already been set
	 *
	 * @param elementName	The type name of the element being set
	 * @throws IllegalStateException if the supplied element is not valid once params have been specified
	 */
	private void rejectIfAnyParamsWritten(final String elementName) {
		if (paramsToFill != null || !shapeParams.isEmpty() || !pathParams.isEmpty()) {
			throw new IllegalStateException(String.format("%s must be the specified before any parameters", elementName));
		}
	}

	/**
	 * Prevents further elements being set if the {@link RegionShape} and its parameters have not already been
	 *
	 * @throws UnsupportedOperationException if the {@link RegionShape} specification is insufficient
	 */
	private void rejectIfNoRegionShapeOrNotEnoughRegionShapeParams() {
		if (!regionShape.isPresent()) {
			throw new UnsupportedOperationException("Invalid Scan clause: RegionShape must be set before AreaScanpath");
		}
		// At this point the regionShape params should have been filled in so check this is the case
		if ((regionShape.get().hasFixedValueCount() && !paramsFull()) || shapeParams.size() < regionShape.get().valueCount()) {
			throw new UnsupportedOperationException("Invalid Scan clause: not enough parameters for the RegionShape");
		}
	}

	/**
	 * Prevents further elements being set if the specified {@link RegionShape} and {@link AreaScanpath} are incompatible
	 *
	 * @param supplied	the {@link AreaScanpath} to be stored
	 * @throws IllegalStateException for incompatible combinations of {@link RegionShape} and {@link AreaScanpath}
	 */
	private void rejectIfInvalidCombintationOfShapeAndPath(final AreaScanpath supplied) {
		if (!VALID_COMBINATIONS.get(regionShape.get()).contains(supplied)) {
			throw new IllegalStateException(String.format(
					"Invalid Scan clause: %s cannot be combined with %s", regionShape.get(), supplied));
		}
	}

	/**
	 * For tests only, hence package private.
	 *
	 * @return A read only version of paramsToFill
	 */
	final List<Number> paramsToFill() {
		List<Number> view = null;
		if (paramsToFill != null) {
			view = Collections.unmodifiableList(paramsToFill);
		}
		return view;
	}

	final ClauseContext withoutValidation() {
		validated = true;
		return this;
	}
}
