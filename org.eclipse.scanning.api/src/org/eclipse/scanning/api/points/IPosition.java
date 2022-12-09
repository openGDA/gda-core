/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.points;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * A position is a location of one or more values. It is
 * used to group scannables when moving to a position
 * by level.
 * <p>
 * For instance a group of scannables which need to be at a
 * certain location. For example at the start of a scan or
 * for a scan datapoint.
 * <p>
 * <B>NOTE:</B> An object is available using IDeviceService.createPositioner()
 * called IPositioner which can move scannables to a given
 * position by level.
 * <p>
 * Implementations of this class should be immutable where possible. Where this is not
 * possible they should not be modified once they have been fully initialized.
 *
 * @see IRunnableDeviceService
 * @see org.eclipse.scanning.api.scan.event.IPositioner
 *
 * @author Matthew Gerring
 *
 */
public interface IPosition {

	// visibility must be public since this is an interface
	@Deprecated(since = "GDA 9.28", forRemoval = true) // TODO: this logger must also be removed when the deprecated method getValue is removed
	public static final DeprecationLogger logger = DeprecationLogger.getLogger(IPosition.class);

	/**
	 * The number of named scalars in this position
	 * @return number of scalars
	 */
	int size();

	/**
	 * The names of all the scalars set for this position.
	 * For instance 'x' and 'y' for a map or 'Temperature'
	 * <em>Note to implementers:</em> should never return <code>null</code>
	 *
	 * @return name of scalars
	 */
	List<String> getNames();

	/**
	 * Get the data index of this point for a given scan dimension.
	 *
	 * For instance
	 *
	 * @param dimension
	 * @return data index for the given dimension index
	 */
	int getIndex(int dimension);

	/**
	 * Get the index of the data for instance in a scan of temperature from 290 to 300 step 1,
	 * the indices will be 0-10.
	 *
	 * If one dimension has more than one motor with it, for instance x and y in a line scan,
	 * both getIndex("x") and getIndex("y") return the same value.
	 *
	 * @return data index for given axis name
	 */
	int getIndex(String name);

	/**
	 * The value of a named position. For instance {@code get("X")} to return the value of the
	 * X IScannable double.
	 *
	 * @param name
	 * @return value for the given axis name
	 */
	Object get(String name);

	/**
	 * Returns the value as a double, equivalent to  {@code ((Number)get(name)).doubleValue()}
	 * Available for convenience. If the value is not a {@link Number}, a {@link ClassCastException} is thrown.
	 *
	 * @param name
	 * @return value for the given axis name as a double
	 * @throws ClassCastException if the value is not a {@link Number}
	 * @deprecated this method has a misleading name, use {@link #getDouble(String)} instead if you know you
	 * 		want a double, or {@link #get(String)} if you want to get the value whatever its type.
	 */
	@Deprecated(since = "GDA 9.28", forRemoval = true)
	default double getValue(String name) {
		// note: once this method has been removed, we may wish to rename the current get(String) method (which returns an Object) to getValue()
		logger.deprecatedMethod("getValue(String)", "GDA 9.30", "getDouble(String)");
		return getDouble(name);
	}

	/**
	 * Returns the value as a double, equivalent to  {@code ((Number)get(name)).doubleValue()}
	 * Available for convenience. If the value is not a {@link Number}, a {@link ClassCastException} is thrown.
	 *
	 * @param name
	 * @return value for the given axis name as a double
	 * @throws ClassCastException if the value is not a {@link Number}
	 */
	default double getDouble(String name) {
		return ((Number) get(name)).doubleValue();
	}

	/**
	 * Creates a composite position with the values of this position
	 * and the values of the passed in position. The passed in position
	 * is assumed to be the parent in the scan.
	 *
	 * <p>
	 * NOTE The scan names are not calculated on the call to compound
	 * because maintaining the list of names in each dimension is inefficient
	 * to calculate for each point (they do not change). Instead the names are
	 * created once and set into the position using setDimensionNames(...)
	 * available on abstract position.
	 *
	 * @param parent
	 * @return the compounded position
	 */
	IPosition compound(IPosition parent);

	/**
	 * The step where the position was in a scan, if it is a position being
	 * generated from a scan. If not the value will be -1
	 * @return position in a scan or -1
	 */
	default int getStepIndex() {
		return -1;
	}

	/**
	 * The step where the position was in a scan, if it is a position being
	 * generated from a scan. If not the value will be -1
	 * @param step
	 */
	default void setStepIndex(@SuppressWarnings("unused") int step) {
		// do nothing, subclasses may override
	}

	/**
	 * Most scans have rank 1 event though they move more
	 * motors line a line scan or a spiral scan. As scans are
	 * aggregated these scan dimensions sum together.
	 *
	 * Some scans start out as having two dimensions like a grid
	 * or raster scan.
	 *
	 * @return scan rank
	 */
	int getScanRank();

	/**
	 * It is not required of an IPosition to provide getValues() but it
	 * may do so to avoid a new map being built up. Implement this method
	 * to ensure that your position runs faster. The default implementation
	 * works.
	 *
	 * @see org.eclipse.scanning.api.points.MapPosition#getValues()
	 * @see org.eclipse.scanning.api.points.Point#getValues()
	 * @return values as a String to Object Map
	 */
	default Map<String, Object> getValues() {
		final Map<String,Object> values = new LinkedHashMap<>(size());
		for (String name : getNames()) values.put(name, get(name));
		return values;
	}

	/**
	 * It is not required of an IPosition to provide getIndices() but it
	 * may do so to avoid a new map being built up. Implement this method
	 * to ensure that your position runs faster. The default implementation
	 * works.
	 *
	 * @see org.eclipse.scanning.api.points.MapPosition#getIndices()
	 * @see org.eclipse.scanning.api.points.Point#getIndices()
	 * @return map of indices for axis name
	 */
	default Map<String, Integer> getIndices() {
		final Map<String,Integer> indices = new LinkedHashMap<>(size());
		for (String name : getNames()) indices.put(name, getIndex(name));
		return indices;
	}

	/**
	 * Get the exposure time to be used for the detector, in seconds.
	 *
	 * Exposure time is normally set before a 2D scan however for energy scans bands
	 * of different energies can be created each with a different step increment and
	 * potentially different exposure time. It is not possible to set different detectors
	 * with different exposure times during the scan; that would require a map here.
	 * Anthony Hull and I decided this should be part of a future change, when required.
	 *
	 * @return the exposure time in seconds. Can be zero but not negative. If zero then
	 * no change is made to the detector's exposure time setting.
	 */
	public double getExposureTime();

	/**
	 * Call to set the exposure time for the position
	 *
	 * Exposure time is normally set before a 2D scan however for energy scans bands
	 * of different energies can be created each with a different step increment and
	 * potentially different exposure time. It is not possible to set different detectors
	 * with different exposure times during the scan with the mechanism. This can
	 * achieved in a malcolm scan by setting {@link IMalcolmDetectorModel#setExposureTime(double)}
	 * for each detector in the {@link IMalcolmModel}.
	 *
	 * @param time exposure time.
	 */
	public void setExposureTime(double time);

	/**
	 * This method makes dimensionNames if they are null.
	 * Dimensions may contain 1-N axes, e.g. when a Mapping scan is bounded by a 2d region of interest.
	 *
	 * @return axis names for each dimension
	 */
	public List<List<String>> getDimensionNames();

	void setDimensionNames(List<List<String>> dimensionNames);

}
