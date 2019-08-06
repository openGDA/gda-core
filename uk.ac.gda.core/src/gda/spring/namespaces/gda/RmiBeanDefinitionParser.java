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

import static uk.ac.gda.remoting.server.GdaRmiServiceExporter.RMI_PREFIX;

import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.remoting.client.GdaRmiProxyFactoryBean;
import uk.ac.gda.remoting.server.GdaRmiServiceExporter;

/**
 * Spring {@link BeanDefinitionParser} for the {@code export} and {@code import} elements.
 * <p>
 * This uses {@link GdaRmiServiceExporter} for exporting and {@link GdaRmiProxyFactoryBean} for
 * importing.
 */
public class RmiBeanDefinitionParser {
	private static final String HOST_NAME = LocalProperties.get("gda.server.host", "localhost");

	/** Return a bean reference to the named bean */
	private BeanReference service(String serviceBeanName) {
		return new RuntimeBeanReference(serviceBeanName);
	}

	/** Get the full service name of the exported service - the prefix and name */
	private String serviceName(String serviceBeanName) {
		return RMI_PREFIX + "/" + serviceBeanName;
	}

	/** Get the URL of the exported service. Built from host, prefix and service name */
	private String serviceUrl(String serviceBeanName) {
		return "rmi://" + HOST_NAME + "/" + serviceName(serviceBeanName);
	}

	/** Get bean name of the service exported */
	private String getExporterBeanName(String name) {
		return "_GDARmiExporter_" + name;
	}

	/** Handle {@code export} tags */
	public AbstractBeanDefinition exportBean(Element element, ParserContext parserContext) {

		final String service = element.getAttribute("service");
		final String serviceInterface = element.getAttribute("serviceInterface");

		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setResource(parserContext.getReaderContext().getResource());
		beanDef.setBeanClass(GdaRmiServiceExporter.class);
		beanDef.getPropertyValues().addPropertyValue("service", service(service));
		beanDef.getPropertyValues().addPropertyValue("serviceName", serviceName(service));
		beanDef.getPropertyValues().addPropertyValue("serviceInterface", serviceInterface);

		BeanDefinitionRegistry registry = parserContext.getRegistry();
		registry.registerBeanDefinition(getExporterBeanName(service), beanDef);
		return null;
	}

	/** Handle {@code import} tags */
	public AbstractBeanDefinition importBean(Element element, ParserContext parserContext) {

		final String service = element.getAttribute("service");
		final String serviceInterface = element.getAttribute("serviceInterface");

		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setResource(parserContext.getReaderContext().getResource());
		beanDef.setBeanClass(GdaRmiProxyFactoryBean.class);
		beanDef.getPropertyValues().addPropertyValue("serviceUrl", serviceUrl(service));
		beanDef.getPropertyValues().addPropertyValue("serviceInterface", serviceInterface);
		beanDef.getPropertyValues().addPropertyValue("refreshStubOnConnectFailure", true);

		parserContext.getRegistry().registerBeanDefinition(service, beanDef);

		return null;
	}
}
