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
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;

import gda.factory.Configurable;
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
public class OsgiServiceBeanHandler extends  BeanPostProcessorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(OsgiServiceBeanHandler.class);

	private static final Dictionary<String, ?> NO_PROPERTIES = new Hashtable<>();

	private final BundleContext bundleContext = GDACoreActivator.getBundleContext();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		logger.trace("Processing bean '{}'", beanName);
		final OsgiService[] osgiServiceAnnotations = bean.getClass().getAnnotationsByType(OsgiService.class);
		for (OsgiService osgiService : osgiServiceAnnotations) {
			final Class<?> osgiServiceInterface = osgiService.value();
			// Check the bean implements the OSGi service interface
			if (!osgiServiceInterface.isInstance(bean)) {
				// The bean does not implement the OSGi service interface
				throw new BeanNotOfRequiredTypeException(beanName, osgiServiceInterface, bean.getClass());
			}
			if (bean instanceof Configurable) {
				logger.warn("'{}' is marked as an OSGi service ({}) and implements Configurable. It will be registered before configure is called",
						beanName, osgiServiceInterface.getCanonicalName());
			}
			if (alreadyInRegister(osgiServiceInterface, bean)) {
				logger.warn("'{}' is already registered as an OSGi service with interface '{}'. This could be done automatically so remove corresponding {} bean",
						beanName, osgiServiceInterface.getCanonicalName(), OSGiServiceRegister.class.getSimpleName());
			} else {
				// We will register it
				bundleContext.registerService(osgiServiceInterface.getCanonicalName(), bean, NO_PROPERTIES);
				logger.info("Registered bean '{}' in OSGi service register with interface '{}'", beanName,
						osgiServiceInterface.getSimpleName());
			}
		}
		return bean;
	}

	private <S> boolean alreadyInRegister(Class<S> osgiServiceInterface, Object bean) {
		try {
			// null for no filter we want all the services
			final Collection<ServiceReference<S>> serviceReferences = bundleContext.getServiceReferences(osgiServiceInterface, null);
			return serviceReferences.stream()
					.anyMatch(serviceReference -> bundleContext.getService(serviceReference).equals(bean));
		} catch (InvalidSyntaxException e) {
			logger.error("Failed to check if '{}' is already an OSGi service. It might be registered twice",
					osgiServiceInterface.getCanonicalName(), e);
			return false;
		}
	}
}
