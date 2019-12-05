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

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.PPointGenerator;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyList;

class TwoAxisSpiralGenerator extends AbstractGenerator<TwoAxisSpiralModel> {

	TwoAxisSpiralGenerator(TwoAxisSpiralModel model) {
		super(model);
		setLabel("Spiral");
		setDescription("Creates a spiral scaled around the center of a bounding region. "
				+ "This is an Archimedean spiral with polar form: r=b*theta. The 'Scale' parameter gives approximately "
				+ "both the distance between arcs and the arclength between consecutive points."
				+ "\nThe scan supports continuous operation and alternating mode [when wrapped in an outer scan].");
		setIconPath("icons/scanner--spiral.png"); // This icon exists in the rendering bundle
	}

	@Override
	public void validate(TwoAxisSpiralModel model) {
		super.validate(model);
		if (model.getScale() == 0.0) throw new ModelValidationException("Scale must be non-zero!", model, "scale");
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> spiralGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisSpiralGeneratorFactory();

		final TwoAxisSpiralModel model = getModel();

        final List<String> axes =  model.getScannableNames();
        final List<String> units = model.getUnits();
        final double scale = model.getScale();
		final double radiusX = model.getBoundingBox().getxAxisLength() / 2;
		final double radiusY = model.getBoundingBox().getyAxisLength() / 2;
		final double maxRadius = Math.pow(Math.pow(radiusX, 2) + Math.pow(radiusY, 2), 0.5);
		final double xCentre = model.getBoundingBox().getxAxisStart() + radiusX;
		final double yCentre = model.getBoundingBox().getyAxisStart() + radiusY;
        final PyList centre = new PyList(Arrays.asList(xCentre, yCentre));
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();

        PPointGenerator pointGen = spiralGeneratorFactory.createObject(
				axes, units, centre, maxRadius, scale, alternating);
        return CompoundSpgIteratorFactory.createSpgCompoundGenerator(new PPointGenerator[] {pointGen},
				getRegions(), axes, EMPTY_PY_ARRAY, -1, continuous);
        }

}
