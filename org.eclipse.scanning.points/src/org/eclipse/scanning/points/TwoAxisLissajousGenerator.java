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
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyDictionary;

public class TwoAxisLissajousGenerator extends AbstractGenerator<TwoAxisLissajousModel> {

	public TwoAxisLissajousGenerator(TwoAxisLissajousModel model) {
		super(model);
	}

	@Override
	public void validate(TwoAxisLissajousModel model) {
		super.validate(model);
		if (model.getPoints() < 1)
			throw new ModelValidationException("Must have one or more points in model!", model, "points");
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> lissajousGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLissajousGeneratorFactory();

		final TwoAxisLissajousModel model = getModel();

		final List<String> axes = model.getScannableNames();
		final List<String> units = model.getUnits();
		final int numLobes = model.getLobes();
		final int numPoints = model.getPoints();
		final double width = model.getBoundingBox().getxAxisLength();
		final double height = model.getBoundingBox().getyAxisLength();
		final PyDictionary box = new PyDictionary();
		box.put("width", width);
		box.put("height", height);
		box.put("centre", new double[] { model.getBoundingBox().getxAxisStart() + width / 2,
				model.getBoundingBox().getyAxisStart() + height / 2 });
		final boolean alternating = model.isAlternating();
		final boolean continuous = model.isContinuous();

		final PPointGenerator pointGen = lissajousGeneratorFactory.createObject(axes, units, box, numLobes, numPoints,
				alternating);
		return CompoundGenerator.createWrappingCompoundGenerator(new PPointGenerator[] { pointGen }, continuous);
	}

}
