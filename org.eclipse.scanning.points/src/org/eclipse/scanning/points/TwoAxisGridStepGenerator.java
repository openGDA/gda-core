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
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;

class TwoAxisGridStepGenerator extends AbstractGridGenerator<TwoAxisGridStepModel> {

	TwoAxisGridStepGenerator(TwoAxisGridStepModel model) {
		super(model);
	}

	@Override
	public TwoAxisGridStepModel validate(TwoAxisGridStepModel model) {
		// super.validate first to avoid div by zero
		super.validate(model);
		if (model.getxAxisStep() == 0) throw new ModelValidationException("Model x-axis step size must be nonzero!", model, "xAxisStep");
		if (model.getyAxisStep() == 0) throw new ModelValidationException("Model y-axis step size must be nonzero!", model, "yAxisStep");

		// Technically the following two throws are not required
		// (The generator could simply produce an empty list.)
		// but we throw errors to avoid potential confusion.
		// Plus, this is consistent with the StepGenerator behaviour.
		if (model.getxAxisStep()/model.getBoundingBox().getxAxisLength() < 0)
			throw new ModelValidationException("Model x-axis step is directed so as to produce no points!", model, "xAxisStep");
		if (model.getyAxisStep()/model.getBoundingBox().getyAxisLength() < 0)
			throw new ModelValidationException("Model y-axis step is directed so as to produce no points!", model, "yAxisStep");
		return model;
	}

	@Override
	protected int getXPoints() {
		final double xStep = model.getxAxisStep();
		return (int) Math.max(1, Math.floor(model.getBoundingBox().getxAxisLength() / xStep));
	}

	@Override
	protected int getYPoints() {
		final double yStep = model.getyAxisStep();
		return (int) Math.max(1, Math.floor(model.getBoundingBox().getyAxisLength() / yStep));
	}

	@Override
	protected double getXStep() {
		if (getXPoints() == 1){
			return model.getBoundingBox().getxAxisLength();
		}
		return model.getxAxisStep();
	}

	@Override
	protected double getYStep() {
		if (getYPoints() == 1){
			return model.getBoundingBox().getyAxisLength();
		}
		return model.getyAxisStep();
	}

}
