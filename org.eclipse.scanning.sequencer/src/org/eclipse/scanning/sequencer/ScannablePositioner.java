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
package org.eclipse.scanning.sequencer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Positions several scannables by level, returning after all the blocking IScannable.setPosition(...)
 * methods have returned.
 *
 * @author Matthew Gerring
 *
 */
final class ScannablePositioner extends LevelRunner<IScannable<?>> implements IPositioner {

	private static final Logger logger = LoggerFactory.getLogger(ScannablePositioner.class);
	private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(3);

	private IScannableDeviceService     scannableDeviceService;
	private List<IScannable<?>>         monitors;
	private List<IScannable<?>>         scannables;

	ScannablePositioner(INameable parent) {
		super(parent);
		this.scannableDeviceService = ServiceProvider.getService(IScannableDeviceService.class);
		setCachingEnabled(false);

		// This is setting the default but the actual value of the timeout
		// is set by implementing ITimeoutable in your IScannable. The devices
		// at a given level are checked for their timeout when they are run.
		setTimeout(DEFAULT_TIMEOUT.getSeconds()); // If this needs to be increased implement getTimeout() on IScannable.
	}

	/**
	 * Objects at a given level are checked to find their maximum timeout.
	 * By default those objects will return -1 so the three minute wait time is used.
	 */
	public long getTimeout(List<IScannable<?>> objects) {
		long defaultTimeout = super.getTimeout(); // Three minutes (see above)
		if (objects==null) return defaultTimeout;

		long time = Long.MIN_VALUE;
		for (IScannable<?> device : objects) {
			time = Math.max(time, device.getTimeout());
		}
		if (time<0) time = defaultTimeout; // seconds
		return time;
	}

	@Override
	public boolean setPosition(IPosition position) throws ScanningException, InterruptedException {
		run(position);
		return true;
	}

	@Override
	public IPosition getPosition() throws ScanningException {
		if (position==null) return null;
		MapPosition ret = new MapPosition();
		for (String name : position.getNames()) {
			try {
				IScannable<?> scannable = scannableDeviceService.getScannable(name);
			    ret.put(name, scannable.getPosition());
			} catch (Exception ne) {
				throw new ScanningException("Cannot read value of "+name, ne);
			}
		}
		ret.setStepIndex(position.getStepIndex());
		return ret;
	}

	@Override
	protected Collection<IScannable<?>> getDevices() throws ScanningException {
		final List<IScannable<?>> devices = new ArrayList<>();

		if (scannables == null || scannables.isEmpty()) {
			if (position != null) {
				for (String name : position.getNames()) {
					devices.add(scannableDeviceService.getScannable(name));
				}
			}
		} else {
			devices.addAll(scannables);
		}

		if (monitors != null) {
			devices.addAll(monitors);
		}

		return devices;
	}

	@Override
	protected void doAbort() {
		// Call abort on each device
		try {
			getDevices().forEach(this::abortScannable);
		} catch (ScanningException e) {
			logger.error("Error retrieving devices", e);
		}

		shutdownThreadPool();
	}

	private void abortScannable(IScannable<?> scannable) {
		try {
			scannable.abort();
		} catch (ScanningException e) {
			logger.error("Could not abort scannable {}", scannable.getName(), e);
		} catch (InterruptedException e) { // this shouldn't happen
			logger.error("Interrupted aborting scannable {}", scannable.getName(), e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	protected Callable<IPosition> createTask(IScannable<?> scannable, IPosition position) {
		return new MoveTask(scannable, position);
	}

	private final class MoveTask implements Callable<IPosition> {

		private IScannable<?> scannable;
		private IPosition     position;

		public MoveTask(IScannable<?> iScannable, IPosition position) {
			this.scannable = iScannable;
			this.position  = position;
		}

		@Override
		public IPosition call() throws Exception {

			// Get the value in this position, may be null for monitors.
			Object value    = position.get(scannable.getName());
			Object achieved = value;
			try {
				achieved = setPosition(scannable, value, position);

			} catch (Exception ne) {
				abortWithError(scannable, value, position, ne);
				throw ne;
			}
			// achieved might not be equal to demand
			if (achieved == null) achieved = scannable.getPosition();
			return new MapPosition(scannable.getName(), position.getIndex(scannable.getName()), achieved);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Object setPosition(IScannable scannable, Object value, IPosition position) throws Exception {

			Object tolerance = scannable.getTolerance();
			if (tolerance==null || !(value instanceof Number) || !(tolerance instanceof Number)) {
				return scannable.setPosition(value, position);
			}
			Object currentValue = scannable.getPosition();
			if (!(currentValue instanceof Number)) return scannable.setPosition(value, position);

			// Check tolerance against number
			double tol = ((Number)tolerance).doubleValue();
			double cur = ((Number)currentValue).doubleValue();
			double val = ((Number)value).doubleValue();

			// If are already within tolerance return the value we are at
			if (cur<(val+tol) &&
			    cur>(val-tol)) {

				return currentValue;
			}

			// We need to move and did an extra getPosition()
			// Note sure if this is really faster, depends how
			// hardware of a given system actually works.
			return scannable.setPosition(value, position);
		}

	}

	@Override
	public List<IScannable<?>> getMonitorsPerPoint() {
		return monitors;
	}

	@Override
	public void setMonitorsPerPoint(List<IScannable<?>> monitors) {
		logger.info("setMonitorsPerPoint({}) was {} ({})", monitors, this.monitors, this);
		this.monitors = monitors;
	}

	@Override
	public void setMonitorsPerPoint(IScannable<?>... monitors) {
		logger.info("setMonitorsPerPoint({}) was {} ({})", monitors, this.monitors, this);
		this.monitors = Arrays.asList(monitors);
	}

	@Override
	public void setScannables(List<IScannable<?>> scannables) {
		this.scannables = scannables;
	}

	@Override
	protected LevelRole getLevelRole() {
		return LevelRole.MOVE;
	}

}
