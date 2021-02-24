/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector.api;

import org.eclipse.scanning.api.AbstractNameable;
import org.eclipse.scanning.api.device.models.IDetectorModel;

public class ZebraModel extends AbstractNameable implements IDetectorModel {

	private double exposureTime;

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
		ZebraModel other = (ZebraModel) obj;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ZebraModel [exposureTime=" + exposureTime + "]";
	}
}