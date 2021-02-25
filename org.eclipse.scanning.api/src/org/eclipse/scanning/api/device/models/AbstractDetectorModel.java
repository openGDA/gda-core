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
package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.AbstractNameableTimeoutable;

public abstract class AbstractDetectorModel extends AbstractNameableTimeoutable implements IDetectorModel {

	/**
	 * The exposure time. If calculation is shorter than this, time is artificially added to make the detector respect
	 * the time that is set.
	 */
	private double exposureTime; // Seconds

	protected AbstractDetectorModel() {
		// no-arg constructor for json
	}

	protected AbstractDetectorModel(String name) {
		setName(name);
	}

	protected AbstractDetectorModel(String name, double exposureTime) {
		this(name);
		setExposureTime(exposureTime);
	}

	protected AbstractDetectorModel(AbstractDetectorModel toCopy) {
		setName(toCopy.getName());
		setTimeout(toCopy.getTimeout());
		setExposureTime(toCopy.getExposureTime());
	}

	@Override
	public double getExposureTime() {
		return exposureTime;
	}

	@Override
	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(exposureTime);
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
		AbstractDetectorModel other = (AbstractDetectorModel) obj;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractDetectorModel [exposureTime=" + exposureTime + "]";
	}
}
