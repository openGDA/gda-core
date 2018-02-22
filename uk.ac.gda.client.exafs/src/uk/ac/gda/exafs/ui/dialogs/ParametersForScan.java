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

public class ParametersForScan {

	private List<ParameterValuesForBean> valuesForBeans;
	private int numberOfRepetitions;

	public ParametersForScan() {
		valuesForBeans = new ArrayList<ParameterValuesForBean>();
		numberOfRepetitions = 1;
	}

	public void addValuesForScanBean(String xmlName, String containingClassName) {
		ParameterValuesForBean overrideForFile = new ParameterValuesForBean(xmlName, containingClassName);
		addValuesForScanBean(overrideForFile);
	}

	public void addValuesForScanBean(ParameterValuesForBean f) {
		valuesForBeans.add(f);
	}

	public void setValuesForScanBeans(List<ParameterValuesForBean> overridesForScanFiles) {
		valuesForBeans = overridesForScanFiles;
	}

	public List<ParameterValuesForBean> getParameterValuesForScanBeans() {
		return valuesForBeans;
	}

	public void clearParameterValuesForScanBeans() {
		valuesForBeans.clear();
	}

	public int getNumberOfRepetitions() {
		return numberOfRepetitions;
	}

	public void setNumberOfRepetitions(int numberOfRepetitions) {
		this.numberOfRepetitions = numberOfRepetitions;
	}

	/**
	 * Return a copy of selected parameters for using to set columns names, content type in table.
	 * Xml file name is set to 'Scan', 'Sample' etc. depending on class type of parameter.
	 */
	public ParametersForScan getParametersForTableColumns() {
		ParametersForScan paramsForTableColumns = new ParametersForScan();
		paramsForTableColumns.setNumberOfRepetitions(numberOfRepetitions);
		for(ParameterValuesForBean paramValuesForBean : valuesForBeans) {
			ParameterValuesForBean newParamValuesForBean = new ParameterValuesForBean();

			// make copy of the original parameters
			newParamValuesForBean.copyFrom(paramValuesForBean);

			// Set the xml filename to Scan, Sample, Detector etc. (this is used for column label)
			String name = paramValuesForBean.getBeanTypeNiceName();
			newParamValuesForBean.setBeanFileName(name);

			paramsForTableColumns.addValuesForScanBean(newParamValuesForBean);
		}
		return paramsForTableColumns;
	}

	/**
	 * Return text string of showing the values that will go in spreadsheet view table.
	 * Used by {@link SpreadsheetViewTable#adjustColumnWidths()} only, to adjust column widths to fit the contents.
	 * @return List of strings
	 */
	public List<String> getTextForTableColumns() {
		List<String> columnText = new ArrayList<>();
		for(ParameterValuesForBean paramValuesForBean : valuesForBeans) {
			columnText.addAll(paramValuesForBean.getTextForTableColumns());
		}
		return columnText;
	}
}
