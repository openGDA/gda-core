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
import java.util.Set;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.device.IActivatable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RunnableDeviceProxy<M> extends AbstractRemoteDeviceProxy<M> implements IRunnableDevice<M>, IActivatable {

	private static final Logger logger = LoggerFactory.getLogger(RunnableDeviceProxy.class);

	RunnableDeviceProxy(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		super(req,
			  Long.getLong("org.eclipse.scanning.event.remote.runnableDeviceTimeout", 1000),
			  uri,
			  eservice);
	}

	RunnableDeviceProxy(DeviceInformation<M> info, URI uri, IEventService eventService) throws EventException {
		super(info,
			  Long.getLong("org.eclipse.scanning.event.remote.runnableDeviceTimeout", 1000),
			  uri,
			  eventService);
	}

	RunnableDeviceProxy(DeviceInformation<M> info, long timeoutMs, URI uri, IEventService eventService) throws EventException {
		super(info, timeoutMs, uri, eventService);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure(M model) throws ScanningException {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName(), DeviceAction.CONFIGURE, model));
			merge((DeviceInformation<M>)req.getDeviceInformation());
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	@Override
	public M getModel() {
		update();
		return info.getModel();
	}

	@Override
	public String getDeviceHealth() throws ScanningException {
		update();
		return info.getHealth();
	}

	@Override
	public boolean isDeviceBusy() throws ScanningException {
		update();
		return info.isBusy();
	}

	@Override
	public DeviceRole getRole() {
		update();
		return info.getDeviceRole();
	}

	@Override
	public void setRole(DeviceRole role) {
		throw new IllegalArgumentException("Clients may not change the role of devices!");
	}

	@Override
	public Set<ScanMode> getSupportedScanModes() {
		update();
		return info.getSupportedScanModes();
	}

	@SuppressWarnings("unchecked")
	@Override
	public M validate(M model) throws ValidationException {
		try {
			DeviceRequest res = requester.post(new DeviceRequest(info.getName(), DeviceAction.VALIDATEWITHRETURN, model));
			res.checkException();
			return (M) res.getDeviceValue();
		} catch (ValidationException ve) {
			throw ve;
		} catch (Exception ne) {
			throw new ValidationException(ne);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DeviceState getDeviceState() throws ScanningException {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(name));
			merge((DeviceInformation<M>)req.getDeviceInformation());
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		return info.getState();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DeviceRequest update() {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(name));
			merge((DeviceInformation<M>)req.getDeviceInformation());
			return req;
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
			return null;
		}
	}

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		DeviceRequest req = new DeviceRequest(info.getName(), DeviceAction.RUN);
		req.setPosition(position);
		method(req);
	}

	@Override
	public void reset() throws ScanningException {
		method(new DeviceRequest(info.getName(), DeviceAction.RESET));
	}

	@Override
	public void abort() throws ScanningException {
		method(new DeviceRequest(info.getName(), DeviceAction.ABORT));
	}

	@Override
	public void disable() throws ScanningException {
		method(new DeviceRequest(info.getName(), DeviceAction.DISABLE));
	}

	@Override
	public boolean isActivated() {
		update();
		return info.isActivated();
	}

	@Override
	public boolean setActivated(boolean activated) throws ScanningException {
		if (info==null) update();
		boolean wasactivated = info.isActivated();
		logger.info("setActivated({}) was {} ({})", activated, wasactivated, this);
		method(new DeviceRequest(info.getName(), DeviceType.RUNNABLE, DeviceAction.ACTIVATE, activated));
		return wasactivated;
	}

	@Override
	public boolean isAlive() {
		return info.isAlive();
	}

	@Override
	public void setAlive(boolean alive) {
		info.setAlive(alive);
	}
}
