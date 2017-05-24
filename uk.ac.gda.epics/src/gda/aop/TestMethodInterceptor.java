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

package gda.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic Method Interceptor derived class to allow confirmation that the OSGi Fragment class loading methods that allow third party jars to reference beans
 * that employ Spring AOP are working.
 */
public class TestMethodInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TestMethodInterceptor.class);

	/**
	 * Triggered when a method on the proxied class is called. This method must return a call to the proceed method of its only parameter.
	 *
	 * @param invocation
	 *            The MethodInvocation object associated with the method being called
	 */
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		logger.debug("In Test Method Interceptor for method {} on class {}",
				invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass().getName());
		return invocation.proceed();
	}

}
