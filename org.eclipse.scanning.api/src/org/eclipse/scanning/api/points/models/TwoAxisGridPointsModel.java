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

import static org.eclipse.scanning.api.constants.PathConstants.X_AXIS_POINTS;
import static org.eclipse.scanning.api.constants.PathConstants.Y_AXIS_POINTS;

import java.util.Map;


/**
 * A model for a raster scan within a rectangular box in two-dimensional space, which evenly fills the box with a grid
 * defined by the numbers of points (along each of the two axes) set in this model.
 *
 * @author Colin Palmer
 *
 * Previously GridModel
 */
public class TwoAxisGridPointsModel extends AbstractTwoAxisGridModel {

	/** The number of points that the grid should run over in the direction of the x-axis, as plotted. */
	private int xAxisPoints = 5;

	/** The number of points that the grid should run over in the direction of the y-axis, as plotted" */
	private int yAxisPoints = 5;

	public TwoAxisGridPointsModel() {
		setName("Grid");
	}

	public TwoAxisGridPointsModel(String xName, String yName) {
		this();
		setxAxisName(xName);
		setyAxisName(yName);
	}
	public TwoAxisGridPointsModel(String xName, String yName, int xPoints, int yPoints) {
		this(xName, yName);
		setxAxisPoints(xPoints);
		setyAxisPoints(yPoints);
	}

	// Note: x and y must be in lower case in getter/setter names for JFace bindings to work correctly.
	public int getxAxisPoints() {
		return xAxisPoints;
	}

	public void setxAxisPoints(int newValue) {
		int oldValue = this.xAxisPoints;
		this.xAxisPoints = newValue;
		this.pcs.firePropertyChange(X_AXIS_POINTS, oldValue, newValue);
	}

	public int getyAxisPoints() {
		return yAxisPoints;
	}

	public void setyAxisPoints(int newValue) {
		int oldValue = this.yAxisPoints;
		this.yAxisPoints = newValue;
		this.pcs.firePropertyChange(Y_AXIS_POINTS, oldValue, newValue);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(X_AXIS_POINTS)) {
			setxAxisPoints(((Number) properties.get(X_AXIS_POINTS)).intValue());
		}
		if (properties.containsKey(Y_AXIS_POINTS)) {
			setyAxisPoints(((Number) properties.get(Y_AXIS_POINTS)).intValue());
		}
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
		if (!super.equals(obj))
			return false;
		TwoAxisGridPointsModel other = (TwoAxisGridPointsModel) obj;
		return (xAxisPoints == other.xAxisPoints && yAxisPoints == other.yAxisPoints);
	}

	@Override
	public String toString() {
		if (this instanceof TwoAxisGridPointsRandomOffsetModel) {
			return super.toString();
		}
		return getClass().getSimpleName() + " [xAxisPoints=" + xAxisPoints + ", yAxisPoints=" + yAxisPoints + ", " + super.toString() + "]";
	}

}
