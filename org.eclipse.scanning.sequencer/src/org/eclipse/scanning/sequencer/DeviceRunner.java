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
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Runs detectors in a task.
 *
 * This is the equivalent to the GDA8 collectData() called on multiple
 * detectors with multiple threads and waiting for isBusy to be unset.
 *
 * @author Matthew Gerring
 *
 */
class DeviceRunner extends LevelRunner<IRunnableDevice<? extends IDetectorModel>> {

	private static final Logger logger = LoggerFactory.getLogger(DeviceRunner.class);

	private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

	private Collection<IRunnableDevice<? extends IDetectorModel>>  devices;

	DeviceRunner(INameable source, Collection<IRunnableDevice<? extends IDetectorModel>> devices) {
		super(source);
		Objects.requireNonNull(devices);
		this.devices = devices;
		setTimeout(calculateTimeout(devices));
	}

	/**
	 * Calculate the timeout for the given devices. This is calculated as follows:
	 * <ul>
	 *   <li>If one of the devices is a malcolm device, then no timeout (i.e. {@link Long#MAX_VALUE});</li>
	 *   <li>Otherwise use the maxium of the timeout per device calculated as:
	 *     <ul>
	 *       <li>The timeout value for the device if specified;</li>
	 *       <li>Otherwise the exposure time.</li>
	 *     </ul>
	 *   </li>
	 *   <li>If the value calculated is less than or equal to 0, default to a timeout of 10 seconds.</li>
	 * </ul>
	 *
	 * @param devices
	 * @return
	 */
	private long calculateTimeout(Collection<IRunnableDevice<? extends IDetectorModel>> devices) {
		final long maxTimeout = devices.stream().mapToLong(this::getTimeout).max().orElse(0);
		return maxTimeout <= 0 ? 10 : maxTimeout;
	}

	private long getTimeout(IRunnableDevice<? extends IDetectorModel> device) {
		final IDetectorModel detModel = device.getModel();
		long timeout = detModel.getTimeout();
		if (timeout <= 0) {
			timeout = (long) Math.ceil(detModel.getExposureTime() + 1);
		}
		return timeout;
	}

	@Override
	protected Callable<IPosition> createTask(IRunnableDevice<? extends IDetectorModel> detector, IPosition position) {
		return new RunTask(detector, position);
	}

	@Override
	protected Collection<IRunnableDevice<? extends IDetectorModel>> getDevices() {
		return devices;
	}

	@Override
	protected void doAbort() {
		// Call abort on each device
		for (IRunnableDevice<?> device : devices) {
			try {
				device.abort();
			} catch (ScanningException e) {
				logger.error("Could not abort device {}", device.getName(), e);
			} catch (InterruptedException e) { // this shouldn't happen
				logger.error("Interrupted aborting device {}", device.getName(), e);
				Thread.currentThread().interrupt();
			}
		}

		shutdownThreadPool();
	}

	@Override
	protected void shutdownThreadPool() {
		if (!threadPool.isShutdown()) {
			threadPool.awaitQuiescence(SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
			threadPool.shutdownNow();
		}
	}

	private final class RunTask implements Callable<IPosition> {

		private IRunnableDevice<?>   detector;
		private IPosition            position;

		public RunTask(IRunnableDevice<?> detector, IPosition position) {
			this.detector = detector;
			this.position = position;
		}

		@Override
		public IPosition call() throws Exception {
			if (detector instanceof IRunnableEventDevice) {
				((IRunnableEventDevice<?>) detector).fireRunWillPerform(position);
			}
			try {
				if (detector instanceof AbstractRunnableDevice) {
					((AbstractRunnableDevice<?>) detector).setBusy(true);
				}
				logger.debug("Starting to run detector {}", detector.getName());
				detector.run(position);
			} catch (Exception ne) {
				abortWithError(detector, position, ne);
			} finally {
				logger.debug("Finished running detector {}", detector.getName());
				if (detector instanceof AbstractRunnableDevice) {
					((AbstractRunnableDevice<?>) detector).setBusy(false);
				}
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Running detector thread was interrupted");
				}
			}
			if (detector instanceof IRunnableEventDevice) {
				((IRunnableEventDevice<?>) detector).fireRunPerformed(position);
			}
			return null; // Faster if we are not adding new information.
		}
	}

	@Override
	protected LevelRole getLevelRole() {
		return LevelRole.RUN;
	}
}
