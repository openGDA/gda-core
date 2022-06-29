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

import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.eclipse.scanning.jython.JythonObjectFactory;

public abstract class AbstractLineGenerator<T extends AbstractBoundingLineModel> extends AbstractScanPointGenerator<T> {

	protected AbstractLineGenerator(T model) {
		super(model);
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLineGeneratorFactory();

		final T model =  getModel();

		final BoundingLine line = model.getBoundingLine();

		final int numPoints = getPoints();
		final double step = getStep();
		final double xLength = line.getLength() * Math.cos(line.getAngle());
		final double yLength = line.getLength() * Math.sin(line.getAngle());
		final double xStep = IBoundsToFit.getLongestFittingStep(xLength, step * Math.cos(line.getAngle()), model.isBoundsToFit());
		final double yStep = IBoundsToFit.getLongestFittingStep(yLength, step * Math.sin(line.getAngle()), model.isBoundsToFit());
		final double minX = IBoundsToFit.getFirstPoint(line.getxStart(), numPoints == 1, xStep, model.isBoundsToFit());
		final double minY = IBoundsToFit.getFirstPoint(line.getyStart(), numPoints == 1, yStep, model.isBoundsToFit());
		final double maxX = IBoundsToFit.getFinalPoint(line.getxStart(), numPoints, xStep, model.isBoundsToFit());
		final double maxY = IBoundsToFit.getFinalPoint(line.getxStart(), numPoints, yStep, model.isBoundsToFit());


		final List<String> axes =  model.getScannableNames();
		final List<String> units = model.getUnits();
		final double[] start = {minX, minY};
		final double[] stop = {maxX, maxY};
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

		final PPointGenerator lineGen = lineGeneratorFactory.createObject(
				axes, units, start, stop, numPoints, alternating);
		return createWrappingCompoundGenerator(lineGen, continuous);

	}

	protected double getStep() {
		int steps = getPoints();
		return model.getBoundingLine().getLength() / ((model.isBoundsToFit() || steps == 1) ? steps : steps - 1);
	}

	protected int getPoints() {
		return IBoundsToFit.getPointsOnLine(model.getBoundingLine().getLength(), getStep(), model.isBoundsToFit());
	}


}
