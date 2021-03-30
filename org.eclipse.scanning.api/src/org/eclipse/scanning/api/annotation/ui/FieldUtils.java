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

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldUtils {

	private FieldUtils() {
		// prevent instantiation
	}

	public static boolean isFileType(Class<? extends Object> clazz) {
		return File.class.isAssignableFrom(clazz)
			|| Path.class.isAssignableFrom(clazz)
			|| clazz.getName().equals("org.eclipse.core.resources.IResource");
	}

	public static FieldDescriptor getAnnotation(Object model, String fieldName) throws NoSuchFieldException, SecurityException {
		final Field field = getField(model, fieldName);
		return (field == null) ? null : field.getAnnotation(FieldDescriptor.class);
	}

	/**
	 * Search for a field in a model class or its superclasses
	 *
	 * @param model
	 *            an object which is an instance of a model class
	 * @param fieldName
	 *            the {@link Field} to find
	 * @return the {@link Field} (if it exists): otherwise, an exception is thrown
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	public static Field getField(Object model, String fieldName) throws NoSuchFieldException, SecurityException {
		Class<? extends Object> cls = model.getClass();
		while (!cls.equals(Object.class)) {
			try {
				return cls.getDeclaredField(fieldName);
			} catch (Exception ne) {
				cls = cls.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}

	/**
	 * Get a collection of the fields of the model that should be edited in the User interface for editing the model.
	 *
	 * @return collection of fields.
	 * @throws Exception
	 */
	public static Collection<FieldValue> getModelFields(Object model) throws Exception {
		// Decided not to use the obvious BeanMap here because class problems with
		// GDA and we have to read annotations anyway.
		final List<Field> allFields = new ArrayList<>(31);
		Class<? extends Object> cls = model.getClass();

		while (!cls.equals(Object.class)) {
			allFields.addAll(Arrays.asList(cls.getDeclaredFields()));
			cls = cls.getSuperclass();
		}

		// The returned descriptor
		final Map<String, FieldValue> map = new HashMap<>();

		// fields
		for (Field field : allFields) {
			if (map.containsKey(field.getName())) continue; // We do not overwrite repeats from the super.

			// If there is a getter/isser for the field we assume it is a model field.
			if (FieldValue.isModelField(model, field.getName())) {
				map.put(field.getName(), new FieldValue(model, field.getName()));
			}
		}

		final List<FieldValue> ret = new ArrayList<>(map.values());
		Collections.sort(ret, FieldUtils::compareFieldValues);
		return ret;
	}

	private static int compareFieldValues(FieldValue o1, FieldValue o2) {
		final FieldDescriptor an1;
		final FieldDescriptor an2;
		try {
			an1 = FieldUtils.getAnnotation(o1.getModel(), o1.getName());
			an2 = FieldUtils.getAnnotation(o2.getModel(), o2.getName());
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("Cannot get field from model, " + o1.getName() + ", " + o2.getName(), e);
		}

		if (an1 != null && an2 != null) {
			if (an1.fieldPosition() != Integer.MAX_VALUE && an2.fieldPosition() != Integer.MAX_VALUE) {
				return (an1.fieldPosition() - an2.fieldPosition());
			} else if (an1.fieldPosition() != Integer.MAX_VALUE) {
				return -1;
			} else if (an2.fieldPosition() != Integer.MAX_VALUE) {
				return 1;
			}
		}
		return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
	}
}