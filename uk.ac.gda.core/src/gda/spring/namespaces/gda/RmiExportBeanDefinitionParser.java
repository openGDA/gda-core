/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.spring.namespaces.gda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.w3c.dom.Element;

import uk.ac.gda.remoting.server.GdaRmiServiceExporter;

/**
 * Spring {@link BeanDefinitionParser} for the {@code rmi-export} element.
 *
 * This now always uses GdaRmiServiceExporter from GDA 9 onwards as it is
 * required to correctly load the Spring beans to be exported - the contained
 * Spring {@link RmiServiceExporter}'s Classloader cannot see the beans under OSGi
 *
 */
public class RmiExportBeanDefinitionParser implements BeanDefinitionParser {
	private static final Logger logger = LoggerFactory.getLogger(RmiExportBeanDefinitionParser.class);

	@Override
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
		final String service = element.getAttribute("service");
		final String serviceName = element.getAttribute("serviceName");
		final String serviceInterface = element.getAttribute("serviceInterface");

		logger.warn("Using deprecated rmi-export tag for {} in file {}. Use export instead - see DAQ-2301",
				service,
				getSource(parserContext));

		boolean events = true;
		if (element.hasAttribute("events")) {
			final String eventsAttr = element.getAttribute("events");
			events = Boolean.valueOf(eventsAttr);
		}

		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setResource(getSource(parserContext));
		beanDef.setBeanClass(GdaRmiServiceExporter.class);
		beanDef.getPropertyValues().addPropertyValue("service", new RuntimeBeanReference(service));
		beanDef.getPropertyValues().addPropertyValue("serviceName", serviceName);
		beanDef.getPropertyValues().addPropertyValue("serviceInterface", serviceInterface);

		BeanDefinitionRegistry registry = parserContext.getRegistry();
		int counter = -1;
		String beanName;
		while (true) {
			counter++;
			beanName = "GDARmiExporter#" + counter;
			if (!registry.containsBeanDefinition(beanName)) {
				break;
			}
		}
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDef);

		return null;
	}

	private Resource getSource(ParserContext parserContext) {
		return parserContext.getReaderContext().getResource();
	}

}
