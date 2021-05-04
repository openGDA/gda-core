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

import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class AxialArrayGenerator extends AbstractScanPointGenerator<AxialArrayModel> {

	public AxialArrayGenerator(AxialArrayModel model) {
		super(model);
	}

	@Override
	protected PPointGenerator createPythonPointGenerator() {
        final JythonObjectFactory<PPointGenerator> arrayGeneratorFactory = ScanPointGeneratorFactory.JOneAxisArrayGeneratorFactory();

		final AxialArrayModel model = getModel();

		final List<String> name = model.getScannableNames();
        final List<String> units = model.getUnits();
        final boolean alternating = model.isAlternating();
        final boolean continuous = model.isContinuous();
        final double[] points = model.getPositions();

        final PPointGenerator array = arrayGeneratorFactory.createObject(name, units, points, alternating);

        return createWrappingCompoundGenerator(array, continuous);
	}

}
