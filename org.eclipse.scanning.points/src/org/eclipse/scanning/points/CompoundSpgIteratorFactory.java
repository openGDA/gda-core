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

import static org.eclipse.scanning.points.AbstractScanPointIterator.EMPTY_PY_ARRAY;

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
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Matthew Gerring
 *
 */
public class CompoundSpgIteratorFactory {

	protected static class CompoundSgpIterator extends SpgIterator {

		public CompoundSgpIterator(ScanPointIterator pyIterator) {
			super(pyIterator);
		}

		@Override
		public PyDictionary toDict() {
			return ((PySerializable) pyIterator).toDict();
		}

	}

	private static Logger logger = LoggerFactory.getLogger(CompoundSpgIteratorFactory.class);

	protected static PPointGenerator createSpgCompoundGenerator(PPointGenerator[] generators, List<IROI> regions,
			List<String> regionAxes, PyObject[] mutators, int duration, boolean continuous) {
		// TODO Matt D. 2017-10-25: where does this method belong?
		final JythonObjectFactory<PyObject> excluderFactory = ScanPointGeneratorFactory.JExcluderFactory();
		final JythonObjectFactory<PPointGenerator> cpgFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();
		final List<PyObject> pyRegions = regions
				.stream()
				.map(AbstractScanPointIterator::makePyRoi)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		final PyObject excluder = excluderFactory.createObject(pyRegions.toArray(), regionAxes);
		final PyObject[] excluders = pyRegions.isEmpty() ? EMPTY_PY_ARRAY : new PyObject[] { excluder };
		return cpgFactory.createObject(generators, excluders, mutators, duration, continuous);
	}

	public CompoundSgpIterator createCompoundSpgIterator(CompoundGenerator gen) {
		return new CompoundSgpIterator(createSpgCompoundGenerator(gen).getPointIterator());
	}

	/**
	 * A Compound model is continuous if its inner most model is a map model and that model is continuous.
	 * @return
	 */
	private static boolean isContinuous(CompoundModel model) {
		List<Object> models = model.getModels();
		if (!models.isEmpty()) {
			final Object innerModel = models.get(models.size() - 1);
			if (innerModel instanceof IScanPathModel) {
				return ((IScanPathModel) innerModel).isContinuous();
			}
		}
		return false;
	}

	private static PPointGenerator[] initGenerators(CompoundGenerator gen) {
		return Arrays.stream(gen.getGenerators()).map(AbstractGenerator.class::cast)
				.map(AbstractGenerator::createPythonPointGenerator)
				.toArray(PPointGenerator[]::new);
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
	 * @param regions
	 * @return
	 */
	public static PyObject[] getExcluders(Collection<?> regions) {
		// regions are grouped into excluders by scan axes covered
		// two regions are in the same excluder iff they have the same axes
		final LinkedHashMap<List<String>, List<PyObject>> excluders = new LinkedHashMap<>();
		final JythonObjectFactory<?> excluderFactory = ScanPointGeneratorFactory.JExcluderFactory();
		if (regions != null) {
			for (Object region : regions) {
				if (region instanceof ScanRegion) {
					final ScanRegion sr = (ScanRegion) region;
					final Optional<List<PyObject>> excluderOptional = excluders.entrySet().stream()
							.filter(e -> sr.getScannables().containsAll(e.getKey()))
							.map(Map.Entry::getValue)
							.findFirst();
					final List<PyObject> rois = excluderOptional.orElse(new LinkedList<>());
					if (!excluderOptional.isPresent()) {
						excluders.put(sr.getScannables(), rois);
					}
					try {
						final PyObject pyRoi = AbstractScanPointIterator.makePyRoi(region);
						if (pyRoi != null) rois.add(pyRoi);
					} catch (Exception e) {
						logger.error("Could not convert ROI to PyRoi", e);
					}
				} else {
					logger.error("Region wasn't of type ScanRegion");
				}
			}
		}

		return excluders.entrySet().stream()
				.filter(e -> !e.getValue().isEmpty())
				.map(e -> excluderFactory.createObject(e.getValue().toArray(), e.getKey()))
				.toArray(PyObject[]::new);
	}

	public static PPointGenerator createSpgCompoundGenerator(CompoundGenerator compoundGenerator) {
		final JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();

		final PPointGenerator[] generators = initGenerators(compoundGenerator);

	    final PyObject[] excluders = getExcluders(compoundGenerator.getModel().getRegions());
	    final PyObject[] mutators = getMutators(compoundGenerator.getModel().getMutators());
	    final double duration = compoundGenerator.getModel().getDuration();
	    final boolean continuous = isContinuous(compoundGenerator.getModel());

	    return compoundGeneratorFactory.createObject(
	    		generators, excluders, mutators, duration, continuous);
	}

}
