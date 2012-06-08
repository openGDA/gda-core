/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.remoting;

import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Localizable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

import uk.ac.gda.remoting.ServiceInterface;

/**
 * Base class to be extended by classes that can import remote objects into a Spring application context.
 */
public abstract class FindableExporterBase implements BeanFactoryPostProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(FindableExporterBase.class);
	
	private static final String INTERFACE_MAPPING_FILE = "interfaces.properties";

	private Properties interfaces;
	
	public FindableExporterBase() {
		interfaces = loadInterfaceMappings();
	}
	
	protected Properties loadInterfaceMappings() {
		Properties mappings = new Properties();
		try {
			InputStream inStream = FindableExporterBase.class.getResourceAsStream(INTERFACE_MAPPING_FILE);
			mappings.load(inStream);
		} catch (IOException ioe) {
			throw new RuntimeException("Could not load interface mappings", ioe);
		}
		return mappings;
	}
	
	// FIXME If object creators are being used, the factories aren't directly available
	private Factory factory;
	
	/**
	 * Sets the factory containing {@link Findable}s that will be exported.
	 * 
	 * @param factory the GDA factory
	 */
	public void setFactory(Factory factory) {
		this.factory = factory;
	}
	
	protected Map<String, String> availableObjects;
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		Collection<Findable> findables;
		if (factory != null) {
			logger.info("Exporting objects in factory " + StringUtils.quote(factory.getName()));
			findables = factory.getFindables();
		} else {
			logger.info("Exporting objects from Spring context");
			findables = beanFactory.getBeansOfType(Findable.class).values();
		}
		
		availableObjects = new LinkedHashMap<String, String>();
		beforeExportingObjects(beanFactory);
		for (Findable findable : findables) {
			if (findable instanceof Localizable && !((Localizable) findable).isLocal()) {
				try {
					Class<?> findableInterface = getInterfaceImplementedByFindable(findable);
					exportObject(findable, findableInterface, beanFactory);
					availableObjects.put(findable.getName(), findableInterface.getName());
				} catch (Exception e) {
					logger.error("Could not export " + StringUtils.quote(findable.getName()), e);
				}
			}
		}
		afterExportingObjects(beanFactory);
	}
	
	/**
	 * Can be overridden by subclasses to perform work before the objects are exported.
	 * 
	 * @param beanFactory the bean factory in which this exporter is defined
	 */
	protected void beforeExportingObjects(@SuppressWarnings("unused") ConfigurableListableBeanFactory beanFactory) {
		// do nothing by default
	}
	
	/**
	 * Must be implemented by subclasses to export each object.
	 * 
	 * @param findable the object to export
	 * @param serviceInterface the interface which the object implements
	 * @param beanFactory the bean factory in which this exporter is defined
	 */
	protected abstract void exportObject(Findable findable, Class<?> serviceInterface, ConfigurableListableBeanFactory beanFactory);
	
	/**
	 * Can be overridden by subclasses to perform work after the objects have been exported.
	 * 
	 * @param beanFactory the bean factory in which this exporter is defined
	 */
	protected void afterExportingObjects(@SuppressWarnings("unused") ConfigurableListableBeanFactory beanFactory) {
		// do nothing by default
	}
	
	protected Class<?> getInterfaceImplementedByFindable(Findable findable) throws ClassNotFoundException {
		
		// Look for annotation first
		final Class<?> findableClass = findable.getClass();
		if (findableClass.isAnnotationPresent(ServiceInterface.class)) {
			return findableClass.getAnnotation(ServiceInterface.class).value();
		}
		
		// If annotation not found, look in interface mappings
		String classOfFindable = findable.getClass().getName();
		if (interfaces.containsKey(classOfFindable)) {
			String interfaceOfFindable = interfaces.getProperty(classOfFindable);
			return Class.forName(interfaceOfFindable);
		}
		
		throw new RuntimeException("Cannot determine the service interface for class " + findableClass.getName() + ": you need to add a @ServiceInterface annotation to it, or add it to " + INTERFACE_MAPPING_FILE);
	}
	
}
