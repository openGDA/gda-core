/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.mapping.impl;

import org.eclipse.scanning.api.device.models.IDetectorModel;

/**
 * A wrapper around a detector model, for use in the mapping UI.
 * It adds a name which is displayed to the user (which can be different to the name
 * of the detector in the model), and a boolean for whether to include this detector in the scan.
 */
public class DetectorModelWrapper<T extends IDetectorModel> extends ScanModelWrapper<T> {

	/**
	 * Indicates whether the detector should be shown by default (i.e. after a client reset) in the detectors section of
	 * the mapping experiment view,
	 */
	private boolean shownByDefault = true;

	public DetectorModelWrapper() {
		// no-arg constructor for json serialization
	}

	public DetectorModelWrapper(String name, T model, boolean includeInScan) {
		super(name, model, includeInScan);
	}

	public boolean isShownByDefault() {
		return shownByDefault;
	}

	public void setShownByDefault(boolean shownByDefault) {
		this.shownByDefault = shownByDefault;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (shownByDefault ? 1231 : 1237);
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
		final DetectorModelWrapper other = (DetectorModelWrapper) obj;
		return shownByDefault == other.shownByDefault;
	}

	@Override
	public String toString() {
		return "DetectorModelWrapper [shownByDefault=" + shownByDefault + ", " + super.toString() + "]";
	}

}
