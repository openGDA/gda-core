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
 * Nth point is at Start + N * Step with boundsFit off
 * Nth point is at Start + (N + 0.5) * Step with boundsFit on
 * A final point that is not within [start, stop] will be allowed if it is <1% outside of range
 * Bounds are half Step either side of points.
 *
 * if boundsToFit is false:
 * 	Number of points = M + 1,
 * 	M is the number of sections the line is split into by the points and is highest integer for which:
 *  	Stop + 0.01 * Step >= Start + M * Step
 *  	Start - Stop = Length >= (M - 0.01) * Step
 *  	Length / Step >= M - 0.01
 * 	As Length -> Step or Step -> Length, 0th point remains at Start.
 * 	If Step > Length, 0th point placed at start, bounds = +- half-step
 *
 * if boundsToFit is true:
 * 	Number of points = M
 * 	M is the number of sections the line is split into by the points, including the half sections on each end
 * 		The value of M is as defined above.
 * 	as Length -> Step or Step -> Length, 0th point moves towards centre of span
 * 	If Step > Length, 0th point placed at centre of span, bonds = +- half-length
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
