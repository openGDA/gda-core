/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.points;

import static org.eclipse.scanning.points.ROIGenerator.EMPTY_PY_ARRAY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

/**
 * Generator for models that use SPG python module. Python CompoundGenerators are wrapped in PPointGenerators.
 * Provide InitialBounds and FinalBounds for the use of ConsecutiveMultiModel, as both can only operate on
 * SPG generators.
 */
public abstract class AbstractScanPointGenerator<T extends AbstractPointsModel> extends AbstractGenerator<T> implements PySerializable {

	protected PPointGenerator pointGenerator;

	protected AbstractScanPointGenerator(T model) {
		super(model);
		pointGenerator = createPythonPointGenerator();
	}

	protected AbstractScanPointGenerator() {}

	/**
	 * To allow Models to be validated prior to the iterator ever being called (to allow {@code ModelValidationException} to be checked,
	 * Generators are now created once with their model. IPointGeneratorService.createGenerator(model) should be used instead.
	 */
	@Deprecated
	@Override
	public void setModel(T model) throws GeneratorException {
		throw new IllegalArgumentException("Generators should be instantiated with their models, to allow validation at this time.");
	}

	public PPointGenerator getPointGenerator() {
		return pointGenerator;
	}

	protected abstract PPointGenerator createPythonPointGenerator();

	@Override
	public void validate(T model) {
		if (model.getUnits().size() != model.getScannableNames().size()) {
			throw new ModelValidationException("Model must have units for each scannable axis!", model, "name"); // Not actually name
		}
		if (!AbstractPointsModel.supportsContinuous(model.getClass()) && model.isContinuous())
			throw new ModelValidationException(model.getClass().getSimpleName() + " cannot be continuous!", model, "continuous");
		if (!AbstractPointsModel.supportsAlternating(model.getClass()) && model.isAlternating())
			throw new ModelValidationException(model.getClass().getSimpleName() + " cannot be alternating!", model, "alternating");
		if (model instanceof IBoundingBoxModel) {
			IBoundingBoxModel boxModel = (IBoundingBoxModel) model;
			if (boxModel.getBoundingBox() == null)
				throw new ModelValidationException("The model must have a Bounding Box!", boxModel, "boundingBox");
			// As implemented, model width and/or height can be negative,
			// and this flips the slow and/or fast point order.
			if (boxModel.getBoundingBox().getxAxisLength()==0 || boxModel.getBoundingBox().getyAxisLength()==0)
	        	throw new ModelValidationException("The length must not be 0!", boxModel, "boundingBox");
		}
	}

	@Override
	public int size() {
		return pointGenerator.getSize();
	}

	@Override
	public int[] getShape() {
		return pointGenerator.getShape();
	}

	@Override
	public int getRank() {
		return pointGenerator.getRank();
	}

	@Override
	public Iterator<IPosition> iterator() {
		return pointGenerator.iterator();
	}

	@Override
	public List<String> getNames() {
		// Unhashable PyList to ArrayList
		return new ArrayList<>(pointGenerator.getNames());
	}

	@Override
	public PyDictionary toDict() {
		return pointGenerator.toDict();
	}

	/*
	 * ConsecutiveGenerator requires that final bounds (final point + half step) is within DIFF_LIMIT (1e-5) of initial
	 * bounds of next generator.
	 * Any generator that returns StaticPosition is invalid target for continuous consecutive
	 */
	public final IPosition finalBounds() {
		return pointGenerator.getFinalBounds();
	}

	public final IPosition initialBounds() {
		return pointGenerator.getInitialBounds();
	}

	/**
	 * A simplified call to create a CompoundGenerator without regions, mutators and with default values of duration and delay
	 * @param generators  an array of at least 1 PPointGenerator, each being repeated for every point in the generator before it
	 * @param continuous  whether the innermost scan should be continuous if possible- non-Malcolm ('software') scans cannot be continuous.
	 * 		A Continuous scan passes through Bounds rather than Points- bounds are half a step either side of each point in the direction of scanning.
	 * @return a PPointGenerator representing a [Jython] CompoundGenerator made of the above
	 */

	protected PPointGenerator createWrappingCompoundGenerator(PPointGenerator generators, boolean continuous) {
		final JythonObjectFactory<PPointGenerator> compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();

	    return compoundGeneratorFactory.createObject(
	    		Arrays.asList(generators), EMPTY_PY_ARRAY, EMPTY_PY_ARRAY, -1, continuous);
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

	public PPointGenerator createSpgCompoundGenerator(PPointGenerator[] generators,
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

}
