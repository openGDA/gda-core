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

import static org.eclipse.scanning.api.constants.PathConstants.LINE_STEP;

import java.util.Map;

/**
 * A model for a scan along a straight line in two-dimensional space, starting at the beginning of the line and moving
 * in steps of the size given in this model.<br>
 * Previously OneDStepModel
 *
 * @author Colin Palmer
 */
public class TwoAxisLineStepModel extends AbstractBoundingLineModel {

	private double step = 1;

	public TwoAxisLineStepModel() {
		setName("Step");
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		double oldValue = this.step;
		this.step = step;
		this.pcs.firePropertyChange(LINE_STEP, oldValue, step);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(LINE_STEP)) {
			setStep(((Number) properties.get(LINE_STEP)).doubleValue());
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [step=" + step + ", " + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		TwoAxisLineStepModel other = (TwoAxisLineStepModel) obj;
		return (Double.doubleToLongBits(step) == Double.doubleToLongBits(other.step));
	}

}
