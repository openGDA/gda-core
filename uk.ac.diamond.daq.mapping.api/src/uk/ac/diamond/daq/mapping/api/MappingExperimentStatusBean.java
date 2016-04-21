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

package uk.ac.diamond.daq.mapping.api;

import org.eclipse.scanning.api.event.status.StatusBean;

public class MappingExperimentStatusBean extends StatusBean {

	@Override
	public String toString() {
		return "MappingExperimentStatusBean [mappingExperimentBean=" + mappingExperimentBean + "]";
	}

	private IMappingExperimentBean mappingExperimentBean;

	public IMappingExperimentBean getMappingExperimentBean() {
		return mappingExperimentBean;
	}

	public void setMappingExperimentBean(IMappingExperimentBean mappingExperimentBean) {
		this.mappingExperimentBean = mappingExperimentBean;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mappingExperimentBean == null) ? 0 : mappingExperimentBean.hashCode());
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
		MappingExperimentStatusBean other = (MappingExperimentStatusBean) obj;
		if (mappingExperimentBean == null) {
			if (other.mappingExperimentBean != null)
				return false;
		} else if (!mappingExperimentBean.equals(other.mappingExperimentBean))
			return false;
		return true;
	}
}
