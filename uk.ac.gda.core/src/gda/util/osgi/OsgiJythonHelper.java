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

package gda.util.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A helper class for Jython to allow server side services to be easily tested without the need for holding them in
 * static references.
 * </p>
 * <p>
 * This allows you to easily get a service instance and call methods on it for development purposes. This should not be
 * used as a replacement for DS injection.
 * </p>
 * <p>
 * Example usage in Jython:
 *
 * <pre>
 * >>> from gda.util.osgi import OsgiJythonHelper # Runs the static initialisation
 * >>> from org.eclipse.scanning.api.scan import IFilePathService # The service interface
 * >>> service = OsgiJythonHelper.getService(IFilePathService) # Get an implementation of the service
 * >>> service.getScanNumber() # Use the service
 * </pre>
 * </p>
 *
 * @author James Mudd
 */
public final class OsgiJythonHelper {
	private static final Logger logger = LoggerFactory.getLogger(OsgiJythonHelper.class);

	private static final BundleContext bundleContext;

	private OsgiJythonHelper() {
		// Prevent Instances
	}

	static {
		// Log warning as this shouldn't become the way to get OSGi services
		logger.warn("Developer tool, not for production use!");
		bundleContext = FrameworkUtil.getBundle(OsgiJythonHelper.class).getBundleContext();
		logger.info("Initialised with bundle context: {}", bundleContext);
	}

	/**
	 * Gets an object implementing the interface requested.
	 *
	 * @param serviceInterface The service interface you want
	 * @return An object implementing the requested interface
	 * @throws ServiceNotFoundException If no implementation of the serviceInterface can be found
	 */
	public static Object getService(Class<?> serviceInterface) {
		final ServiceReference<?> serviceReference = bundleContext.getServiceReference(serviceInterface);
		if (serviceReference == null) {
			// Happens when the service is not defined/implemented anywhere
			throw new ServiceNotFoundException("Could not find a service implementation of the interface: " + serviceInterface.getCanonicalName());
		}

		Object serviceImpl = getService(serviceReference);
		if (serviceImpl == null) {
			// Happens when the service is defined but fails to initialise
			throw new ServiceNotFoundException("Could not find a service implementation of the interface: " + serviceInterface.getCanonicalName());
		}

		return serviceImpl;
	}

	/**
	 * Gets a service implementation instance for a service reference
	 *
	 * @param serviceReference The service reference to resolve
	 * @return A service instance from the reference if one is available or null otherwise
	 */
	private static Object getService(ServiceReference<?> serviceReference) {
		Object service = bundleContext.getService(serviceReference);
		logger.debug("Got service: {}", service); // Log the service instance
		return service;
	}

	public static class ServiceNotFoundException extends RuntimeException {

		public ServiceNotFoundException(String message) {
			super(message);
		}
	}

}
