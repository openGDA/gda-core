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
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;

class TwoAxisSpiralModelValidator extends AbstractPointsModelValidator<TwoAxisSpiralModel> {

	@Override
	public TwoAxisSpiralModel validate(TwoAxisSpiralModel model) {
		if (model.getScale() == 0.0) {
			throw new ModelValidationException("Scale must be non-zero!", model, "scale");
		}
		return super.validate(model);
	}
}
