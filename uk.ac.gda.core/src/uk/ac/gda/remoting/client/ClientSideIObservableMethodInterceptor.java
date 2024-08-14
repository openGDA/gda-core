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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static uk.ac.diamond.daq.classloading.GDAClassLoaderService.temporaryClassLoader;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.rmi.RmiServiceExporter;

import com.google.common.base.Stopwatch;

import gda.observable.IObservable;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.util.logging.LoggingUtils;

/**
 * Implementation of {@link MethodInterceptor} that handles calls to methods in the {@link IObservable} interface, using
 * its own {@link ObservableComponent} instance.
 */
public class ClientSideIObservableMethodInterceptor implements MethodInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(ClientSideIObservableMethodInterceptor.class);

	/** If an RMI call takes longer than this threshold then logging will be produced. */
	private static final long RMI_CALL_TIME_LOGGING_THRESHOLD_MS = 100;

	/**
	 * Expected name of UI thread, see
	 * {@link uk.ac.gda.remoting.client.ClientSideIObservableMethodInterceptor#isUiThread}
	 */
	private static final String UI_THREAD = "main";

	/**
	 * Delegate object for the {@link IObservable} interface. Calls to methods in this interface are dealt with by this
	 * interceptor.
	 */
	private final ObservableComponent observableComponent = new ObservableComponent();

	public ObservableComponent getObservableComponent() {
		return observableComponent;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		final Method method = invocation.getMethod();
		final Class<?> declaringClass = method.getDeclaringClass();

		// If it's an IObservable method we want to intercept it
		if (declaringClass.equals(IObservable.class)) {
			// Invoke the method on our IObservable delegate
			return method.invoke(observableComponent, invocation.getArguments());
		}
		// Otherwise allow the method call to proceed
		final Stopwatch invokeStopwatch = Stopwatch.createStarted();
		try (var tcclRunner = temporaryClassLoader(s -> s.getClassLoaderForLibrary(RmiServiceExporter.class))) {
			return invocation.proceed();
		} finally {
			final long elapsedTime = invokeStopwatch.elapsed(MILLISECONDS);
			if (elapsedTime > RMI_CALL_TIME_LOGGING_THRESHOLD_MS && logger.isWarnEnabled()) {
				logRmiCalls(method, declaringClass, elapsedTime);
			}
		}
	}

	/**
	 * Logs time taken for RMI calls including detecting if they are from the UI thread.
	 *
	 * @param method the method being called
	 * @param declaringClass the class that defined the method
	 * @param elapsedTime the time the RMI call took
	 */
	private void logRmiCalls(final Method method, final Class<?> declaringClass, final long elapsedTime) {
		final String threadName = Thread.currentThread().getName();
		if (isUiThread(threadName)) {
			logger.warn("RPC call to '{}.{}' took {} ms from UI thread [{}]", declaringClass.getName(),
					method.getName(), elapsedTime, threadName);
		} else {
			logger.debug("RPC call to '{}.{}' took {} ms from Non UI thread [{}]", declaringClass.getName(),
					method.getName(), elapsedTime, threadName);
		}
		LoggingUtils.logStackTrace(logger, "logRmiCalls()");
	}

	/**
	 * Checking against a static name removes the need to add swt dependencies to core, but there may be circumstances
	 * in which this method doesn't reliably differentiate UI from non UI threads. If we identify any, we may need to
	 * move this code out of core and into the client so we can use Display.getCurrent().getThread() instead.
	 *
	 * @param threadName
	 * @return whether this is the UI thread
	 */
	private static boolean isUiThread(String threadName) {
		return threadName.equals(UI_THREAD);
	}
}
