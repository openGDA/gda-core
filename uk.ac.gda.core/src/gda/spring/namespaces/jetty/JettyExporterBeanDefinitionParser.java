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

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the {@code <exporter>} element.
 * 
 * <p>A {@code <exporter>} causes a {@link ServletHolder} and a
 * {@link ServletMapping} to be added to the parent context's servlet handler.
 * 
 * <p>The servlet is an automatically-generated
 * {@link HttpRequestHandlerServlet} that delegates to an
 * automatically-generated {@link HessianServiceExporter}. (The exporter is
 * added to the Spring application context; its ID is the same as that used for
 * the {@code <exporter>}.)
 */
public class JettyExporterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	
	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
	
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		final String path = element.getAttribute("path");
		final String service = element.getAttribute("service");
		final String serviceInterface = element.getAttribute("serviceInterface");
		final String id = element.getAttribute("id");
		
		// The servlet holder and servlet mapping need to be added to the parent
		// context.
		buildBeanDefinitionForServletHolder(builder, id);
		AbstractBeanDefinition servletMappingBeanDef = buildBeanDefinitionForServletMapping(path, id);
		addServletHolderAndMappingToParentContext(parserContext, builder.getBeanDefinition(), servletMappingBeanDef);
		
		// The request handler is a new bean that needs to be added to the
		// Spring context.
		BeanDefinition requestHandlerBeanDef = buildBeanDefinitionForRequestHandler(service, serviceInterface);
		parserContext.getRegistry().registerBeanDefinition(id, requestHandlerBeanDef);
	}
	
	protected static void buildBeanDefinitionForServletHolder(BeanDefinitionBuilder builder, String id) {
		final AbstractBeanDefinition beanDef = builder.getBeanDefinition();
		beanDef.setBeanClass(ServletHolder.class);
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("name", id));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("servlet", buildBeanDefinitionForRequestHandlerServlet()));
	}
	
	protected static AbstractBeanDefinition buildBeanDefinitionForRequestHandlerServlet() {
		// The request handler servlet has no direct link to its request
		// handler, because HttpRequestHandlerServlet uses its servlet name to
		// find the corresponding request handler in the application context.
		
		GenericBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(HttpRequestHandlerServlet.class);
		return beanDef;
	}
	
	protected static AbstractBeanDefinition buildBeanDefinitionForServletMapping(String path, String id) {
		GenericBeanDefinition servletMappingBeanDef = new GenericBeanDefinition();
		servletMappingBeanDef.setBeanClass(ServletMapping.class);
		servletMappingBeanDef.getPropertyValues().addPropertyValue(new PropertyValue("pathSpec", path));
		servletMappingBeanDef.getPropertyValues().addPropertyValue(new PropertyValue("servletName", id));
		return servletMappingBeanDef;
	}
	
	protected static void addServletHolderAndMappingToParentContext(ParserContext parserContext, BeanDefinition servletHolder, BeanDefinition servletMapping) {
		BeanDefinition parentContext = parserContext.getContainingBeanDefinition();
		BeanDefinition servletHandler = (BeanDefinition) parentContext.getPropertyValues().getPropertyValue("servletHandler").getValue();
		
		@SuppressWarnings("unchecked")
		ManagedList<BeanDefinition> servlets = (ManagedList<BeanDefinition>) servletHandler.getPropertyValues().getPropertyValue("servlets").getValue();
		@SuppressWarnings("unchecked")
		ManagedList<BeanDefinition> servletMappings = (ManagedList<BeanDefinition>) servletHandler.getPropertyValues().getPropertyValue("servletMappings").getValue();
		
		NamespaceUtils.addToManagedList(servlets, servletHolder);
		NamespaceUtils.addToManagedList(servletMappings, servletMapping);
	}
	
	protected static AbstractBeanDefinition buildBeanDefinitionForRequestHandler(String service, String serviceInterface) {
		GenericBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(HessianServiceExporter.class);
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("service", new RuntimeBeanReference(service)));
		beanDef.getPropertyValues().addPropertyValue(new PropertyValue("serviceInterface", serviceInterface));
		return beanDef;
	}
	
}
