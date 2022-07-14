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
        final double step = model.getStep();
        final double start  = model.getStart(model.getStart(), step);
        final double length = model.getStop() - model.getStart();
        final int numPoints = model.getPointsOnLine(length, step);
        final double stop   = model.getStop(start, length, step);

        final PPointGenerator pointGen = lineGeneratorFactory.createObject(name, units, start, stop, numPoints, alternating);

        return createWrappingCompoundGenerator(pointGen, continuous);
	}

}
