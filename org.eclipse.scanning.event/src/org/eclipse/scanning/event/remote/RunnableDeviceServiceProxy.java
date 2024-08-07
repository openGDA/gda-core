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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IRequester.ResponseType;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

public class RunnableDeviceServiceProxy extends AbstractRemoteService implements IRunnableDeviceService {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(RunnableDeviceServiceProxy.class);

	private IRequester<DeviceRequest> requester;
	private IScannableDeviceService   cservice;
	private Map<String, IRunnableDevice<?>> runnables;

	@Override
	public void init() throws EventException {
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
		long timeout = Long.getLong("org.eclipse.scanning.event.remote.runnableDeviceServiceTimeout", 2000);
		logger.debug("Setting timeout {} ms" , timeout);
		requester.setResponseType(ResponseType.ONE);
		requester.setTimeout(timeout, TimeUnit.MILLISECONDS);
		runnables = new ConcurrentHashMap<>();
	}

	@Override
	public void disconnect() throws EventException {
		requester.disconnect(); // Requester can still be used again after a disconnect
		for (String name : runnables.keySet()) {
			IRunnableDevice<?> runnable = runnables.remove(name);
			if (runnable instanceof IConnection connection)
				connection.disconnect();
		}
		runnables.clear();
		setConnected(false);
	}

	@Override
	public IPositioner createPositioner(INameable parent) throws ScanningException {
		try {
			return new PositionerProxy(uri, eservice);
		} catch (EventException e) {
			throw new ScanningException("Cannot create a positioner!", e);
		}
	}

	@Override
	public IPositioner createPositioner(String name) throws ScanningException {
		try {
			return new PositionerProxy(uri, eservice);
		} catch (EventException e) {
			throw new ScanningException("Cannot create a positioner!", e);
		}
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name) throws ScanningException {
		return getRunnableDevice(name, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name, IPublisher<ScanBean> publisher) throws ScanningException {
		try {
			return (IRunnableDevice<T>) runnables.computeIfAbsent(name, this::createRunnableDevice);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof ScanningException scanningException) throw scanningException;
			throw e;
		}
	}

	private <T> IRunnableDevice<T> createRunnableDevice(String name) {
		try {
			final DeviceRequest req = new DeviceRequest(name);
			final DeviceRequest response = requester.post(req);
			response.checkException();
			@SuppressWarnings("unchecked")
			final DeviceInformation<T> info = (DeviceInformation<T>) response.getDeviceInformation();
			if (info.getDeviceRole() == DeviceRole.MALCOLM) {
				@SuppressWarnings({"unchecked", "resource"})
				IMalcolmDevice malcolmDevice = new MalcolmDeviceProxy((DeviceInformation<IMalcolmModel>) info, uri, eservice);
				@SuppressWarnings("unchecked")
				IRunnableDevice<T> device = (IRunnableDevice<T>) malcolmDevice;
				return device;
			} else {
				return new RunnableDeviceProxy<>(info, uri, eservice);
			}
		} catch (EventException | InterruptedException e) {
			throw new RuntimeException(new ScanningException(e));
		}
	}


	@Override
	public Collection<String> getRunnableDeviceNames() throws ScanningException {
		return Arrays.stream(getDevices()).map(DeviceInformation::getName).toList();
	}

	@Override
	@Deprecated(since = "GDA 9.33", forRemoval = true)
	public IScannableDeviceService getDeviceConnectorService() {
		logger.deprecatedMethod("getDeviceConnectorService()", "GDA 9.35", "eservice.createRemoteService(new URI(CommandConstants.getScanningBrokerUri()), IScannableDeviceService.class);");

		if (cservice == null) {
			try {
				cservice = RemoteServiceFactory.getRemoteService(uri, IScannableDeviceService.class, eservice);
			} catch (InstantiationException | IllegalAccessException | EventException e) {
				logger.error("Cannot get service!", e);
			}
		}
		return cservice;
	}

	private DeviceInformation<?>[] getDevices() throws ScanningException {
	    return getDevices(false);
	}

	private DeviceInformation<?>[] getDevices(boolean getNonAlive) throws ScanningException {
	    DeviceRequest req;
		try {
			DeviceRequest outboundRequest = new DeviceRequest();
			outboundRequest.setIncludeNonAlive(getNonAlive);
			req = requester.post(outboundRequest);
			req.checkException();
		} catch (EventException | InterruptedException e) {
			throw new ScanningException("Cannot get devices! Connection to broker may be lost or no server up!", e);
		}
	    return req.getDevices().toArray(new DeviceInformation<?>[req.size()]);
	}

	@Override
	public Collection<DeviceInformation<?>> getDeviceInformationIncludingNonAlive() throws ScanningException {
		return Arrays.asList(getDevices(true));
	}

	@Override
	public Collection<DeviceInformation<?>> getDeviceInformation() throws ScanningException {
		return Arrays.asList(getDevices());
	}
	@Override
	public Collection<DeviceInformation<?>> getDeviceInformation(DeviceRole role) throws ScanningException {
		return getDeviceInformation().stream().filter(info -> info.getDeviceRole()==role).toList();
	}

	@Override
	public DeviceInformation<?> getDeviceInformation(String name) throws ScanningException {
	    DeviceRequest req;
		try {
			req = requester.post(new DeviceRequest(name));
		} catch (EventException | InterruptedException e) {
			throw new ScanningException("Cannot get devices! Connection to broker may be lost or no server up!", e);
		}
	    return req.getDeviceInformation();
	}

	@Override
	public <T> void register(IRunnableDevice<T> device) {
		throw new IllegalArgumentException("New devices may not be registered on a remote service implementation!");
	}
}
