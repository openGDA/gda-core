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

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * A model for a raster scan within a rectangular box in a two-dimensional space, beginning at the box's start
 * coordinates and moving in steps of the sizes set in this model.
 * <p>
 * The "fast" axis forms the inner loop of the scan and the "slow" axis the outer loop.
 *
 * @author Colin Palmer
 *
 */
public class RasterModel extends AbstractGridModel {

	@FieldDescriptor(label="Fast Step",
			         scannable="fastAxisName",
				     maximum=100000,
					 minimum=1,
			         hint="The step in the fast direction to take in the units of the fast scannable.")
	private double fastAxisStep = 1;

	@FieldDescriptor(label="Slow Step",
			         scannable="slowAxisName",
					 maximum=100000,
					 minimum=1,
			         hint="The step in the slow direction to take in the units of the slow scannable.")
	private double slowAxisStep = 1;

	public RasterModel() {
		setName( "Raster" );
	}
	public RasterModel(String f, String s) {
		setFastAxisName(f);
		setSlowAxisName(s);
	}
	public double getFastAxisStep() {
		return fastAxisStep;
	}
	public void setFastAxisStep(double newValue) {
		double oldValue = this.fastAxisStep;
		this.fastAxisStep = newValue;
		this.pcs.firePropertyChange("fastAxisStep", oldValue, newValue);
	}
	public double getSlowAxisStep() {
		return slowAxisStep;
	}
	public void setSlowAxisStep(double newValue) {
		double oldValue = this.slowAxisStep;
		this.slowAxisStep = newValue;
		this.pcs.firePropertyChange("slowAxisStep", oldValue, newValue);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(fastAxisStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(slowAxisStep);
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
		RasterModel other = (RasterModel) obj;
		if (Double.doubleToLongBits(fastAxisStep) != Double
				.doubleToLongBits(other.fastAxisStep))
			return false;
		if (Double.doubleToLongBits(slowAxisStep) != Double
				.doubleToLongBits(other.slowAxisStep))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "RasterModel [fastAxisStep=" + fastAxisStep + ", slowAxisStep=" + slowAxisStep
				+ ", " + super.toString() + "]";
	}






}
