/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.core.tool.spring;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.rest.SpringContextConfig;
import uk.ac.diamond.daq.rest.SpringDispatcherConfig;

/**
 * Instantiates a {@link AnnotationConfigWebApplicationContext} for GDA Rest services.
 *
 * <p>
 * The implementation is based on an embedded Jetty server and Spring MVC libraries.
 *
 * When GDA starts Springs also follows scanning a number of annotated classes including this one. On the postConstruct
 * this class instantiates the Jetty server and attach to its life cycle the necessary call to start the
 * {@link AnnotationConfigWebApplicationContext}.
 * </p>
 *
 * <p>
 * The service endpoint URL is the same as for GDA, however the port is specified by a property
 * {@link #REST_SERVICE_PORT} (default 8888)
 * </p>
 *
 * @author Maurizio Nagni
 */
@Configuration
public class SpringWebInitializer {

	private static final Logger logger = LoggerFactory.getLogger(SpringWebInitializer.class);

	/**
	 * The REST service port property
	 */
	public static final String REST_SERVICE_PORT = "rest.service.port";

	/**
	 * The Spring web application context
	 */
	private AnnotationConfigWebApplicationContext rootContext;

	@PostConstruct
	private void postConstruct() throws Exception {
		final Slf4jLog log = new Slf4jLog("ExperimentRestService");

		Server server = createServer(LocalProperties.getAsInt(REST_SERVICE_PORT, 8888));
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.getServletContext().setExtendedListenerTypes(true);
		context.setLogger(log);
		server.setHandler(context);

		context.addLifeCycleListener(new Listener() {

			@Override
			public void lifeCycleStopping(LifeCycle event) {
				// Not used
			}

			@Override
			public void lifeCycleStopped(LifeCycle event) {
				// Not used
			}

			@Override
			public void lifeCycleStarting(LifeCycle event) {
				onStartup(context.getServletContext());
			}

			@Override
			public void lifeCycleStarted(LifeCycle event) {
				// Not used
			}

			@Override
			public void lifeCycleFailure(LifeCycle event, Throwable cause) {
				// Not used
			}
		});

		try {
			server.start();
		} catch (IOException e) {
			logger.error("Failed to start the Jetty server", e);
		}

	}

	/**
	 * A really simple sever instance
	 *
	 * @param port
	 *            where the server will listen for connections
	 * @return
	 */
	private Server createServer(int port) {
		return new Server(port);
	}

	private void onStartup(ServletContext container) {
		addRootContext(container);
		addDispatcherContext(container);
	}

	private void addRootContext(ServletContext container) {
		// Create the application context
		rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.register(SpringContextConfig.class);

		// Register application context with ContextLoaderListener
		container.addListener(new MyContextLoaderListener(rootContext));
	}

	private void addDispatcherContext(ServletContext container) {
		// Create the dispatcher servlet's Spring application context
		AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
		dispatcherContext.register(SpringDispatcherConfig.class);

		ServletRegistration.Dynamic dispatcher = container.addServlet("restServlet",
				new DispatcherServlet(dispatcherContext));
		dispatcher.addMapping("/");
		dispatcher.setLoadOnStartup(1);
	}

	/**
	 * A custom listener to refresh the context scanned classes once ready
	 */
	private class MyContextLoaderListener extends ContextLoaderListener {

		public MyContextLoaderListener(WebApplicationContext context) {
			super(context);
		}

		@Override
		public void contextInitialized(ServletContextEvent event) {
			super.contextInitialized(event);
			rootContext.scan("uk.ac.diamond.daq.experiment.rest");
			rootContext.refresh();
		}

	}

}
