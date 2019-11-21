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
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyList;

class AxialStepGenerator extends AbstractGenerator<AxialStepModel> {

	AxialStepGenerator() {
		setLabel("AxialStep Scan");
		setDescription("Creates a scan that steps through a Scannable axis, from the start to the highest multiple of the step lower than the stop."
				+ "\nIf the last requested point is within 1% of the end it will still be included in the scan."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setIconPath("icons/scanner--step.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		double div = ((model.getStop()-model.getStart())/model.getStep());
		if (div < 0) throw new ModelValidationException("Model step is directed in the wrong direction!", model, "start", "stop", "step");
		if (!Double.isFinite(div)) throw new ModelValidationException("Model step size must be nonzero!", model, "start", "stop", "step");
	}

	@Override
	public int sizeOfValidModel() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in a step scan!");
		return getModel().size();
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		final ScanPointIterator pyIterator = createPythonPointGenerator().getPointIterator();
		return new AxialStepIterator(getModel(), pyIterator);
	}

	@Override
	public int[] getShape() throws GeneratorException {
		return new int[] { sizeOfValidModel() };
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final AxialStepModel model = getModel();

        final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

        final String name = model.getName();
        final PyList units = new PyList(model.getUnits());
        final int numPoints = model.size();
        final double start  = model.getStart();
        final double stop   = start + model.getStep() * (numPoints-1);

        PPointGenerator pointGen = lineGeneratorFactory.createObject(name, units, start, stop, numPoints, model.isAlternating(), model.isContinuous());
        if (getRegions().isEmpty()) {
        	return pointGen;
        }
        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {pointGen},
				getRegions().toArray(),	model.getScannableNames(), EMPTY_PY_ARRAY, -1, model.isContinuous());
	}

}
