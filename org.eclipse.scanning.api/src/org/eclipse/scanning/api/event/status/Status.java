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
package org.eclipse.scanning.api.event.status;

/**
 * States of jobs on the cluster.
 *
 * @author Matthew Gerring
 *
 */
public enum Status {

	SUBMITTED,
	PREPARING,
	RUNNING,
	REQUEST_PAUSE,
	PAUSED,
	REQUEST_RESUME,
	RESUMED,
	REQUEST_TERMINATE,
	TERMINATED,
	FAILED,
	FINISHING,
	COMPLETE,
	UNFINISHED,
	DEFERRED,
	NONE;

	/**
	 *
	 * @return true if the run was taken from the queue and something was actually executed on it.
	 */
	public boolean isStarted() {
		return this!=SUBMITTED;
	}

	public boolean isFinal() {
		return this==TERMINATED || this==FAILED || this==COMPLETE || this==UNFINISHED || this==NONE;
	}

	public boolean isRunning() {
		return this==RUNNING || this.isResumed() || this==PREPARING || this==FINISHING;
	}

	public boolean isRequest() {
		return toString().startsWith("REQUEST_");
	}

	public boolean isPaused() {
		return this==REQUEST_PAUSE || this==PAUSED || this==DEFERRED;
	}

	public boolean isResumed() {
		return this==REQUEST_RESUME || this==RESUMED;
	}

	/**
	 * Being actively run, including pause.
	 * @return
	 */
	public boolean isActive() {
		return (isRunning() || isPaused()) && !isFinal() && this!=DEFERRED;
	}

	public boolean isTerminated() {
		return this==REQUEST_TERMINATE || this==TERMINATED;
	}
}
