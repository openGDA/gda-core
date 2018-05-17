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
package org.eclipse.scanning.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.activemq.broker.BrokerService;

public class BrokerDelegate {

	private URI           uri;
	private BrokerService service;

	public void start() throws Exception {
		uri = createUri();
		System.setProperty("org.eclipse.scanning.broker.uri", uri.toString());
        service = new BrokerService();
        service.setPersistent(false);
        service.setUseJmx(false);
        service.start();
		boolean ok = service.waitUntilStarted();
		if (!ok) throw new Exception("Broker was not started properly!");
	}

	public void stop() throws Exception {

		if (service!=null) {
			service.stop();
			service.waitUntilStopped();
			service = null;
		}
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}


	private static URI createUri() throws URISyntaxException {
			return new URI("vm://localhost?broker.persistent=false");
	}

}
