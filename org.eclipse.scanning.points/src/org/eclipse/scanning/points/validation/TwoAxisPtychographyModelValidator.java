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
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;

class TwoAxisPtychographyModelValidator extends AbstractPointsModelValidator<TwoAxisPtychographyModel> {

	@Override
	public TwoAxisPtychographyModel validate(TwoAxisPtychographyModel model) {
		if (model.getxBeamSize() == 0) {
			throw new ModelValidationException("X beam size cannot be zero", model, "xBeamSize");
		}
		if (model.getyBeamSize() == 0) {
			throw new ModelValidationException("Y beam size cannot be zero", model, "yBeamSize");
		}
		if (model.getOverlap() < 0 || model.getOverlap() >= 1) {
			throw new ModelValidationException("Overlap must be positive between 0 [inclusive] and 1", model, "overlap");
		}
		return super.validate(model);
	}
}
