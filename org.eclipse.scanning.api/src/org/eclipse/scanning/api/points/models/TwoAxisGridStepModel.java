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

import static org.eclipse.scanning.api.constants.PathConstants.X_AXIS_STEP;
import static org.eclipse.scanning.api.constants.PathConstants.Y_AXIS_STEP;

import java.util.Map;

/**
 * A model for a raster scan within a rectangular box in a two-dimensional space, beginning at the box's start
 * coordinates and moving in steps of the sizes set in this model.
 * <p>
 * The "fast" axis forms the inner loop of the scan and the "slow" axis the outer loop.
 *
 * @author Colin Palmer
 *
 * Previously RasterModel
 */
public class TwoAxisGridStepModel extends AbstractTwoAxisGridModel {

	/** The step in the fast direction to take in the units of the x-axis, as plotted. */
	private double xAxisStep = 1;

	/** The step in the slow direction to take in the units of the y-axis, as plotted. */
	private double yAxisStep = 1;

	public TwoAxisGridStepModel() {
		setName("Raster");
	}
	public TwoAxisGridStepModel(String xAxisName, String yAxisName) {
		this();
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
		this.pcs.firePropertyChange(X_AXIS_STEP, oldValue, newValue);
	}

	public double getyAxisStep() {
		return yAxisStep;
	}

	public void setyAxisStep(double newValue) {
		double oldValue = this.yAxisStep;
		this.yAxisStep = newValue;
		this.pcs.firePropertyChange(Y_AXIS_STEP, oldValue, newValue);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(X_AXIS_STEP)) {
			setxAxisStep((double) properties.get(X_AXIS_STEP));
		}
		if (properties.containsKey(Y_AXIS_STEP)) {
			setyAxisStep((double) properties.get(Y_AXIS_STEP));
		}
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
		TwoAxisGridStepModel other = (TwoAxisGridStepModel) obj;
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
		return getClass().getSimpleName() + " [xAxisStep=" + xAxisStep + ", yAxisStep=" + yAxisStep
				+ ", " + super.toString() + "]";
	}

}
