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
import java.util.List;

/**
 * A model for a scan along multiple axes with the same start and stop positions and step size.
 */
public class AxialCollatedStepModel extends AxialStepModel {

	private List<String> names;

	public AxialCollatedStepModel() {
	}

	public AxialCollatedStepModel(double start, double stop, double step, String... names) {
		super();
		this.names = Arrays.asList(names);
		setStart(start);
		setStop(stop);
		setStep(step);
	}

	public List<String> getNames() {
		return names;
	}

	@Override
	public String getName() {
		if (super.getName() != null)
			return super.getName();
		return names.get(0);
	}

	public void setNames(List<String> name) {
		this.names = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AxialCollatedStepModel other = (AxialCollatedStepModel) obj;
		if (names == null) {
			return other.names == null;
			}
		return names.equals(other.names);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[names=" + names + super.toString() + "]";
	}

}
