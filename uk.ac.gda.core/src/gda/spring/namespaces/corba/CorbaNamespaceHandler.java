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

package gda.spring.namespaces.corba;

import gda.spring.NetServiceFactoryBean;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.util.StringUtils;

/**
 * Spring {@link NamespaceHandler} for the {@code corba} namespace.
 */
public class CorbaNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("import", new ImportBeanDefinitionParser());
		registerBeanDefinitionParser("export", new ExportBeanDefinitionParser());
		registerBeanDefinitionParser("adapterfactory", new AdapterFactoryBeanDefinitionParser());
	}

	/**
	 * Name for the {@link NetServiceFactoryBean} in the bean registry.
	 */
	public static final String NET_SERVICE_BEAN_NAME = "netService";

	/**
	 * Ensures that the specified bean registry contains a {@link NetServiceFactoryBean} with the name
	 * {@link #NET_SERVICE_BEAN_NAME}. If a bean already exists with that name, it ensures that the bean is a
	 * {@link NetServiceFactoryBean}.
	 *
	 * @param registry the bean registry
	 */
	public static void registerNetServiceFactoryBean(BeanDefinitionRegistry registry) {
		if (registry.containsBeanDefinition(NET_SERVICE_BEAN_NAME)) {
			BeanDefinition netService = registry.getBeanDefinition(NET_SERVICE_BEAN_NAME);
			if (!netService.getBeanClassName().equals(NetServiceFactoryBean.class.getName())) {
				throw new RuntimeException("You have already defined a bean called " + StringUtils.quote(NET_SERVICE_BEAN_NAME) + ", and it is not a " + NetServiceFactoryBean.class.getSimpleName());
			}
		} else {
			AbstractBeanDefinition beanDef = new GenericBeanDefinition();
			beanDef.setResourceDescription("class " + CorbaNamespaceHandler.class.getName());
			beanDef.setBeanClass(NetServiceFactoryBean.class);
			registry.registerBeanDefinition(NET_SERVICE_BEAN_NAME, beanDef);
		}
	}

}
