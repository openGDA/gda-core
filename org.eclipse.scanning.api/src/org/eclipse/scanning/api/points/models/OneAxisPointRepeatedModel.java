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
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * A model for a generator which repeats the same position a set number of times.
 * @author Matthew Gerring
 *
 */
public class OneAxisPointRepeatedModel extends AbstractPointsModel {

	@FieldDescriptor(label="Device", device=DeviceType.SCANNABLE, fieldPosition=1)
	private String name;

	@FieldDescriptor(label="Value", scannable="name", hint="The value to set the scannable to at each point.", fieldPosition=2)
	private double value;

	@FieldDescriptor(label="Count", hint="Number of points to generate", minimum=1, maximum=10000, fieldPosition=3)
	private int count;

	@FieldDescriptor(label="Sleep", hint="Sleep time between points, if any", minimum=1, maximum=10000, fieldPosition=4)
	private long sleep;

	public OneAxisPointRepeatedModel() {

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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (sleep ^ (sleep >>> 32));
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
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

	@Override
	public int size() {
		return count;
	}
}
