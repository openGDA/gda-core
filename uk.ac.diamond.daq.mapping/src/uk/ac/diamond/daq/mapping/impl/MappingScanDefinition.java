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

import java.util.Collections;
import java.util.List;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;

public class MappingScanDefinition implements IScanDefinition {

	private IMappingScanRegion mappingScanRegion;
	private List<IScanPathModelWrapper> outerScannables;

	public MappingScanDefinition() {
		mappingScanRegion = new MappingScanRegion();
		outerScannables = Collections.emptyList();
	}

	@Override
	public IMappingScanRegion getMappingScanRegion() {
		return mappingScanRegion;
	}

	@Override
	public void setMappingScanRegion(IMappingScanRegion mappingScanRegion) {
		this.mappingScanRegion = mappingScanRegion;
	}

	@Override
	public List<IScanPathModelWrapper> getOuterScannables() {
		return outerScannables;
	}

	@Override
	public void setOuterScannables(List<IScanPathModelWrapper> outerScannables) {
		if (outerScannables == null) {
			throw new NullPointerException();
		}
		this.outerScannables = outerScannables;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappingScanRegion == null) ? 0 : mappingScanRegion.hashCode());
		result = prime * result + ((outerScannables == null) ? 0 : outerScannables.hashCode());
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
		MappingScanDefinition other = (MappingScanDefinition) obj;
		if (mappingScanRegion == null) {
			if (other.mappingScanRegion != null)
				return false;
		} else if (!mappingScanRegion.equals(other.mappingScanRegion))
			return false;
		if (outerScannables == null) {
			if (other.outerScannables != null)
				return false;
		} else if (!outerScannables.equals(other.outerScannables))
			return false;
		return true;
	}

}
