/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition;


/**
 * Identifies a type of acquisition
 */
public class AcquisitionKeys {

	private AcquisitionPropertyType propertyType;
	private AcquisitionSubType subType;
	private TrajectoryShape templateType;

	public AcquisitionKeys() {} /* required for deserialisation */

	public AcquisitionKeys(AcquisitionPropertyType propertyType, AcquisitionSubType subType, TrajectoryShape templateType) {
		this.propertyType = propertyType;
		this.subType = subType;
		this.templateType = templateType;
	}

	public AcquisitionPropertyType getPropertyType() {
		return propertyType;
	}

	public AcquisitionSubType getSubType() {
		return subType;
	}

	public TrajectoryShape getTemplateType() {
		return templateType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyType == null) ? 0 : propertyType.hashCode());
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		result = prime * result + ((templateType == null) ? 0 : templateType.hashCode());
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
		AcquisitionKeys other = (AcquisitionKeys) obj;
		if (propertyType != other.propertyType)
			return false;
		if (subType != other.subType)
			return false;
		if (templateType != other.templateType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AcquisitionKeys [propertyType=" + propertyType + ", subType=" + subType + ", templateType="
				+ templateType + "]";
	}
}
