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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;

/**
 * This class contains a full set of parameters for a single scan in a {@code List<ParameterValuesForBean>}.
 * Each {@link ParameterValuesForBean} object contains parameters to be applied to a 'scan settings' bean.
 * Usually there will be 4 of these (for scan, detector, sample, output settings)
 * corresponding to the 4 xml files used QExafs, Xas, Xanes scans.
 *
 */
public class ParametersForScan {

	private List<ParameterValuesForBean> valuesForBeans;
	private int numberOfRepetitions;

	public ParametersForScan() {
		valuesForBeans = new ArrayList<>();
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
		valuesForBeans = new ArrayList<>(overridesForScanFiles);
	}

	@JsonProperty("ParametersForScanBean")
	public List<ParameterValuesForBean> getParameterValuesForScanBeans() {
		return valuesForBeans;
	}

	/**
	 * Loop over all parameters for the scan in order, return indices of bean and value of 'index'th item in the loop.
	 * @param index which parameter to find the indices of
	 * @return pair : Bean index, Parameter value index
	 */
	@JsonIgnore
	public Pair<Integer, Integer> getParameterValueByIndex(int index) {
		int count = 0;
		for(int i=0; i<valuesForBeans.size(); i++) {
			List<ParameterValue> paramsForBean = valuesForBeans.get(i).getParameterValues();
			if (count++ == index) {
				return Pair.create(i, null);
			}
			for(int j=0; j<paramsForBean.size(); j++) {
				if (count++ == index) {
					return Pair.create(i, j);
				}
			}
		}
		return null;
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
	 * Return a copy of scan parameters suitable for setting column names and content type in GUI table.
	 * Xml file name is set to 'Scan', 'Sample' etc. depending on class type of parameter.
	 */
	@JsonIgnore
	public ParametersForScan getParametersForTableColumns() {
		ParametersForScan paramsForTableColumns = new ParametersForScan();
		paramsForTableColumns.setNumberOfRepetitions(numberOfRepetitions);
		for(ParameterValuesForBean paramValuesForBean : valuesForBeans) {
			ParameterValuesForBean newParamValuesForBean = new ParameterValuesForBean();

			// make copy of the original parameters
			newParamValuesForBean.copyFrom(paramValuesForBean);

			// Set the xml filename to Scan, Sample, Detector etc. (this is used for column label)
			newParamValuesForBean.setBeanFileName(paramValuesForBean.getBeanTypeNiceName()+" xml");

			paramsForTableColumns.addValuesForScanBean(newParamValuesForBean);
		}
		return paramsForTableColumns;
	}

	/**
	 *
	 * @return List of XML files names containing the scan beans
	 */
	@JsonIgnore
	public List<String> getFileNames() {
		return valuesForBeans.stream()
			.map(ParameterValuesForBean::getBeanFileName)
			.collect(Collectors.toList());
	}

	/**
	 * Scan xml files required for several scan and make sure they exist
	 * @param parametersForAllScans
	 * @return  Warning message if required files are missing, empty string otherwise
	 */
	public static String checkRequiredXmlsExist(List<ParametersForScan> parametersForAllScans) {
		String warningMessage = "";
		int scanIndex = 0;
		for(ParametersForScan parametersForScan : parametersForAllScans) {
			for (ParameterValuesForBean parameterForScanBean : parametersForScan.getParameterValuesForScanBeans()) {
				String fullXmlPath = parameterForScanBean.getBeanFileName();
				File xmlFile = new File(fullXmlPath);
				if (!xmlFile.exists() || !xmlFile.isFile()) {
					warningMessage += "Scan " + scanIndex + " : file '" + fullXmlPath + "' cannot be read\n";
				}
			}
			scanIndex++;
		}
		return warningMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numberOfRepetitions;
		result = prime * result + ((valuesForBeans == null) ? 0 : valuesForBeans.hashCode());
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
		ParametersForScan other = (ParametersForScan) obj;
		if (numberOfRepetitions != other.numberOfRepetitions)
			return false;
		if (valuesForBeans == null) {
			if (other.valuesForBeans != null)
				return false;
		} else if (!valuesForBeans.equals(other.valuesForBeans))
			return false;
		return true;
	}
}
