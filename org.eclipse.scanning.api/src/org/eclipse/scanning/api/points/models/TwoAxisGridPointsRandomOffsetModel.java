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

import static org.eclipse.scanning.api.constants.PathConstants.OFFSET;
import static org.eclipse.scanning.api.constants.PathConstants.SEED;

import java.util.Map;

/**
 * Previously RandomOffsetGridModel
 */
public class TwoAxisGridPointsRandomOffsetModel extends TwoAxisGridPointsModel {


	/**
	 * The maximum allowed offset, as a percentage of fast axis step size
	 */
	private double offset;
	/**
	 * Seed to initialise random number generator with
	 */
	private int seed;

	public TwoAxisGridPointsRandomOffsetModel() {
		setName("Random Offset Grid");
	}

	public TwoAxisGridPointsRandomOffsetModel(String f, String s) {
		super(f, s);
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double newValue) {
		double oldValue = this.offset;
		this.offset = newValue;
		this.pcs.firePropertyChange(OFFSET, oldValue, newValue);
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int newValue) {
		double oldValue = this.seed;
		this.seed = newValue;
		this.pcs.firePropertyChange(SEED, oldValue, newValue);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(OFFSET)) {
			setOffset((double) properties.get(OFFSET));
		}
		if (properties.containsKey(SEED)) {
			setSeed(((Number) properties.get(SEED)).intValue());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(offset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		TwoAxisGridPointsRandomOffsetModel other = (TwoAxisGridPointsRandomOffsetModel) obj;
		if (Double.doubleToLongBits(offset) != Double.doubleToLongBits(other.offset))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [xAxisPoints=" + getxAxisPoints() + ", yAxisPoints=" + getyAxisPoints() +
				", offset=" + offset + ", seed=" + seed + ", " + super.toString() + "]";
	}
}
