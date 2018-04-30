/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.example.scannable;

import java.util.concurrent.Semaphore;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * An scannable whose {@link #setPosition(Number)} method blocks
 * if {@link #waitForSetPosition()} has first been called. until a resume method is called. This
 * allows us to be able to test the scan at a specific point before resuming.
 */
public class WaitingScannable extends MockScannable {

	private Semaphore semaphore = new Semaphore(1, true); // true to make semaphore fair, see javadoc

	public WaitingScannable(String name) throws InterruptedException {
		super(name, 295);
		semaphore.acquire();
	}

	@Override
	public Number setPosition(Number position, IPosition loc) throws ScanningException {
		try {
			semaphore.release(); // Notify waiting thread
			semaphore.acquire(); // Wait to be notified ourselves, note this only works because the semaphore is fair
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // shouldn't happen in test code
			throw new ScanningException(e);
		}

		return super.setPosition(position, loc);
	}

	public void waitForSetPosition() throws InterruptedException {
		// waits for setPosition() to release semaphore
		// this requires the semaphore to be fair to be work, see javadoc
		semaphore.acquire();
	}

	public void resume() {
		semaphore.release();
	}

}

