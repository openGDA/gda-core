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

package uk.ac.gda.exafs.ui.dialogs;

import gda.device.DeviceException;
import gda.device.EnumPositioner;

/**
 * Class to specify field from a bean that that can be edited in SpreadSheet view
 */
public class ParameterConfig {

	private String description;
	private String beanType;
	private String fullPathToGetter;
	private String[] allowedValues;
	private boolean showInParameterSelectionDialog;

	public ParameterConfig() {
		description = "";
		beanType = "";
		fullPathToGetter = "";
		allowedValues = new String[0];
		showInParameterSelectionDialog = true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBeanType() {
		return beanType;
	}

	public void setBeanType(String beanType) {
		this.beanType = beanType;
	}

	public String getFullPathToGetter() {
		return fullPathToGetter;
	}

	public void setFullPathToGetter(String fullPathToGetter) {
		this.fullPathToGetter = fullPathToGetter;
	}

	public String[] getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(String[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	public void setAllowedValuesFromEnum(EnumPositioner positioner) throws DeviceException {
		allowedValues = positioner.getPositions();
	}

	public void setAllowedValuesFromBoolean(boolean tf) {
		setAllowedValues(new String[] { "true", "false" });
	}

	public boolean getShowInParameterSelectionDialog() {
		return showInParameterSelectionDialog;
	}

	public void setShowInParameterSelectionDialog(boolean showInParameterSelectionDialog) {
		this.showInParameterSelectionDialog = showInParameterSelectionDialog;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beanType == null) ? 0 : beanType.hashCode());
		result = prime * result + ((fullPathToGetter == null) ? 0 : fullPathToGetter.hashCode());
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
		ParameterConfig other = (ParameterConfig) obj;
		if (beanType == null) {
			if (other.beanType != null)
				return false;
		} else if (!beanType.equals(other.beanType))
			return false;
		if (fullPathToGetter == null) {
			if (other.fullPathToGetter != null)
				return false;
		} else if (!fullPathToGetter.equals(other.fullPathToGetter))
			return false;
		return true;
	}
}
