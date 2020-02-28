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

import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * Point generator for {@link AxialMultiStepModel}s.
 *
 * @author Matthew Dickie
 */
class AxialMultiStepGenerator extends AbstractMultiGenerator<AxialMultiStepModel> {

	protected AxialMultiStepGenerator(AxialMultiStepModel model, IPointGeneratorService pgs) {
		super(model, pgs);
	}

	@Override
	public void validate(AxialMultiStepModel model) {
		super.validate(model);
		String axis = model.getScannableNames().get(0);
		List<String> units = model.getUnits();
		for (AxialStepModel models : model.getModels()) {
			if (!models.getScannableNames().get(0).equals(axis)) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same axes!", model,
						"models");
			}
			if (!models.getUnits().equals(units)) {
				throw new ModelValidationException("All models in ConsecutiveModel must be in the same units!", model,
						"models");
			}
		}
		if (model.isContinuous()) {

			for (int i = 1; i < cachedGenerators.size(); i++) {
				IPosition previousModel = ((AbstractScanPointGenerator<?>) cachedGenerators.get(i - 1)).finalBounds();
				IPosition nextModel = ((AbstractScanPointGenerator<?>) cachedGenerators.get(i)).initialBounds();
				if (Math.abs(previousModel.getValue(axis) - nextModel.getValue(axis)) > DIFF_LIMIT)
					throw new ModelValidationException(
							String.format("Continuous ConsecutiveModels must have the final bounds of each model"
								+ " within %s of the initial bounds of the next.", DIFF_LIMIT), model, "models");
			}
		}
		cachedGenerators = null;
	}

	@Override
	protected JythonObjectFactory<PPointGenerator> getFactory() {
		return ScanPointGeneratorFactory.JConcatGeneratorFactory();
	}

}
