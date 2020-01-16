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

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

public class TwoAxisGridPointsRandomOffsetGenerator extends AbstractGridGenerator<TwoAxisGridPointsRandomOffsetModel> {

	TwoAxisGridPointsRandomOffsetGenerator(TwoAxisGridPointsRandomOffsetModel model) {
		super(model);
	}

	@Override
	protected int getXPoints() {
		return model.getxAxisPoints();
	}

	@Override
	protected int getYPoints() {
		return model.getyAxisPoints();
	}

	@Override
	protected double getXStep() {
		return model.getBoundingBox().getxAxisLength() / model.getxAxisPoints();
	}

	@Override
	protected double getYStep() {
		return model.getBoundingBox().getyAxisLength() / model.getyAxisPoints();
	}

	@Override
	protected PyObject[] getMutator() {
		final JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();

		final TwoAxisGridPointsRandomOffsetModel model = getModel();

		final List<String> axes = model.getScannableNames();
		final double maxOffset = (model.getOffset() / 100) * (model.isVerticalOrientation() ? getYStep() : getXStep());
		final int seed = model.getSeed();

		final PyDictionary offset = new PyDictionary();
        // "the same standard deviation is used for the random Gaussian offset for each axis." - See {Code @RandomOffsetDecorator}
		offset.put(model.getyAxisName(), maxOffset);
		offset.put(model.getxAxisName(), maxOffset);

		return new PyObject[] { randomOffsetMutatorFactory.createObject(seed, axes, offset) };
	}

	@Override
	public void validate(TwoAxisGridPointsRandomOffsetModel model) {
		super.validate(model);
		if (model.getyAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of y-axis points!", model, "yAxisPoints");
		if (model.getxAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of x-axis points!", model, "xAxisPoints");
	}

}