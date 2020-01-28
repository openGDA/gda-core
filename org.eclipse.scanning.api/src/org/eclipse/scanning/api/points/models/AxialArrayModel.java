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

import java.util.Arrays;

/**
 * A model for a scan at an array of defined positions along a single axis.
 *
 * @author Colin Palmer
 *
 */
public class AxialArrayModel extends AbstractPointsModel {

	private double[] positions;

	public AxialArrayModel() {

	}

	public AxialArrayModel(String scannableName) {
		setName(scannableName);
	}

	public AxialArrayModel(double... positions) {
		this.positions = positions;
	}

	public double[] getPositions() {
		return positions;
	}

	public void setPositions(double... positions) {
		double[] oldValue = this.positions;
		this.positions = positions;
		this.pcs.firePropertyChange("positions", oldValue, positions);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(positions);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AxialArrayModel other = (AxialArrayModel) obj;
		return Arrays.equals(positions, other.positions);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[positions=" + Arrays.toString(positions) + super.toString() + "]";
	}

}
