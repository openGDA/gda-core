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
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
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
	protected PPointGenerator createPythonPointGenerator() {
//		logger.info("Creating point generator from class {}\n{}", getClass().getName(), Arrays.toString(Thread.currentThread().getStackTrace()));
		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final T model = getModel();
		final BoundingBox box = model.getBoundingBox();

		final List<String> axes = model.getScannableNames();
		final String xName = model.getxAxisName();
		final String xUnits = model.getxAxisUnits();
		final String yName = model.getyAxisName();
		final String yUnits = model.getyAxisUnits();
		final int xCount = getXPoints();
		final int yCount = getYPoints();
		final double xStep = IBoundsToFit.getLongestFittingStep(box.getxAxisLength(), getXStep(), model.isBoundsToFit());
		final double yStep = IBoundsToFit.getLongestFittingStep(box.getyAxisLength(), getYStep(), model.isBoundsToFit());
		final double minX = IBoundsToFit.getFirstPoint(box.getxAxisStart(), xCount == 1, xStep, model.isBoundsToFit());
		final double minY = IBoundsToFit.getFirstPoint(box.getyAxisStart(), yCount == 1, yStep, model.isBoundsToFit());
		final double maxX = IBoundsToFit.getFinalPoint(box.getxAxisStart(), xCount, xStep, model.isBoundsToFit());
		final double maxY = IBoundsToFit.getFinalPoint(box.getyAxisStart(), yCount, yStep, model.isBoundsToFit());
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

		final PPointGenerator yLine = lineGeneratorFactory.createObject(
				yName, yUnits, minY, maxY, yCount,
				// If !model.isAlternateBothAxes(), we only want to alternate the innermost axis
				alternating && (model.isAlternateBothAxes() || model.isVerticalOrientation()));
		final PPointGenerator xLine = lineGeneratorFactory.createObject(
				xName, xUnits, minX, maxX, xCount,
				alternating && (model.isAlternateBothAxes() || !model.isVerticalOrientation()));

		final PPointGenerator[] generators = new PPointGenerator[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;
		//Must use full CompoundGenerator for grids as 2/4 grid models use RandomOffsetMutator
		return createSpgCompoundGenerator(generators, new ArrayList<>(), axes, getMutator(),
				-1d, continuous);
	}

	protected int getXPoints() {
		return IBoundsToFit.getPointsOnLine(model.getBoundingBox().getxAxisLength(), getXStep(), model.isBoundsToFit());
	}

	protected int getYPoints() {
		return IBoundsToFit.getPointsOnLine(model.getBoundingBox().getyAxisLength(), getYStep(), model.isBoundsToFit());
	}

	protected double getXStep() {
		int points = getXPoints();
		if (!model.isBoundsToFit() && Math.abs(points) != 1) {
			points -= Math.signum(points);
		}
		return model.getBoundingBox().getxAxisLength() / points;
	}

	protected double getYStep() {
		int points = getYPoints();
		if (!model.isBoundsToFit() && Math.abs(points) != 1) {
			points -= Math.signum(points);
		}
		return model.getBoundingBox().getyAxisLength() / points;
	}

	protected PyObject[] getMutator() {
		return EMPTY_PY_ARRAY;
	}

}
