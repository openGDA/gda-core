/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class DummyCallable implements Callable<Object> {

	CountDownLatch readySignal;
	private final Object toReturn;
	private final String id;

	public DummyCallable(String id, Object toReturn, Boolean startReady) {
		this.id = id;
		this.toReturn = toReturn;
		readySignal = startReady ? new CountDownLatch(0) : new CountDownLatch(1);
	}

	@Override
	public Object call() throws Exception {
		readySignal.await();
		return toReturn;
	}

	public void makeReady() {
		System.out.println("Making DummyCallable: " + id + " ready");
		readySignal.countDown();
	}

	public Boolean isReady() {
		return (readySignal.getCount()==0);
	}
	
	@Override
	public String toString() {
		return "DummyCallable: " + id + (isReady() ? " is ready" : " is not ready");
	}
}