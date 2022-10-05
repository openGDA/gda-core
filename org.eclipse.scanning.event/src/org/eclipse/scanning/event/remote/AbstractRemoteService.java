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
package org.eclipse.scanning.event.remote;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConnection;

/**
 *
 * @author Matthew Gerring
 *
 */
abstract class AbstractRemoteService implements IConnection {

	protected IEventService eservice;
	protected URI uri;
	private boolean isConnected;

	protected AbstractRemoteService() {
		// this is true initially as there is no connect method, we assume that the connection
		// is made as required
		this.isConnected = true;
	}

	protected IEventService getEventService() {
		return eservice;
	}

	public void setEventService(IEventService eservice) {
		this.eservice = eservice;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * Does nothing, requires override.
	 */
	void init() throws EventException {

	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
}
