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
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * A software generator that generates a static (i.e. empty) point one or more times.
 *
 * @author Matthew Dickie
 */
class StaticGenerator extends AbstractScanPointGenerator<StaticModel> {

	private static final int[] EMPTY_SHAPE = new int[0];

	StaticGenerator(StaticModel model) {
		super(model);
	}

	@Override
	public void validate(StaticModel model) {
		super.validate(model);
		if (model.getSize() < 1) throw new ModelValidationException("Size must be greater than zero!", model, "size");
	}

	@Override
	public int[] getShape() {
		return model.getSize() == 1 ? EMPTY_SHAPE : new int[] { model.getSize() };
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> staticGeneratorFactory = ScanPointGeneratorFactory.JStaticGeneratorFactory();

		final StaticModel model = getModel();

		final int numPoints = model.getSize();

		return staticGeneratorFactory.createObject(numPoints);
	}
}
