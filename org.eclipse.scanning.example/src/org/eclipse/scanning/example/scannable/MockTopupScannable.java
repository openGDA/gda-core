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
package org.eclipse.scanning.example.scannable;

import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Designed to monitor topup (pretty badly, just conceptually).
 * On a step divisible by ten, will force a wait
 * until imaginary topup value is reached.
 *
 * @author Matthew Gerring
 *
 */
public class MockTopupScannable extends MockScannable implements IConnection {

	private long start;
	private long period;
    private volatile boolean isRunning;
	private Thread thread;
	/**
	 *
	 * @param name
	 * @param period in ms that topup happens over e.g. 5000 for testing
	 */
	public MockTopupScannable(String name, long period) {
		super(name, 6000);
		setUnit("ms");
		this.period = period;
	}

	public void start() {

		if (thread!=null && isRunning) return; // We have one going.
		this.start = System.currentTimeMillis();
		this.thread = new Thread(()->{
			isRunning = true;
			try {
				while(isRunning && !Thread.interrupted()) {
					position = nextPosition();
				    delegate.firePositionChanged(getLevel(), new Scalar<Number>(getName(), -1, position));
				    Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				return; // Normal
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				isRunning = false;
			}
		}, "Topup value thread");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY+1);
		thread.start();
		System.out.println("Topup started @ 10Hz");
	}

	@Override
	public void disconnect() {
		isRunning = false;
		if (thread!=null) thread.interrupt();
		System.out.println("Topup stopped");
		this.position = 5000;
	}

	@Override
	public boolean isConnected() {
		return isRunning;
	}

	@Override
	public Number setPosition(Number position) throws ScanningException {
		return setPosition(position, null);
	}
	@Override
	public Number setPosition(Number position, IPosition loc) throws ScanningException {
		this.position = position;
	    delegate.firePositionChanged(getLevel(), new Scalar<>(getName(), -1, position));
	    return this.position;
	}

	/**
	 * Time in ms until next topup
	 */
    private Number nextPosition() {
		long diff = System.currentTimeMillis() - start;
		return period - (diff % period); // time in ms
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

}
