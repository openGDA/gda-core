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

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

class GridGenerator extends AbstractGenerator<GridModel> {

	GridGenerator() {
		setLabel("Grid");
		setDescription("Creates a grid scan (a scan of x and y).\nThe scan support alternating/bidirectional/'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getyAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of y-axis points!", model, "yAxisPoints");
		if (model.getxAxisPoints() <= 0) throw new ModelValidationException("Model must have a positive number of x-axis points!", model, "xAxisPoints");
		if (model.getxAxisName()==null) throw new ModelValidationException("The model must have a fast axis!\nIt is the motor name used for this axis.", model, "xAxisName");
		if (model.getyAxisName()==null) throw new ModelValidationException("The model must have a slow axis!\nIt is the motor name used for this axis.", model, "yAxisName");
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		final GridModel model = getModel();

		final int columns = model.getxAxisPoints();
		final int rows = model.getyAxisPoints();
		final String xName = model.getxAxisName();
		final String xUnits = model.getxAxisUnits();
		final String yName = model.getyAxisName();
		final String yUnits = model.getyAxisUnits();
		final double xStep = model.getBoundingBox().getxAxisLength() / columns;
		final double yStep = model.getBoundingBox().getyAxisLength() / rows;
		final double minX = model.getBoundingBox().getxAxisStart() + xStep / 2;
		final double minY = model.getBoundingBox().getyAxisStart() + yStep / 2;

		final JythonObjectFactory<ScanPointIterator> lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();

		final ScanPointIterator yLine = lineGeneratorFactory.createObject(
				yName, yUnits, minY, minY + (rows - 1) * yStep, rows, model.isAlternating());
		final ScanPointIterator xLine = lineGeneratorFactory.createObject(
				xName, xUnits, minX, minX + (columns - 1) * xStep, columns, model.isAlternating());

		final Iterator<?>[] generators = new Iterator<?>[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;

        final String[] axisNames = new String[] { xName, yName };
		final ScanPointIterator pyIterator = CompoundSpgIteratorFactory.createSpgCompoundGenerator(generators,
				getRegions().toArray(),	axisNames, EMPTY_PY_ARRAY, -1, model.isContinuous());

		return new SpgIterator(pyIterator);
	}

	@Override
	public String toString() {
		return "GridGenerator [" + super.toString() + "]";
	}

}
