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
package org.eclipse.scanning.event;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequestResponseConnection;

abstract class AbstractRequestResponseConnection implements IRequestResponseConnection {


	private   URI           uri;
	private   String        requestTopic;
	private   String        responseTopic;
	protected IEventService eservice;
	private boolean connected = true;

	AbstractRequestResponseConnection(URI uri, String reqTopic, String resTopic, IEventService eservice) {
		this.uri           = uri;
		this.requestTopic  = reqTopic;
		this.responseTopic = resTopic;
		this.eservice     = eservice;
	}

	@Override
	public IEventConnectorService getConnectorService() {
		return eservice.getEventConnectorService();
	}

	@Override
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	@Override
	public String getRequestTopic() {
		return requestTopic;
	}

	@Override
	public void setRequestTopic(String requestTopic) {
		this.requestTopic = requestTopic;
	}

	@Override
	public String getResponseTopic() {
		return responseTopic;
	}

	@Override
	public void setResponseTopic(String responseTopic) {
		this.responseTopic = responseTopic;
	}

	/**
	 * Call to disconnect
	 * @throws EventException
	 */
	@Override
	public void disconnect() throws EventException {
		setConnected(false);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requestTopic == null) ? 0 : requestTopic.hashCode());
		result = prime * result + ((responseTopic == null) ? 0 : responseTopic.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractRequestResponseConnection other = (AbstractRequestResponseConnection) obj;
		if (requestTopic == null) {
			if (other.requestTopic != null)
				return false;
		} else if (!requestTopic.equals(other.requestTopic))
			return false;
		if (responseTopic == null) {
			if (other.responseTopic != null)
				return false;
		} else if (!responseTopic.equals(other.responseTopic))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
