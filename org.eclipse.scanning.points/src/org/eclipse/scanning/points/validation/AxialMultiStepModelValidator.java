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

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.points.AbstractScanPointGenerator;

class AxialMultiStepModelValidator extends AbstractMultiModelValidator<AxialMultiStepModel> {

	@Override
	public AxialMultiStepModel validate(AxialMultiStepModel multiModel) {
		// Intensive validation so check super first
		super.validate(multiModel);

		// Check that all models have the same axes & units
		final String axis = multiModel.getScannableNames().get(0);
		final List<String> units = multiModel.getUnits();

		for (AxialStepModel model : multiModel.getModels()) {
			if (!model.getScannableNames().get(0).equals(axis)) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same axes!", multiModel, "models");
			}
			if (!model.getUnits().equals(units)) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same units!", multiModel, "models");
			}
		}

		// For a continuous scan, check that there are no significant gaps (or overlaps) between adjacent models
		if (multiModel.isContinuous()) {
			final List<IPointGenerator<IScanPointGeneratorModel>> pointGenerators = createPointGenerators(multiModel.getModels());
			for (int i = 1; i < pointGenerators.size(); i++) {
				final IPosition previousModelEndPosition = ((AbstractScanPointGenerator<?>) pointGenerators.get(i - 1)).finalBounds();
				final IPosition nextModelStartPosition = ((AbstractScanPointGenerator<?>) pointGenerators.get(i)).initialBounds();

				if (Math.abs(previousModelEndPosition.getValue(axis) - nextModelStartPosition.getValue(axis)) > DIFF_LIMIT)
					throw new ModelValidationException(
							String.format("Continuous ConsecutiveModels must have the final bounds of each model"
								+ " within %s of the initial bounds of the next.", DIFF_LIMIT), multiModel, "models");
			}
		}
		return multiModel;
	}
}
