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

import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

import org.eclipse.richbeans.api.generator.IListenableProxyFactory;

public class ListenableProxyFactory implements IListenableProxyFactory{
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T, T> T createProxyFor(S original, Class<T> interfaceImplemented) {
		if (hasGenerics(interfaceImplemented)){
			throw new UnsupportedOperationException("because of javas implementation of proxy, we can't currently support generic property types");
		}

		PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(original);
		InvocationHandler handler = new ListenableProxyInvocationHandler<T>(original, propertyChangeSupport);
		return (T) Proxy.newProxyInstance(
				original.getClass().getClassLoader(),
				new Class[]{interfaceImplemented, PropertyChangeInterface.class},
				handler
			);
	}

	private <S> boolean hasGenerics(Class<S> clazz) {
		return Stream.of(clazz.getMethods())
				.map(method -> method.getGenericReturnType())
				.anyMatch(type -> type instanceof ParameterizedType);
	}


}
