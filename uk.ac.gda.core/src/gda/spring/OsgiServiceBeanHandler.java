/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.spring;

import java.util.Collection;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;

import gda.util.osgi.OSGiServiceRegister;
import uk.ac.diamond.daq.osgi.OsgiService;
import uk.ac.gda.core.GDACoreActivator;

/**
 * This class inspects beans for the {@link OsgiService} annotation. If a bean is marked as an OSGi service it will be
 * registered in the OSGi service register automatically. Beans may implement many service interfaces in which case they
 * will be registered for all. If the bean is already registered a warning will be logged and the bean will not be
 * registered.
 *
 * @author James Mudd
 * @since GDA 9.12
 */
public class OsgiServiceBeanHandler {
	private static final Logger logger = LoggerFactory.getLogger(OsgiServiceBeanHandler.class);

	private final BundleContext bundleContext = GDACoreActivator.getBundleContext();

	public void processBean(Object bean, String beanName) {
		logger.trace("Processing bean '{}'", beanName);
		OsgiService[] osgiServiceAnnotations = bean.getClass().getAnnotationsByType(OsgiService.class);
		for (OsgiService osgiService : osgiServiceAnnotations) {
			Class<?> osigServiceInterface = osgiService.value();
			// Check the bean implements the OSGi service interface
			if (!osigServiceInterface.isInstance(bean)) {
				// The bean does not implement the OSGi service interface
				throw new BeanNotOfRequiredTypeException(beanName, osigServiceInterface, bean.getClass());
			}

			if (alreadyInRegister(osigServiceInterface, bean)) {
				logger.warn(
						"'{}' is already registered as an OSGi service with interface '{}'. This could be done automatically so remove corresponding "
								+ OSGiServiceRegister.class.getSimpleName() + " bean",
						beanName, osigServiceInterface.getCanonicalName());
			} else {
				// We will register it
				bundleContext.registerService(osigServiceInterface.getCanonicalName(), bean,
						new Hashtable<String, Object>(0));
				logger.info("Registered bean '{}' in OSGi service register with interface '{}'", beanName,
						osigServiceInterface.getSimpleName());
			}
		}
	}

	private <S> boolean alreadyInRegister(Class<S> osigServiceInterface, Object bean) {
		try {
			// null for no filter we want all the services
			Collection<ServiceReference<S>> serviceReferences = bundleContext.getServiceReferences(osigServiceInterface,
					null);
			return serviceReferences.stream()
					.anyMatch(serviceReference -> bundleContext.getService(serviceReference).equals(bean));
		} catch (InvalidSyntaxException e) {
			logger.error("Failed to check if '{}' is already an OSGi service. It might be registered twice", e);
			return false;
		}
	}

}
