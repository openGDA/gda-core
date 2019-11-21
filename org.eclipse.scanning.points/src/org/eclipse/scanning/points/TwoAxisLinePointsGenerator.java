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
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;

public class TwoAxisLinePointsGenerator extends AbstractLineGenerator<TwoAxisLinePointsModel> {

	TwoAxisLinePointsGenerator() {
		setLabel("Two-Axis Grid Points Scan");
		setDescription("Creates a line scan along a line defined in two dimensions, with points evenly spaced along the bounding line."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setIconPath("icons/scanner--line.png"); // This icon exists in the rendering bundle
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getPoints() < 1) throw new ModelValidationException("Must have one or more points in model!", model, "points");
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		return createPythonPointGenerator().getPointIterator();
	}

	@Override
	public int[] getShape() {
		return new int[] { getPoints() };
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
