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
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;

public class TwoAxisLissajousGenerator extends AbstractGenerator<TwoAxisLissajousModel> {

	public TwoAxisLissajousGenerator() {
		setLabel("Two-Axis Lissajous Curve Scan");
		setDescription("Creates a Lissajous curve inside a bounding box, with points placed evenly in t."
				+ "\na/b is floored and used as the number of lobes, and phase difference is locked to 0 for (a/b)%2=0, pi/2 for (a/b)%2=1."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setIconPath("icons/scanner--lissajous.png"); // This icon exists in the rendering bundle
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		PPointGenerator liss = createPythonPointGenerator();
        if (!getRegions().isEmpty()) {
			return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {createPythonPointGenerator()}, getRegions().toArray(),
					model.getScannableNames(), EMPTY_PY_ARRAY, -1, model.isContinuous()).getPointIterator();
			}
		return new SpgIterator(liss.getPointIterator());
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getPoints() < 1) throw new ModelValidationException("Must have one or more points in model!", model, "points");
		if (model.getxAxisName()==null) throw new ModelValidationException("The model must have an x-axis!\nIt is the motor name used for this axis.", model, "xAxisName");
		if (model.getyAxisName()==null) throw new ModelValidationException("The model must have a y-axix!\nIt is the motor name used for this axis.", model, "yAxisName");
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final double width = model.getBoundingBox().getxAxisLength();
		final double height = model.getBoundingBox().getyAxisLength();

        final JythonObjectFactory<PPointGenerator> lissajousGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLissajousGeneratorFactory();

        final PyDictionary box = new PyDictionary();
        box.put("width", width);
        box.put("height", height);
        box.put("centre", new double[] {model.getBoundingBox().getxAxisStart() + width / 2,
									model.getBoundingBox().getyAxisStart() + height / 2});

        final PyList names =  new PyList(model.getScannableNames());
        final PyList units = new PyList(model.getUnits());
        final int numLobes = (int) (model.getA() / model.getB());
        final int numPoints = model.getPoints();

        PPointGenerator pointGen = lissajousGeneratorFactory.createObject(names, units, box, numLobes, numPoints, model.isAlternating());
        if (getRegions().isEmpty()) {
        	return pointGen;
        }
        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {pointGen},
				getRegions().toArray(),	model.getScannableNames(), EMPTY_PY_ARRAY, -1, model.isContinuous());
	}

}
