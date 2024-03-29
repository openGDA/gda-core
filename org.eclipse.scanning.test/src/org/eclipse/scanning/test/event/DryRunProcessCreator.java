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
package org.eclipse.scanning.test.event;

import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * An {@link IProcessCreator} whose {@link IProcessCreator#createProcess(Object, IPublisher)}
 * method creates and returns a {@link DryRunProcess}, a process that simulates a scan from
 * the point of view of a job queue.
 * @param <T>
 */
public class DryRunProcessCreator<T extends StatusBean> implements IProcessCreator<T> {

	private boolean blocking;
	private long sleep;
	private int start, stop, step;

	public DryRunProcessCreator() {
		this(true);
	}

	public DryRunProcessCreator(boolean blocking) {
		this(50, blocking);
	}
	public DryRunProcessCreator(long sleep, boolean blocking) {
		this(0, 100, 10, sleep, blocking);
	}
	public DryRunProcessCreator(int start, int stop, int step, long sleep, boolean blocking) {
		this.sleep = sleep;
		this.blocking = blocking;
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	@Override
	public IBeanProcess<T> createProcess(T bean, IPublisher<T> statusNotifier) {
		System.out.println("Creating process for name = "+bean.getName()+" id = "+bean.getUniqueId());
		return new DryRunProcess<T>(bean, statusNotifier, blocking, start, stop, step, sleep);
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
