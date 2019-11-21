/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;

public class TwoAxisPtychographyGenerator extends AbstractGridGenerator<TwoAxisPtychographyModel> {

	TwoAxisPtychographyGenerator() {
		setLabel("Ptychography Model/TwoAxisGridStep (with RandomOffset) Model");
		setDescription(
				"Creates a grid scan by taking equally sized steps in each axis: each position is then offset in both axes by an amount proportional to its step.\nThe scan supports alternating/bidirectional/'snake' mode.");
		setIconPath("icons/scanner--raster.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (getModel().getxBeamSize() == 0)
			throw new ModelValidationException("X beam size cannot be zero", getModel(), "xBeamSize");
		if (getModel().getyBeamSize() == 0)
			throw new ModelValidationException("Y beam size cannot be zero", getModel(), "yBeamSize");
		if (getModel().getOverlap() < 0)
			throw new ModelValidationException("Overlap must be positive", getModel(), "overlap");
		if (getModel().getOverlap() >= 1)
			throw new ModelValidationException("Overlap must be smaller than 1", getModel(), "overlap");
	}

	@Override
	protected int getXPoints() {
		return (int) Math.floor(model.getBoundingBox().getxAxisLength() / getXStep());
	}

	@Override
	protected int getYPoints() {
		return (int) Math.floor(model.getBoundingBox().getyAxisLength() / getYStep());
	}

	@Override
	protected double getXStep() {
		return (1 - model.getOverlap()) * model.getxBeamSize();
	}

	@Override
	protected double getYStep() {
		return (1 - model.getOverlap()) * model.getyBeamSize();
	}

	@Override
	protected PyObject[] getMutators() {

		double maxXOffset = (1 - model.getOverlap()) * model.getxBeamSize() * model.getRandomOffset();
		double maxYOffset = (1 - model.getOverlap()) * model.getyBeamSize() * model.getRandomOffset();

		final PyList axes = new PyList(model.getScannableNames());
		final PyDictionary maxOffset = new PyDictionary();
		maxOffset.put(model.getyAxisName(), maxYOffset);
		maxOffset.put(model.getxAxisName(), maxXOffset);
		JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory
				.JRandomOffsetMutatorFactory();

		return new PyObject[] { randomOffsetMutatorFactory.createObject(model.getSeed(), axes, maxOffset) };
	}

}
