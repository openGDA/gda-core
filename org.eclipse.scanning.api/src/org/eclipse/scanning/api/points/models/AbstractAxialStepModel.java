/*-
 * Copyright © 2022 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.api.points.models;

import static org.eclipse.scanning.api.constants.PathConstants.START;
import static org.eclipse.scanning.api.constants.PathConstants.STEP;
import static org.eclipse.scanning.api.constants.PathConstants.STOP;

/**
 * Abstract superclass of models with a single start, stop, and step value.
 * Concrete subclasses include {@link AxialStepModel} for the single-axis case
 * and {@link AxialCollatedStepModel} for multiple axes with the same start stop, step values.
 */
public abstract class AbstractAxialStepModel extends AbstractAxialModel {

	/** Start position for the scan */
	private double start;

	/** Stop position for the scan */
	private double stop;

	/** Step during the scan */
	private double step;

	// Left to prevent problems with deserialisation, see {@link AxialPointsModel}
	private int count;

	protected AbstractAxialStepModel() {
		super(); // no-arg constructor for json
	}

	protected AbstractAxialStepModel(String name, double start, double stop, double step) {
		super();
		setName(name);
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	public double getStart() {
		return start;
	}
	public void setStart(double newValue) {
		final double oldValue = this.start;
		this.start = newValue;
		this.pcs.firePropertyChange(START, oldValue, newValue);
	}
	public double getStop() {
		return stop;
	}
	public void setStop(double newValue) {
		final double oldValue = this.stop;
		this.stop = newValue;
		this.pcs.firePropertyChange(STOP, oldValue, newValue);
	}
	public double getStep() {
		return step;
	}
	public void setStep(double newValue) {
		final double oldValue = this.step;
		this.step = newValue;
		this.pcs.firePropertyChange(STEP, oldValue, newValue);
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
		AbstractAxialStepModel other = (AbstractAxialStepModel) obj;
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
