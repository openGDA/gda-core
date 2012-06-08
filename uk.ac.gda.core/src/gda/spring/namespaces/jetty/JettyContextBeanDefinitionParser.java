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

import gda.spring.BeanFactoryAwareContextLoaderListener;
import gda.spring.parsers.NamespaceUtils;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
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
 * {@link BeanDefinitionParser} for the {@code <context>} element.
 * 
 *  <p>A context is represented by a {@link ContextHandler} which is added to
 *  the parent {@link Server}'s list of handlers. A {@link ContextHandler} has
 *  the following children:
 *  
 *  <ul>
 *  <li>A context path.</li>
 *  <li>A security handler.</li>
 *  <li>A session handler.</li>
 *  <li>A servlet handler. This has a list of {@link ServletHolder}s
 *      (name → servlet), and a list of {@link ServletMapping}s
 *      (path spec → servlet name). These lists are populated by
 *      {@code <exporter>} elements being added to the {@code <context>}.</li>
 *  <li>A list of event listeners. This list will initially contain a
 *      {@link BeanFactoryAwareContextLoaderListener} so that the servlets
 *      have access to the Spring application context in which the
 *      {@code <context>} is defined.</li>
 *  </ul>
 */
public class JettyContextBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
	
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		final String contextPath = element.getAttribute("path");
		buildBeanDefinitionForContext(element, parserContext, builder, contextPath);
	}
	
	protected static void buildBeanDefinitionForContext(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String contextPath) {
		AbstractBeanDefinition beanDef = builder.getBeanDefinition();
		beanDef.setBeanClass(Context.class);
		beanDef.setPropertyValues(new MutablePropertyValues());
		
		ManagedList<BeanDefinition> eventListeners = new ManagedList<BeanDefinition>();
		NamespaceUtils.addToManagedList(eventListeners, buildBeanDefinitionForContextLoader());
		
		BeanDefinition sessionHandler = buildBeanDefinitionForSessionHandler();
		BeanDefinition securityHandler = buildBeanDefinitionForSecurityHandler();
		BeanDefinition servletHandler = buildBeanDefinitionForServletHandler();
		
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("sessionHandler", sessionHandler));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("securityHandler", securityHandler));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("servletHandler", servletHandler));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("eventListeners", eventListeners));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("contextPath", contextPath));
		
		parseNestedExporterElements(element, parserContext, beanDef);
	}
	
	protected static BeanDefinition buildBeanDefinitionForContextLoader() {
		AbstractBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(BeanFactoryAwareContextLoaderListener.class);
		return beanDef;
	}
	
	protected static AbstractBeanDefinition buildBeanDefinitionForSessionHandler() {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SessionHandler.class);
		return builder.getBeanDefinition();
	}
	
	protected static AbstractBeanDefinition buildBeanDefinitionForSecurityHandler() {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SecurityHandler.class);
		return builder.getBeanDefinition();
	}
	
	protected static AbstractBeanDefinition buildBeanDefinitionForServletHandler() {
		BeanDefinitionBuilder servletHandler = BeanDefinitionBuilder.genericBeanDefinition(ServletHandler.class);
		servletHandler.addPropertyValue("servlets", new ManagedList<BeanDefinition>());
		servletHandler.addPropertyValue("servletMappings", new ManagedList<BeanDefinition>());
		return servletHandler.getBeanDefinition();
	}
	
	protected static void parseNestedExporterElements(Element element, ParserContext parserContext, BeanDefinition beanDef) {
		NodeList exporterElements = element.getElementsByTagNameNS("*", "exporter");
		for (int i=0; i<exporterElements.getLength(); i++) {
			final Element exporterElement = (Element) exporterElements.item(i);
			parserContext.getDelegate().parseCustomElement(exporterElement, beanDef);
		}
	}

}
