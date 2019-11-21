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

import static org.eclipse.scanning.points.AbstractScanPointIterator.EMPTY_PY_ARRAY;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyList;

public abstract class AbstractLineGenerator<T extends AbstractBoundingLineModel> extends AbstractGenerator<T> {

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getBoundingLine() == null) throw new ModelValidationException("Model must have BoundingLine!", model, "boundingLine");
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
		final T model =  getModel();
		final BoundingLine line = model.getBoundingLine();

		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLineGeneratorFactory();

		final int numPoints = getPoints();
		final double step = getStep();
		final double xStep = step * Math.cos(line.getAngle());
		final double yStep = step * Math.sin(line.getAngle());
		final double minX = line.getxStart() + xStep/2;
		final double minY = line.getyStart() + yStep/2;

		final PyList names =  new PyList(model.getScannableNames());
		final PyList units = new PyList(model.getUnits());
		final double[] start = {minX, minY};
		final double[] stop = {minX + (numPoints - 1) * xStep, minY + (numPoints - 1) * yStep};

		final PPointGenerator lineIt = lineGeneratorFactory.createObject(
				names, units, start, stop, numPoints, model.isAlternating(), model.isContinuous());
		if (getRegions().isEmpty()) {
			return lineIt;
		}
		return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[]{lineIt}, getRegions().toArray(),
				model.getScannableNames(), EMPTY_PY_ARRAY, -1, model.isContinuous());
	}

	protected abstract double getStep();

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		return createPythonPointGenerator().getPointIterator();
	}

	@Override
	public int[] getShape() throws GeneratorException {
		BoundingLine line = getModel().getBoundingLine();
		if (line != null) {
			return new int[] { getPoints() };
		}
		return super.getShape();
	}

	protected abstract int getPoints();

}
