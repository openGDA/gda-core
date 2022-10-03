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

import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

/**
 * Point generator for {@link AxialMultiStepModel}s.
 *
 * @author Matthew Dickie
 */
class AxialMultiStepGenerator extends AbstractMultiGenerator<AxialMultiStepModel> {

	protected AxialMultiStepGenerator(AxialMultiStepModel model, IPointGeneratorService pgs) {
		super(model, pgs);
	}

	@Override
	protected JythonObjectFactory<PPointGenerator> getFactory() {
		return ScanPointGeneratorFactory.JConcatGeneratorFactory();
	}

}
