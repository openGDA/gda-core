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

public class SpiralModel extends AbstractBoundingBoxModel {

	private double scale = 1;

	public SpiralModel() {
		setName("Spiral");
	}
	public SpiralModel(String fastName, String slowName, double scale, BoundingBox box) {
		super(fastName, slowName, box);
		setName("Spiral");
		this.scale = scale;
	}

	public SpiralModel(String fastName, String slowName) {
		super(fastName, slowName, null);
	}
	public double getScale() {
		return scale;
	}

	public void setScale(double newValue) {
		double oldValue = this.scale;
		this.scale = newValue;
		this.pcs.firePropertyChange("scale", oldValue, newValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(scale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		SpiralModel other = (SpiralModel) obj;
		return (Double.doubleToLongBits(scale) == Double.doubleToLongBits(other.scale));
	}
	@Override
	public String toString() {
		return "SpiralModel [scale=" + scale + ", " + super.toString() + "]";
	}
}
