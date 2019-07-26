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

import org.eclipse.scanning.api.annotation.MinimumValue;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;


/**
 * A model for a raster scan within a rectangular box in two-dimensional space, which evenly fills the box with a grid
 * defined by the numbers of points (along each of the two axes) set in this model.
 *
 * @author Colin Palmer
 *
 */
public class GridModel extends AbstractGridModel {


	@FieldDescriptor(label="X Axis Count",
			         maximum=100000,
			         minimum=1,
			         hint="The number of points that the grid should run over in the direction of the x-axis, as plotted.")
	private int xAxisPoints = 5;

	@FieldDescriptor(label="Y Axis Count",
			         maximum=100000,
			         minimum=1,
			         hint="The number of points that the grid should run over in the direction of the y-axis, as plotted")
	private int yAxisPoints = 5;

	public GridModel() {
		setName("Grid");
	}

	public GridModel(String xName, String yName) {
		this();
		setXAxisName(xName);
		setYAxisName(yName);
	}
	public GridModel(String xName, String yName, int xPoints, int yPoints) {
		this(xName, yName);
		setXAxisPoints(xPoints);
		setYAxisPoints(yPoints);
	}

	@MinimumValue("1")
	public int getXAxisPoints() {
		return xAxisPoints;
	}
	public void setXAxisPoints(int newValue) {
		int oldValue = this.xAxisPoints;
		this.xAxisPoints = newValue;
		this.pcs.firePropertyChange("xAxisPoints", oldValue, newValue);
	}
	@MinimumValue("1")
	public int getYAxisPoints() {
		return yAxisPoints;
	}
	public void setYAxisPoints(int newValue) {
		int oldValue = this.yAxisPoints;
		this.yAxisPoints = newValue;
		this.pcs.firePropertyChange("yAxisPoints", oldValue, newValue);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + xAxisPoints;
		result = prime * result + yAxisPoints;
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
		GridModel other = (GridModel) obj;
		if (xAxisPoints != other.xAxisPoints)
			return false;
		if (yAxisPoints != other.yAxisPoints)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GridModel [xAxisPoints=" + xAxisPoints + ", yAxisPoints=" + yAxisPoints + ", " + super.toString() + "]";
	}
}
