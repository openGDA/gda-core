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

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.jython.JythonObjectFactory;

public abstract class AbstractLineGenerator<T extends AbstractBoundingLineModel> extends AbstractGenerator<T> {

	protected AbstractLineGenerator(T model) {
		super(model);
	}

	@Override
	public void validate(T model) {
		super.validate(model);
		if (model.getBoundingLine() == null) throw new ModelValidationException("Model must have BoundingLine!", model, "boundingLine");
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLineGeneratorFactory();

		final T model =  getModel();

		final BoundingLine line = model.getBoundingLine();

		final int numPoints = getPoints();
		final double step = getStep();
		final double xStep = step * Math.cos(line.getAngle());
		final double yStep = step * Math.sin(line.getAngle());
		final double minX = line.getxStart() + xStep/2;
		final double minY = line.getyStart() + yStep/2;

		final List<String> axes =  model.getScannableNames();
		final List<String> units = model.getUnits();
		final double[] start = {minX, minY};
		final double[] stop = {minX + (numPoints - 1) * xStep, minY + (numPoints - 1) * yStep};
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

		PPointGenerator lineGen = lineGeneratorFactory.createObject(
				axes, units, start, stop, numPoints, alternating);
		return CompoundGenerator.createWrappingCompoundGenerator(new PPointGenerator[] {lineGen}, continuous);

	}

	protected abstract double getStep();

	protected abstract int getPoints();


}
