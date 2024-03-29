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
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractRemoteDeviceProxy<M> extends AbstractRemoteService {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRemoteDeviceProxy.class);

	protected String name;
	protected DeviceInformation<M> info;
	protected final IRequester<DeviceRequest> requester;

	private AbstractRemoteDeviceProxy(URI uri, IEventService eservice) throws EventException {
		setEventService(eservice);
		setUri(uri);
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
	}

	AbstractRemoteDeviceProxy(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		this(uri, eservice);
		logger.debug("Setting timeout {} {}", RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit());
		requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing
		connect(req);
	}

	AbstractRemoteDeviceProxy(DeviceRequest req, long timeoutMs, URI uri, IEventService eservice) throws EventException, InterruptedException {
		this(uri, eservice);
		logger.debug("Setting  {} {}", timeoutMs, "ms");
		requester.setTimeout(timeoutMs, TimeUnit.MILLISECONDS);
		connect(req);
	}

	AbstractRemoteDeviceProxy(DeviceInformation<M> info, URI uri, IEventService eventService) throws EventException {
		this(uri, eventService);
		this.info = info;
		this.name = info.getName();
	}

	AbstractRemoteDeviceProxy(DeviceInformation<M> info, long timeoutMs, URI uri, IEventService eventService) throws EventException {
		this(uri, eventService);
		this.info = info;
		requester.setTimeout(timeoutMs, TimeUnit.MILLISECONDS);
		this.name = info.getName();
	}

	private void connect(DeviceRequest req) throws EventException, InterruptedException, ValidationException {
		req = requester.post(req);
		req.checkException();
		info = (DeviceInformation<M>)req.getDeviceInformation();
		this.name = info.getName();
	}

	@Override
	public void disconnect() throws EventException {
		requester.disconnect(); // Requester can still be used again after a disconnect
		setConnected(false);
	}

	public String getName() {
		if (info==null) update();
		return info.getName();
	}

	public void setName(String name) {
		// TODO
		throw new RuntimeException("Devices may not have this changed remotely currently!");
	}

	public void setLevel(int level) {
		// TODO
		throw new RuntimeException("Devices may not have this changed remotely currently!");
	}

	public int getLevel() {
		if (info==null) update();
		return info.getLevel();
	}

	protected abstract DeviceRequest update();


	protected void merge(DeviceInformation<M> info) {
		if (info == null) return; // Nothing to merge
		if (this.info == null) {
			this.info = info;
			return;
		}
		this.info.merge(info);
	}

	protected void method(DeviceRequest deviceRequest) throws ScanningException {
		try {
			DeviceRequest req = requester.post(deviceRequest);
			merge((DeviceInformation<M>)req.getDeviceInformation());
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

}
