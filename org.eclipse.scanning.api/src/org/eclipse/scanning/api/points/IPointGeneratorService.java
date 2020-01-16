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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.ScanRegion;

/**
 * This service generates points for a given scan type.
 * <p>
 * Usage:
 * <usage><code><pre>
 * IPointGeneratorService pservice  = ... // OSGi
 *
 * LissajousModel model = new LissajousModel();
 *  ... // Set values
 *
 * IPointGenerator<LissajousModel> generator = pservice.createGenerator(model);
 * or
 * IPointGenerator<CompoundModel> generator = pservice.createGenerator(model, [roi[s], [mutators, [durationAndDelay]]]);
 *
 * Unless specified, duration [of a point] takes its default value: -1 (variable)
 * If not using a CompoundModel, these values take the default: all points are included, no mutator will be applied,
 * point duration is variable.
 *
 * Upon the creation of a generator, its model is checked for validity and a Jython point generator is constructed. IPointGenerator
 * are therefore immutable and due to the relative speed of Jython should be produced only when necessary.
 *
 * Iterator<Point> it = generator.iterator();
 * ... // Use iterator in a scan.
 *
 * // Use size to tell user in GUI the whole size. Does not produce all points
 * int size = generator.size();
 *
 * // Create and return all the points in memory (might be large). Avoid if possible
 * List<Point> allPoints = generator.createPoints();
 * </pre></code></usage>
 *
 * @author Matthew Gerring
 *
 */
public interface IPointGeneratorService {

	/**
	 * Used to create a point generator of a given type
	 * @param model
	 * @return
	 */
	<T> IPointGenerator<T> createGenerator(T model) throws GeneratorException;

	/**
	 * Used to create a point generator of a given type.
	 * <p>
	 * Convenience implementation when using only one region of interest
	 *
	 * <p>
	 * If the model has a bounding box, it will be extended automatically to
	 * include the IROI defined. If this is not required, the bounding box of
	 * the model must be manually changed to either reflect this ROI's bounds
	 * or nullified (in which case the BoundingBox will be set to that which
	 * encompasses all IROIs.
	 *
	 * @param model
	 * @param region which implements IPointContainer (most useful) or IROI (less useful because IROI is in the data coordinates, no the motor coordinates)
	 * @return
	 */
	default <T> IPointGenerator<CompoundModel> createGenerator(T model, IROI region) throws GeneratorException {
		return createGenerator(model, Arrays.asList(region));
	}

	default <T> IPointGenerator<CompoundModel> createGenerator(T model, Collection<IROI> regions) throws GeneratorException{
		return createGenerator(model, new ArrayList<>(regions), new ArrayList<>());
	}

	/**
	 * Used to create a point generator of a given type
	 * <p>
	 * If the model has a bounding box, it will be extended automatically to
	 * include the IROI defined. If this is not required, the bounding box of
	 * the model must be manually changed to either reflect this ROI's bounds
	 * or nullified (in which case the BoundingBox will be set to that which
	 * encompasses all IROIs.
	 *
	 * @param model
	 * @param regions a reference to zero or more IROIs for instance
	 * @return
	 * @throws GeneratorException
	 */

	default <T> IPointGenerator<CompoundModel> createGenerator(T model, List<IROI> regions, List<IMutator> mutators) throws GeneratorException {
		return createGenerator(model, regions, mutators, -1f);
	}

	<T> IPointGenerator<CompoundModel> createGenerator(T model, List<IROI> regions, List<IMutator> mutators, float duration) throws GeneratorException;


	/**
	 * Create a nested or compound generator.
	 * Each generator in the varargs argument is another level to the loop.
	 *
	 * @param generators
	 * @return
	 * @throws GeneratorException
	 */
	IPointGenerator<CompoundModel> createCompoundGenerator(IPointGenerator<?>... generators) throws GeneratorException;

	/**
	 * Create a nested or compound generator from a list of models.
	 *
	 * @param cmodel
	 * @return
	 * @throws GeneratorException
	 */
	IPointGenerator<CompoundModel> createCompoundGenerator(CompoundModel cmodel) throws GeneratorException;

	/**
	 *
	 * @param cmodel
	 * @param models
	 * @return regions, never <code>null</code>
	 * @throws GeneratorException
	 */
	List<IROI> findRegions(Object model, Collection<ScanRegion> regions) throws GeneratorException;

	/**
	 * Each IPointGenerator must have a unique id which is used to refer to it in the user interface.
	 * @return
	 */
	@Deprecated
	Collection<String> getRegisteredGenerators();

	/**
	 * Creates a generator by id which has an model associated with it.
	 * The model may either be retrieved and have fields set or the generator
	 * may have a new model set in it.
	 *
	 * @param id
	 * @return
	 */
	@Deprecated
	<T> IPointGenerator<T> createGenerator(String id) throws GeneratorException;

	public <T, R> void setBounds(T model, List<R> regions);
}
