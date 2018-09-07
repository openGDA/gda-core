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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * This class is used to store a set of parameters for single bean.
 * The list of {@link ParameterValue}s is used to set the new values on a bean using the {@link #setValuesOnBean(Object)} function.
 *  For each {@link ParameterValue}, the full path to the setter method for the required parameter is obtained using reflection
 *  and set to the new value.
 *
 */
public class ParameterValuesForBean {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ParameterValuesForBean.class);

	/** Full path to XML file containing bean object */
	private String beanFileName = "";

	/** class type of bean, as returned by object.getClass().getName() */
	private String beanType = "";

	/** New values to be applied to the bean */
	private List<ParameterValue> parameterValues;

	// Constructor
	public ParameterValuesForBean() {
		parameterValues = new ArrayList<>();
	}

	// Constructor
	public ParameterValuesForBean(String beanName, String className) {
		parameterValues = new ArrayList<>();
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

	/**
	 * ParameterValue with same fullPathToGetter value as the 'otherParam' object
	 * @param otherParam
	 * @return ParameterValue
	 */
	public ParameterValue getParameterValue(ParameterValue otherParam) {
		return getParameterValue(otherParam.getFullPathToGetter());
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

	/**
	 * Stores settings used to set value of a parameter on bean :
	 * <li> fullPathToGetter - full path to the getter method that returns the required parameter (e.g. getXPosition, geXYThetaStage.getX)
	 * <li> newValue - new value to use for the parameter
	 */
	public static class ParameterValue {
		/** Full path to getter method for parameter */
		private String fullPathToGetter = "";

		/** New value for the parameter */
		private Object newValue = "";

		/** Whether user can alter value of this parameter (e.g. in gui) */
		private boolean editable = true;

		public ParameterValue() {
		}

		public ParameterValue(ParameterValue paramValue) {
			this.fullPathToGetter = paramValue.fullPathToGetter;
			this.newValue = paramValue.newValue;
			this.editable = paramValue.editable;
		}

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (editable ? 1231 : 1237);
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
			if (editable != other.editable)
				return false;
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
	}

    /**
     * Sort parameter override list into alphabetical order
     */
	public void sort() {
		parameterValues.sort((param1, param2) -> param1.getFullPathToGetter().compareTo(param2.getFullPathToGetter()));
	}

	public static void addAliases(XStream xstream) {
		xstream.alias("ParameterValuesForScanBean", ParameterValuesForBean.class);
		xstream.alias("ParameterValue", ParameterValue.class);
		xstream.addImplicitCollection(ParameterValuesForBean.class, "parameterValues");
	}

	public static XStream getXStream() {
		XStream xstream = new XStream();
		addAliases(xstream);
		return xstream;
	}

	public String toXML() {
		return getXStream().toXML(this);
	}

	public String getBeanTypeNiceName() {
		String niceName = "";
		Class<XMLRichBean> clazz = getBeanClass();
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

	public boolean isScanBean() {
		Class<XMLRichBean> clazz = getBeanClass();
		if (clazz != null) {
			return IScanParameters.class.isAssignableFrom(clazz);
		} else {
			return false;
		}
	}

	/**
	 * @return class object corresponding to bean type; null if ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public Class<XMLRichBean> getBeanClass() {
		Class<XMLRichBean> classForName = null;
		try {
			classForName = (Class<XMLRichBean>) Class.forName(beanType);
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
	 * This uses 'getter' methods given in the {@link ParameterValue} list to get names of methods to be
	 * called to get the values from the bean object.
	 *
	 * @return Map with : key = full path of 'getter' method, value = value from bean
	 */
	public Map<String, Object> getValuesFromBean(Object beanObject) {
		Map<String, Object> valuesFromGetters = new HashMap<>();
		for(ParameterValue param : parameterValues) {
			try {
				String getter = param.getFullPathToGetter();
				Object result = invokeMethodFromName(beanObject, getter, null);
				valuesFromGetters.put(getter, result);
			} catch (IllegalAccessException | InvocationTargetException e) {
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

	/**
	 * Load bean object from XML file and update it using {@link #setValuesOnBean(Object)} and return the modified bean object
	 * @return beanObject with updated values.
	 * @throws Exception
	 */
	public Object getModifiedBeanObject() throws Exception {
		Object beanObject = getBeanObject();
		setValuesOnBean(beanObject);
		return beanObject;
	}

	/**
	 * Update bean object with using values from {@link ParameterValue}s list
	 * This uses 'getter' methods given in the {@link ParameterValue} list to get names of 'setter' methods
	 * to be called to set the new values on the bean object.
	 *
	 * @param beanObject
	 */
	public void setValuesOnBean(Object beanObject) {
		if (beanObject.getClass().getName().equals(getBeanType())) {
			logger.debug("Bean object matches expected type {}. Setting new values.", getBeanType());
		} else {
			logger.warn("Bean object type {} does not match expected type {}. Values might not get set correctly", beanObject.getClass().getName(), getBeanType());
		}

		for (ParameterValue paramOverride : getParameterValues()) {
			String fullPathToGetter = paramOverride.getFullPathToGetter();

			String fullPathToSetter;
			if (fullPathToGetter.contains(".is")) {
				fullPathToSetter = fullPathToGetter.replaceFirst(".is", ".set");
			} else if (fullPathToGetter.contains(".get")) {
				fullPathToSetter = fullPathToGetter.replaceFirst(".get", ".set");
			} else {
				fullPathToSetter = fullPathToGetter.replaceFirst("get", "set");
			}

			try {
				logger.debug("Calling method {} with value {}",  fullPathToGetter, paramOverride.getNewValue());
				invokeMethodFromName(beanObject, fullPathToSetter, paramOverride.getNewValue());
			} catch (Exception e) {
				logger.error("Problem calling method {} with value {}",  fullPathToSetter, paramOverride.getNewValue(), e);
			}
		}
	}

	/**
	 * Invoke named method on the supplied object. The method name can chain together several method calls by separating the
	 * parts by dots. (i.e. as would be typed on Jython console to invoke method)
	 * <p>
	 * If specified, {@code valueToSet}, will be used to create a new Integer, Double or String object to pass to the final method
	 * invoked. e.g. for a {@link DetectorParameters} object with {@code pathToMethod} = getSoftXRaysParameters.setConfigFileName,
	 * the 'getSoftXRaysParameters()' method will be invoked first, followed by setConfigName(valueToSet) on the returned object.
	 * <p>
	 * The {@code pathToMethod} name may also contain a value in brackets e.g. getSampleParameterMotor(motor1).getDoMove
	 * In this case, the value in brackets ('motor1') will be passed as parameter to the 'getSampleParameterMotor' method when
	 * invoking it (as a string), before invoking the 'getDoMove' method on the returned object.
	 *
	 * @param obj
	 * @param pathToMethod
	 * @param valueToSet (null if not needed)
	 * @return Final return value of called method
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object invokeMethodFromName(Object obj, String pathToMethod, Object valueToSet) throws IllegalAccessException, InvocationTargetException {
		String[] splitPath = pathToMethod.split("[.]");
		Object parentObject = obj;
		for(String pathPart : splitPath) {

			String methodName = pathPart;

			// Extract method name and parameter from string if path part contains brackets : e.g. someMethod(someValue)
			String paramValueFromPath = "";
			if (pathPart.contains("(")) {
				String[] splitMethodCall = pathPart.split("[()]");
				methodName = splitMethodCall[0];
				paramValueFromPath = splitMethodCall[1];
			}

			Method methodObj = getMethodWithName(parentObject.getClass(), methodName);
			if (methodObj!=null) {
				if (methodObj.getParameterCount()==1) {
					//Method expects a parameter...
					Object valueForMethod = null;
					if (paramValueFromPath.length()>0) {
						// Value extracted from brackets of part part
						valueForMethod = paramValueFromPath;
					} else if (valueToSet!=null) {
						// Value passed in function call
						valueForMethod = createNumberOrString(valueToSet, methodObj.getParameterTypes()[0]);
					}
					if (valueForMethod == null) {
						logger.warn("Invoking {} with null parameter", pathPart);
					}
					parentObject = methodObj.invoke(parentObject, valueForMethod);
				}
				else {
					parentObject = methodObj.invoke(parentObject);
				}
			} else {
				logger.warn("Unable to invoke {} ", pathPart);
			}
		}
		return parentObject;
	}

	/**
	 * Create new Integer, Double, Boolean, List<String> or String from supplied object depending on its contents.
	 *
	 * @param value
	 * @return New instance of object
	 */
	private static Object createNumberOrString(Object value, Class<?> requiredType) {
		String stringValue = value.toString().trim();

		if (requiredType == Integer.class || requiredType == int.class) {
			int decimalPlaceIndex = stringValue.indexOf(".");
			int lastPos = decimalPlaceIndex > 0 ? decimalPlaceIndex : stringValue.length();
			return Integer.parseInt(stringValue.substring(0, lastPos));
		} else if (requiredType == Double.class || requiredType == double.class) {
			return Double.parseDouble(stringValue);
		} else if (requiredType == Boolean.class || requiredType == boolean.class) {
			return Boolean.parseBoolean(stringValue);
		} else if (requiredType == List.class) {
			// list of strings from space separated values
			return Arrays.asList(stringValue.split("[ ]"));
		} else {
			// Not a number, assume it's a string
			return stringValue;
		}
	}

	/**
	 * Return method with given name from class object; returns null if method was not found.
	 * Alternative to using clazz.getMethod(methodName) to avoid throwing exceptions.
	 * @param clazz
	 * @param methodName
	 * @return method object matching methodName; null if no matching method was found.
	 */
	private static Method getMethodWithName(Class<?> clazz, String methodName) {
		for(Method meth : clazz.getMethods()) {
			if (meth.getName().equals(methodName)) {
				return meth;
			}
		}
		return null;
	}

}
