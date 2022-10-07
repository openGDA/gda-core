/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ListenableProxyInvocationHandler<T> implements InvocationHandler {
	private static final List<String> ADD_REMOVE_PROPERTY_CHANGE_LISTENER_METHODS = Arrays.asList("addPropertyChangeListener","removePropertyChangeListener");

	private final T deligate;
	private final PropertyChangeSupport propertyChangeSupport;

	public ListenableProxyInvocationHandler(T deligate, PropertyChangeSupport propertyChangeSupport) {
		this.deligate = deligate;
		this.propertyChangeSupport = propertyChangeSupport;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (isChangingPropertyChangeListeners(method)){
			return modifyListenersOnChangeSupport(method, args);
		}

		Object oldValue = getCurrentValue(method);

		Object result = method.invoke(deligate, args);

		if (isSetter(method)){
			Object newValue = getCurrentValue(method);
			propertyChangeSupport.firePropertyChange(getPropertyName(method), oldValue, newValue);
		}
		return result;
	}

	private String getPropertyName(Method method) {
		String propertyName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
		return propertyName;
	}

	private Object getCurrentValue(Method method) {
		if (isSetter(method)){
			try{
				Method getter = getGetterMethod(method);
				return getter.invoke(deligate);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex){
				throw new UnsupportedOperationException("there is a setter method with no getter", ex);
			}
		}
		return null;
	}

	private Method getGetterMethod(Method method) throws NoSuchMethodException {
		String getBasedName = method.getName().replaceFirst("set", "get");
		String isBasedName = method.getName().replaceFirst("set", "is");
		try{
			return deligate.getClass().getMethod(getBasedName);
		} catch (NoSuchMethodException ex){
			return deligate.getClass().getMethod(isBasedName);
		}
	}

	private boolean isSetter(Method method) {
		return method.getName().startsWith("set");
	}

	private Object modifyListenersOnChangeSupport(Method method, Object[] args) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method propertyChangeListenerMethod = propertyChangeSupport.getClass().getMethod(method.getName(), PropertyChangeListener.class);
		return propertyChangeListenerMethod.invoke(propertyChangeSupport, args);
	}

	private boolean isChangingPropertyChangeListeners(Method method) {
		return ADD_REMOVE_PROPERTY_CHANGE_LISTENER_METHODS.contains(method.getName());
	}
}
