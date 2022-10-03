/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.points.validation;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;

class TwoAxisGridStepModelValidator extends AbstractPointsModelValidator<TwoAxisGridStepModel> {

	@Override
	public TwoAxisGridStepModel validate(TwoAxisGridStepModel model) {
		// super.validate first to avoid div by zero
		super.validate(model);

		if (model.getxAxisStep() == 0) {
			throw new ModelValidationException("Model x-axis step size must be nonzero!", model, "xAxisStep");
		}
		if (model.getyAxisStep() == 0) {
			throw new ModelValidationException("Model y-axis step size must be nonzero!", model, "yAxisStep");
		}

		/*
		 * Technically the following two throws are not required (The generator could simply produce an empty list), but
		 * we throw errors to avoid potential confusion. Plus, this is consistent with the StepGenerator behaviour.
		 */
		if (model.getxAxisStep() / model.getBoundingBox().getxAxisLength() < 0) {
			throw new ModelValidationException("Model x-axis step is directed so as to produce no points!", model, "xAxisStep");
		}
		if (model.getyAxisStep() / model.getBoundingBox().getyAxisLength() < 0) {
			throw new ModelValidationException("Model y-axis step is directed so as to produce no points!", model, "yAxisStep");
		}
		return model;
	}
}
