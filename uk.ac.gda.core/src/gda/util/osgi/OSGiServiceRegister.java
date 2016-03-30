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

package gda.util.osgi;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.core.GDACoreActivator;

/**
 * Registers the specified service object with the specified properties
 * under the specified class name with the Framework.
 *
 */
public class OSGiServiceRegister implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(OSGiServiceRegister.class);

	private String className;

	/**
	 * @param clazz The class name under which the service can be located.
	 */
	public void setClass(Class<?> clazz) {
		this.className = clazz.getName();
	}

	private Object service;

	/**
	 *
	 * @param service The service object or a <code>ServiceFactory</code> object
	 */
	public void setService(Object service) {
		this.service = service;
	}

	private Dictionary<String, ?> properties;

	/**
	 *
	 * @param properties The properties for this service
	 */
	public void setProperties(Dictionary<String, ?> properties) {
		this.properties = properties;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		BundleContext bundleContext = GDACoreActivator.getBundleContext();
		bundleContext.registerService(className, service, properties);
		logger.debug("Registered  " + service + " as service " + className);
	}

}
