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

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.PtychographyGridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;

public class PtychographyGridGenerator extends AbstractGenerator<PtychographyGridModel> {
	PtychographyGridGenerator() {
		setLabel("Ptychography Grid");
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (getModel().getxBeamSize() == 0) throw new ModelValidationException("X beam size cannot be zero", getModel(), "xBeamSize");
		if (getModel().getyBeamSize() == 0) throw new ModelValidationException("Y beam size cannot be zero", getModel(), "yBeamSize");
		if (getModel().getOverlap() < 0) throw new ModelValidationException("Overlap must be positive", getModel(), "overlap");
		if (getModel().getOverlap() >= 1) throw new ModelValidationException("Overlap must be smaller than 1", getModel(), "overlap");
	}

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		PtychographyGridModel model = getModel();

		double xBeamDim = model.getxBeamSize();
		double yBeamDim = model.getyBeamSize();

		double overlap = model.getOverlap();
		double xStep = (1 - overlap) * xBeamDim;
		double yStep = (1 - overlap) * yBeamDim;

		double xLength = model.getBoundingBox().getxAxisLength();
		double yLength = model.getBoundingBox().getyAxisLength();

		int yPoints = (int) Math.floor(yLength/yStep + 1);
		int xPoints = (int) Math.floor(xLength/xStep + 1);

		String xName = model.getxAxisName();
		String xUnits = model.getxAxisUnits();
		String yName = model.getyAxisName();
		String yUnits = model.getyAxisUnits();

		JythonObjectFactory<ScanPointIterator> lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();

		ScanPointIterator yLine = lineGeneratorFactory.createObject(
				yName, yUnits,
				model.getBoundingBox().getyAxisStart(),
				model.getBoundingBox().getyAxisStart() + (yPoints - 1) * yStep,
				yPoints,
				model.isSnake());

		ScanPointIterator xLine = lineGeneratorFactory.createObject(
				xName, xUnits,
				model.getBoundingBox().getxAxisStart(),
				model.getBoundingBox().getxAxisStart() + (xPoints - 1) * xStep,
				xPoints,
				model.isSnake());

		JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();
		int seed = model.getSeed();
		double maxXOffset = xStep * model.getRandomOffset();
		double maxYOffset = yStep * model.getRandomOffset();

		final PyDictionary maxOffset = new PyDictionary();
        maxOffset.put(yName, maxYOffset);
        maxOffset.put(xName, maxXOffset);

        final PyList axes = new PyList(Arrays.asList(yName, xName));
		final PyObject randomOffset = randomOffsetMutatorFactory.createObject(seed, axes, maxOffset);

		final Iterator<?>[] generators = new Iterator<?>[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;
        final PyObject[] mutators = { randomOffset };

        final String[] axisNames = new String[] { xName, yName };
		final ScanPointIterator pyIterator = CompoundSpgIteratorFactory.createSpgCompoundGenerator(
				generators, getRegions().toArray(), axisNames, mutators, -1, model.isContinuous());

		return new SpgIterator(pyIterator);
	}

}
