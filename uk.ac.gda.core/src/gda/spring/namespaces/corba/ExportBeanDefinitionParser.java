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

import gda.factory.corba.util.SpringImplFactory;

import java.util.Arrays;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Spring {@link BeanDefinitionParser} for the {@code export} element.
 */
public class ExportBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
		String namespace = element.getAttribute("namespace");
		String exclude = element.getAttribute("exclude");

		CorbaNamespaceHandler.registerNetServiceFactoryBean(parserContext.getRegistry());

		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(SpringImplFactory.class);
		beanDef.getPropertyValues().addPropertyValue("namespace", namespace);
		beanDef.getPropertyValues().addPropertyValue("netService", new RuntimeBeanReference(CorbaNamespaceHandler.NET_SERVICE_BEAN_NAME));

		if (StringUtils.hasText(exclude)) {
			ManagedList<String> excludedObjects = new ManagedList<String>();
			String[] names = exclude.split(",");
			excludedObjects.addAll(Arrays.asList(names));
			beanDef.getPropertyValues().addPropertyValue("excludedObjects", excludedObjects);
		}

		BeanDefinitionRegistry registry = parserContext.getRegistry();
		int counter = -1;
		String beanName;
		while (true) {
			counter++;
			beanName = "ImplFactory#" + counter;
			if (!registry.containsBeanDefinition(beanName)) {
				break;
			}
		}
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDef);

		return null;
	}

}
