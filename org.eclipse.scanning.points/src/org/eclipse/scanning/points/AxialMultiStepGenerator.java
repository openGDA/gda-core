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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * Point generator for {@link AxialMultiStepModel}s.
 *
 * @author Matthew Dickie
 */
class AxialMultiStepGenerator extends AbstractGenerator<AxialMultiStepModel> {

	AxialMultiStepGenerator(AxialMultiStepModel model) {
		super(model);
		setLabel("AxialMultiStep Scan");
		setDescription("Creates a scan that steps through a Scannable axis, from the start to the highest multiple of the step lower than the stop for several values of Start, Stop, Step:"
				+ "\nif the first point of a scan is within 1% of the final point of the previous scan, the point is removed."
				+ "\nIf the last requested point is within 1% of the end it will still be included in the scan."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
	}

	@Override
	public void validate(AxialMultiStepModel model) {
		super.validate(model);

		AxialStepGenerator stepGen = new AxialStepGenerator(); // to validate step models
		double dir = 0; // +1 for forwards, -1 for backwards, 0 when not yet calculated
		double lastStop = 0;

		if (model.getStepModels().isEmpty()) {
			throw new ModelValidationException("At least one step model must be specified", model, "stepModels");
		}

		for (AxialStepModel stepModel : model.getStepModels()) {
			stepGen.validate(stepModel);

			// check the inner step model has the same sign
			if (!model.getName().equals(stepModel.getName())) {
				throw new ModelValidationException(MessageFormat.format(
						"Child step model must have the same name as the MultiStepModel. Expected ''{0}'', was ''{1}''", model.getName(), stepModel.getName()),
						model, "name");
			}

			// check the inner step model is valid according to StepGenerator.validate()

			double stepDir = Math.signum(stepModel.getStop() - stepModel.getStart());
			if (dir == 0) {
				dir = stepDir;
			} else {
				// check this step model starts ahead (in same direction) of previous one
				if (stepDir != dir) {
					throw new ModelValidationException(
							"Each step model must have the the same direction", model, "stepModels");
				}
				double gapDir = Math.signum(stepModel.getStart() - lastStop);
				// check this step model is in same direction as previous ones
				if (gapDir != dir && gapDir != 0) {
					throw new ModelValidationException(MessageFormat.format(
							"A step model must start at a point with a {0} (or equal) value than the stop value of the previous step model.",
							dir > 0 ? "higher" : "lower") , model, "stepModels");
				}
			}

			// check the start of the next step is in the same direction as the
			lastStop = stepModel.getStop();
		}
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> arrayGeneratorFactory = ScanPointGeneratorFactory.JOneAxisArrayGeneratorFactory();

		final AxialMultiStepModel model = getModel();

		int totalSize = 0;
		boolean finalPosWasEnd = false;
		final List<double[]> positionArrays = new ArrayList<>(model.getStepModels().size());
		double previousEnd = 0;
		for (AxialStepModel stepModel : model.getStepModels()) {
			int size = stepModel.size();
			double pos = stepModel.getStart();

			// if the start of this model is the end of the previous one, and the end of the
			// previous was was its final point, skip the first point
			if (finalPosWasEnd &&
					Math.abs(stepModel.getStart() - previousEnd) < Math.abs(stepModel.getStep() / 100)) {
				pos += stepModel.getStep();
				size--;
			}
			double[] positions = new double[size];

			for (int i = 0; i < size; i++) {
				positions[i] = pos;
				pos += stepModel.getStep();
			}
			positionArrays.add(positions);
			totalSize += size;

			// record if the final position of this model is its end position (within a tolerance of step/100)
			// this is not always the case, e.g. if start=0, stop=10 and step=3
			double finalPos = positions[positions.length - 1];
			finalPosWasEnd = Math.abs(stepModel.getStop() - finalPos) < Math.abs(stepModel.getStep() / 100);
			previousEnd = stepModel.getStop();
		}

		final double[] points = new double[totalSize];

		int pos = 0;
		for (double[] positions : positionArrays) {
			System.arraycopy(positions, 0, points, pos, positions.length);
			pos += positions.length;
		}

		final List<String> axes = model.getScannableNames();
		final List<String> units = model.getUnits();
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

        PPointGenerator pointGen = arrayGeneratorFactory.createObject(axes, units, points, alternating);

        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {pointGen},
				getRegions(), axes, EMPTY_PY_ARRAY, -1, continuous);
	}

}
