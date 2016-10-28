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

import gda.factory.corba.util.AdapterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Spring {@link BeanDefinitionParser} for the {@code adapterfactory} element.
 */
public class AdapterFactoryBeanDefinitionParser implements BeanDefinitionParser {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AdapterFactoryBeanDefinitionParser.class);

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {

		final String namespace = element.getAttribute("namespace");

		// Namespace is mandatory
		if (!StringUtils.hasText(namespace)) {
			throw new IllegalArgumentException("You must specify a namespace when using the <corba:adapterfactory> element");
		}

		CorbaNamespaceHandler.registerNetServiceFactoryBean(parserContext.getRegistry());

		final BeanReference netServiceBeanRef = new RuntimeBeanReference(CorbaNamespaceHandler.NET_SERVICE_BEAN_NAME);

		final AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setResource(parserContext.getReaderContext().getResource());
		beanDef.setBeanClass(AdapterFactory.class);
		beanDef.getConstructorArgumentValues().addGenericArgumentValue(namespace);
		beanDef.getConstructorArgumentValues().addGenericArgumentValue(netServiceBeanRef);

		final BeanDefinitionRegistry beanDefRegistry = parserContext.getRegistry();

		final String beanName = parserContext.getReaderContext().generateBeanName(beanDef);
		beanDefRegistry.registerBeanDefinition(beanName, beanDef);

		return beanDef;
	}

}
