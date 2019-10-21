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

/**
 * A model for one or more positions where nothing is moved. This can be used to expose detectors
 * without moving any scannables.
 *
 * @author Matthew Gerring
 */
public class StaticModel extends AbstractPointsModel {

	private int size = 1;

	public StaticModel() {
		setName("Static");
	}

	public StaticModel(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		StaticModel other = (StaticModel) obj;
		return (size == other.size);
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public void setContinuous(boolean continuous) {
		if (continuous) {
			throw new ModelValidationException("StaticModel does not support continuous operation", this, "continuous");
		}
	}

	@Override
	public boolean isAlternating() {
		return false;
	}

	@Override
	public void setAlternating(boolean alternating) {
		if (alternating) {
			throw new ModelValidationException("StaticModel does not support alternating operation", this, "continuous");
		}
	}
}
