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
import org.eclipse.scanning.api.event.scan.ScanRequest;

import com.google.common.collect.ImmutableMap;

import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.mscan.element.AreaScanpath;
import gda.mscan.element.Mutator;
import gda.mscan.element.Roi;
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
			Scannable.class,    Arrays.asList(Scannable.class, Roi.class, Number.class),
			Roi.class,          Arrays.asList(Number.class),
			AreaScanpath.class, Arrays.asList(Number.class),
			Number.class,       Arrays.asList(Number.class, AreaScanpath.class, Mutator.class),
			Mutator.class,      Arrays.asList(Number.class, Mutator.class));

	private static final int REQUIRED_SCANNABLES_FOR_AREA = 2;
	private static final List<Class<?>> INVALID_SCANNABLE_SUBTYPES = Arrays.asList(Detector.class, Monitor.class);

	// 'Output' lists which will be read in by the ScanRequest constructor
	private final List<Scannable> scannables = new ArrayList<>();
	private final List<Number> pathParams = new ArrayList<>();
	private final List<Number> roiParams = new ArrayList<>();

	/**
	 * Map of {@link Mutator} to its {@link List} of parameters (which may be empty)
	 */
	private final Map<Mutator, List<Number>> mutatorUses = new EnumMap<>(Mutator.class);

	private Class<?> previousType = Scannable.class; // Initial value; every Scan Clause starts with a Scannable

	// Metadata with default values in case their corresponding typed element is never processed
	private int requiredParamCount = 0;
	private Optional<Roi> roi = Optional.empty();
	private boolean roiDefaulted = false;
	private Optional<AreaScanpath> areaScanpath = Optional.empty();
	private boolean areaScanpathDefaulted = false;
	private boolean paramNullCheckValid = true;
	protected boolean validated = false;

	private List<Number> paramsToFill;  // Re-pointable reference used to select whether Roi or Scanpath parameters are
										// being stored

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
		// Scannables must be added before roi and scanpath
		rejectIfAlreadySet(areaScanpathDefaulted, areaScanpath);
		rejectIfAlreadySet(roiDefaulted, roi);
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
	 * Store the selected {@link Roi} and switch the filler reference to the corresponding param list. Also initialise
	 * other {@link Roi} related metadata. The {@link Roi} class is used for scans bounded in 2 axes, consequently there
	 * must be 2 {@Scannables} defined for the call to be valid. As {@Scannables} are the first entries in the clause,
	 * these must therefore have already been set. Checks are also made that the {@link Roi} selection hasn't already
	 * been defaulted or explicitly set.
	 *
	 * N.B. Rois without a fixed value count will never be 'full' but if the scanpath is defaulted, this will be
	 * picked up in the validateAndAdjust method
	 *
	 * @param supplied		The {@link Roi} instance to be stored
	 */
	public void setRoi(final Roi supplied) {
		// Roi must be set before scanpath
		rejectIfAlreadySet(areaScanpathDefaulted, areaScanpath);
		rejectIfAnyParamsWritten(Roi.class.getSimpleName());
		// If the default roi has already been set or defaulted the reject the supplied one
		rejectIfAlreadySet(roiDefaulted, roi);
		rejectIncorrectNumberOfScannables(Roi.class.getSimpleName());
		nullCheck(supplied, Roi.class.getSimpleName());
		roi = Optional.of(supplied);
		paramsToFill = roiParams;
		requiredParamCount = supplied.valueCount();
		previousType = Roi.class;
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
					"AreaScanpath must be the specified before its parameters");
		}
		rejectIncorrectNumberOfScannables(AreaScanpath.class.getSimpleName());

		// If the default scanpath has already been set or defaulted the reject the supplied one
		rejectIfAlreadySet(areaScanpathDefaulted, areaScanpath);
		rejectIfNoRoiOrNotEnoughRoiParams();
		nullCheck(supplied, AreaScanpath.class.getSimpleName());
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
		areaScanMustHaveRoiPlusAreaScanpath();
		nullCheck(supplied, Mutator.class.getSimpleName());
		if (Mutator.RANDOM_OFFSET == supplied) {
			if (AreaScanpath.GRID != areaScanpath.get()) {
				throw new UnsupportedOperationException("Random offsets may only be applied to Grid paths");
			}
			paramNullCheckValid = true;
		} else {
			if (AreaScanpath.GRID != areaScanpath.get() && AreaScanpath.RASTER != areaScanpath.get()) {
				throw new UnsupportedOperationException("Snake may only be applied to Grid or Raster paths");
			}
			paramNullCheckValid = false;
		}
		paramsToFill = new ArrayList<>();
		mutatorUses.put(supplied, paramsToFill);
		requiredParamCount = supplied.maxValueCount();
		previousType = Mutator.class;
	}

	/**
	 * Add the supplied number to the param list that has been selected by the setting of a {@link Roi}, {@link Mutator}
	 * or {@link AreaScanpath}. If either or both of {@link Roi} or {@link AreaScanpath} has not been set,  the default
	 * value is used and the list selection is  made based on the order ({@link Roi} is the first thing that can have
	 * parameters in the clause). If too many params are supplied this will also be rejected for all {@link Roi}s and
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
		 * The required order for a scan clause is <scannable(s)><roi (if required)><roi params> so if this method is
		 * called which two scannables but no roi that mans the default roi is required. In the old style SPEC case,
		 * there would be no roi but only one scannable would be specified.
		 */
		if (!roi.isPresent() && REQUIRED_SCANNABLES_FOR_AREA == scannables.size()) {
			// we are doing a 2D map scan with a bounding box with the default Roi so set this for future comparisons
			roi = Optional.of(Roi.defaultValue());
			roiDefaulted = true;
			paramsToFill = roiParams;
			requiredParamCount = roi.get().valueCount();
			resetParamList();
		} else if (paramsToFill == roiParams && paramsFull() && !areaScanpath.isPresent()) {
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
					"Parameters may not be added until either a Roi or a Scanpath has been specified");
		}
		// Because of defaulting behaviour, this next condition can only be true for AreaScanpathss
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
	 * of parameters for {@link Roi} or {@link AreaScanpath}, or more (more should not be possible).
	 * N.B.parameter lists for unbounded {@link Roi}s e.g. polygons can never be full.
	 *
	 * @return true if the currently reference param list has the required count of params or more
	 */
	public boolean paramsFull() {
		if (paramsToFill == null) {
			return false;
		}
		boolean isBounded = (paramsToFill == roiParams) ? roi.get().hasFixedValueCount() : true;
		return (isBounded && paramsToFill.size() >= requiredParamCount);
	}

	/**
	 * Validates that the context is consistent.
	 */
	public boolean validateAndAdjust() {
		if (scannables.isEmpty() || scannables.size() > REQUIRED_SCANNABLES_FOR_AREA) {
			throw new IllegalStateException("Invalid Scan clause: scan must have the required number of Scannables");
		}
		areaScanMustHaveRoiPlusAreaScanpath();
		areaScanMustHaveCorrectNumberOfParametersForRoiAndAreaScanpath();
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
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Number> getPathParams() {
		throwIfNotValidated();
		return Collections.unmodifiableList(pathParams);
	}

	/**
	 * @throws	NoSuchElementException if the {@link ClauseContext} is not complete and valid.
	 */
	public List<Number> getRoiParams() {
		throwIfNotValidated();
		return Collections.unmodifiableList(roiParams);
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
	 * @return	The specified {@link Roi} if set or defaulted, throwing otherwise
	 *
	 * @throws	NoSuchElementException if the  {@link ClauseContext} is not complete and valid.
	 */
	public Roi getRoi() {
		throwIfNotValidated();
		return roi.get();
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
	 * 			in the case of unbounded {@link Roi}s the minimum required number
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
	 */
	private void nullCheck(final Object obj, final String name) {
		if (obj == null) {
			throw new IllegalArgumentException(String.format("The supplied %s was null", name));
		}
	}

	/**
	 *Checks that both Roi and AreaScanpath asre set for an area based scan
	 */
	private void areaScanMustHaveRoiPlusAreaScanpath() {
		if (scannables.size() == REQUIRED_SCANNABLES_FOR_AREA && (!roi.isPresent() || !areaScanpath.isPresent())) {
			throw new IllegalStateException("Invalid Scan clause: area scan must have both Roi and AreaScanpath");
		}
	}

	/**
	 * Checks that the correct number of parameters have been set for the specified {@link AreaScanpath}
	 * and {@link Roi} whether bounded or not.
	 */
	private void areaScanMustHaveCorrectNumberOfParametersForRoiAndAreaScanpath() {
		if (scannables.size() == REQUIRED_SCANNABLES_FOR_AREA && roi.isPresent() && areaScanpath.isPresent()) {
			if (roi.get().hasFixedValueCount()) {
				if (roi.get().valueCount() != roiParams.size() || areaScanpath.get().valueCount() != pathParams.size()) {
					throw new IllegalStateException(
							"Invalid Scan clause: clause must have correct no of params for Roi and Scanpath");
				}
			} else if (roi.get().valueCount() > roiParams.size() || areaScanpath.get().valueCount() != pathParams.size()) {
				throw new IllegalStateException(
						"Invalid Scan clause: clause must have correct no of params for Roi and Scanpath");
			} else if (roi.get() == Roi.POLYGON && (roiParams.size() & 1) > 0) {
				throw new IllegalStateException("Invalid Scan clause: Polygon requires an even number of params");
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
	 */
	private void rejectIncorrectNumberOfScannables(final String elementName) {
		if (scannables.size() != REQUIRED_SCANNABLES_FOR_AREA) {
			throw new UnsupportedOperationException(String.format(
				"Invalid Scan clause: Scan with %s requires %d scannables", elementName, REQUIRED_SCANNABLES_FOR_AREA));
		}
	}

	/**
	 * Prevents a {@link Roi} or {@link AreaScanpath} being set when this has already happened in this clause
	 *
	 * @param defaulted		Indicates the element being tessted has beeen set by defaulting
	 * @param element		An {@link Optional} of the {@link Roi} or {@link Area Scanpath} being set
	 */
	private void rejectIfAlreadySet(final boolean defaulted, final Optional<?> element) {
		if (defaulted || element.isPresent()) {
			String elementName = element.get().getClass().getSimpleName();
			String setMessage = String.format("set; Scan clause can only specify one %s", elementName);
			String defaultedMessage = String.format(
					"defaulted, it must be specified straight after the %s", ("Roi".equals(elementName))
							? "Scannables"
							: "Roi parameters (do you have too many for your specified Roi?");

			throw new UnsupportedOperationException(String.format(
					"Invalid Scan clause: %s already %s", elementName, (defaulted) ? defaultedMessage : setMessage));
		}
	}

	/**
	 * Prevents out of order specification of a elements when some parameters have already been set
	 *
	 * @param elementName	The type name of the element being set
	 */
	private void rejectIfAnyParamsWritten(final String elementName) {
		if (paramsToFill != null || !roiParams.isEmpty() || !pathParams.isEmpty()) {
			throw new IllegalStateException(String.format("%s must be the specified before any parameters", elementName));
		}
	}

	/**
	 * Prevents further elements being set if the {@link Roi} and its parameters have not already been
	 */
	private void rejectIfNoRoiOrNotEnoughRoiParams() {
		if (!roi.isPresent()) {
			throw new UnsupportedOperationException("Invalid Scan clause: Roi must be set before AreaScanpath");
		}
		// At this point the roi params should have been filled in so check this is the case
		if ((roi.get().hasFixedValueCount() && !paramsFull()) || roiParams.size() < roi.get().valueCount()) {
			throw new UnsupportedOperationException("Invalid Scan clause: not enough parameters for the Roi");
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
