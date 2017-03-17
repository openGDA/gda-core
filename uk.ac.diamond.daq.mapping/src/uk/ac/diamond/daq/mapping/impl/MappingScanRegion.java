/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.points.models.IScanPathModel;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class MappingScanRegion implements IMappingScanRegion {

	private IMappingScanRegionShape region;
	private IScanPathModel scanPath;

	@Override
	public IMappingScanRegionShape getRegion() {
		return region;
	}

	@Override
	public void setRegion(IMappingScanRegionShape region) {
		this.region = region;
	}

	@Override
	public IScanPathModel getScanPath() {
		return scanPath;
	}

	@Override
	public void setScanPath(IScanPathModel scanPath) {
		this.scanPath = scanPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((scanPath == null) ? 0 : scanPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingScanRegion other = (MappingScanRegion) obj;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (scanPath == null) {
			if (other.scanPath != null)
				return false;
		} else if (!scanPath.equals(other.scanPath))
			return false;
		return true;
	}

}
