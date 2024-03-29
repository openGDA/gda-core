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

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyList;

class TwoAxisSpiralGenerator extends AbstractScanPointGenerator<TwoAxisSpiralModel> {

	TwoAxisSpiralGenerator(TwoAxisSpiralModel model) {
		super(model);
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> spiralGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisSpiralGeneratorFactory();

		final TwoAxisSpiralModel model = getModel();

        final List<String> axes =  model.getScannableNames();
        final List<String> units = model.getUnits();
        final double scale = model.getScale();
		final double radiusX = model.getBoundingBox().getxAxisLength() / 2;
		final double radiusY = model.getBoundingBox().getyAxisLength() / 2;
		final double maxRadius = Math.pow(Math.pow(radiusX, 2) + Math.pow(radiusY, 2), 0.5);
		final double xCentre = model.getBoundingBox().getxAxisStart() + radiusX;
		final double yCentre = model.getBoundingBox().getyAxisStart() + radiusY;
        final PyList centre = new PyList(Arrays.asList(xCentre, yCentre));
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();

        final PPointGenerator pointGen = spiralGeneratorFactory.createObject(
				axes, units, centre, maxRadius, scale, alternating);
        return createWrappingCompoundGenerator(pointGen, continuous);
        }

}
