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

public class TwoAxisLissajousModel extends AbstractBoundingBoxModel {

	private double a = 1;
	private double b = 0.25;
	private int points = 503; // this gives a closed path with the other default values

	public TwoAxisLissajousModel() {
		setName("Lissajous Curve");
	}

	public double getA() {
		return a;
	}
	public void setA(double a) {
		double oldValue = this.a;
		this.a = a;
		this.pcs.firePropertyChange("a", oldValue, a);
	}
	public double getB() {
		return b;
	}
	public void setB(double b) {
		double oldValue = this.b;
		this.b = b;
		this.pcs.firePropertyChange("b", oldValue, b);
	}

	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		if (points == 0) {
			throw new ModelValidationException("Cannot have 0 points!", this, "points");
		}
		int oldValue = this.points;
		this.points = points;
		this.pcs.firePropertyChange("points", oldValue, points);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(a);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(b);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + points;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		TwoAxisLissajousModel other = (TwoAxisLissajousModel) obj;
		if (Double.doubleToLongBits(a) != Double.doubleToLongBits(other.a))
			return false;
		if (Double.doubleToLongBits(b) != Double.doubleToLongBits(other.b))
			return false;
		return points == other.points;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [a=" + a + ", b=" + b + ", phaseDifference=" + getPhaseDifference() + ", thetaStep=" + getThetaStep() + ", points="
				+ points + ", " + super.toString() + "]";
	}

	public double getThetaStep() {
		return 2*Math.PI/points;
	}

	/**
	 * #lobes = floor(a/b)
	 * @return 0 for even number of lobes
	 *         pi/2 for odd number
	 */

	public double getPhaseDifference() {
		return Math.PI /2  * (((int) a/b) % 2);
	}

	public int getLobes() {
		return (int) (a/b);
	}

}
