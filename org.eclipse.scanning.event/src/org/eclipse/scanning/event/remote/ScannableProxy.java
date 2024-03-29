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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ITerminatable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.ILocationListener;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.event.LocationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ScannableProxy<T> extends AbstractRemoteDeviceProxy<T> implements IScannable<T>, IPositionListenable, ITerminatable, ILocationListener {

	private static final Logger logger = LoggerFactory.getLogger(ScannableProxy.class);

	private final ISubscriber<ILocationListener> subscriber;

	ScannableProxy(DeviceRequest req, URI uri, ISubscriber<ILocationListener> positionListener, IEventService eservice) throws EventException, InterruptedException {
		super(req,
			  Long.getLong("org.eclipse.scanning.event.remote.scannableTimeout", 250),
			  uri,
			  eservice);
		this.subscriber = positionListener;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getPosition() throws ScanningException {
		DeviceRequest req = update();
		if (req==null) return null;
		try {
			req.checkException();
		} catch (Exception e) {
			throw new ScanningException("Could not set position of scannable: " + getName(), e);
		}
		return (T)req.getDeviceValue();
	}

	/**
	 * Calls setPosition and waits for a response or up to a timeout (five minutes by default).
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T setPosition(T value, IPosition position) throws ScanningException {
		try {
			// Will tell us that the value is changing by recording the time of the change
			addListener();

			DeviceRequest req = new DeviceRequest(info.getName(), DeviceType.SCANNABLE);
			req.setDeviceAction(DeviceAction.SET);
			req.setDeviceValue(value);
			req.setPosition(position);
			req = requester.post(req, this::waitAgain); // Blocks until position set.
			if (req.getDeviceInformation()!=null) {
				merge((DeviceInformation<T>)req.getDeviceInformation());
			}
		} catch (Exception ne) {
			throw new ScanningException("Cannot update device info for "+info.getName(), ne);
		}
		return value;
	}


	@Override
	public void terminate(TerminationPreference pref) throws Exception {

		// Use a separate call
		IRequester<DeviceRequest> srequestor = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
		srequestor.setTimeout(100, TimeUnit.SECONDS); /** TODO How long to wait until a motor <i>should</i> be terminated? **/
		try {
			DeviceRequest req = new DeviceRequest(info.getName(), DeviceType.SCANNABLE);
			req.setDeviceAction(DeviceAction.as(pref));
			req = srequestor.post(req);
			req.checkException();
			if (req.getDeviceInformation()!=null) {
				merge((DeviceInformation<T>)req.getDeviceInformation());
			}
		} finally {
			srequestor.disconnect();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DeviceRequest update() {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(name, DeviceType.SCANNABLE));
			this.merge(req.getDeviceInformation()!=null ? (DeviceInformation<T>)req.getDeviceInformation() : this.info);
			return req;
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info, ne);
			return null;
		}
	}

	@Override
	public String getUnit() {
		if (info==null) update();  // We assume that they do not change unit.
		return info.getUnit();
	}

	/**
	 * Gets the current upper limit.
	 * @return upper limit
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getMaximum() {
		if (info==null) update();
		return (T)info.getUpper();
	}

	/**
	 * Gets the current lower limit.
	 * @return lower limit
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getMinimum() {
		if (info==null) update();
		return (T)info.getLower();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] getPermittedValues() {
		if (info == null) update();
		return (T[]) info.getPermittedValues();
	}

	private Collection<IPositionListener> listeners;

	@Override
	public void addPositionListener(IPositionListener listener) {
		addListener();
		if (listeners==null) listeners = new LinkedHashSet<>();
		listeners.add(listener);
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		if (listeners==null) return;
		listeners.remove(listener);
	}

	private boolean listenerAdded = false;
	/**
	 * A subscriber that notifies position and also resets
	 * timeouts for calls to set position, because it knows
	 * that the link is not dead.
	 */
	private void addListener() {

		if (this.listenerAdded) return;

		// We must use the name as the key to avoid too many events.
		try {
			subscriber.addListener(getName(), this);
		} catch (EventException e) {
			logger.error("Problem creating subscriber!", e);
		}
		listenerAdded = true;
	}

	private long lastActive = System.currentTimeMillis();

	@Override
	public void locationPerformed(LocationEvent evt) {
		if (listeners == null) return;
		if (listeners.isEmpty()) return;

		lastActive = System.currentTimeMillis();
		final Location      loc  = evt.getLocation();
		if (loc.getType()==null) return;

		final PositionEvent evnt = new PositionEvent(loc.getPosition(), ScannableProxy.this);
		evnt.setLevel(loc.getLevel());

		IPositionListener[] ls = listeners.toArray(new IPositionListener[listeners.size()]);
		try {
			final Method method = IPositionListener.class.getMethod(loc.getType().toString(), PositionEvent.class);
			for (IPositionListener l : ls)  {
				method.invoke(l, evnt);
			}
		} catch (Exception ne) {
			logger.error("Cannot diseminate event "+loc, ne);
		}
	}

	private boolean waitAgain() {
		final long since = System.currentTimeMillis() - lastActive;
		return since < 1000 * 60 * 2; // If a last value update was within two minutes, we wait some more.
	}

	@Override
	public boolean isActivated() {
		if (info==null) update();
		return info.isActivated();
	}

	@Override
	public boolean setActivated(boolean activated) throws ScanningException {
		if (info==null) update();
		boolean wasactivated = info.isActivated();
		logger.info("setActivated({}) was {} ({})", activated, wasactivated, this);
		method(new DeviceRequest(info.getName(), DeviceType.SCANNABLE, DeviceAction.ACTIVATE, activated));
		return wasactivated;
	}

	@Override
	// from RunnableDeviceProxy
	public void abort() throws ScanningException {
		method(new DeviceRequest(info.getName(), DeviceAction.ABORT));
	}

}
