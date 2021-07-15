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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A model for a scan along multiple axes with the same start and stop positions and step size.
 *
 * Previously CollatedStepModel
 */
public class AxialCollatedStepModel extends AxialStepModel {

	private List<String> names = new ArrayList<>();

	public AxialCollatedStepModel() {
	}

	public AxialCollatedStepModel(double start, double stop, double step, String... names) {
		super();
		this.names = Arrays.asList(names);
		setUnits(getUnits());
		setStart(start);
		setStop(stop);
		setStep(step);
	}

	public List<String> getNames() {
		return names;
	}

	@Override
	public List<String> getScannableNames(){
		return names;
	}

	@Override
	public String getName() {
		if (super.getName() != null)
			return super.getName();
		return names.get(0);
	}

	public void setNames(List<String> name) {
		pcs.firePropertyChange("names", this.names, name);
		this.names = name;
	}

	@Override
	public void setUnits(List<String> units) {
		final List<String> copiedList = new ArrayList<>(units);
		// TODO: Should we throw an IllegalArgumentException, or allow the ModelValidation to fail? Jython demands we
		//   pass either 1 common unit, or 1 for each axis, so we expand here (other validation prevents passing len 1)
		// ACSM isn't available from MScan or Mapping but can be instantiated in Jython and put into ScanRequest...
		// Not all Scannables will have units, and if they don't we assume "mm" to match default, mapping, mscan.
		while (copiedList.size() < names.size()) {
			copiedList.add(HARDCODED_UNITS);
		}
		super.setUnits(copiedList);
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
