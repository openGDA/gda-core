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

	@FieldDescriptor(label="X Step",
			         scannable="xAxisName",
				     maximum=100000,
					 minimum=1,
			         hint="The step in the fast direction to take in the units of the x-axis, as plotted.")
	private double xAxisStep = 1;

	@FieldDescriptor(label="Y Step",
			         scannable="yAxisName",
					 maximum=100000,
					 minimum=1,
			         hint="The step in the slow direction to take in the units of the y-axis, as plotted.")
	private double yAxisStep = 1;

	public RasterModel() {
		setName("Raster");
	}
	public RasterModel(String xAxisName, String yAxisName) {
		setxAxisName(xAxisName);
		setyAxisName(yAxisName);
	}

	// Note: x and y must be in lower case in getter/setter names for JFace bindings to work correctly.
	public double getxAxisStep() {
		return xAxisStep;
	}
	public void setxAxisStep(double newValue) {
		double oldValue = this.xAxisStep;
		this.xAxisStep = newValue;
		this.pcs.firePropertyChange("xAxisStep", oldValue, newValue);
	}
	public double getyAxisStep() {
		return yAxisStep;
	}
	public void setyAxisStep(double newValue) {
		double oldValue = this.yAxisStep;
		this.yAxisStep = newValue;
		this.pcs.firePropertyChange("yAxisStep", oldValue, newValue);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(xAxisStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yAxisStep);
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
		if (Double.doubleToLongBits(xAxisStep) != Double
				.doubleToLongBits(other.xAxisStep))
			return false;
		if (Double.doubleToLongBits(yAxisStep) != Double
				.doubleToLongBits(other.yAxisStep))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "RasterModel [xAxisStep=" + xAxisStep + ", yAxisStep=" + yAxisStep
				+ ", " + super.toString() + "]";
	}






}
