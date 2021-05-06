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
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;

class TwoAxisLineStepModelValidator extends AbstractBoundingLineModelValidator<TwoAxisLineStepModel> {

	@Override
	public TwoAxisLineStepModel validate(TwoAxisLineStepModel model) {
		if (model.getStep() <= 0) {
			throw new ModelValidationException("Model step size must be positive!", model, "step");
		}
		// DAQ-3426, discourage use of ambiguous case where step > length, unsure whether to put 1 point at start or mid.
		// In the case of a step > length, a TwoAxisPoint model should be used instead
		if (model.getStep() > model.getBoundingLine().getLength()) {
			throw new ModelValidationException("Model step larger than its length", model, "step");
		}

		return super.validate(model);
	}
}
