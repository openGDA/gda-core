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

import java.util.Collection;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ITerminatable;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * TODO FIXME Is the idea of having request/response calls correct for exposing
 * services on the server to the client?
 * Disadvantages:
 * 1. Pushes to much to client because it has make/receive request/responses.
 * 2. Lot of specific code around a given service in the client. Would be nicer to discover services
 *    using the original service directly, but it going back to the server...
 *
 * Advantages:
 * 1. Can work for any client, python and javascript included
 * 2. No JSON serialization issues
 *
 *
 * @author Matthew Gerring
 *
 */
public class DeviceRequestHandler implements IRequestHandler<DeviceRequest> {

	private IRunnableDeviceService    dservice;
	private DeviceRequest             bean;
	private IPublisher<DeviceRequest> publisher;
	private IScannableDeviceService   cservice;

	public DeviceRequestHandler(IRunnableDeviceService  dservice,
			              IScannableDeviceService cservice,
			              DeviceRequest           bean,
			              IPublisher<DeviceRequest> statusNotifier) {

		this.dservice = dservice;
		this.cservice = cservice;
		this.bean     = bean;
		this.publisher = statusNotifier;
	}

	@Override
	public DeviceRequest getBean() {
		return bean;
	}

	@Override
	public IPublisher<DeviceRequest> getPublisher() {
		return publisher;
	}

	@Override
	public DeviceRequest process(DeviceRequest request) {
		try {
			if (request.getDeviceType()==DeviceType.SCANNABLE) {
				processScannables(request, cservice);
			} else {
				processRunnables(request, dservice);
			}
			return request;

		} catch (ModelValidationException ne) {
			DeviceRequest error = new DeviceRequest();
			error.merge(request);
			error.setErrorMessage(ne.getMessage());
			error.setErrorFieldNames(ne.getFieldNames());
			return error;

		} catch (Exception ne) {
			ne.printStackTrace();
			DeviceRequest error = new DeviceRequest();
			error.merge(request);
			error.setErrorMessage(ne.getMessage());
			return error;
		}
	}

	private static void processScannables(DeviceRequest request, IScannableDeviceService cservice) throws Exception {

		if (request.getDeviceName()!=null) { // Named device required

			IScannable<Object> device = cservice.getScannable(request.getDeviceName());
			DeviceAction action = request.getDeviceAction();
			if (action==DeviceAction.SET && request.getDeviceValue()!=null) {
				device.setPosition(request.getDeviceValue(), request.getPosition());
				/* This thread is executing to set position, while it does that it
				 * sends events over AMQ. These events queue up with no higher priority
				 * than this call. Therefore if we return immediately it is possible
				 * for this call to return before position events are sent out.
				 * FUDGE warning
				 */
				Thread.sleep(10);
				 /* End warning */
			} else if (action==DeviceAction.ACTIVATE) {
				device.setActivated((Boolean)request.getDeviceValue());
			}


			if (action!=null && action.isTerminate() && device instanceof ITerminatable) {
				ITerminatable tdevice = (ITerminatable)device;
				tdevice.terminate(action.to());
			}

			request.setDeviceValue(device.getPosition());

			DeviceInformation<?> info = new DeviceInformation<Object>(device.getName());
			merge(info, device);
			request.addDeviceInformation(info);

		} else {
			final Collection<String> scannableNames = cservice.getScannableNames();
			scannableNames.stream().map(DeviceInformation::new).forEach(request::addDeviceInformation);
		}
	}


	private static void merge(DeviceInformation<?> info, IScannable<?> device) throws Exception {
		info.setLevel(device.getLevel());
		info.setUnit(device.getUnit());
		info.setUpper(device.getMaximum());
		info.setLower(device.getMinimum());
		info.setPermittedValues(device.getPermittedValues());
		info.setActivated(device.isActivated());
	}

	private static void processRunnables(DeviceRequest request, IRunnableDeviceService dservice) throws Exception {

		if (request.getDeviceName()!=null) { // Named device required
			IRunnableDevice<Object> device = dservice.getRunnableDevice(request.getDeviceName());
			if (device==null) throw new EventException("There is no created device called '"+request.getDeviceName()+"'");

			// TODO We should have a much more reflection based way of
			// calling arbitrary methods.
			if (request.getDeviceAction()!=null) {
				DeviceAction action = request.getDeviceAction();
				if (action.isTerminate() && device instanceof ITerminatable) {
					ITerminatable tdevice = (ITerminatable)device;
					tdevice.terminate(action.to());
				} else if (action==DeviceAction.VALIDATE) {
					device.validate(request.getDeviceModel());
				} else if (action==DeviceAction.VALIDATEWITHRETURN) {
					request.setDeviceValue(device.validate(request.getDeviceModel()));
				} else if (action==DeviceAction.CONFIGURE) {
					device.configure(request.getDeviceModel());
				} else if (action==DeviceAction.RUN) {
					device.run(request.getPosition());
				} else if (action==DeviceAction.ABORT) {
					device.abort();
				} else if (action==DeviceAction.RESET) {
					device.reset();
				} else if (action==DeviceAction.ACTIVATE) {
					if (device instanceof IActivatable) {
						IActivatable adevice = (IActivatable)device;
						adevice.setActivated((Boolean)request.getDeviceValue());
					} else {
						throw new EventException("The device '"+device.getName()+"' is not "+IActivatable.class.getSimpleName());
					}
				}
			}

			addDeviceInformation(device, request);
		} else {  // Device list needed.

			Collection<DeviceInformation<?>> info;
			if (request.isIncludeNonAlive()) {
				info = dservice.getDeviceInformationIncludingNonAlive();
			} else {
				info = dservice.getDeviceInformation();
			}
			request.setDevices(info);
		}
	}

	private static void addDeviceInformation(
			IRunnableDevice<?> device, DeviceRequest request) throws ScanningException {
		// get the device information and set it in the request
		DeviceInformation<?> info = ((AbstractRunnableDevice<?>)device).getDeviceInformation();
		request.addDeviceInformation(info);

		if (device instanceof IMalcolmDevice && request.isGetDatasets()) {
			// convert the list of attributes to a map and set in the request
			request.setDatasets(((IMalcolmDevice) device).getDatasets());
		}
	}

}
