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

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;

public class TwoAxisGridPointsRandomOffsetGenerator extends AbstractGridGenerator<TwoAxisGridPointsRandomOffsetModel> {

	TwoAxisGridPointsRandomOffsetGenerator() {
		setLabel("Two-Axis Grid Points (with RandomOffset) Scan");
		setDescription("Creates a grid scan by slicing each axis of a box into equal sized portions: each position is then offset in both axes by an amount proportional to the fast axis step."
				+ "\nThe scan supports alternating/bidirectional/'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle
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
	protected PyObject[] getMutators() {

		BoundingBox box = model.getBoundingBox();
		double maxOffset = (model.getOffset() / 100) * (model.isVerticalOrientation() ? box.getyAxisLength()/getYPoints() : box.getxAxisLength()/getXPoints());

        final PyList axes = new PyList(model.getScannableNames());
		final PyDictionary offset = new PyDictionary();
        // "the same standard deviation is used for the random Gaussian offset for each axis." - See {Code @RandomOffsetDecorator}
		offset.put(model.getyAxisName(), maxOffset);
		offset.put(model.getxAxisName(), maxOffset);
		JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();

		return new PyObject[] { randomOffsetMutatorFactory.createObject(model.getSeed(), axes, offset) };
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getyAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of y-axis points!", model, "yAxisPoints");
		if (model.getxAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of x-axis points!", model, "xAxisPoints");
		if (model.getxAxisName()==null) throw new ModelValidationException("The model must have a fast axis!\nIt is the motor name used for this axis.", model, "xAxisName");
		if (model.getyAxisName()==null) throw new ModelValidationException("The model must have a slow axis!\nIt is the motor name used for this axis.", model, "yAxisName");
	}

}