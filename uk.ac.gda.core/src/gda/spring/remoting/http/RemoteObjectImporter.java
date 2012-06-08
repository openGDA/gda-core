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

package gda.spring.remoting.http;

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
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.util.StringUtils;

/**
 * A Spring {@link BeanFactoryPostProcessor} which reads a list of available
 * remote objects from a {@link RemoteObjectListerServlet} (using a
 * {@link ClientSideRemoteObjectLister}), and adds a proxy object to its
 * containing {@link BeanFactory} for each of the remote objects.
 */
public class RemoteObjectImporter implements BeanFactoryPostProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteObjectImporter.class);
	
	private String url;
	
	/**
	 * Sets the URL that this remote object importer will use to access the
	 * catalogue of available objects.
	 * 
	 * @param url the remote URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanDefinitionRegistry beanDefRegistry = (BeanDefinitionRegistry) beanFactory;
		
		String listerUrl = url + Constants.CONTEXT_PATH + Constants.REMOTE_OBJECT_LISTER_PATH;
		RemoteObjectLister lister = new ClientSideRemoteObjectLister(listerUrl);
		Map<String, String> availableObjects = lister.getAvailableObjects();

		for (Map.Entry<String, String> entry : availableObjects.entrySet()) {
			String objectName = entry.getKey();
			String className = entry.getValue();
			try {
				Class<?> objectInterface = Class.forName(className);
				String objectUrl = url + Constants.CONTEXT_PATH + "/" + objectName;
				BeanDefinition proxyBeanDefinition = createProxyBeanDefinition(objectUrl, objectInterface);
				beanDefRegistry.registerBeanDefinition(objectName, proxyBeanDefinition);
			} catch (Exception e) {
				logger.error("Could not import " + StringUtils.quote(objectName), e);
			}
		}
	}
	
	protected BeanDefinition createProxyBeanDefinition(String serviceUrl, Class<?> objectInterface) {
		BeanDefinition beanDefinition = new RootBeanDefinition(HttpInvokerProxyFactoryBean.class);
		beanDefinition.getPropertyValues().addPropertyValue("serviceUrl", serviceUrl);
		beanDefinition.getPropertyValues().addPropertyValue("serviceInterface", objectInterface.getName());
		return beanDefinition;
	}
	
}
