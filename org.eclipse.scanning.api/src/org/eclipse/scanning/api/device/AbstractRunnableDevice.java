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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScanAttributeContainer;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A device should create its own model when its constructor is called. This
 * can be done by reading the current hardware state for the device. In this
 * case the runnable device service does not set its model. If a given device
 * does not set its own model, when the service makes the device, it will attempt
 * to create a new empty model and set this empty model as the current model.
 * This means that the device does not have a null model and the user can get
 * the model and configure it.
 *
 * @see IRunnableDevice
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractRunnableDevice<T> implements IRunnableEventDevice<T>,
		IModelProvider<T>, IScanAttributeContainer, IActivatable {
	private static Logger logger = LoggerFactory.getLogger(AbstractRunnableDevice.class);

	// Data
	protected T model;
	private String name;
	private int level = 1;
	private String scanId;
	private DeviceState deviceState;
	private DeviceInformation<T> deviceInformation;
	private DeviceRole role = DeviceRole.HARDWARE;
	private Set<ScanMode> supportedScanModes = EnumSet.of(ScanMode.SOFTWARE);

	// OSGi services and intraprocess events
	protected IRunnableDeviceService runnableDeviceService;
	protected IScannableDeviceService connectorService;
	private IPublisher<ScanBean> publisher;

	// Listeners
	private Collection<IRunListener> runListeners;

	// Attributes
	private Map<String, Object> scanAttributes;

	private volatile boolean busy = false;
	/**
	 * Alive here is taken to represent the device being on and responding.
	 */
	private boolean alive = true;

	/**
	 * Since making the tree takes a while we measure its
	 * time and make that available to clients.
	 * It is optional if a given AbstractRunnableDevice
	 * saves the configure time.
	 */
	private long configureTime;

    /**
     * Do not make this constructor public. In order for the device
     * to be used with spring, the device service must be provided when
     * the object is constructed currently. If making this constructor
     * public please also fix the spring configuration which requires
     * the 'register' method to be called and the service to be
     * non-null.
     */
	private AbstractRunnableDevice() {
		this.scanId     = UUID.randomUUID().toString();
		this.scanAttributes = new HashMap<>();
		this.deviceState = DeviceState.READY;
	}

	/**
	 * Devices may be created during the cycle of a runnable device service being
	 * made. Therefore the parameter dservice may be null. This is acceptable
	 * because when used in spring the service is going and then the register(...)
	 * method may be used.
	 *
	 * @param dservice
	 */
	protected AbstractRunnableDevice(IRunnableDeviceService dservice) {
		this();
		setRunnableDeviceService(dservice);
	}

	/**
	 * Used by spring to register the detector with the Runnable device service
	 * *WARNING* Before calling register the detector must be given a service to
	 * register this. This can be done from the constructor super(IRunnableDeviceService)
	 * of the detector to make it easy to instantiate a no-argument detector and
	 * register it from spring.
	 */
	public void register() {
		if (runnableDeviceService==null) throw new RuntimeException("Unable to register "+getClass().getSimpleName()+" because the runnable device service was not injected correctly.");
		runnableDeviceService.register(this);
	}

	public IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		this.runnableDeviceService = runnableDeviceService;
	}

	public IScannableDeviceService getConnectorService() {
		return connectorService;
	}

	public void setConnectorService(IScannableDeviceService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
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
	public void reset() throws ScanningException {
		setDeviceState(DeviceState.READY);
	}

	/**
	 *
	 * @param nstate
	 * @param position
	 * @throws ScanningException
	 */
	protected void setDeviceState(DeviceState newDeviceState) throws ScanningException {
		final DeviceState previousDeviceState = deviceState;
		deviceState = newDeviceState;
		try {
			fireStateChanged(previousDeviceState, deviceState);
		} catch (ScanningException e) {
			logger.error("Error firing state change event ", e);
		}
	}

	@Override
	public DeviceState getDeviceState() throws ScanningException {
		return deviceState;
	}

	public String getScanId() {
		return scanId;
	}

	public void setScanId(String scanId) {
		this.scanId = scanId;
	}
	public IPublisher<ScanBean> getPublisher() {
		return publisher;
	}
	public void setPublisher(IPublisher<ScanBean> publisher) {
		this.publisher = publisher;
	}

	@Override
	public void addRunListener(IRunListener l) {
		if (runListeners==null) runListeners = Collections.synchronizedCollection(new LinkedHashSet<>());
		runListeners.add(l);
	}

	@Override
	public void removeRunListener(IRunListener l) {
		if (runListeners==null) return;
		runListeners.remove(l);
	}

	protected void fireStateChanged(DeviceState oldState, DeviceState newState) throws ScanningException {

		if (runListeners==null || runListeners.isEmpty()) return;

		final RunEvent evt = new RunEvent(this, null, newState);
		evt.setOldState(oldState);

		// Make array, avoid multi-threading issues.
		final IRunListener[] la = runListeners.toArray(new IRunListener[runListeners.size()]);
		for (IRunListener l : la) l.stateChanged(evt);
	}


	private long startTime;

	@Override
	public void fireRunWillPerform(IPosition position) throws ScanningException {
		if (runListeners==null) return;

		final RunEvent evt = new RunEvent(this, position, getDeviceState());

		// Make array, avoid multi-threading issues.
		final IRunListener[] la = runListeners.toArray(new IRunListener[runListeners.size()]);
		for (IRunListener l : la) l.runWillPerform(evt);
	}

	@Override
	public void fireRunPerformed(IPosition position) throws ScanningException {
		if (runListeners==null) return;

		final RunEvent evt = new RunEvent(this, position, getDeviceState());

		// Make array, avoid multi-threading issues.
		final IRunListener[] la = runListeners.toArray(new IRunListener[runListeners.size()]);
		for (IRunListener l : la) l.runPerformed(evt);
	}

	@Override
	public void fireWriteWillPerform(IPosition position) throws ScanningException {

		if (runListeners==null) return;

		final RunEvent evt = new RunEvent(this, position, getDeviceState());

		// Make array, avoid multi-threading issues.
		final IRunListener[] la = runListeners.toArray(new IRunListener[runListeners.size()]);
		for (IRunListener l : la) l.writeWillPerform(evt);
	}

	@Override
	public void fireWritePerformed(IPosition position) throws ScanningException {

		if (runListeners==null) return;

		final RunEvent evt = new RunEvent(this, position, getDeviceState());

		// Make array, avoid multi-threading issues.
		final IRunListener[] la = runListeners.toArray(new IRunListener[runListeners.size()]);
		for (IRunListener l : la) l.writePerformed(evt);
	}


	@Override
	public T getModel() {
		return model;
	}

	@Override
	public void setModel(T model) {
		this.model = model;
	}

	@Override
	public void configure(T model) throws ScanningException {
		this.model = model;
		setDeviceState(DeviceState.ARMED);
	}


	@Override
	public void abort() throws ScanningException, InterruptedException {

	}

	@Override
	public void disable() throws ScanningException {

	}

	@Override
	public void pause() throws ScanningException, InterruptedException {

	}

	@Override
	public void seek(int stepNumber) throws ScanningException, InterruptedException {
       // Do nothing
	}


	@Override
	public void resume() throws ScanningException, InterruptedException {

	}

	/**
	 *
	 * @return null if no attributes, otherwise collection of the names of the attributes set
	 */
	@Override
	public Set<String> getScanAttributeNames() {
		return scanAttributes.keySet();
	}

	/**
	 * Set any attribute the implementing classes may provide
	 *
	 * @param attributeName
	 *            is the name of the attribute
	 * @param value
	 *            is the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be set
	 */
	@Override
	public <A> void setScanAttribute(String attributeName, A value) throws Exception {
		scanAttributes.put(attributeName, value);
	}

	/**
	 * Get the value of the specified attribute
	 *
	 * @param attributeName
	 *            is the name of the attribute
	 * @return the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be retrieved
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <A> A getScanAttribute(String attributeName) throws Exception {
		return (A)scanAttributes.get(attributeName);
	}

	/**
	 * Do not override without calling super.getDeviceInformation()
	 * Method is final for now to help avoid that problem.
	 * @return
	 * @throws ScanningException
	 */
	public final DeviceInformation<T> getDeviceInformation() throws ScanningException {
		return getDeviceInformation(false);
	}

	/**
	 * Do not override without calling super.getDeviceInformation()
	 * Method is final for now to help avoid that problem.
	 * Gets the device information, with the ability to specify whether to get information that is potentially held
	 * on a device or not in the case that the device is not marked as being alive.
	 *
	 * @param includeNonAlive If set to false, if a device is not alive, information potentially held on the device will not be retrieved
	 * @return
	 * @throws ScanningException
	 */
	public DeviceInformation<T> getDeviceInformation(boolean includeNonAlive) throws ScanningException {
		if (deviceInformation==null) {
			deviceInformation = new DeviceInformation<T>();
		}
		deviceInformation.setModel(getModel());
		deviceInformation.setDeviceRole(getRole());
		deviceInformation.setSupportedScanModes(getSupportedScanModes());
		if (getName()!=null) deviceInformation.setName(getName());
		deviceInformation.setLevel(getLevel());
		deviceInformation.setActivated(isActivated());
		deviceInformation.setAlive(isAlive());

		// Information below may come from an actual device. Check if device is alive before attempting to get this
		if (includeNonAlive || deviceInformation.isAlive()) {
			try {
				deviceInformation.setState(getDeviceState());
				deviceInformation.setHealth(getDeviceHealth());
				deviceInformation.setBusy(isDeviceBusy());
				deviceInformation.setAlive(isAlive());
			} catch (Exception ex) {
				ex.printStackTrace();
				deviceInformation.setAlive(false);
			}
		}

		// TODO TEMPFIX for DAQ-419 before GUI updated. Just need some way of showing user if a device is offline.
		if (deviceInformation.getLabel() != null) {
			deviceInformation.setLabel(deviceInformation.getLabel().replace(" [*]","")); // Get rid of any existing non-alive flag
		}
		if (!deviceInformation.isAlive()) {
			if (deviceInformation.getLabel() != null) {
				deviceInformation.setLabel(deviceInformation.getLabel() + " [*]");
			}
			deviceInformation.setState(DeviceState.OFFLINE);
		}

		return deviceInformation;
	}

	public void setDeviceInformation(DeviceInformation<T> deviceInformation) {
		this.deviceInformation = deviceInformation;
	}

	/**
	 * If overriding don't forget the old super.validate(...)
	 */
	@Override
	public void validate(T model) throws ValidationException {
		if (model instanceof INameable) {
			INameable dmodel = (INameable)model;
		    if (dmodel.getName()==null || dmodel.getName().length()<1) {
			throw new ModelValidationException("The name must be set!", model, "name");
		    }
		}
		if (model instanceof IDetectorModel) {
			IDetectorModel dmodel = (IDetectorModel)model;
			if (dmodel.getExposureTime()<=0) throw new ModelValidationException("The exposure time for '"+getName()+"' must be non-zero!", model, "exposureTime");
		}
	}

	private boolean activated = false;

	@Override
	public boolean isActivated() {
		return activated;
	}

	@Override
	public boolean setActivated(boolean activated) {
		logger.trace("setActivated({}) was {} ({})", activated, this.activated, this);
		boolean wasactivated = this.activated;
		this.activated = activated;
		return wasactivated;
	}

	/**
	 * Please override to provide a device health (which a malcolm device will have)
	 * The default returns null.
	 * @return the current device Health.
	 */
	@Override
	public String getDeviceHealth() throws ScanningException {
		return null;
	}

	/**
	 * Gets whether the device is busy or not
	 * @return the current value of the device 'busy' flag.
	 */
	@Override
	public boolean isDeviceBusy() throws ScanningException {
		return busy;
	}

	/**
	 * Call to set the busy state while the device is running.
	 * This should not be part of IRunnableDevice, it is derived
	 * by the device when it is running or set by the scanning when
	 * it is scanning on CPU devices. This means that the creator of
	 * a Detector does not have to worry about setting it busy during
	 * scans.
	 *
	 * @param busy
	 */
	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	@Override
	public DeviceRole getRole() {
		return role;
	}

	@Override
	public void setRole(DeviceRole role) {
		this.role = role;
	}

	@Override
	public Set<ScanMode> getSupportedScanModes() {
		return supportedScanModes;
	}

	public void setSupportedScanModes(Set<ScanMode> supportedScanModes) {
		this.supportedScanModes = supportedScanModes;
	}


	protected void setSupportedScanModes(ScanMode... supportedScanModes) {
		if (supportedScanModes==null) {
			supportedScanModes = null;
			return;
		}
		this.supportedScanModes = EnumSet.of(supportedScanModes[0], supportedScanModes);
	}

	public void setSupportedScanMode(ScanMode supportedScanMode) {
		this.supportedScanModes = EnumSet.of(supportedScanMode);
	}

	public long getConfigureTime() {
		return configureTime;
	}

	public void setConfigureTime(long configureTime) {
		this.configureTime = configureTime;
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(hashCode()) +" [name=" + name + "]";
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
