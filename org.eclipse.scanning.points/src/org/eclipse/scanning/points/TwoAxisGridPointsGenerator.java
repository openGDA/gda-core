/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;

public class TwoAxisGridPointsGenerator extends AbstractGridGenerator<TwoAxisGridPointsModel> {

	TwoAxisGridPointsGenerator(TwoAxisGridPointsModel model) {
		super(model);
		setLabel("Two-Axis Grid Points Scan");
		setDescription("Creates a grid scan by slicing each axis of a box into equal sized portions."
				+ "\nThe scan supports alternating/bidirectional/'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected int getXPoints() {
		return model.getxAxisPoints();
	}

	@Override
	protected int getYPoints() {
		return model.getyAxisPoints();
	}

	@Override
	public void validate(TwoAxisGridPointsModel model) {
		super.validate(model);
		if (model.getyAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of y-axis points!", model, "yAxisPoints");
		if (model.getxAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of x-axis points!", model, "xAxisPoints");
	}

	@Override
	protected double getXStep() {
		return model.getBoundingBox().getxAxisLength() / model.getxAxisPoints();
	}

	@Override
	protected double getYStep() {
		return model.getBoundingBox().getyAxisLength() / model.getyAxisPoints();
	}

}
