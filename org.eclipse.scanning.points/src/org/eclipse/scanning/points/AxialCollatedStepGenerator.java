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
import org.eclipse.scanning.api.points.models.AxialCollatedStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;

/**
 * An iterator along points where one or more axes move to the same value together.
 * TODO: this class should be reimplemented in jython or removed.
 */
public class AxialCollatedStepGenerator extends AxialStepGenerator {

	AxialCollatedStepGenerator() {
		super();
		setLabel("AxialStep Scan (Collated)");
		setDescription("Creates a scan that steps through several Scannable axes simultaneously, from the start to the highest multiple of the step lower than the stop."
				+ "\nIf the last requested point is within 1%\nof the end it will still be included in the scan."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setVisible(false);
	}

	@Override
	public AxialCollatedStepModel getModel() {
		return (AxialCollatedStepModel) super.getModel();
	}

	@Override
	public ScanPointIterator iteratorFromValidModel() {
		final ScanPointIterator pyIterator = createPythonPointGenerator().getPointIterator();
		return new AxialStepCollatedIterator(this.getModel(), pyIterator);

	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (!(model instanceof AxialCollatedStepModel)) {
			throw new ModelValidationException("The model must be a " + AxialCollatedStepModel.class.getSimpleName(),
					model, "offset"); // TODO Not really an offset problem.
		}
	}

	@Override
	public void setModel(AxialStepModel model) {
		if (!(model instanceof AxialCollatedStepModel)) {
			throw new IllegalArgumentException("The model must be a " + AxialCollatedStepModel.class.getSimpleName());
		}
		super.setModel(model);
	}

}
