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

import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractGridGenerator<T extends AbstractTwoAxisGridModel> extends AbstractScanPointGenerator<T> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractGridGenerator.class);

	protected AbstractGridGenerator(T model) {
		super(model);
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
//		logger.info("Creating point generator from class {}\n{}", getClass().getName(), Arrays.toString(Thread.currentThread().getStackTrace()));
		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final T model = getModel();

		final int columns = getXPoints();
		final int rows = getYPoints();
		final List<String> axes = model.getScannableNames();
		final String xName = model.getxAxisName();
		final String xUnits = model.getxAxisUnits();
		final String yName = model.getyAxisName();
		final String yUnits = model.getyAxisUnits();
		// length/step<2 => step>length/2, just put point in middle
		final double xStep = getXPoints() == 1 ? model.getBoundingBox().getxAxisLength() : getXStep();
		final double yStep = getYPoints() == 1 ? model.getBoundingBox().getyAxisLength() : getYStep();
		final double minX = model.getBoundingBox().getxAxisStart() + xStep / 2;
		final double minY = model.getBoundingBox().getyAxisStart() + yStep / 2;
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

		final PPointGenerator yLine = lineGeneratorFactory.createObject(
				yName, yUnits, minY, minY + (rows - 1) * yStep, rows,
				// If !model.isAlternateBothAxes(), we only want to alternate the innermost axis
				alternating && (model.isAlternateBothAxes() || model.isVerticalOrientation()));
		final PPointGenerator xLine = lineGeneratorFactory.createObject(
				xName, xUnits, minX, minX + (columns - 1) * xStep, columns,
				alternating && (model.isAlternateBothAxes() || !model.isVerticalOrientation()));

		final PPointGenerator[] generators = new PPointGenerator[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;
		//Must use full CompoundGenerator for grids as 2/4 grid models use RandomOffsetMutator
		return createSpgCompoundGenerator(generators, new ArrayList<>(), axes, getMutator(),
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
