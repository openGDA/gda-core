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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.python.core.PyDictionary;

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

}
