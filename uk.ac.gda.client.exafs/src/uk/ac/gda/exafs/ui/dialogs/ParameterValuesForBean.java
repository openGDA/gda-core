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

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class ParameterValuesForBean {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ParameterValuesForBean.class);

	private String beanFileName = ""; // Full path to XML file containing bean object
	private String beanType = ""; // class type of bean, as returned by object.getClass().getName();
	private List<ParameterValue> parameterValues; // new values to be applied to the bean

	// Constructor
	public ParameterValuesForBean() {
		parameterValues = new ArrayList<ParameterValue>();
	}

	// Constructor
	public ParameterValuesForBean(String beanName, String className) {
		parameterValues = new ArrayList<ParameterValue>();
		this.beanFileName = beanName;
		this.beanType = className;
	}

	public void copyFrom(ParameterValuesForBean valuesForScanBean) {
		setBeanFileName(valuesForScanBean.getBeanFileName());
		setBeanType(valuesForScanBean.getBeanType());
		parameterValues.clear();
		for(ParameterValue override : valuesForScanBean.getParameterValues()) {
			addParameterValue(override.getFullPathToGetter(), override.getNewValue());
		}
	}

	public void addParameterValue(ParameterValue paramValue) {
		parameterValues.add( new ParameterValue(paramValue) );
	}

	public void addParameterValue(String fullPathToGetter, Object newValue) {
		parameterValues.add( new ParameterValue(fullPathToGetter, newValue) );
	}

	public List<ParameterValue> getParameterValues() {
		return parameterValues;
	}

	/**
	 * ParameterValue that has the given fullPathToGetter
	 * @param fullPathToGetter
	 * @return ParameterValue
	 */
	public ParameterValue getParameterValue(String fullPathToGetter) {
		for(ParameterValue param : parameterValues) {
			if (param.getFullPathToGetter().equals(fullPathToGetter)){
				return param;
			}
		}
		return null;
	}

	public String getBeanFileName() {
		return beanFileName;
	}

	public void setBeanFileName(String xmlFileName) {
		this.beanFileName = xmlFileName;
	}

	public String getBeanType() {
		return beanType;
	}

	public void setBeanType(String beanType) {
		this.beanType = beanType;
	}

	static public class ParameterValue {
		private String fullPathToGetter = ""; //e.g.  getXPosition,  getXYThetaStage.getX
		private Object newValue = ""; // new value for parameter
		private boolean editable = true; // whether user can alter value of this parameter (e.g. in gui)

		public ParameterValue(ParameterValue paramValue) {
			this.fullPathToGetter = paramValue.fullPathToGetter;
			this.newValue = paramValue.newValue;
			this.editable = paramValue.editable;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fullPathToGetter == null) ? 0 : fullPathToGetter.hashCode());
			result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
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
			ParameterValue other = (ParameterValue) obj;
			if (fullPathToGetter == null) {
				if (other.fullPathToGetter != null)
					return false;
			} else if (!fullPathToGetter.equals(other.fullPathToGetter))
				return false;
			if (newValue == null) {
				if (other.newValue != null)
					return false;
			} else if (!newValue.equals(other.newValue))
				return false;
			return true;
		}

		public ParameterValue() {
		}

		// Constructor
		public ParameterValue(String fullPathToGetter, Object newValue) {
			this.fullPathToGetter = fullPathToGetter;
			this.newValue = newValue;
		}

		public String getFullPathToGetter() {
			return fullPathToGetter;
		}

		public Object getNewValue() {
			return newValue;
		}

		public void setNewValue(Object newValue) {
			this.newValue = newValue;
		}

		public void setEditable(boolean editable) {
			this.editable = editable;
		}

		public boolean isEditable() {
			return editable;
		}
	}

	// Comparator for sorting ParameterOverrides by name
	private class ParameterComparator implements Comparator<ParameterValue> {
		@Override
		public int compare(ParameterValue p1, ParameterValue p2) {
			return p1.getFullPathToGetter().compareTo(p2.getFullPathToGetter());
		}
	}

    /**
     * Sort parameter override list into alphabetical order
     */
	public void sort() {
		parameterValues.sort(new ParameterComparator());
	}

	static public void addAliases(XStream xstream) {
		xstream.alias("ParameterValuesForScanBean", ParameterValuesForBean.class);
		xstream.alias("ParameterValue", ParameterValue.class);
		xstream.addImplicitCollection(ParameterValuesForBean.class, "parameterValue");
	}

	static public XStream getXStream() {
		XStream xstream = new XStream();
		addAliases(xstream);
		return xstream;
	}

	public String toXML() {
		return getXStream().toXML(this);
	}

	public String getBeanTypeNiceName() {
		String niceName = "";
		Class<?> clazz = getBeanClass();
		if (clazz != null) {
			if (ISampleParameters.class.isAssignableFrom(clazz)) {
				niceName = "Sample xml";
			} else if (IDetectorParameters.class.isAssignableFrom(clazz)) {
				niceName = "Detector xml";
			} else if (IScanParameters.class.isAssignableFrom(clazz)) {
				niceName = "Scan xml";
			} else if (IOutputParameters.class.isAssignableFrom(clazz)) {
				niceName = "Output xml";
			}
		}
		return niceName;
	}

	/**
	 * @return class object corresponding to bean type; null if ClassNotFoundException
	 */
	public Class<?> getBeanClass() {
		Class<?> classForName = null;
		try {
			classForName = Class.forName(beanType);
		} catch (ClassNotFoundException e) {
			logger.error("Problem creating class for {}", beanType, e);
		}
		return classForName;
	}

	/**
	 * @return Bean object loaded from xml file
	 * @throws Exception
	 */
	public Object getBeanObject() throws Exception {
		try {
			return XMLHelpers.getBeanObject(null, beanFileName);
		}catch(FileNotFoundException ex) {
			throw new Exception("Unable to read from file "+beanFileName, ex);
		}
	}

	/**
	 * Get values from bean object stored in xml file - see {@link #getValuesFromBean(Object)}.
	 * @return Map with : key = full path of 'getter' method, value = value from bean
	 * @throws Exception
	 */
	public Map<String, Object> getValuesFromBean() throws Exception {
		return getValuesFromBean(getBeanObject());
	}

	/**
	 * Get values from bean object.
	 * Use 'getter' methods from ParametersValue list to get names of methods to be called to get the values.
	 * @return Map with : key = full path of 'getter' method, value = value from bean
	 * @throws Exception
	 */
	public Map<String, Object> getValuesFromBean(Object beanObject) throws Exception {
		Map<String, Object> valuesFromGetters = new HashMap<>();
		for(ParameterValue param : parameterValues) {
			try {
				String getter = param.getFullPathToGetter();
				Object result = SpreadsheetViewHelperClasses.invokeMethodFromName(beanObject, getter, null);
				valuesFromGetters.put(getter, result);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.error("Problem getting value {} from bean in {}", param.getFullPathToGetter(), beanFileName);
			}
		}
		return valuesFromGetters;
	}

	public List<String> getTextForTableColumns() {
		List<String> columnText = new ArrayList<>();
		columnText.add(FilenameUtils.getName(beanFileName));
		for(ParameterValue param : parameterValues) {
			columnText.add(param.getNewValue().toString());
		}
		return columnText;
	}
}
