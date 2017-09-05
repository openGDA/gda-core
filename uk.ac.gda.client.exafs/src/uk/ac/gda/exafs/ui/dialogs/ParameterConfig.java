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

	public ParameterConfig() {
		description = "";
		beanType = "";
		fullPathToGetter = "";
		allowedValues = new String[0];
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

}
