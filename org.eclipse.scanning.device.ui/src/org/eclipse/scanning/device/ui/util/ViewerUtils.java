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
package org.eclipse.scanning.device.ui.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.swt.widgets.Composite;

public class ViewerUtils {

	public static void setItemHeight(Composite tree, int height) {
		try {
			Method method = null;

			Method[] methods = tree.getClass().getDeclaredMethods();
			method = findMethod(methods, "setItemHeight", 1); //$NON-NLS-1$
			if (method != null) {
				boolean accessible = method.isAccessible();
				method.setAccessible(true);
				method.invoke(tree, Integer.valueOf(height));
				method.setAccessible(accessible);
			}
		} catch (SecurityException e) {
			// ignore
		} catch (IllegalArgumentException e) {
			// ignore
		} catch (IllegalAccessException e) {
			// ignore
		} catch (InvocationTargetException e) {
			// ignore
		}
	}

	/**
	 * Finds the method with the given name and parameter count from the specified methods.
	 * @param methods the methods to search through
	 * @param name the name of the method to find
	 * @param parameterCount the count of parameters of the method to find
	 * @return the method or <code>null</code> if not found
	 */
	private static Method findMethod(Method[] methods, String name, int parameterCount) {
		for (Method method : methods) {
			if (method.getName().equals(name) && method.getParameterTypes().length == parameterCount) {
				return method;
			}
		}
		return null;
	}

	public static <T> ICheckStateProvider createCheckStateProvider(Function<T, Boolean> checkStateFunction) {
		return new ICheckStateProvider() {

			@Override
			public boolean isGrayed(Object element) {
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean isChecked(Object element) {
				return checkStateFunction.apply((T) element).booleanValue();
			}
		};

	}

	public static <T> ColumnLabelProvider createTableColumnLabelProvider(Function<T, String> labelFunction) {
		return createTableColumnLabelProvider(labelFunction, null);
	}

	public static <T> ColumnLabelProvider createTableColumnLabelProvider(Function<T, String> labelFunction, String tooltipText) {
		return new ColumnLabelProvider() {

			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return labelFunction.apply((T) element);
			}

			@Override
			public String getToolTipText(Object element) {
				return tooltipText;
			}

		};
	}
}
