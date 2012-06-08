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

import gda.factory.Factory;
import gda.factory.Findable;
import gda.spring.SimpleHttpRequestHandlerServlet;
import gda.spring.remoting.FindableExporterBase;

import javax.servlet.http.HttpServlet;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;

/**
 * A Spring {@link BeanFactoryPostProcessor} that exposes all {@link Findable}s
 * in a GDA {@link Factory} using a HTTP-based exporter.
 * 
 * <p>Also sets up a {@link RemoteObjectListerServlet} that lists the available
 * objects.
 */
public class FindableExporter extends FindableExporterBase {
	
	private static final Logger logger = LoggerFactory.getLogger(FindableExporter.class);
	
	private static final String REMOTE_OBJECT_LISTER_SERVLET_NAME = "objects-servlet";
	
	private Server server;
	
	/**
	 * Sets the Jetty server to which the servlets will be added.
	 * 
	 * @param server the Jetty server
	 */
	public void setServer(Server server) {
		this.server = server;
	}
	
	protected Context context;
	
	@Override
	protected void beforeExportingObjects(ConfigurableListableBeanFactory beanFactory) {
		// Constructing the Context causes it to be added to the Jetty server
		context = new Context(server, Constants.CONTEXT_PATH, true, false);
	}

	@Override
	protected void exportObject(Findable findable, Class<?> serviceInterface, ConfigurableListableBeanFactory beanFactory) {
		HttpRequestHandler requestHandler = createHttpRequestHandler(findable, serviceInterface);
		SimpleHttpRequestHandlerServlet servlet = new SimpleHttpRequestHandlerServlet(requestHandler);

		String servletName = findable.getName() + "-servlet";
		String servletPath = "/" + findable.getName();
		logger.info("Exporting " + StringUtils.quote(findable.getName()) + " at " + StringUtils.quote(servletPath) + " using servlet " + StringUtils.quote(servletName));
		addServletToServletContext(context, servlet, servletPath, servletName);
	}

	protected HttpRequestHandler createHttpRequestHandler(Object object, Class<?> interfaceOfFindable) {
		HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
		exporter.setService(object);
		exporter.setServiceInterface(interfaceOfFindable);
		exporter.afterPropertiesSet();
		return exporter;
	}
	
	protected void addServletToServletContext(Context context, HttpServlet servlet, String servletPath, String servletName) {
		ServletHolder servletHolder = createServletHolder(servlet, servletName);
		final ServletHandler servletHandler = context.getServletHandler();
		servletHandler.addServlet(servletHolder);
		servletHandler.addServletWithMapping(servletHolder, servletPath);
	}
	
	protected ServletHolder createServletHolder(HttpServlet servlet, String servletName) {
		ServletHolder holder = new ServletHolder();
		holder.setServlet(servlet);
		holder.setName(servletName);
		return holder;
	}

	@Override
	protected void afterExportingObjects(ConfigurableListableBeanFactory beanFactory) {
		// Add lister servlet to the context
		HttpServlet listerServlet = new RemoteObjectListerServlet(availableObjects);
		addServletToServletContext(context, listerServlet, Constants.REMOTE_OBJECT_LISTER_PATH, REMOTE_OBJECT_LISTER_SERVLET_NAME);
	}

}
