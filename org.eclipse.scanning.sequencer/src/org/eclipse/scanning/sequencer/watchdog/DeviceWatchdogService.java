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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceWatchdogService implements IDeviceWatchdogService {

	private static Logger logger = LoggerFactory.getLogger(DeviceWatchdogService.class);

	private static final String WATCHDOGS_ACTIVE_PROPERTY_NAME = "org.eclipse.scanning.watchdogs.active";

	private Map<String, IDeviceWatchdog> templates = Collections.synchronizedMap(new LinkedHashMap<>(3));

	static {
		if (System.getProperty(WATCHDOGS_ACTIVE_PROPERTY_NAME)==null) {
			System.setProperty(WATCHDOGS_ACTIVE_PROPERTY_NAME, "true");
		}
	}

	@Override
	public void register(IDeviceWatchdog dog) {
		if (templates.containsKey(dog.getName()))
			throw new IllegalArgumentException("The watchdog name '"+dog.getName()+"' is already registered! A watchdog with a given name may only be registered once.");
		templates.put(dog.getName(), dog);
	}

	@Override
	public void unregister(IDeviceWatchdog dog) {
		templates.remove(dog.getName());
	}

	@Override
	public IDeviceController create(IPausableDevice<?> device, ScanBean scanBean) {
		if (!Boolean.getBoolean(WATCHDOGS_ACTIVE_PROPERTY_NAME)) return null;
		if (templates == null)
			return null;

		try {
			DeviceController controller = new DeviceController(device);
			final List<IDeviceWatchdog> watchdogs = templates.values().stream()
				.filter(IDeviceWatchdog::isEnabled)
				.map(this::cloneWatchdogTemplate)
				.filter(Objects::nonNull)
				.peek(watchdog -> watchdog.setController(controller))
				.collect(toList());
			controller.setObjects(watchdogs);
			return controller;
		} catch (Exception ne) {
			ne.printStackTrace();
			logger.error("Cannot create watchdogs", ne);
			return null;
		}
	}

	private IDeviceWatchdog cloneWatchdogTemplate(IDeviceWatchdog template) {
		try {
			IDeviceWatchdog newWatchdog = template.getClass().newInstance();
			newWatchdog.setModel(template.getModel());
			return newWatchdog;
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Could not create watchdog {}", template.getName(), e);
			return null;
		}
	}

	@Override
	public IDeviceWatchdog getWatchdog(String name) {
		return templates.get(name);
	}

	@Override
	public List<String> getRegisteredNames() {
		return new ArrayList<>(templates.keySet());
	}

}
