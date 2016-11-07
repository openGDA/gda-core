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

import gda.factory.FactoryException;
import gda.factory.corba.util.NetService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Spring {@link BeanDefinitionParser} for the {@code import} element.
 */
public class ImportBeanDefinitionParser implements BeanDefinitionParser {
	private static final Logger logger = LoggerFactory.getLogger(ImportBeanDefinitionParser.class);

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
		String fullname = element.getAttribute("name");

		String namespace = element.getAttribute("namespace");
		String namelist = element.getAttribute("names");

		// Must specify either 'namespace', or 'name', but not both
		if (StringUtils.hasText(namespace) && StringUtils.hasText(fullname)) {
			throw new IllegalArgumentException("You cannot specify both a namespace and a name when using the <corba:import> element");
		}
		if (!StringUtils.hasText(namespace) && !StringUtils.hasText(fullname)) {
			throw new IllegalArgumentException("You must specify either a namespace or a name when using the <corba:import> element");
		}

		// If using 'name', cannot also use 'names'
		if (StringUtils.hasText(fullname) && StringUtils.hasText(namelist)) {
			throw new IllegalArgumentException("You cannot specify both a namelist and a name when using the <corba:import> element");
		}

		// If using 'names', also need 'namespace'
		if (StringUtils.hasText(namelist) && !StringUtils.hasText(namespace)) {
			throw new IllegalArgumentException("You must specify a namespace when specifying names with the <corba:import> element");
		}

		CorbaNamespaceHandler.registerNetServiceFactoryBean(parserContext.getRegistry());


		final BeanDefinitionRegistry beanDefRegistry = parserContext.getRegistry();

		if (StringUtils.hasText(fullname)) {
			addBeanDefinitionForObject(fullname, beanDefRegistry, parserContext);
		} else {
			if( StringUtils.hasText(namelist)){
				for( String name : namelist.split(",")){
					name = name.trim();
					if( !name.isEmpty())
						addBeanDefinitionForObject(namespace +"/"+name, beanDefRegistry, parserContext);
				}
			} else {
				logger.warn("Using the 'namespace' attribute with the <corba:import> element is deprecated");
				List<String> objectNames;
				try {
					NetService netService;
					try {
						netService = NetService.getInstance();
					} catch (FactoryException e) {
						throw new RuntimeException("Could not get NetService", e);
					}
					objectNames = netService.listAllFindables(namespace);
				} catch (Exception e) {
					throw new RuntimeException("Couldn't import remote objects", e);
				}
				for (String objectName : objectNames) {
					addBeanDefinitionForObject(objectName, beanDefRegistry, parserContext);
				}
			}
		}
		return null;
	}

	private static void addBeanDefinitionForObject(String fullName, BeanDefinitionRegistry registry, ParserContext parserContext) {
		logger.debug("Registering bean for remote object " + fullName);

		String objectName = fullName.substring(fullName.lastIndexOf('/') + 1);

		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setResource(parserContext.getReaderContext().getResource());
		beanDef.setBeanClass(RemoteObjectFactoryBean.class);
		beanDef.getPropertyValues().addPropertyValue("remoteName", fullName);
		beanDef.getPropertyValues().addPropertyValue("netService", new RuntimeBeanReference(CorbaNamespaceHandler.NET_SERVICE_BEAN_NAME));
		registry.registerBeanDefinition(objectName, beanDef);
	}

}
