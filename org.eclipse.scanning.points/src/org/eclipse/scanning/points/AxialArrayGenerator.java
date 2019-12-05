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

import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class AxialArrayGenerator extends AbstractGenerator<AxialArrayModel> {

	public AxialArrayGenerator(AxialArrayModel model) {
		super(model);
		setLabel("AxialArray Scan");
		setDescription("Creates a scan from an array of positions for a single Scannable axis."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
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
		final List<IROI> regions = getRegions();

		final List<String> axes = model.getScannableNames();
		final String name = model.getName();
        final String units = model.getUnits().get(0);
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final double[] points = model.getPositions();

        PPointGenerator array = arrayGeneratorFactory.createObject(name, units, points, alternating);

        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {array},
        		regions, axes, EMPTY_PY_ARRAY, -1, continuous);
	}

}
