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

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;

class TwoAxisLineStepGenerator extends AbstractLineGenerator<TwoAxisLineStepModel> {

	TwoAxisLineStepGenerator() {
		setLabel("Two-Axis Line Step Scan");
		setDescription("Creates a line scan along a line defined in two dimensions, with points placed from the start to the highest multiple of the step lower than the stop."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setIconPath("icons/scanner--line.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getStep() <= 0) throw new ModelValidationException("Model step size must be positive!", model, "step");
	}

	@Override
	protected int getPoints() {
		double length = getModel().getBoundingLine().getLength();
		return Math.max(1, BigDecimal.valueOf(length).divide(BigDecimal.valueOf(model.getStep())).intValue());
	}

	@Override
	protected double getStep() {
		return model.getStep();
	}

}
