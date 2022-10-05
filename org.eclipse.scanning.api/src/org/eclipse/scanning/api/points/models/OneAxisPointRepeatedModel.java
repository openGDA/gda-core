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

import org.eclipse.scanning.api.ModelValidationException;

/**
 * A model for a generator which repeats the same position a set number of times.
 * @author Matthew Gerring
 *
 * Previously RepeatedPointModel
 */
public class OneAxisPointRepeatedModel extends AbstractPointsModel implements IAxialModel {

	/** The value to set the scannable to at each point. */
	private double value;

	/** Number of points to generate */
	private int count;

	/** Sleep time between points, if any */
	private long sleep;

	public OneAxisPointRepeatedModel() {
		// required for deserialisation
	}

	public OneAxisPointRepeatedModel(String name, int count, double value, long sleep) {
		setName(name);
		this.count = count;
		this.value = value;
		this.sleep = sleep;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getSleep() {
		return sleep;
	}

	public void setSleep(long sleep) {
		this.sleep = sleep;
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void setContinuous(boolean continuous) {
		if (continuous) {
			throw new ModelValidationException("RepeatedPointModel does not support continuous operation", this, "continuous");
		}
	}

	@Override
	public boolean isAlternating() {
		return false;
	}

	@Override
	public void setAlternating(boolean alternating) {
		if (alternating) {
			throw new ModelValidationException("RepeatedPointModel does not support alternating operation", this, "alternating");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + count;
		result = prime * result + (int) (sleep ^ (sleep >>> 32));
		long temp;
		temp = Double.doubleToLongBits(value);
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
		OneAxisPointRepeatedModel other = (OneAxisPointRepeatedModel) obj;
		if (count != other.count)
			return false;
		if (sleep != other.sleep)
			return false;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[value="+value+", count="+count+", sleep="+sleep+", "+super.toString()+"]";
	}

}
