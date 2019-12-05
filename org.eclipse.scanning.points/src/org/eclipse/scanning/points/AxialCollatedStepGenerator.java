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
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class AxialCollatedStepGenerator extends AbstractGenerator<AxialCollatedStepModel> {

	AxialCollatedStepGenerator(AxialCollatedStepModel model) {
		super(model);
		setLabel("AxialStep Scan (Collated)");
		setDescription("Creates a scan that steps through several Scannable axes simultaneously, from the start to the highest multiple of the step lower than the stop."
				+ "\nIf the last requested point is within 1%\nof the end it will still be included in the scan."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setVisible(false);
	}

	@Override
	public void validate(AxialCollatedStepModel model) {
		super.validate(model);
		if (model.getNames() == null || model.getNames().isEmpty()) {
			throw new ModelValidationException("AxialCollatedStepModel requires a list of names of axes to step in",
					model, "names");
		}
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLineGeneratorFactory();

		final AxialCollatedStepModel model = getModel();
		final List<IROI> regions = getRegions();

		final List<String> axes = model.getScannableNames();
		final int numAxes = axes.size();
        final List<String> units = model.getUnits();
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final int points = model.size();
        final double start = model.getStart();
        final double step = model.getStep();
        final double[] starts = new double[numAxes];
        final double[] stops = new double[numAxes];
        for (int i = 0; i< numAxes; i++) {
        	starts[i] = start;
        	stops[i] = start + step * (points-1);
        }

        PPointGenerator pointGen = lineGeneratorFactory.createObject(axes, units, starts, stops, points, alternating);

        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {pointGen},
				regions, axes, EMPTY_PY_ARRAY, -1, continuous);
	}

}
