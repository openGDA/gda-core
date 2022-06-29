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

package org.eclipse.scanning.points;

import java.util.List;

import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.eclipse.scanning.jython.JythonObjectFactory;

class AxialPointsGenerator extends AbstractScanPointGenerator<AxialPointsModel> {

	public AxialPointsGenerator(AxialPointsModel model) {
		super(model);
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final AxialPointsModel model = getModel();

		final List<String> name = model.getScannableNames();
        final List<String> units = model.getUnits();
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final double length = model.getStop() - model.getStart();
        final int numPoints = model.getPoints();
        final double denominator = model.isBoundsToFit() ? numPoints : numPoints - 1;
        final double step = numPoints == 1 ? length : length / denominator;
        final double start = IBoundsToFit.getFirstPoint(model.getStart(), numPoints == 1, step, model.isBoundsToFit());
        final double stop   = IBoundsToFit.getFinalPoint(model.getStart(), numPoints, step, model.isBoundsToFit());

        final PPointGenerator pointGen = lineGeneratorFactory.createObject(name, units, start, stop, numPoints, alternating);

        return createWrappingCompoundGenerator(pointGen, continuous);
	}
}
