/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.points;

import java.util.List;

import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.eclipse.scanning.jython.JythonObjectFactory;

class AxialStepGenerator extends AbstractScanPointGenerator<AxialStepModel> {

	public AxialStepGenerator(AxialStepModel model) {
		super(model);
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final AxialStepModel model = getModel();

		final List<String> name = model.getScannableNames();
        final List<String> units = model.getUnits();
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final double length = model.getStop() - model.getStart();
        final double step = IBoundsToFit.getLongestFittingStep(length, model.getStep(), model.isBoundsToFit());
        final int numPoints = IBoundsToFit.getPointsOnLine(length, step, model.isBoundsToFit());
        final double start = IBoundsToFit.getFirstPoint(model.getStart(), numPoints == 1, step, model.isBoundsToFit());
        final double stop = IBoundsToFit.getFinalPoint(model.getStart(), numPoints, step, model.isBoundsToFit());


        final PPointGenerator pointGen = lineGeneratorFactory.createObject(name, units, start, stop, numPoints, alternating);

        return createWrappingCompoundGenerator(pointGen, continuous);
	}

}
