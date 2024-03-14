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

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDeviceWatchdogModel;
import org.eclipse.scanning.api.scan.ScanningException;

import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractWatchdog<T extends IDeviceWatchdogModel> implements IDeviceWatchdog<T> {

	protected T model;
	protected IDeviceController  controller;
	protected boolean active = false;

	/**
	 * Name should be set by spring as it is the mechanism by
	 * which a watchdog can be retrieved and turned on or off.
	 */
	private String name = getClass().getSimpleName();

	/**
	 * A disabled watchdog will not monitor when a scan runs.
	 */
	private boolean enabled=true;

	@Override
	public T getModel() {
		return model;
	}
	@Override
	public void setModel(T model) {
		this.model = model;
	}

	protected <S> IScannable<S> getScannable(String name) throws ScanningException {
		final IScannableDeviceService cservice = ServiceProvider.getService(IScannableDeviceService.class);
		return cservice.getScannable(name);
	}

	/**
	 * Used by spring
	 */
	@Override
	public void activate() {
		ServiceProvider.getService(IDeviceWatchdogService.class).register(this);
	}

	@Override
	public void deactivate() {
		ServiceProvider.getService(IDeviceWatchdogService.class).unregister(this);
	}

	public IDeviceController getController() {
		return controller;
	}

	@Override
	public void setController(IDeviceController controller) {
		this.controller = controller;
	}

	@ScanStart
	public void scanStarted() {
		active = true;
	}

	@ScanFinally
	public void scanFinally() {
		active = false;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}