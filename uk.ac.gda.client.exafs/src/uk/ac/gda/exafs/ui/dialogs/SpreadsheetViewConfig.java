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
	private List<ParameterConfigGenerator> parameterConfigGenerators;
	private List<String> parameterTypes;

	public SpreadsheetViewConfig() {
		paramConfigList = new ArrayList<>();
		parameterTypes = new ArrayList<>();
		parameterConfigGenerators = new ArrayList<>();
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

	public void setGenerators(List<ParameterConfigGenerator> parameterConfigGenerators) {
		this.parameterConfigGenerators = parameterConfigGenerators;
	}

	public List<ParameterConfigGenerator> getGenerators() {
		return parameterConfigGenerators;
	}

	public void updateGeneratedParameterConfigs(List<ParameterValuesForBean> paramValueForBean) {
		for(ParameterConfigGenerator gen : parameterConfigGenerators) {
			var c = gen.createParameterConfigs(paramValueForBean);
			paramConfigList.removeAll(c);
			paramConfigList.addAll(c);
		}
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

	/**
	 * Return the ParameterConfig object that matches the given beantype  and fullPathToGetter
	 * @param beanType
	 * @param fullPathToGetter
	 * @return ParameterConfig object, or null if no matching object could be found
	 */
	public ParameterConfig getParameter(String beanType, String fullPathToGetter) {
		for(ParameterConfig paramConfig : paramConfigList) {
			if (checkParameterConfigMatch(paramConfig, beanType, fullPathToGetter)) {
				return paramConfig;
			}
			// Check the additional parameter config for a match
			for(var p : paramConfig.getAdditionalConfig()) {
				if (checkParameterConfigMatch(p, beanType, fullPathToGetter)) {
					return p;
				}
			}
		}
		return null;
	}

	private boolean checkParameterConfigMatch(ParameterConfig paramConfig, String beanType, String pathToGetter) {
		return paramConfig.getBeanType().equals(beanType) && paramConfig.getFullPathToGetter().equals(pathToGetter);
	}
}
