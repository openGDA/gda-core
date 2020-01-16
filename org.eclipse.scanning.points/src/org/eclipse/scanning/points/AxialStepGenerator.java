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

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

class AxialStepGenerator extends AbstractGenerator<AxialStepModel> {

	public AxialStepGenerator(AxialStepModel model) {
		super(model);
	}

	AxialStepGenerator() {
		// For validating AxialMultiStepModels only
	}

	@Override
	public void validate(AxialStepModel model) {
		super.validate(model);
		if (model.getStep() == 0) {
			throw new ModelValidationException("Model step size must be nonzero!", model, "step");
		}
		int dir = Integer.signum(BigDecimal.valueOf(model.getStop()-model.getStart()).divideToIntegralValue(BigDecimal.valueOf(model.getStep())).intValue());
		if (dir < 0) {
			throw new ModelValidationException("Model step is directed in the wrong direction!", model, "start", "stop", "step");
		}
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		final AxialStepModel model = getModel();

        final String name = model.getName();
        final List<String> units = model.getUnits();
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final int numPoints = model.size();
        final double start  = model.getStart();
        final double stop   = start + model.getStep() * (numPoints-1);

        PPointGenerator pointGen = lineGeneratorFactory.createObject(name, units, start, stop, numPoints, alternating);

        return CompoundGenerator.createWrappingCompoundGenerator(new PPointGenerator[] {pointGen}, continuous);
	}

}
