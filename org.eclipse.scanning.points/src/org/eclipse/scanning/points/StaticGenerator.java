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

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * A software generator that generates a static (i.e. empty) point one or more times.
 *
 * @author Matthew Dickie
 */
class StaticGenerator extends AbstractGenerator<StaticModel> {

	private static final int[] EMPTY_SHAPE = new int[0];

	StaticGenerator(StaticModel model) {
		super(model);
		setLabel("Empty");
		setDescription("Empty generator used when wrapping malcolm scans with no CPU steps.");
	}

	// Users to not edit the StaticGenerator
	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void setRegions(List<IROI> regions) throws GeneratorException {
		throw new GeneratorException("StaticGenerator cannot have regions.");
	}

	@Override
	public void validate(StaticModel model) {
		super.validate(model);
		if (model.getSize() < 1) throw new ModelValidationException("Size must be greater than zero!", model, "size");
	}

	@Override
	public int[] getShape() throws GeneratorException {
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
