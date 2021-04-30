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

import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;

class TwoAxisLineStepGenerator extends AbstractLineGenerator<TwoAxisLineStepModel> {

	TwoAxisLineStepGenerator(TwoAxisLineStepModel model) {
		super(model);
	}

	@Override
	protected int getPoints() {
		double length = getModel().getBoundingLine().getLength();
		return Math.max(1, BigDecimal.valueOf(length).divideToIntegralValue(BigDecimal.valueOf(model.getStep())).intValue());
	}

	@Override
	protected double getStep() {
		return model.getStep();
	}

}
