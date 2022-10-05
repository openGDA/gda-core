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
package org.eclipse.scanning.api.device;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionerRequestHandler implements IRequestHandler<PositionerRequest> {

	private static final Map<String, Reference<IPositioner>> positioners = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(PositionerRequestHandler.class);

	public static final String CANNOT_READ_POSITION = "Error reading position";
	public static final String CANNOT_SET_POSITION = "Error setting position";
	public static final String CANNOT_CREATE_POSITIONER = "Error creating positioner";
	public static final String POSITION_NOT_REACHED = "Position not reached";

	private final IRunnableDeviceService runnableDeviceService;
	private final PositionerRequest bean;
	private final IPublisher<PositionerRequest> publisher;

	public PositionerRequestHandler(IRunnableDeviceService runnableDeviceService, PositionerRequest bean, IPublisher<PositionerRequest> statusNotifier) {
		this.runnableDeviceService = runnableDeviceService;
		this.bean = bean;
		this.publisher = statusNotifier;
	}

	@Override
	public PositionerRequest getBean() {
		return bean;
	}

	@Override
	public IPublisher<PositionerRequest> getPublisher() {
		return publisher;
	}

	@Override
	public PositionerRequest process(PositionerRequest request) throws EventException {

		IPositioner positioner;

		try {
			positioner = getPositioner(request);
		} catch (ScanningException e) {
			logger.error("Failed to create positioner for request {}", request);
			request.setErrorMessage(CANNOT_CREATE_POSITIONER + ": " + e.getMessage());
			return request;
		}

		switch (request.getRequestType()) {
		case ABORT:
			positioner.abort();
			break;
		case CLOSE:
			positioner.close();
			break;
		case GET:
			getPosition(positioner, request);
			break;
		case SET:
			setPosition(positioner, request);
			break;
		default:
			request.setErrorMessage("Unsupported request type: " + request.getRequestType().toString());
			break;
		}

		return request;
	}

	private IPositioner getPositioner(PositionerRequest request) throws ScanningException {
		final String id = request.getUniqueId();
		if (positioners.containsKey(id) && positioners.get(id).get()!=null) {
			return positioners.get(id).get();
		}

		IPositioner positioner = runnableDeviceService.createPositioner("positioner " + id);
		positioners.put(request.getUniqueId(), new SoftReference<>(positioner));
		return positioner;
	}

	private void getPosition(IPositioner positioner, PositionerRequest request) {
		try {
			request.setPosition(positioner.getPosition());
		} catch (ScanningException e) {
			request.setErrorMessage(CANNOT_READ_POSITION + ": " + e.getMessage());
		}
	}

	private void setPosition(IPositioner positioner, PositionerRequest request) {
		try {
			var position = Objects.requireNonNull(request.getPosition(), "Unspecified position");
			var completed = positioner.setPosition(position);
			if (!completed) request.setErrorMessage(POSITION_NOT_REACHED);
		} catch (InterruptedException interrupted) {
			request.setErrorMessage(CANNOT_SET_POSITION + ": " + interrupted.getMessage());
			Thread.currentThread().interrupt();

		} catch (Exception e) {
			request.setErrorMessage(CANNOT_SET_POSITION + ": " + e.getMessage());
		}
	}

}
