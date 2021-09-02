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

package uk.ac.gda.client.properties.acquisition;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;

/**
 * Identify a specific acquisition configuration.
 *
 * <p>
 * This class contains the informations required to correctly display an acquisition configuration on the GUI:
 * <ul>
 * <li>
 * {@link #getPropertyType()} which is associated with a perspective
 * </li>
 * <li>
 * {@link #getTemplateType()} which defines the configuration gui specific layout
 * </li>
 * </ul>
 * </p>
 *
 * @author Maurizio Nagni
 */
public class AcquisitionKeys {

	private AcquisitionPropertyType propertyType;
	private AcquisitionTemplateType templateType;

	public AcquisitionKeys() {
	}

	public AcquisitionKeys(AcquisitionPropertyType propertyType, AcquisitionTemplateType templateType) {
		this.propertyType = propertyType;
		this.templateType = templateType;
	}

	public AcquisitionPropertyType getPropertyType() {
		return propertyType;
	}
	public void setPropertyType(AcquisitionPropertyType propertyType) {
		this.propertyType = propertyType;
	}
	public AcquisitionTemplateType getTemplateType() {
		return templateType;
	}
	public void setTemplateType(AcquisitionTemplateType templateType) {
		this.templateType = templateType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyType == null) ? 0 : propertyType.hashCode());
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
		if (templateType != other.templateType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AcquisitionKeys [propertyType=" + propertyType + ", templateType=" + templateType + "]";
	}
}
