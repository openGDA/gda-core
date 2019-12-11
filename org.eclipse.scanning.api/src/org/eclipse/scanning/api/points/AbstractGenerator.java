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
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractGenerator<T extends AbstractPointsModel> implements IPointGenerator<T> {

	private static Logger logger = LoggerFactory.getLogger(AbstractGenerator.class);

	protected T model;
	protected PPointGenerator pointGenerator;

	protected AbstractGenerator(T model) {
		this.model = model;
		validateModel();
		pointGenerator = createPythonPointGenerator();
	}

	protected AbstractGenerator() {
		// For validating AxialMultiStepModels and for NoModel generators only
	}

	@Override
	public T getModel() {
		return model;
	}

	/**
	 * To allow Models to be validated prior to the iterator ever being called (to allow {@code ModelValidationException} to be checked,
	 * Generators are now created once with their model. IPointGeneratorService.createGenerator(model) should be used instead.
	 */
	@Deprecated
	@Override
	public void setModel(T model) throws GeneratorException {
		throw new IllegalArgumentException("Generators should be instantiated with their models, to allow validation at this time.");
	}

	@Override
	public ScanPointIterator iterator() {
		return pointGenerator.getPointIterator();
	}

	public abstract PPointGenerator createPythonPointGenerator();

	/**
	 * If the given model is considered "invalid", this method throws a
	 * ModelValidationException explaining why it is considered invalid.
	 * Otherwise, just returns. A model should be considered invalid if its
	 * parameters would cause the generator implementation to hang or crash.
	 *
	 * @throw exception if model invalid
	 */
	protected final void validateModel() throws ModelValidationException {
		validate(this.model);
	}

	@Override
	public void validate(T model) throws ModelValidationException {
		logger.info("{} validating model: {}", getClass().getSimpleName(), model);
		if (model.getScannableNames() == null || model.getScannableNames().contains(null)) throw new ModelValidationException("The model must have all the names of the scannables it is acting upon!", model, "name");
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
	public int getRank() throws GeneratorException {
		return getShape().length;
	}

	@Override
	public int[] getShape() throws GeneratorException {
		return pointGenerator.getShape();
	}

	@Override
	public List<IPosition> createPoints() throws GeneratorException {
		logger.info("{} creating points from: {}", getClass().getSimpleName(), model);
		final List<IPosition> points = new ArrayList<>();
		iterator().forEachRemaining(points::add);
		return points;
	}

	@Override
	public List<String> getNames(){
		return new ArrayList<>(pointGenerator.getNames());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		AbstractGenerator<?> other = (AbstractGenerator<?>) obj;
		if (model == null) {
			return other.model == null;
		}
		return model.equals(other.model);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [model=" + model + "]";
	}

}
