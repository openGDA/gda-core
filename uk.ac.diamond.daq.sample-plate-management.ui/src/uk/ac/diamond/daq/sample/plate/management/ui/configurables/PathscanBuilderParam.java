/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.configurables;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import gda.factory.FindableBase;
import uk.ac.diamond.daq.sample.plate.management.ui.enums.ManagingMode;
import uk.ac.diamond.daq.sample.plate.management.ui.enums.ParamType;
import uk.ac.diamond.daq.sample.plate.management.ui.enums.ValueType;

public class PathscanBuilderParam extends FindableBase {

	private String labelName;

	private String scannableName;

	private String defaultValue;

	private ParamType paramType;

	private ValueType valueType = ValueType.NUMBER;

	private ManagingMode managingMode = ManagingMode.USER;

	private Map<Integer, String> statesMap;

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String pvName) {
		this.scannableName = pvName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public ParamType getParamType() {
		return paramType;
	}

	public void setParamType(ParamType paramType) {
		this.paramType = paramType;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public ManagingMode getManagingMode() {
		return managingMode;
	}

	public void setManagingMode(ManagingMode managingMode) {
		this.managingMode = managingMode;
	}

	public Map<Integer, String> getStatesMap() {
		return statesMap;
	}

	public void setStatesMap(Map<Integer, String> statesMap) {
		this.statesMap = statesMap;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
