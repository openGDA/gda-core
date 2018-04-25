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

import static org.eclipse.scanning.points.AbstractScanPointIterator.EMPTY_PY_ARRAY;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.OverlapGridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class OverlapGridGenerator extends AbstractGenerator<OverlapGridModel> {
	OverlapGridGenerator() {
		setLabel("Overlap grid");
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (getModel().getXBeamSize() == 0) throw new ModelValidationException("X beam size cannot be zero", getModel(), "xBeamSize");
		if (getModel().getYBeamSize() == 0) throw new ModelValidationException("Y beam size cannot be zero", getModel(), "yBeamSize");
		if (getModel().getOverlap() < 0) throw new ModelValidationException("Overlap must be positive", getModel(), "overlap");
		if (getModel().getOverlap() >= 1) throw new ModelValidationException("Overlap must be smaller than 1", getModel(), "overlap");
	}

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		OverlapGridModel model = getModel();

		double xBeamDim = model.getXBeamSize();
		double yBeamDim = model.getYBeamSize();

		double overlap = model.getOverlap();
		double xStep = (1 - overlap) * xBeamDim;
		double yStep = (1 - overlap) * yBeamDim;

		double xLength = model.getBoundingBox().getFastAxisLength();
		double yLength = model.getBoundingBox().getSlowAxisLength();

		int yPoints = (int) Math.floor(yLength/yStep + 1);
		int xPoints = (int) Math.floor(xLength/xStep + 1);

		String xName = model.getFastAxisName();
		String yName = model.getSlowAxisName();

		JythonObjectFactory<ScanPointIterator> lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();

		ScanPointIterator outerLine = lineGeneratorFactory.createObject(
				yName, "mm",
				model.getBoundingBox().getSlowAxisStart(),
				model.getBoundingBox().getSlowAxisStart() + (yPoints - 1) * yStep,
				yPoints,
				model.isSnake());

		ScanPointIterator innerLine = lineGeneratorFactory.createObject(
				xName, "mm",
				model.getBoundingBox().getFastAxisStart(),
				model.getBoundingBox().getFastAxisStart() + (xPoints - 1) * xStep,
				xPoints,
				model.isSnake());

		Iterator<?>[] generators = { outerLine, innerLine };
		String[] axisNames = new String[] { xName, yName };
		ScanPointIterator pyIterator = CompoundSpgIteratorFactory.createSpgCompoundGenerator(generators,
				getRegions().toArray(),	axisNames, EMPTY_PY_ARRAY, -1, model.isContinuous());

		return new SpgIterator(pyIterator);
	}

}
