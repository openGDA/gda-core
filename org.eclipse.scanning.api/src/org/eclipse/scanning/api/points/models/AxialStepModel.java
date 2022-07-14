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
package org.eclipse.scanning.api.points.models;

/**
 * A model for a scan along one axis with start and stop positions and a step size.
 *
 * Adds no additional behaviour to abstract superclass {@link AbstractAxialStepModel}.
 * The purpose of this subclass is so that this can implement {@link IAxialModel}, which
 * is for single axis models only.
 *
 * Previously StepModel
 */
public class AxialStepModel extends AbstractAxialStepModel implements IAxialModel {

	public AxialStepModel() {
		super(); // no-arg constructor for json
	}

	public AxialStepModel(String name, double start, double stop, double step) {
		super(name, start, stop, step);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +" ["+description()+"]";
	}

}
