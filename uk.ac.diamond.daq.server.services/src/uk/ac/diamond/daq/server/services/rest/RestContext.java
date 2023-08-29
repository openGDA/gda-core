/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.rest;

import static java.util.Collections.emptySet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

public class RestContext implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RestContext.class);

	private static final List<Class<?>> COMPONENTS_TO_REGISTER = List.of();

	private AnnotationConfigWebApplicationContext rootContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		createServer();

	}

	private void createServer() throws Exception {
		Server server = createServer(8088);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.getServletContext().setExtendedListenerTypes(true);
		server.setHandler(context);

		context.addEventListener(new Listener() {

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
		// Create the application context
		rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.setClassLoader(
				GDAClassLoaderService.getClassLoaderService().getClassLoaderForLibraryWithGlobalResourceLoading(
						AnnotationConfigWebApplicationContext.class, emptySet()));

		COMPONENTS_TO_REGISTER.forEach(comp -> rootContext.register(RestConfig.class, comp));

		ServletRegistration.Dynamic dispatcher = container.addServlet("restServlet",
				new DispatcherServlet(rootContext));
		dispatcher.addMapping("/");
		dispatcher.setLoadOnStartup(1);
	}

}
