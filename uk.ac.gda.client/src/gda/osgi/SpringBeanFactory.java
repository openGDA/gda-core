/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.osgi;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class SpringBeanFactory implements IExecutableExtension, IExecutableExtensionFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringBeanFactory.class);
	
	// from ConfigurableOsgiBundleApplicationContext
	private static final String APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME = "org.eclipse.gemini.blueprint.context.service.name";
	
	private String bundleName;
	
	private String beanName;
	
	private ApplicationContext appContext;
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		
		bundleName = config.getContributor().getName();
		
		if (!propertyName.equals("class")) {
			throw new IllegalArgumentException(String.format("Unexpected property '%s'", propertyName));
		}
		
		if (data == null) {
			throw new IllegalArgumentException("No data");
		}
		
		if (!(data instanceof String)) {
			throw new IllegalArgumentException("Unexpected data type: " + data.getClass());
		}
		
		beanName = (String) data;
	}
	
	@Override
	public Object create() throws CoreException {
		
		final Bundle bundle = Platform.getBundle(bundleName);
		
		final String filterText = String.format("(&(objectClass=%s)(%s=%s))",
			ApplicationContext.class.getName(),
			APPLICATION_CONTEXT_SERVICE_PROPERTY_NAME,
			bundleName);
		
		Filter filter = null;
		try {
			filter = FrameworkUtil.createFilter(filterText);
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
		
		final ServiceTracker<Void, ApplicationContext> tracker = new ServiceTracker<Void, ApplicationContext>(bundle.getBundleContext(), filter, null);
		logger.info("Waiting for Spring application context for '{}' bundle", bundleName);
		
		tracker.open();
		
		while (appContext == null) {
			try {
				appContext = tracker.waitForService(1000);
			} catch (InterruptedException e) {
				logger.debug("Interrupted while waiting for Spring application context for '{}' bundle to appear; continuing to wait...", bundleName);
			}
			if (appContext == null) {
				logger.debug("Spring application context for '{}' bundle has not yet appeared; continuing to wait...", bundleName);
			}
		}
		
		tracker.close();
		
		logger.info("Spring application context for '{}' bundle has appeared", bundleName);
		
		final Object bean = appContext.getBean(beanName);
		return bean;
	}

}
