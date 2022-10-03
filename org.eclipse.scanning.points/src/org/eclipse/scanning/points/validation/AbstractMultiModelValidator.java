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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AbstractMultiModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.points.ServiceHolder;

abstract class AbstractMultiModelValidator<T extends AbstractMultiModel<? extends IScanPointGeneratorModel>> extends AbstractPointsModelValidator<T> {
	// Tolerance allowed when checking whether scan regions are contiguous
	protected static final double DIFF_LIMIT = 1e-5;

	@Override
	public T validate(T multiModel) {
		// Intensive validation so check super first
		super.validate(multiModel);

		if (multiModel.getModels() == null || multiModel.getModels().isEmpty()) {
			throw new ModelValidationException("MultiModel requires at least one internal model!", multiModel, "models");
		}

		for (IScanPointGeneratorModel model : multiModel.getModels()) {
			if (model instanceof AbstractTwoAxisGridModel) {
				throw new ModelValidationException("MultiGenerators cannot operate on already Compounded models, like grids.", multiModel, "models");
			}
			if (model.isAlternating()) {
				throw new ModelValidationException("MultiGenerators cannot contain Alternating models, set it on the multimodel instead", multiModel, "models");
			}
		}
		return multiModel;
	}

	/**
	 * Create scan point generators corresponding to the models passed.<br>
	 * This serves also to validate the models, as the constructor of each generator will validate its model.
	 *
	 * @param models
	 *            list of models for which generators are to be created
	 * @return list of generators created
	 */
	protected static List<IPointGenerator<IScanPointGeneratorModel>> createPointGenerators(List<? extends IScanPointGeneratorModel> models) {
		final IPointGeneratorService pointGeneratorService = ServiceHolder.getPointGeneratorService();
		final List<IPointGenerator<IScanPointGeneratorModel>> generators = new ArrayList<>(models.size());
		for (IScanPointGeneratorModel model : models) {
			try {
				generators.add(pointGeneratorService.createGenerator(model));
			} catch (GeneratorException e) {
				throw new ModelValidationException(e);
			}
		}
		return generators;
	}
}
