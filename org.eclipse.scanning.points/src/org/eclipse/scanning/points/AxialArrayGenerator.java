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
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class AxialArrayGenerator extends AbstractGenerator<AxialArrayModel> {

	public AxialArrayGenerator() {
		setLabel("AxialArray Scan");
		setDescription("Creates a scan from an array of positions for a single Scannable axis."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
	}

	@Override
	protected void validateModel() throws ValidationException {
		super.validateModel();
		if (getModel().getPositions()==null) throw new ModelValidationException("There are no positions!", model, "positions");
		if (getModel().getName()==null) throw new ModelValidationException("The model must have a name!\nIt is the motor name used for the array of points.", model, "name");
		if (getModel().getPositions().length < 2) throw new ModelValidationException("ArrayModel requires at least 2 positions, did you want SinglePointModel?", model, "positions");
	}

	@Override
	public int sizeOfValidModel() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in an array scan!");
		if (model.getPositions() == null) {
			return 0;
		}
		return model.size();
	}

	@Override
	protected ScanPointIterator iteratorFromValidModel() {
		return createPythonPointGenerator().getPointIterator();
	}

	@Override
	public int[] getShape() throws GeneratorException {
		return new int[] { size() };
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final AxialArrayModel model = getModel();
        final JythonObjectFactory<PPointGenerator> arrayGeneratorFactory = ScanPointGeneratorFactory.JOneAxisArrayGeneratorFactory();

        final double[] points = model.getPositions();
        final String name = model.getName();
        final String units = model.getUnits().get(0);
        PPointGenerator array = arrayGeneratorFactory.createObject(name, units, points, model.isAlternating(), model.isContinuous());
        if (getRegions().isEmpty()) {
			return array;
		}
        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {array},
				getRegions().toArray(),	model.getScannableNames(), EMPTY_PY_ARRAY, -1, model.isContinuous());
	}

}
