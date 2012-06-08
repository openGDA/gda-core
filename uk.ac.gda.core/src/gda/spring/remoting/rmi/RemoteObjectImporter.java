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

package gda.spring.remoting.rmi;

import gda.spring.remoting.RemoteObjectLister;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.util.StringUtils;

/**
 * A Spring {@link BeanFactoryPostProcessor} which reads a list of available
 * remote objects from an RMI {@link RemoteObjectLister}, and adds a proxy
 * object to its containing {@link BeanFactory} for each of the remote objects.
 */
public class RemoteObjectImporter implements BeanFactoryPostProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteObjectImporter.class);
	
	private String host;
	
	/**
	 * Sets the host from which remote objects will be imported.
	 * 
	 * @param host the host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanDefinitionRegistry beanDefRegistry = (BeanDefinitionRegistry) beanFactory;
		
		// Get remote object lister
		String listerUrl = String.format("rmi://%s/%s%s", host, Constants.RMI_NAME_PREFIX, Constants.REMOTE_OBJECT_LISTER_RMI_NAME);
		RemoteObjectLister lister = getRmiServiceProxy(listerUrl, RemoteObjectLister.class);
		
		// Get available objects
		Map<String, String> availableObjects = lister.getAvailableObjects();

		// Create proxy bean definition for each object
		for (Map.Entry<String, String> entry : availableObjects.entrySet()) {
			final String objectName = entry.getKey();
			final String className = entry.getValue();
			try {
				Class<?> objectInterface = Class.forName(className);
				String objectUrl = String.format("rmi://%s/%s%s", host, Constants.RMI_NAME_PREFIX, objectName);
				BeanDefinition proxyBeanDefinition = createRmiProxyFactoryBeanDefinition(objectUrl, objectInterface);
				beanDefRegistry.registerBeanDefinition(objectName, proxyBeanDefinition);
			} catch (Exception e) {
				logger.error("Could not import " + StringUtils.quote(objectName), e);
			}
		}
	}
	
	/**
	 * Gets a proxy for a remote service, which implements the specified interface and is located at the specified URL. 
	 * 
	 * @param <T> the type implemented by the remote service
	 * @param serviceUrl the service URL
	 * @param serviceInterface the type implemented by the remote service
	 * 
	 * @return a proxy for the remote service
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getRmiServiceProxy(String serviceUrl, Class<T> serviceInterface) {
		RmiProxyFactoryBean proxyBean = new RmiProxyFactoryBean();
		proxyBean.setServiceUrl(serviceUrl);
		proxyBean.setServiceInterface(serviceInterface);
		proxyBean.afterPropertiesSet();
		return (T) proxyBean.getObject();
	}
	
	protected BeanDefinition createRmiProxyFactoryBeanDefinition(String serviceUrl, Class<?> objectInterface) {
		BeanDefinition beanDefinition = new RootBeanDefinition(RmiProxyFactoryBean.class);
		beanDefinition.getPropertyValues().addPropertyValue("serviceUrl", serviceUrl);
		beanDefinition.getPropertyValues().addPropertyValue("serviceInterface", objectInterface.getName());
		return beanDefinition;
	}
	
}
