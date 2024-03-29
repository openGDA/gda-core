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
package org.eclipse.scanning.connector.jms;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IMessagingService;
import org.eclipse.scanning.api.scan.IFilePathService;

import uk.ac.diamond.mq.ISessionService;

/**
 * This class is temporarily in this plugin and needs to be moved out of it once:
 * 1. We move the ActiveMQ dependency to bundle imports rather than jar file.
 * 2. We create a bundle called org.eclipse.scanning.event.activemq to donate the dependency
 * 3. We start the scanning eclipse project and get ActiveMQ ip checked (rather large unless already done might be hard).
 * <p>
 * JSON marshalling is done by delegating to the new JsonMarshaller service which encapsulates all JSON interactions
 * behind one cohesive interface.
 *
 * @author Matthew Gerring
 * @author Colin Palmer
 *
 */
public class JmsConnectorService implements IEventConnectorService, IMessagingService {

	private IMarshallerService jsonMarshaller;

	private IFilePathService filePathService;

	private ISessionService sessionService;

	public void setJsonMarshaller(IMarshallerService jsonMarshaller) {
		this.jsonMarshaller = jsonMarshaller;
	}

	public void setFilePathService(IFilePathService filePathService) {
		this.filePathService = filePathService;
	}

	public void setSessionService(ISessionService sessionService) {
		this.sessionService = sessionService;
	}

	@Override
	public ISessionService getSessionService(){
		return sessionService;
	}

	static {
		System.out.println("Started " + JmsConnectorService.class.getSimpleName());
	}

	/**
	 * Default public constructor - for testing purposes only! Otherwise use OSGi to get the service.
	 */
	public JmsConnectorService() {
		// nothing to do
	}

	@Override
	public String marshal(Object anyObject) throws Exception {
		checkJsonMarshaller();
		return jsonMarshaller.marshal(anyObject);
	}

	@Override
	public <U> U unmarshal(String json, Class<U> beanClass) throws Exception {
		checkJsonMarshaller();
		return jsonMarshaller.unmarshal(json, beanClass);
	}

	@Override
	public String getPersistenceDir() {
		return filePathService.getPersistenceDir();
	}

	private void checkJsonMarshaller() {
		if (jsonMarshaller == null) {
			// OSGi should always provide the JSON marshaller. If it's not present, probably someone is calling this
			// constructor directly and may have forgotten to set the JSON marshaller first, so we print a warning
			String msg = this.getClass().getSimpleName() + " needs an IJsonMarshaller to function correctly";
			System.err.println(msg);
			throw new NullPointerException(msg);
		}
	}

	private BrokerService service;

	/**
	 * @param The activemq connector uri, for instance: "failover:(tcp://localhost:61616)?startupMaxReconnectAttempts=3"
	 *        The failover:() is stipped out so that a tcp:// uri is created for the server.
	 */
	@Override
	public URI start(String suggestedURI) throws EventException {

		try {
			Pattern pattern = Pattern.compile(".*(tcp://[a-zA-Z\\.]+:\\d+).*");
			Matcher matcher = pattern.matcher(suggestedURI);
			if (matcher.matches()) suggestedURI = matcher.group(1);

			URI uri = new URI(suggestedURI); // Each test uses a new port if the port is running on another test.
			service = new BrokerService();
	        service.addConnector(uri);
	        service.setPersistent(false);
			service.addConnector("stomp://localhost:61613"); // Allow stomp connections (for Python clients, etc.).
	        SystemUsage systemUsage = service.getSystemUsage();
	        systemUsage.getStoreUsage().setLimit(1024 * 1024 * 8);
	        systemUsage.getTempUsage().setLimit(1024 * 1024 * 8);
	        service.start();
			service.waitUntilStarted();
			return uri;

		} catch (Exception ne) {
			throw new EventException(ne);
		}
	}

	@Override
	public void stop() throws EventException {
		if (service==null) return;
		try {
			service.stop();
			service.waitUntilStopped();
			service = null;
		} catch (Exception ne) {
			throw new EventException(ne);
		}
	}

	private static int getFreePort(final int startPort) {
		int port = startPort;
		while (!isPortFree(port))
			port++;
		return port;
	}
	/**
	 * Checks if a port is free.
	 * @param port
	 * @return
	 */
	public static boolean isPortFree(int port) {
		try (ServerSocket ss = new ServerSocket(port);
				DatagramSocket ds = new DatagramSocket(port)) {
			ss.setReuseAddress(true);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

}
