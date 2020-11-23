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

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

public class TwoAxisPtychographyGenerator extends AbstractGridGenerator<TwoAxisPtychographyModel> {

	TwoAxisPtychographyGenerator(TwoAxisPtychographyModel model) {
		super(model);
	}

	@Override
	public TwoAxisPtychographyModel validate(TwoAxisPtychographyModel model) {
		if (model.getxBeamSize() == 0)
			throw new ModelValidationException("X beam size cannot be zero", getModel(), "xBeamSize");
		if (model.getyBeamSize() == 0)
			throw new ModelValidationException("Y beam size cannot be zero", getModel(), "yBeamSize");
		if (model.getOverlap() < 0 || model.getOverlap() >= 1)
			throw new ModelValidationException("Overlap must be positive between 0 [inclusive] and 1", getModel(), "overlap");
		return super.validate(model);
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
	protected PyObject[] getMutator() {
		final JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();

		final TwoAxisPtychographyModel model = getModel();

		final List<String> axes = model.getScannableNames();
		final int seed = model.getSeed();
		final double offsetScale = (1 - model.getOverlap())  * model.getRandomOffset();
		final double maxXOffset = offsetScale * model.getxBeamSize();
		final double maxYOffset = offsetScale * model.getyBeamSize();
		final PyDictionary maxOffset = new PyDictionary();
		maxOffset.put(model.getyAxisName(), maxYOffset);
		maxOffset.put(model.getxAxisName(), maxXOffset);

		return new PyObject[] { randomOffsetMutatorFactory.createObject(seed, axes, maxOffset) };
	}

}
