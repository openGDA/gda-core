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

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class AxialArrayGenerator extends AbstractGenerator<AxialArrayModel> {

	public AxialArrayGenerator(AxialArrayModel model) {
		super(model);
	}

	@Override
	public void validate(AxialArrayModel model) throws ModelValidationException {
		super.validate(model);
		if (getModel().getPositions()==null) throw new ModelValidationException("There are no positions!", model, "positions");
		if (getModel().getPositions().length < 2) throw new ModelValidationException("ArrayModel requires at least 2 positions, did you want SinglePointModel?", model, "positions");
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> arrayGeneratorFactory = ScanPointGeneratorFactory.JOneAxisArrayGeneratorFactory();

		final AxialArrayModel model = getModel();

		final String name = model.getName();
        final String units = model.getUnits().get(0);
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final double[] points = model.getPositions();

        PPointGenerator array = arrayGeneratorFactory.createObject(name, units, points, alternating);

        return CompoundGenerator.createWrappingCompoundGenerator(new PPointGenerator[] {array}, continuous);
	}

}
