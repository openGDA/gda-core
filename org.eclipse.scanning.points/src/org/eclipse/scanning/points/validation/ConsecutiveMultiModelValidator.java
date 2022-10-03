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
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.points.AbstractScanPointGenerator;

class ConsecutiveMultiModelValidator extends AbstractMultiModelValidator<ConsecutiveMultiModel> {

	@Override
	public ConsecutiveMultiModel validate(ConsecutiveMultiModel multiModel) {
		super.validate(multiModel);

		final List<String> dimensions = multiModel.getScannableNames();
		final List<String> units = multiModel.getUnits();
		for (IScanPointGeneratorModel model : multiModel.getModels()) {
			if (!model.getScannableNames().equals(dimensions)) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same axes!", multiModel, "models");
			}
			if (!(model.getUnits().equals(units))) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same units!", multiModel, "models");
			}
		}

		if (multiModel.isContinuous()) {
			validateContinuousMultiModel(multiModel);
		}

		return multiModel;
	}

	/**
	 * Check that all sub-models are capable of continuousness and that the points generated from consecutive models
	 * neither overlap nor have a significant gap between them
	 */
	private void validateContinuousMultiModel(ConsecutiveMultiModel multiModel) {
		final List<IPointGenerator<IScanPointGeneratorModel>> pointGenerators = createPointGenerators(multiModel.getModels());
		for (int i = 1; i < pointGenerators.size(); i++) {
			final IPosition previousModelEndPosition = ((AbstractScanPointGenerator<?>) pointGenerators.get(i - 1)).finalBounds();
			final IPosition nextModelStartPosition = ((AbstractScanPointGenerator<?>) pointGenerators.get(i)).initialBounds();

			if (previousModelEndPosition instanceof StaticPosition || nextModelStartPosition instanceof StaticPosition) {
				throw new ModelValidationException(
						"All models within a Continuous ConsecutiveModel must be capable of continuousness", multiModel, "models");
			}
			for (String axis : multiModel.getScannableNames()) {
				if (Math.abs(previousModelEndPosition.getValue(axis) - nextModelStartPosition.getValue(axis)) > DIFF_LIMIT)
					throw new ModelValidationException(
							String.format("Continuous ConsecutiveModels must have the final bounds of each model"
									+ " within %s of the initial bounds of the next.", DIFF_LIMIT),
							multiModel, "models");
			}
		}
	}
}
