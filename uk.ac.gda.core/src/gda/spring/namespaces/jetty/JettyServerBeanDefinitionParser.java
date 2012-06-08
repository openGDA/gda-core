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

package gda.spring.namespaces.jetty;

import gda.spring.parsers.NamespaceUtils;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * {@link BeanDefinitionParser} for the {@code <server>} element.
 * 
 * <p>A Jetty {@link Server} has a list of connectors, and a list of handlers.
 * The {@link Server} created by this parser initially has a single connector
 * (the server port is used when creating this), and an empty list of
 * handlers. Each {@code <context>} nested within the {@code <server>} will
 * result in a handler being added to the {@link Server}.
 */
public class JettyServerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
	
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		final int serverPort = determineServerPortNumber(element);
		
		AbstractBeanDefinition connectorBeanDef = buildBeanDefinitionForConnector(serverPort);
		ManagedList<BeanDefinition> connectors = createListOfConnectorsContaining(connectorBeanDef);
		
		ManagedList<BeanDefinition> handlers = new ManagedList<BeanDefinition>();
		
		buildBeanDefinitionForServer(element, parserContext, builder, connectors, handlers);
	}
	
	protected static int determineServerPortNumber(Element element) {
		if (element.hasAttribute("port")) {
			return Integer.parseInt(element.getAttribute("port"));
		}
		return 8080;
	}
	
	private static AbstractBeanDefinition buildBeanDefinitionForConnector(int serverPort) {
		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(SelectChannelConnector.class);
		beanDef.setPropertyValues(new MutablePropertyValues());
		
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("port", serverPort));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("maxIdleTime", 30000));
		
		return beanDef;
	}
	
	protected static ManagedList<BeanDefinition> createListOfConnectorsContaining(BeanDefinition connector) {
		ManagedList<BeanDefinition> connectors = new ManagedList<BeanDefinition>();
		NamespaceUtils.addToManagedList(connectors, connector);
		return connectors;
	}
	
	protected static void buildBeanDefinitionForServer(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, ManagedList<BeanDefinition> connectors, ManagedList<BeanDefinition> handlers) {
		AbstractBeanDefinition beanDef = builder.getBeanDefinition();
		beanDef.setBeanClass(Server.class);
		beanDef.setPropertyValues(new MutablePropertyValues());
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("connectors", connectors));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("handlers", handlers));
		
		parseNestedContextElements(element, parserContext, handlers, beanDef);
	}
	
	protected static void parseNestedContextElements(Element element, ParserContext parserContext, ManagedList<BeanDefinition> handlers, BeanDefinition serverBeanDefinition) {
		NodeList contextElements = element.getElementsByTagNameNS("*", "context");
		for (int i=0; i<contextElements.getLength(); i++) {
			final Element contextElement = (Element) contextElements.item(i);
			BeanDefinition contextBeanDef = parserContext.getDelegate().parseCustomElement(contextElement, serverBeanDefinition);
			NamespaceUtils.addToManagedList(handlers, contextBeanDef);
		}
	}
	
}
