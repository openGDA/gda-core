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

import static org.eclipse.scanning.points.ROIGenerator.EMPTY_PY_ARRAY;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyObject;

abstract class AbstractGridGenerator<T extends AbstractTwoAxisGridModel> extends AbstractGenerator<T> {

	protected AbstractGridGenerator(T model) {
		super(model);
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final T model = getModel();

		final int columns = getXPoints();
		final int rows = getYPoints();
		final List<String> axes = model.getScannableNames();
		final String xName = model.getxAxisName();
		final String xUnits = model.getxAxisUnits();
		final String yName = model.getyAxisName();
		final String yUnits = model.getyAxisUnits();
		final double xStep = getXStep();
		final double yStep = getYStep();
		final double minX = model.getBoundingBox().getxAxisStart() + xStep / 2;
		final double minY = model.getBoundingBox().getyAxisStart() + yStep / 2;
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

		final PPointGenerator yLine = lineGeneratorFactory.createObject(
				yName, yUnits, minY, minY + (rows - 1) * yStep, rows, alternating);
		final PPointGenerator xLine = lineGeneratorFactory.createObject(
				xName, xUnits, minX, minX + (columns - 1) * xStep, columns, alternating);

		final PPointGenerator[] generators = new PPointGenerator[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;
		//Must use full CompoundGenerator for grids as 2/4 grid models use RandomOffsetMutator
		return CompoundGenerator.createSpgCompoundGenerator(generators, new ArrayList<>(), axes, getMutator(),
				-1d, continuous);
	}

	protected abstract int getXPoints();

	protected abstract double getXStep();

	protected abstract int getYPoints();

	protected abstract double getYStep();

	protected PyObject[] getMutator() {
		return EMPTY_PY_ARRAY;
	}

}
