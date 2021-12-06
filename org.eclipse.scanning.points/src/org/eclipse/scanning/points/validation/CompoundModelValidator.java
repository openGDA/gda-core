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
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.mutators.RandomOffsetMutator;

class CompoundModelValidator extends AbstractMultiModelValidator<CompoundModel> {

	@Override
	public CompoundModel validate(CompoundModel compoundModel) {
		validateConstituentModels(compoundModel);
		validateRegions(compoundModel);
		validateMutators(compoundModel);
		return compoundModel;
	}

	private void validateConstituentModels(CompoundModel compoundModel) {
		final List<String> axes = new ArrayList<>();
		final IPointGeneratorService pointGeneratorService = ServiceHolder.getPointGeneratorService();

		for (IScanPointGeneratorModel model : compoundModel.getModels()) {
			for (String axis : model.getScannableNames()) {
				if (axes.contains(axis)) {
					throw new ModelValidationException("Cannot have repeated axis within CompoundModel", compoundModel, "models");
				}
				axes.add(axis);
			}
			List<IROI> modelRois = pointGeneratorService.findRegions(model, compoundModel.getRegions());
			pointGeneratorService.setBounds(model, modelRois);
			ServiceHolder.getValidatorService().validate(model);
		}
	}

	private void validateRegions(CompoundModel compoundModel) {
		final Collection<ScanRegion> regions = compoundModel.getRegions();
		if (regions == null) {
			return;
		}

		for (ScanRegion region : regions) {
			if (!compoundModel.getScannableNames().containsAll(region.getScannables())) {
				throw new ModelValidationException(
						"CompoundModel contains regions that operate in scannable axes that it doesn't contain!",
						compoundModel, "regions", "models");
			}
		}
	}

	// Check that any mutators correspond to the axes
	private void validateMutators(CompoundModel compoundModel) {
		final List<IMutator> mutators = compoundModel.getMutators();
		if (mutators == null) {
			return;
		}
		for (IMutator mutator : mutators) {
			if (mutator instanceof RandomOffsetMutator) {
				final RandomOffsetMutator rMut = (RandomOffsetMutator) mutator;
				if (!compoundModel.getScannableNames().containsAll(rMut.getAxes())
						|| !CollectionUtils.isEqualCollection(rMut.getAxes(), rMut.getMaxOffsets().keySet())) {
					throw new ModelValidationException(
							"CompoundModel contains mutators that operate in scannable axes that it doesn't contain!",
							compoundModel, "mutators", "models");
				}
			}
		}
	}
}
