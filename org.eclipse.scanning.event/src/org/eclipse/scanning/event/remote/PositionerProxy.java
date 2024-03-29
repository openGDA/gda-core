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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.PositionRequestType;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PositionerProxy extends AbstractRemoteService implements IPositioner {

	private static final Logger logger = LoggerFactory.getLogger(PositionerProxy.class);

	private IRequester<PositionerRequest> requester;

	// We use the uniqueid of the request to get a kind of 'session'
	private PositionerRequest             request;

	public PositionerProxy(URI uri, IEventService eservice) throws EventException {
		setUri(uri);
		setEventService(eservice);
		init();
	}

	@Override
	public void disconnect() throws EventException {
		requester.disconnect();
		setConnected(false);
	}

	@Override
	public void init()  throws EventException {
		requester = eservice.createRequestor(uri, EventConstants.POSITIONER_REQUEST_TOPIC, EventConstants.POSITIONER_RESPONSE_TOPIC);
		long timeout = Long.getLong("org.eclipse.scanning.event.remote.positionerTimeout", 30000);
	    logger.debug("Setting timeout {} {}" , timeout , " ms");
		requester.setTimeout(timeout, TimeUnit.MILLISECONDS); // Useful for debugging testing
		request   = new PositionerRequest();
	}

	@Override
	public void addPositionListener(IPositionListener listener) {
		// TODO If/When this is required, modify PositionDelegate to send topic events, listen to those
        throw new RuntimeException("Not implemented as yet!");
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		// TODO If/When this is required, modify PositionDelegate to send topic events, listen to those
        throw new RuntimeException("Not implemented as yet!");
	}

	@Override
	public boolean setPosition(IPosition position) throws ScanningException, InterruptedException {
		request.setRequestType(PositionRequestType.SET);
		request.setPosition(position);
		try {
			request = requester.post(request);
			return true;
		} catch (EventException e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public IPosition getPosition() throws ScanningException {
		request.setRequestType(PositionRequestType.GET);
		request.setPosition(null);
		try {
			request = requester.post(request);
		    return request.getPosition();
		} catch (EventException | InterruptedException e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public List<IScannable<?>> getMonitorsPerPoint() throws ScanningException {
		// TODO Use the _Scannable which is a remote scannable connection.
		return null;
	}

	@Override
	public void setMonitorsPerPoint(List<IScannable<?>> monitors) {
		// TODO Use the _Scannable which is a remote scannable connection.
		throw new UnsupportedOperationException("Monitors may not be set on a remote positioner!");
	}

	@Override
	public void setMonitorsPerPoint(IScannable<?>... monitor) {
		setMonitorsPerPoint(Arrays.asList(monitor));
	}

	@Override
	public void setScannables(List<IScannable<?>> scannables) {
		// TODO Use the _Scannable which is a remote scannable connection.
		throw new UnsupportedOperationException("Scannables may not be set on a remote positioner!");
	}

	@Override
	public void abort() {
		request.setRequestType(PositionRequestType.ABORT);
		try {
			request = requester.post(request);
		} catch (EventException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		request.setRequestType(PositionRequestType.CLOSE);
		try {
			request = requester.post(request);
		} catch (EventException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
