/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.ui;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.scanning.sequencer.expression.ServerExpressionService;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.test.BrokerDelegate;
import org.eclipse.scanning.test.ServiceTestHelper;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class UITestServicesSetup {

	private static DeviceServlet dservlet;
	private static BrokerDelegate delegate;

	public static void createTestServices(boolean requireBroker) throws Exception {

		// DO NOT COPY this outside of tests, these services and servlets are created in OSGi and Spring.
		if (requireBroker) {
			delegate = new BrokerDelegate();
			delegate.start();
		}

		System.setProperty("org.eclipse.scanning.broker.uri", "vm://localhost?broker.persistent=false");

		ServiceTestHelper.setupServices();
		ServiceProvider.setService(IExpressionService.class, new ServerExpressionService());

		// Servlet to provide access to the remote scannables.
		dservlet = new DeviceServlet();
		dservlet.setBroker("vm://localhost?broker.persistent=false");
		dservlet.connect(); // Gets called by Spring automatically
	}

	public static void disposeTestServices() throws Exception {
		if (delegate!=null) delegate.stop();
		try {
			dservlet.disconnect();
		} catch (Exception ignored) {
			// exception ignored
		} finally {
			ServiceProvider.reset();
		}
	}


}
