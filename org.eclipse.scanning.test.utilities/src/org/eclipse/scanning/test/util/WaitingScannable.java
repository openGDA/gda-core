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

package org.eclipse.scanning.test.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.scannable.MockScannable;

/**
 * A {@link MockScannable} whose {@link #setPosition(Number)} method blocks
 * if {@link #waitForSetPosition()} has first been called. until a resume method is called. This
 * allows us to be able to test the scan at a specific point before resuming.
 */
public class WaitingScannable extends MockScannable {

	private final WaitingRunnable runnable;
	private final AtomicBoolean shouldBlock = new AtomicBoolean(false);

	public WaitingScannable(String name, double pos) {
		super(name, pos);
		this.runnable = new WaitingRunnable();
	}
	
	public void enableBlocking() {
		shouldBlock.set(true);
	}
	
	public void disableBlocking() {
		shouldBlock.set(false);
	}
	
	public void waitForSetPosition() throws InterruptedException {
		// Wait for setPosition() to run runnable
		if (shouldBlock.get())
			runnable.waitUntilRun();
	}

	@Override
	public Number setPosition(Number position, IPosition loc) throws ScanningException {
		// Run runnable if we have to block
		if (shouldBlock.get())
			runnable.run();
		
		// Actually move the Scannable
		return super.setPosition(position, loc);
	}

	public void resume() {
		// If we're not in blocking mode, this does nothing
		runnable.release();
	}
}

