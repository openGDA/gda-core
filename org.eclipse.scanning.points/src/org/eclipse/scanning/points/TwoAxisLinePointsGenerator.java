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
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;

public class TwoAxisLinePointsGenerator extends AbstractLineGenerator<TwoAxisLinePointsModel> {

	TwoAxisLinePointsGenerator(TwoAxisLinePointsModel model) {
		super(model);
	}

	@Override
	public void validate(TwoAxisLinePointsModel model) {
		super.validate(model);
		if (model.getPoints() < 1) throw new ModelValidationException("Must have one or more points in model!", model, "points");
	}

	@Override
	protected int getPoints() {
		return getModel().getPoints();
	}

	@Override
	protected double getStep() {
		return model.getBoundingLine().getLength() / model.getPoints();
	}

}
