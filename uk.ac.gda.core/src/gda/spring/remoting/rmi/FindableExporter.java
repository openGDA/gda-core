/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.spring.remoting.rmi;

import gda.factory.Factory;
import gda.factory.Findable;
import gda.spring.remoting.FindableExporterBase;
import gda.spring.remoting.MapBackedRemoteObjectLister;
import gda.spring.remoting.RemoteObjectLister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.util.StringUtils;

/**
 * A Spring {@link BeanFactoryPostProcessor} that exposes all {@link Findable}s
 * in a GDA {@link Factory} using RMI.
 * 
 * <p>Also exposes a {@link MapBackedRemoteObjectLister} that lists the
 * available objects and provides more information than the standard RMI
 * registry.
 */
public class FindableExporter extends FindableExporterBase {
	
	private static final Logger logger = LoggerFactory.getLogger(FindableExporter.class);

	private static final String BEAN_NAME_PREFIX = "rmiexporter-";
	
	@Override
	protected void exportObject(Findable findable, Class<?> serviceInterface, ConfigurableListableBeanFactory beanFactory) {
		final String rmiName = Constants.RMI_NAME_PREFIX + findable.getName();
		final String exporterBeanName = BEAN_NAME_PREFIX + findable.getName();
		logger.info("Exporting " + StringUtils.quote(findable.getName()) + " with RMI name " + StringUtils.quote(rmiName));
		BeanDefinitionRegistry beanDefRegistry = (BeanDefinitionRegistry) beanFactory;
		BeanDefinition beanDefinition = createRmiServiceExporterBeanDefinition(rmiName, findable, serviceInterface);
		beanDefRegistry.registerBeanDefinition(exporterBeanName, beanDefinition);
	}

	@Override
	protected void afterExportingObjects(ConfigurableListableBeanFactory beanFactory) {
		final String rmiName = Constants.RMI_NAME_PREFIX + Constants.REMOTE_OBJECT_LISTER_RMI_NAME;
		final String exporterBeanName = BEAN_NAME_PREFIX + Constants.REMOTE_OBJECT_LISTER_RMI_NAME;
		BeanDefinitionRegistry beanDefRegistry = (BeanDefinitionRegistry) beanFactory;
		RemoteObjectLister lister = new MapBackedRemoteObjectLister(availableObjects);
		BeanDefinition beanDefinition = createRmiServiceExporterBeanDefinition(rmiName, lister, RemoteObjectLister.class);
		beanDefRegistry.registerBeanDefinition(exporterBeanName, beanDefinition);
	}
	
	protected BeanDefinition createRmiServiceExporterBeanDefinition(String serviceName, Object service, Class<?> serviceInterface) {
		BeanDefinition beanDefinition = new RootBeanDefinition(RmiServiceExporter.class);
		beanDefinition.getPropertyValues().addPropertyValue("serviceName", serviceName);
		beanDefinition.getPropertyValues().addPropertyValue("service", service);
		beanDefinition.getPropertyValues().addPropertyValue("serviceInterface", serviceInterface);
		return beanDefinition;
	}

}
