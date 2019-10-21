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
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

class RasterGenerator extends AbstractGenerator<RasterModel> {

	RasterGenerator() {
		setLabel("Raster");
		setDescription("Creates a raster scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--raster.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getxAxisStep() == 0) throw new ModelValidationException("Model x-axis step size must be nonzero!", model, "xAxisStep");
		if (model.getyAxisStep() == 0) throw new ModelValidationException("Model y-axis step size must be nonzero!", model, "yAxisStep");

		// Technically the following two throws are not required
		// (The generator could simply produce an empty list.)
		// but we throw errors to avoid potential confusion.
		// Plus, this is consistent with the StepGenerator behaviour.
		if (model.getxAxisStep()/model.getBoundingBox().getxAxisLength() < 0)
			throw new ModelValidationException("Model x-axis step is directed so as to produce no points!", model, "xAxisStep");
		if (model.getyAxisStep()/model.getBoundingBox().getyAxisLength() < 0)
			throw new ModelValidationException("Model y-axis step is directed so as to produce no points!", model, "yAxisStep");
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		final RasterModel model = getModel();
		final double xStep = model.getxAxisStep();
		final double yStep = model.getyAxisStep();
		final String xName = model.getxAxisName();
		final String xUnits = model.getxAxisUnits();
		final String yName = model.getyAxisName();
		final String yUnits = model.getyAxisUnits();
		final double minX = model.getBoundingBox().getxAxisStart();
		final double minY = model.getBoundingBox().getyAxisStart();
		final int columns = (int) Math.floor(model.getBoundingBox().getxAxisLength() / xStep + 1);
		final int rows = (int) Math.floor(model.getBoundingBox().getyAxisLength() / yStep + 1);

		final JythonObjectFactory<ScanPointIterator> lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();

		final ScanPointIterator yLine = lineGeneratorFactory.createObject(
				yName, yUnits, minY, minY + (rows - 1) * yStep, rows, model.isAlternating());
		final ScanPointIterator xLine = lineGeneratorFactory.createObject(
				xName, xUnits, minX, minX + (columns - 1) * xStep, columns, model.isAlternating());

		final Iterator<?>[] generators = new Iterator<?>[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;

        final String[] axisNames = new String[] { xName, yName };

		final ScanPointIterator pyIterator = CompoundSpgIteratorFactory.createSpgCompoundGenerator(
				generators, getRegions().toArray(), axisNames,
				EMPTY_PY_ARRAY, -1, model.isContinuous());
		return new SpgIterator(pyIterator);
	}

}
