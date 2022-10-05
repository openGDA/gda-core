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

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;

abstract class AbstractPointsModelValidator<T extends AbstractPointsModel> implements IValidator<T>{

	@Override
	public T validate(T model) {
		if (model.getUnits().size() != model.getScannableNames().size()) {
			throw new ModelValidationException("Model must have units for each scannable axis!", model, "name"); // Not actually name
		}
		if (!AbstractPointsModel.supportsContinuous(model.getClass()) && model.isContinuous()) {
			throw new ModelValidationException(model.getClass().getSimpleName() + " cannot be continuous!", model, "continuous");
		}
		if (!AbstractPointsModel.supportsAlternating(model.getClass()) && model.isAlternating()) {
			throw new ModelValidationException(model.getClass().getSimpleName() + " cannot be alternating!", model, "alternating");
		}
		if (model instanceof IBoundingBoxModel) {
			final IBoundingBoxModel boxModel = (IBoundingBoxModel) model;
			if (boxModel.getBoundingBox() == null) {
				throw new ModelValidationException("The model must have a Bounding Box!", boxModel, "boundingBox");
			}
			// As implemented, model width and/or height can be negative, and this flips the slow and/or fast point order
			if (boxModel.getBoundingBox().getxAxisLength() == 0 || boxModel.getBoundingBox().getyAxisLength() == 0) {
				throw new ModelValidationException("The length must not be 0!", boxModel, "boundingBox");
			}
		}
		return model;
	}
}
