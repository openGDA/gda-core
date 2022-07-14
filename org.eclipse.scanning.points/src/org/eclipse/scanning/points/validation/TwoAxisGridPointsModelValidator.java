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
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;

class TwoAxisGridPointsModelValidator extends AbstractMapModelValidator<TwoAxisGridPointsModel> {

	@Override
	public TwoAxisGridPointsModel validate(TwoAxisGridPointsModel model) {
		// DAQ-3426, discourage use of ambiguous case where unsure whether to put 1 point at start or mid.
		// In the case of a single axis points = 1, an Axial model should be used, and in the case of both, a TwoAxisPoint
		if (model.getyAxisPoints() < 2) {
			throw new ModelValidationException("Model must have at least 2 y-axis points!", model, "yAxisPoints");
		}
		if (model.getxAxisPoints() < 2) {
			throw new ModelValidationException("Model must have at least 2 x-axis points!", model, "xAxisPoints");
		}
		return super.validate(model);
	}
}
