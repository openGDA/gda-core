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

import java.math.BigDecimal;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.AxialStepModel;

class AxialStepModelValidator extends AbstractPointsModelValidator<AxialStepModel> {

	@Override
	public AxialStepModel validate(AxialStepModel model) {
		if (model.getStep() == 0) {
			throw new ModelValidationException("Model step size must be nonzero!", model, "step");
		}
		if (Math.abs(model.getStep()) > Math.abs(model.getStop() - model.getStart())) {
			throw new ModelValidationException("Model step size must be less than length, to allow at least 2 points", model, "start", "stop", "step");
		}
		final int dir = Integer.signum(BigDecimal.valueOf(model.getStop() - model.getStart())
				.divideToIntegralValue(BigDecimal.valueOf(model.getStep()))
				.intValue());
		if (dir < 0) {
			throw new ModelValidationException("Model step is directed in the wrong direction!", model, "start", "stop", "step", "count");
		}
		return super.validate(model);
	}
}
