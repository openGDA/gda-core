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

import static org.eclipse.scanning.api.constants.PathConstants.LINE_POINTS;

import java.util.Map;

/**
 * A model for a scan along a straight line in two-dimensional space, dividing the line into the number of points given
 * in this model.<br>
 * Previously OneDEqualSpacingModel
 *
 * @author Colin Palmer
 */
public class TwoAxisLinePointsModel extends AbstractBoundingLineModel {

	private int points = 5;

	public TwoAxisLinePointsModel() {
		setName("Equal Spacing");
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		int oldValue = this.points;
		this.points = points;
		this.pcs.firePropertyChange(LINE_POINTS, oldValue, points);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(LINE_POINTS)) {
			setPoints(((Number) properties.get(LINE_POINTS)).intValue());
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [points=" + points + ", " + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + points;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		TwoAxisLinePointsModel other = (TwoAxisLinePointsModel) obj;
		return points == other.points;
	}

}
