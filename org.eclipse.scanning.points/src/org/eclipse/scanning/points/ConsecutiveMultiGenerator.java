/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package org.eclipse.scanning.points;

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.ConsecutiveMultiModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * A Generator for {@link ConsecutiveMultiModel}s
 */
public class ConsecutiveMultiGenerator extends AbstractMultiGenerator<ConsecutiveMultiModel> {

	public ConsecutiveMultiGenerator(ConsecutiveMultiModel model, IPointGeneratorService service) {
		super(model, service);
	}

	public ConsecutiveMultiGenerator(InterpolatedMultiScanModel model, IPointGeneratorService service) {
		// this constructor is required for InterpolatedConsecutiveModel due to how the point gen service uses
		// reflection to invoke the constructor
		super(model, service);
	}

	@Override
	protected JythonObjectFactory<PPointGenerator> getFactory() {
		return ScanPointGeneratorFactory.JConcatGeneratorFactory();
	}

	@Override
	public ConsecutiveMultiModel validate(ConsecutiveMultiModel model) {
		// Need cachedGenerators to be set, so call super.validate first
		super.validate(model);
		List<String> dimensions = model.getScannableNames();
		List<String> units = model.getUnits();
		for (IScanPointGeneratorModel models : model.getModels()) {
			if (!models.getScannableNames().equals(dimensions)) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same axes!", model,
						"models");
			}
			if (!(models.getUnits().equals(units))) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same units!", model,
						"models");
			}
		}
		if (model.isContinuous()) {

			for (int i = 1; i < cachedGenerators.size(); i++) {
				IPosition previousModel = ((AbstractScanPointGenerator<?>) cachedGenerators.get(i - 1)).finalBounds();
				IPosition nextModel = ((AbstractScanPointGenerator<?>) cachedGenerators.get(i)).initialBounds();
				if (previousModel instanceof StaticPosition || nextModel instanceof StaticPosition) {
					throw new ModelValidationException(
							"All models within a Continuous ConsecutiveModel must be capable of continuousness", model,
							"models");
				}
				for (String axis : model.getScannableNames()) {
					if (Math.abs(previousModel.getValue(axis) - nextModel.getValue(axis)) > DIFF_LIMIT)
						throw new ModelValidationException(
								String.format("Continuous ConsecutiveModels must have the final bounds of each model"
										+ " within %s of the initial bounds of the next.", DIFF_LIMIT),
								model, "models");
				}
			}
		}
		cachedGenerators = null;
		return model;
	}
}
