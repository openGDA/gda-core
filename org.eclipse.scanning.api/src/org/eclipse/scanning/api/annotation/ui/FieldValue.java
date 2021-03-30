/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.annotation.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import org.eclipse.scanning.api.points.models.IScanPathModel;

/**
 * Class to be used for editing the field of a Model
 * <p>
 * Reads annotations used on the models field and provides discovery information about the field.
 */
public class FieldValue {

	private Object model;
	private String name;

	public FieldValue(Object model, String name) {
		this.model = model;
		this.name = name;
	}

	public Object getModel() {
		return model;
	}

	public void setModel(IScanPathModel model) {
		this.model = model;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		final FieldDescriptor anot;
		try {
			anot = FieldUtils.getAnnotation(model, name);
		} catch (NoSuchFieldException | SecurityException e) {
			return e.getMessage();
		}
		if (anot != null) {
			final String label = anot.label();
			if (label != null && !label.isEmpty())
				return label;
		}
		return decamel(name);
	}

	/**
	 * Method to decamel case field names.
	 *
	 * @param fieldName
	 * @return decamelled version of name
	 */
	private static String decamel(String fieldName) {
		try {
			final String[] words = fieldName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
			final StringBuilder buf = new StringBuilder();
			for (String string : words) {
				buf.append(String.valueOf(string.charAt(0)).toUpperCase());
				buf.append(string.substring(1));
				buf.append(" ");
			}
			return buf.toString().trim();
		} catch (Exception ne) {
			return fieldName;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FieldValue other = (FieldValue) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Class<? extends Object> getType() throws NoSuchFieldException, SecurityException {
		final Field field = FieldUtils.getField(model, name);
		return field.getType();
	}

	public FieldDescriptor getAnnotation() {
		try {
			return FieldUtils.getAnnotation(model, name);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
	}

	public boolean isFileProperty() {
		final FieldDescriptor anot = getAnnotation();
		if (anot != null && anot.file() != FileType.NONE) {
			return true;
		}

		final Class<? extends Object> clazz;
		try {
			clazz = getType();
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
		return FieldUtils.isFileType(clazz);
	}

	/**
	 * Set fields value
	 *
	 * @param value
	 * @throws Exception
	 */
	public void set(Object value) throws Exception {
		set(model, name, value);
	}

	/**
	 * Get fields value
	 *
	 * @return value
	 */
	public Object get() {
		try {
			return get(model, name);
		} catch (Exception ne) {
			return null;
		}
	}

	public Object get(boolean create) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		final Object value = get();
		if (value != null || !create) {
			return value;
		}
		final Method method = getMethod(model.getClass(), name);
		if (method != null) {
			return method.getReturnType().getDeclaredConstructor().newInstance();
		}
		return null;
	}

	/**
	 * Tries to find the no-argument getter for this field, ignoring case so that camel case may be used in method
	 * names. This means that this method is not particularly fast, so avoid calling in big loops!
	 *
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object get(Object model, String name) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Object> cls = model.getClass();
		while (!cls.equals(Object.class)) {
			final Object val = get(model, cls, name);
			if (val != null) {
				return val;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}

	private static Object get(Object model, Class<?> clazz, String name) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (clazz == null || clazz.equals(Object.class)) {
			return null;
		}

		final Method[] methods = clazz.getMethods();

		final String getter = getGetterName(name);
		if (getter != null && !getter.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(getter) && method.getParameterTypes().length < 1) {
					method.setAccessible(true);
					return method.invoke(model);
				}
			}
		}

		final String isser = getIsserName(name);
		if (isser != null && !isser.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(isser) && method.getParameterTypes().length < 1) {
					method.setAccessible(true);
					return method.invoke(model);
				}
			}
		}
		return null;
	}

	private static Method getMethod(Class<?> clazz, String name) throws IllegalArgumentException {
		if (clazz == null || clazz.equals(Object.class)) {
			return null;
		}

		final Method[] methods = clazz.getMethods();

		final String getter = getGetterName(name);
		if (getter != null && !getter.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(getter) && method.getParameterTypes().length < 1) {
					return method;
				}
			}
		}

		final String isser = getIsserName(name);
		if (isser != null && !isser.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(isser) && method.getParameterTypes().length < 1) {
					return method;
				}
			}
		}
		return null;
	}

	public static boolean isModelField(Object model, String name) throws SecurityException {
		Field field = null;
		Class<? extends Object> cls = model.getClass();
		while (!cls.equals(Object.class)) {
			try {
				field = cls.getDeclaredField(name);
				break;
			} catch (Exception ne) {
				cls = cls.getSuperclass();
			}
		}
		if (field == null) {
			return false;
		}

		final FieldDescriptor omf = field.getAnnotation(FieldDescriptor.class);
		if (omf != null && !omf.visible()) {
			return false;
		}

		final Method[] methods = model.getClass().getMethods();

		final String getter = getGetterName(name);
		if (getter != null && !getter.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(getter) && method.getParameterTypes().length < 1) {
					return true;
				}
			}
		}

		final String isser = getIsserName(name);
		if (isser != null && !isser.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(isser) && method.getParameterTypes().length < 1) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Set a field by name using reflection.
	 *
	 * @param name
	 * @return field's old value, or null
	 */
	private static Object set(Object model, String name, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Object oldValue = get(model, name);
		final Method[] methods = model.getClass().getMethods();

		final String setter = getSetterName(name);
		if (setter != null && !setter.isEmpty()) {
			for (Method method : methods) {
				if (method.getName().equalsIgnoreCase(setter) && method.getParameterTypes().length == 1) {
					method.setAccessible(true);
					method.invoke(model, value);
				}
			}
		}
		return oldValue;
	}

	private static String getSetterName(final String fieldName) {
		return fieldName == null ? null : getName("set", fieldName);
	}

	/**
	 * There must be a smarter way of doing this i.e. a JDK method I cannot find. However it is one line of Java so
	 * after spending some time looking have coded self.
	 *
	 * @param fieldName
	 * @return String
	 */
	private static String getGetterName(final String fieldName) {
		return fieldName == null ? null : getName("get", fieldName);
	}

	private static String getIsserName(final String fieldName) {
		return fieldName == null ? null : getName("is", fieldName);
	}

	private static String getName(final String prefix, final String fieldName) {
		return prefix + getFieldWithUpperCaseFirstLetter(fieldName);
	}

	private static String getFieldWithUpperCaseFirstLetter(final String fieldName) {
		return fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1);
	}

	@Override
	public String toString() {
		return "FieldValue [name=" + name + "]";
	}
}
