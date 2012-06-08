/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.remoting.client;

import gda.observable.IObservable;
import gda.observable.ObservableComponent;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Implementation of {@link MethodInterceptor} that handles calls to methods in the {@link IObservable} interface, using
 * its own {@link ObservableComponent} instance.
 */
public class ClientSideIObservableMethodInterceptor implements MethodInterceptor {

	/**
	 * Delegate object for the {@link IObservable} interface. Calls to methods in this interface are dealt with by this
	 * interceptor.
	 */
	private ObservableComponent observableComponent = new ObservableComponent();
	
	public ObservableComponent getObservableComponent() {
		return observableComponent;
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Method method = invocation.getMethod();
		final Class<?> declaringClass = method.getDeclaringClass();
		
		if (declaringClass.equals(IObservable.class)) {
			// Invoke the method on our IObservable delegate
			return method.invoke(observableComponent, invocation.getArguments());
		}
		
		// Otherwise allow the method call to proceed
		return invocation.proceed();
	}
	
}
