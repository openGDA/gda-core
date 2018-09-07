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

import java.util.ArrayList;
import java.util.List;

import gda.factory.FindableBase;

public class SpreadsheetViewConfig extends FindableBase {

	private List<ParameterConfig> paramConfigList;
	private List<String> parameterTypes;

	public SpreadsheetViewConfig() {
		paramConfigList = new ArrayList<>();
		parameterTypes = new ArrayList<>();
		setName("");
	}

	public List<ParameterConfig> getParameters() {
		return paramConfigList;
	}

	public void setParameters(List<ParameterConfig> editableFields) {
		this.paramConfigList = editableFields;
	}

	public void addParameter(ParameterConfig field) {
		paramConfigList.add(field);
	}

	public void clearParameters() {
		paramConfigList.clear();
	}

	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public ParameterConfig getParameter(String beanType, String fullPathToGetter) {
		for(ParameterConfig paramConfig : paramConfigList) {
			if (paramConfig.getBeanType().equals(beanType) && paramConfig.getFullPathToGetter().equals(fullPathToGetter)) {
				return paramConfig;
			}
		}
		return null;
	}
}
