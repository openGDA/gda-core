/*-
 * Copyright © 2021 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.scannable;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.python.core.Py;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.concurrent.Async.ListeningFuture;

public class RestrictedScannableManager {
	private static final Logger logger = LoggerFactory.getLogger(RestrictedScannableManager.class);

	public static class ParkableScannable {
		private Scannable scannable;
		private Object parkPosition;
		public ParkableScannable(Scannable scannable, Object park) {
			this.scannable = scannable;
			this.parkPosition = park;
		}
		private Callable<Boolean> parkJob() {
			return () -> {
				if (scannable.isBusy()) {
					throw new DeviceException("Scannable could not be parked while busy");
				}
				if (!scannable.isAt(parkPosition)) {
					InterfaceProvider.getTerminalPrinter().print(
							"Moving " + scannable.getName()
							+ " to park position (" + parkPosition + ") to allow other devices to move");
				}
				scannable.moveTo(parkPosition);
				return scannable.isAt(parkPosition);
			};
		}
	}


	private Map<String, ParkableScannable> scannables;
	private ReentrantLock baton = new ReentrantLock();

	public Scannable getScannable(String name) {
		return new RestrictedScannable(scannables.get(name).scannable);
	}

	public void setScannables(List<ParkableScannable> scannables) {
		try {
			if (baton.tryLock()) {
				this.scannables = scannables.stream()
						.collect(toMap(s -> s.scannable.getName(), s -> s));
			} else {
				throw new IllegalStateException("Cannot set scannables when currently set scannables are moving");
			}
		} finally {
			if (baton.isHeldByCurrentThread()) {
				baton.unlock();
			}
		}
	}

	private void prepareForMove(String name) throws DeviceException {
		if (!baton.isHeldByCurrentThread()) {
			throw new IllegalStateException("prepareForMove must be called by the thread that initiated the move");
		}
		logger.debug("Parking other scannables");
		List<Callable<Boolean>> moves = scannables.entrySet()
				.stream()
				.filter(e -> !e.getKey().equals(name))
				.map(e -> e.getValue().parkJob())
				.collect(toList());

		var move = Async.submitAll(moves);
		try {
			var results = move.get();
			for (var result: results) {
				var success = result.get();
				if (success == null || !success) {
					throw new DeviceException("Failed to park devices");
				}
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Interrupted while waiting for other devices to park");
		} catch (ExecutionException ee) {
			throw new DeviceException("Error parking other devices", ee.getCause());
		}
	}

	public final class RestrictedScannable extends PassthroughScannableDecorator {
		/**
		 * Flag to indicate that an async move has been requested. This prevents other
		 * scannables in this group being moved between an async move being started and the
		 * move starting. It also allows the device to appear as busy even though it may not
		 * yet be moving (eg waiting for other devices to park).
		 */
		AtomicBoolean moving = new AtomicBoolean();

		private RestrictedScannable(Scannable delegate) {
			super(delegate);
		}

		@Override
		public void moveTo(Object position) throws DeviceException {
			try {
				if (baton.tryLock()) {
					logger.info("Locked by {}", getName());
					prepareForMove(getName());
					super.moveTo(position);
				} else {
					throw new DeviceException("Cannot move " + getName() + " as other linked scannables are moving");
				}
			} finally {
				if (baton.isHeldByCurrentThread()) {
					baton.unlock();
				}
			}
		}

		@Override
		public void asynchronousMoveTo(Object position) throws DeviceException {
			if (moving.compareAndSet(false, true)) {
				ListeningFuture<?> move = Async.submit(() -> {
					try {
						moveTo(position);
					} catch (DeviceException e) {
						logger.error("Error moving device: {}", getName(), e);
					}
				});
				move.onComplete(() -> moving.set(false));
			} else {
				throw new DeviceException(getName() + " - is already moving");
			}
		}

		@Override
		public void waitWhileBusy() throws DeviceException, InterruptedException {
			while (isBusy()) {
				Thread.sleep(200);
			}
			super.waitWhileBusy();
		}

		@Override
		public String toString() {
			return super.toFormattedString();
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return baton.isLocked() || moving.get() || super.isBusy();
		}

		public Object __call__() throws DeviceException {
			return super.getPosition();
		}

		public PyObject __call__(PyObject position) throws DeviceException {
			moveTo(position);
			return Py.None;
		}
	}
}
