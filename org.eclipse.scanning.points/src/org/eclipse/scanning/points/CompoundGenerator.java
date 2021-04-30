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
package org.eclipse.scanning.points;

import static org.eclipse.scanning.points.ROIGenerator.EMPTY_PY_ARRAY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * CompoundGenerators (analogously to their Python equivalents) handle not only compounding scans within one another,
 * but additionally mutators, regions of interests, the duration of a point within a scan.
 *
 * Compounding a scan means repeating every point of a scan for each point of another:
 *
 * gen1.createPoints() = [("x": 1), ("x" :3), ("x": 5)]
 * -> a scan visiting points in a scannable axis "x", starting at 0, stopping at 5 and stepping 2.
 * gen2.createPoints() = [("y": 0.6), ("y": 0.9),("y": 1.2)}
 * -> and a scan visiting points in axis "y" from 0.6 to 1.2 in steps of 0.3
 * cgen = new CompoundGenerator(gen1, gen2)
 * cgen.createPoints() = [("x" : 1, "y": 0.6), ("x": 1, "y":0.9), ("x":1, "y":1.2), ("x":2, "y": 0.3), ...]
 *
 * Compounding may either be performed on PPointGenerators or GDA ScanPathModels through constructing a CompoundModel
 *
 * Compounding may be performed an arbitrary number of levels deep, although no two models or generators may share axes
 *
 * @author Matthew Gerring
 * @author Joseph Ware
 *
 */
public class CompoundGenerator extends AbstractMultiGenerator<CompoundModel> {

	private static Logger logger = LoggerFactory.getLogger(CompoundGenerator.class);

	/**
	 * @param generators an array of IPointGenerator[s], which are compounded together to produce a new PPointGenerator
	 * represented by this generator's CompoundModel. The rightmost/last generator is the "fastest" to change, with every
	 * point that it produces being repeated for every point of its leftwards neighbour, itself repeated for every point
	 * of its neighbour until the outermost generator.
	 * @throws GeneratorException when validation throws a ModelValidationException for the CompoundModel constructed from
	 * the IPointGenerators- e.g. when generators contains nulls, when more than one generator acts on an axis, or when
	 * something causes the call to the Python CompoundGenerator to fail.
	 *
	 * If any of the generators passed to this constructor are {@link NoModelGenerator}, the CompoundModel will be
	 * an incomplete representation of the scan, and any GeneratorException thrown may have less complete information.
	 */

	public CompoundGenerator(List<IPointGenerator<? extends IScanPointGeneratorModel>> generators, IPointGeneratorService pgs) throws GeneratorException {
		super(pgs);
        CompoundModel model = new CompoundModel();
        for (IPointGenerator<? extends IScanPointGeneratorModel> g : generators) {
        	if (g instanceof NoModelGenerator) continue;
        	model.addModel(g.getModel());
        	if (g instanceof CompoundGenerator) {
        		// While the jython_spg_interface could handle this for us, we want the information about mutators and regions in the model
        		model.addMutators(((CompoundModel) g.getModel()).getMutators());
        		model.addRegions(((CompoundModel) g.getModel()).getRegions());
        	}
        }
		this.model = model;
		try {
			// Not guaranteed to catch all non-valid models because of NoModelGenerators
			validateModel();
		} catch (ModelValidationException e) {
			throw new GeneratorException(e);
		}
        this.generators = new ArrayList<>(generators);
        pointGenerator = createPythonPointGenerator();
	}

	public CompoundGenerator(CompoundModel model, IPointGeneratorService pgs) {
		super(model, pgs);
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = getFactory();

		final CompoundModel model = getModel();

		final PPointGenerator[] pyGenerators = initGenerators();
	    final PyObject[] mutators = getMutators(model.getMutators());
	    final PyObject[] excluders = getExcluders(model.getRegions());
	    final double duration = model.getDuration();
	    final boolean continuous = model.isContinuous();
	    return compoundGeneratorFactory.createObject(pyGenerators, excluders, mutators, duration, continuous);

	}

	/**
	 * Creates an array of python objects representing the mutators
	 * @param mutators
	 * @return
	 */
	private PyObject[] getMutators(Collection<IMutator> mutators) {
		if (mutators != null) {
			return mutators.stream().map(IMutator::getMutatorAsJythonObject).toArray(PyObject[]::new);
		}
		return EMPTY_PY_ARRAY;
	}

	/**
	 * Creates an array of python objects representing the excluders
	 * Each Excluder is made of all IROIs in shared axes with each other
	 * @param regions
	 * @return
	 */
	public static PyObject[] getExcluders(Collection<ScanRegion> regions) {
		final JythonObjectFactory<PyObject> excluderFactory = ScanPointGeneratorFactory.JExcluderFactory();
		// regions are grouped into excluders by scan axes covered
		// two regions are in the same excluder iff they have the same axes
		final LinkedHashMap<List<String>, List<PyObject>> excluders = new LinkedHashMap<>();
		if (regions != null) {
			for (ScanRegion region : regions) {
				final Optional<List<PyObject>> excluderOptional = excluders.entrySet().stream()
						.filter(e -> region.getScannables().containsAll(e.getKey())).map(Map.Entry::getValue)
						.findFirst();
				final List<PyObject> rois = excluderOptional.orElse(new LinkedList<>());
				if (!excluderOptional.isPresent()) {
					excluders.put(region.getScannables(), rois);
				}
				try {
					final PyObject pyRoi = ROIGenerator.makePyRoi(region);
					if (pyRoi != null) rois.add(pyRoi);
				} catch (Exception e) {
					logger.error("Could not convert ROI to PyRoi", e);
				}
			}
		}
		return excluders.entrySet().stream()
				.filter(e -> !e.getValue().isEmpty()).map(e -> excluderFactory.createObject(e.getValue()
				.toArray(), e.getKey())).toArray(PyObject[]::new);
	}

	@Override
	protected JythonObjectFactory<PPointGenerator> getFactory() {
		return ScanPointGeneratorFactory.JCompoundGeneratorFactory();
	}

}
