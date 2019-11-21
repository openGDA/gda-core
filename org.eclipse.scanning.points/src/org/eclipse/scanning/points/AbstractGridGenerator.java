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

import static org.eclipse.scanning.points.AbstractScanPointIterator.EMPTY_PY_ARRAY;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyObject;

abstract class AbstractGridGenerator<T extends AbstractTwoAxisGridModel> extends AbstractGenerator<T> {

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getBoundingBox() == null)
			throw new ModelValidationException("The model must have a Bounding Box!", model, "boundingBox");
	    if (model.getBoundingBox().getxAxisLength()==0 || model.getBoundingBox().getyAxisLength()==0)
	        throw new ModelValidationException("The length must not be 0!", model, "boundingBox");
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final T model = getModel();

		final int columns = getXPoints();
		final int rows = getYPoints();
		final String xName = model.getxAxisName();
		final String xUnits = model.getxAxisUnits();
		final String yName = model.getyAxisName();
		final String yUnits = model.getyAxisUnits();
		final double xStep = getXStep();
		final double yStep = getYStep();
		final double minX = model.getBoundingBox().getxAxisStart() + xStep / 2;
		final double minY = model.getBoundingBox().getyAxisStart() + yStep / 2;

		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final PPointGenerator yLine = lineGeneratorFactory.createObject(
				yName, yUnits, minY, minY + (rows - 1) * yStep, rows, model.isAlternating(), model.isContinuous());
		final PPointGenerator xLine = lineGeneratorFactory.createObject(
				xName, xUnits, minX, minX + (columns - 1) * xStep, columns, model.isAlternating(), model.isContinuous());

		final PPointGenerator[] generators = new PPointGenerator[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;

		return CompoundSpgIteratorFactory.createSpgCompoundGenerator(generators,
				getRegions().toArray(),	model.getScannableNames(), getMutators(), -1, model.isContinuous());
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		return createPythonPointGenerator().getPointIterator();
	}

	protected abstract int getXPoints();

	protected abstract double getXStep();

	protected abstract int getYPoints();

	protected abstract double getYStep();

	protected PyObject[] getMutators() {
		return EMPTY_PY_ARRAY;
	}

}
