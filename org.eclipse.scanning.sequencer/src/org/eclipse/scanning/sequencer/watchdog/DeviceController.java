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
package org.eclipse.scanning.sequencer.watchdog;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * <pre>
	  Rules for controller:
	  o Pause proceeds and sets preference to pause.
	  o Resume is allowed if none of the states are paused.
	  o Seek is allowed if all names bar the current device are not paused.
	  o Abort is allowed regardless.
   </pre>

 * @author Matthew Gerring
 *
 * @param <T>
 */
class DeviceController implements IDeviceController {

	private static Logger logger = LoggerFactory.getLogger(DeviceController.class);

	private IPausableDevice<?> device;
	private List<?>            objects;
	private ScanBean           bean;

	/**
	 * Rules for states:
	 * Pause proceeds and sets preference to pause.
	 * Resume is allowed if none of the states are paused.
	 * Seek is allowed if all names bar the current device are not paused.
	 * Abort is allowed regardless.
	 */
	private Map<String, DeviceState>         states;
	private Map<String, DeviceWatchdogModel> models;

	public DeviceController(IPausableDevice<?> device) {
		this.device = device;
		this.states = Collections.synchronizedMap(new HashMap<>(3));
		this.models = Collections.synchronizedMap(new HashMap<>(3));
	}

	/**
	 * Only pauses the delegate if is running, otherwise returns
	 * silently.
	 */
	@Override
	public void pause(String id, DeviceWatchdogModel model) throws ScanningException, InterruptedException {
		states.put(id, DeviceState.PAUSED);
		models.put(id, model); // May be null
		if (device.getDeviceState() != DeviceState.RUNNING) {
			logger.debug("Controller ignoring pause request from {} as device state is {}, expected {}",
					id, device.getDeviceState(), DeviceState.RUNNING);
			return; // Cannot pause it.
		}

		if (bean != null && model != null) {
			bean.setMessage(model.getMessage());
		}
		logger.info("Controller pausing on {} because of id {}", getName(), id);
		device.pause();
	}

	@Override
	public void seek(String id, int stepNumber) throws ScanningException, InterruptedException {

		// If any of the others think it should be paused, we do not resume
		Map<String, DeviceState> copy = new HashMap<>(states);
		copy.put(id, DeviceState.RUNNING);
		if (device.getDeviceState()!=DeviceState.PAUSED) return; // Cannot seek it.
		if (!canResume(copy)) return;

		device.seek(stepNumber);
	}

	@Override
	public void resume(String id) throws ScanningException, InterruptedException {

		states.put(id, DeviceState.RUNNING);
		if (device.getDeviceState()!=DeviceState.PAUSED) return; // Cannot resume it.

		// If any of the others think it should be paused, we do not resume
		if (canResume(states)) {
			logger.debug("Controller resuming on {} because of id {}", getName(), id);
			device.resume();
		} else {
			// Attempt to set a message in the bean about why.
			if (getBean()!=null) {
				// set the message of the bean to that of the first paused watchdog
				states.entrySet().stream()
					.filter(entry -> entry.getValue() == DeviceState.PAUSED)
					.map(Map.Entry::getKey) // get id of first paused watchdog
					.map(models::get).filter(Objects::nonNull).findFirst()
					.map(DeviceWatchdogModel::getMessage)
					.ifPresent(message -> getBean().setMessage(message));
			}
		}
	}

	private static final boolean canResume(Map<String, DeviceState> states) {
		// we can resume if none of the states are PAUSED
		return states.values().stream().noneMatch(state -> state == DeviceState.PAUSED);
	}

	@Override
	public void abort(String id) throws ScanningException, InterruptedException {
		logger.debug("Controller aborting on {} because of id {}", getName(), id);
		device.abort();
	}

	@Override
	public IPausableDevice<?> getDevice() {
		return device;
	}

	public void setDevice(IPausableDevice<?> device) {
		this.device = device;
	}

	@Override
	public List<?> getObjects() {
		return objects;
	}

	public void setObjects(List<?> objects) {
		this.objects = objects;
	}

	@Override
	public boolean isActive() {
		boolean is = true;
		for (Object object : objects) {
			if (object instanceof IDeviceWatchdog) is = is && ((IDeviceWatchdog)object).isActive();
		}
		return is;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	public ScanBean getBean() {
		return bean;
	}

	public void setBean(ScanBean bean) {
		this.bean = bean;
	}
}