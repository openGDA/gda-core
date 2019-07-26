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
import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;

public class RandomOffsetGridGenerator extends GridGenerator {

	RandomOffsetGridGenerator() {
		setLabel("Random Offset Grid");
		setDescription("Creates a grid scan (a scan of x and y) with random offsets applied to each point.\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (!(model instanceof RandomOffsetGridModel)) {
			throw new ModelValidationException("The model must be a " + RandomOffsetGridModel.class.getSimpleName(),
					model, "offset"); // TODO Not really an offset problem.
		}
	}

	@Override
	public void setModel(GridModel model) {
		if (!(model instanceof RandomOffsetGridModel)) {
			throw new IllegalArgumentException("The model must be a " + RandomOffsetGridModel.class.getSimpleName());
		}
		super.setModel(model);
	}

	@Override
	public RandomOffsetGridModel getModel() {
		return (RandomOffsetGridModel) super.getModel();
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		final RandomOffsetGridModel model = getModel();

		final int columns = model.getXAxisPoints();
		final int rows = model.getYAxisPoints();
		final String xName = model.getXAxisName();
		final String yName = model.getYAxisName();
		final double xStep = model.getBoundingBox().getXAxisLength() / columns;
		final double yStep = model.getBoundingBox().getYAxisLength() / rows;
		final double minX = model.getBoundingBox().getXAxisStart() + xStep / 2;
		final double minY = model.getBoundingBox().getYAxisStart() + yStep / 2;

        final JythonObjectFactory<ScanPointIterator> lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator1DFactory();

		final ScanPointIterator yLine = lineGeneratorFactory.createObject(
				yName, "mm", minY, minY + (rows - 1) * yStep, rows);
		final ScanPointIterator xLine = lineGeneratorFactory.createObject(
				xName, "mm", minX, minX + (columns - 1) * xStep, columns, model.isSnake());

        final JythonObjectFactory<PyObject> randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();

        final int seed = model.getSeed();
        final double offset = xStep * model.getOffset() / 100;

        final PyDictionary maxOffset = new PyDictionary();
        maxOffset.put(yName, offset);
        maxOffset.put(xName, offset);

        final PyList axes = new PyList(Arrays.asList(yName, xName));
		final PyObject randomOffset = randomOffsetMutatorFactory.createObject(seed, axes, maxOffset);

		final Iterator<?>[] generators = new Iterator<?>[2];
		generators[0] = model.isVerticalOrientation() ? xLine : yLine;
		generators[1] = model.isVerticalOrientation() ? yLine : xLine;
        final PyObject[] mutators = { randomOffset };

        final String[] axisNames = new String[] { xName, yName };
		final ScanPointIterator pyIterator = CompoundSpgIteratorFactory.createSpgCompoundGenerator(
				generators, getRegions().toArray(), axisNames, mutators, -1, model.isContinuous());

		return new SpgIterator(pyIterator);
	}

}