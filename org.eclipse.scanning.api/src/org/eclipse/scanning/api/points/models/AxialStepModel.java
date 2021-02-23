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
 * Previously StepModel
 */
public class AxialStepModel extends AbstractPointsModel {

	/** Start position for the scan */
	private double start;

	/** Stop position for the scan */
	private double stop;

	/** Step during the scan */
	private double step;

	// Left to prevent problems with deserialisation, see {@link AxialPointsModel}
	private int count;

	public AxialStepModel() {
		// no-arg constructor for json
	}

	public AxialStepModel(String name, double start, double stop, double step) {
		super();
		setName(name);
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public double getStop() {
		return stop;
	}
	public void setStop(double stop) {
		this.stop = stop;
	}
	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		this.step = step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		long temp;
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AxialStepModel other = (AxialStepModel) obj;
		if (count != other.count)
			return false;
		if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
			return false;
		if (Double.doubleToLongBits(step) != Double.doubleToLongBits(other.step))
			return false;
		if (Double.doubleToLongBits(stop) != Double.doubleToLongBits(other.stop))
			return false;
		return true;
	}

	protected String description() {
		return "start=" + start + ", stop=" + stop + ", step=" + step + ", " + super.toString();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +" ["+description()+"]";
	}

}
