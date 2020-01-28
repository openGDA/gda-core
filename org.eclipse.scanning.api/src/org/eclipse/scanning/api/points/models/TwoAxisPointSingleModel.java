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
 * A model for a scan at a single two-dimensional point.
 *
 * @author Colin Palmer
 *
 */
public class TwoAxisPointSingleModel extends AbstractMapModel {

	private double x;
	private double y;

	public TwoAxisPointSingleModel() {
		setName("Single point");
	}

	public double getX() {
		return x;
	}
	public void setX(double x) {
		double oldValue = this.x;
		this.x = x;
		this.pcs.firePropertyChange("x", oldValue, x);
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		double oldValue = this.y;
		this.y = y;
		this.pcs.firePropertyChange("y", oldValue, y);
	}

	@Override
	public String toString() {
		return "SinglePointModel [x=" + x + ", y=" + y + ", " + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		TwoAxisPointSingleModel other = (TwoAxisPointSingleModel) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void setContinuous(boolean continuous) {
		if (continuous) {
			throw new ModelValidationException("SinglePointModel does not support continuous operation", this, "continuous");
		}
	}

	@Override
	public boolean isAlternating() {
		return false;
	}

	@Override
	public void setAlternating(boolean alternating) {
		if (alternating) {
			throw new ModelValidationException("SinglePointModel does not support alternating operation", this, "alternating");
		}
	}

}
