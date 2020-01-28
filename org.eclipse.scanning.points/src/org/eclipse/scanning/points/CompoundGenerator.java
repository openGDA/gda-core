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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
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
public class CompoundGenerator extends AbstractGenerator<CompoundModel> implements PySerializable {

	private static Logger logger = LoggerFactory.getLogger(CompoundGenerator.class);

	private IPointGenerator<?>[]     generators;

	private IPointGeneratorService pointGeneratorService;

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

	public CompoundGenerator(IPointGenerator<?>[] generators, IPointGeneratorService pgs) throws GeneratorException {
		pointGeneratorService = pgs;
        CompoundModel model = new CompoundModel();
        for (IPointGenerator<?> g : generators) {
        	if (g instanceof NoModelGenerator) continue;
        	model.addModel(g.getModel());
        	if (g instanceof CompoundGenerator) {
        		// While the jython_spg_interface could handle this for us, we want the information about mutators and regions in the model
        		model.addMutators(((CompoundModel) g.getModel()).getMutators());
        		model.addRegions(((CompoundModel) g.getModel()).getRegions());
        	}
        }
        this.model = model;
        this.generators = generators;
        pointGenerator = createPythonPointGenerator();
	}

	public CompoundGenerator(CompoundModel model, IPointGeneratorService pgs) throws GeneratorException {
		pointGeneratorService = pgs;
		this.model = model;
		try {
        validateModel();
		} catch(ModelValidationException e) {
			throw new GeneratorException(e);
		}
		this.generators = initGenerators();
		pointGenerator = createPythonPointGenerator();
	}

	@Override
	public void validate(CompoundModel model) {
		List<String> axes = new ArrayList<>();
		for (Object smodel : model.getModels()) {
			IScanPathModel imodel = (IScanPathModel) smodel;
			for (String axis : imodel.getScannableNames()) {
				if (axes.contains(axis)) {
					throw new ModelValidationException("Cannot have repeated axis within CompoundModel", model, "models");
				}
				axes.add(axis);
			}
		}
		/*
		 * CompoundValidator from ValidatorService will validate all models after Regions have been applied
		 * So here we just check that axes are mutually exclusive
		 */
	}

    @Override
	public PyDictionary toDict() {
    	return ((PySerializable) pointGenerator).toDict();
    }

	public List<IScanPathModel> getModels(){
		return (List<IScanPathModel>) Arrays.asList(generators).stream().map(IPointGenerator::getModel).collect(Collectors.toList());
	}

	/**
	 * @param generators  an array of at least 1 PPointGenerator, each being repeated for every point in the generator before it
	 * @param regions  a list of IROI regions of interest, any point produced that would be inside at least 1 IROI is included, otherwise
	 * 		they are removed. This occurs *before* Mutators are applied and therefore points in boundary regions may behave unexpectedly
	 * @param axes  list of axes names that IROIs should operate in: should always be either 0 or 2 long.
	 * @param mutators  an array of PyObject Mutators to apply to all points generated by the PPointGenerator this produces.
	 * @param duration  the length of time (in seconds) that each point in the scan should last for. -1 is variable and the default.
	 * @param continuous  whether the innermost scan should be continuous if possible- non-Malcolm ('software') scans cannot be continuous.
	 * 		A Continuous scan passes through Bounds rather than Points- bounds are half a step either side of each point in the direction of scanning.
	 * @return a PPointGenerator representing a [Jython] CompoundGenerator made of the above
	 */

	public static PPointGenerator createSpgCompoundGenerator(PPointGenerator[] generators,
			List<IROI> regions, List<String> axes, PyObject[] mutators, double duration, boolean continuous) {
		final JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();
		final JythonObjectFactory<PyObject> excluderFactory = ScanPointGeneratorFactory.JExcluderFactory();

		final List<PyObject> pyRegions = regions
				.stream()
				.map(ROIGenerator::makePyRoi)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		final PyObject excluder = excluderFactory.createObject(pyRegions.toArray(), axes);
		final PyObject[] excluders = pyRegions.isEmpty() ? EMPTY_PY_ARRAY : new PyObject[] { excluder };

	    return compoundGeneratorFactory.createObject(
	    		generators, excluders, mutators, duration, continuous);
	}

	/**
	 * A simplified call to create a CompoundGenerator without regions, mutators and with default values of duration and delay
	 * @param generators  an array of at least 1 PPointGenerator, each being repeated for every point in the generator before it
	 * @param continuous  whether the innermost scan should be continuous if possible- non-Malcolm ('software') scans cannot be continuous.
	 * 		A Continuous scan passes through Bounds rather than Points- bounds are half a step either side of each point in the direction of scanning.
	 * @return a PPointGenerator representing a [Jython] CompoundGenerator made of the above
	 */

	public static PPointGenerator createWrappingCompoundGenerator(PPointGenerator[] generators, boolean continuous) {
		final JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();

	    return compoundGeneratorFactory.createObject(
	    		generators, EMPTY_PY_ARRAY, EMPTY_PY_ARRAY, -1, continuous);
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();

		final CompoundModel model = getModel();

		final PPointGenerator[] pyGenerators = initGenerators(generators);
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
	private static PyObject[] getMutators(Collection<IMutator> mutators) {
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

	/*
	 * Extract PPointGenerators from IPointGenerators
	 */
	private PPointGenerator[] initGenerators(IPointGenerator<?>[] gens) {
		return Arrays.stream(gens).map(AbstractGenerator.class::cast)
				.map(AbstractGenerator::getPointGenerator)
				.toArray(PPointGenerator[]::new);
	}

	/*
	 * Create IPointGenerators from all models
	 */
	private IPointGenerator<?>[] initGenerators() throws GeneratorException {
			return model.getModels().stream().map(t -> {
				try {
					pointGeneratorService.setBounds(t, pointGeneratorService.findRegions(t, model.getRegions()));
					return pointGeneratorService.createGenerator(t);
				} catch (GeneratorException e) {
					logger.error(String.format("Unable to create generator for %s in %s", t, model), e);
					return null;
				}
			}).filter(Objects::nonNull).toArray(IPointGenerator[]::new);
	}

	public IPointGenerator<?>[] getGenerators() {
		return generators;
	}

}
