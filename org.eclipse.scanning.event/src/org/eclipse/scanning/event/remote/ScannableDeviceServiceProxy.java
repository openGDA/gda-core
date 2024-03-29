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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.ILocationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ScannableDeviceServiceProxy extends AbstractRemoteService implements IScannableDeviceService {

	private static final Logger logger = LoggerFactory.getLogger(ScannableDeviceServiceProxy.class);
	private IRequester<DeviceRequest>  requester;
	private Map<String, IScannable<?>> scannables;
	private ISubscriber<ILocationListener> subscriber;

	@Override
	public void init() throws EventException {
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
		long timeout = Long.getLong("org.eclipse.scanning.event.remote.scannableServiceTimeout", 1000);
	    logger.debug("Setting timeout {} {}" , timeout , " ms");
	    requester.setTimeout(timeout, TimeUnit.MILLISECONDS);
	    scannables = new HashMap<>();

		subscriber = eservice.createSubscriber(uri, EventConstants.POSITION_TOPIC);
	}

	@Override
	public void disconnect() throws EventException {
		requester.disconnect(); // Requester can still be used again after a disconnect
		for (String name : scannables.keySet()) {
			IScannable<?> scannable = scannables.remove(name);
			if (scannable instanceof IConnection) ((IConnection)scannable).disconnect();
		}
		subscriber.disconnect();
		scannables.clear();
		setConnected(false);
	}

	@Override
	public List<String> getScannableNames() throws ScanningException {

		DeviceInformation<?>[] devices = getDevices();
	    String[] names = new String[devices.length];
	    for (int i = 0; i < devices.length; i++) names[i] = devices[i].getName();
		return Arrays.asList(names);
	}

	private DeviceInformation<?>[] getDevices() throws ScanningException {
	    DeviceRequest req;
		try {
			req = requester.post(new DeviceRequest(DeviceType.SCANNABLE));
		} catch (EventException | InterruptedException e) {
			throw new ScanningException("Cannot get devices! Connection to broker may be lost or no server up!", e);
		}
	    return req.getDevices().toArray(new DeviceInformation<?>[req.size()]);
	}

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {

		if (scannables.containsKey(name)) return (IScannable<T>)scannables.get(name);
		try {
			ScannableProxy<T> ret = new ScannableProxy<T>(new DeviceRequest(name, DeviceType.SCANNABLE), uri, subscriber, eservice);
			scannables.put(name, ret);
			return ret;
		} catch (EventException | InterruptedException e) { // If no Scannable
			throw new ScanningException(e);
		}
	}

	@Override
	public <T> void register(IScannable<T> device) {
		throw new IllegalArgumentException("New scannables may not be registered on a remote service implementation!");
	}

}
