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

import org.eclipse.scanning.api.event.queue.QueueCommandBean.Command;

/**
 * States of jobs on the cluster.
 * <P><UL>
 * <LI>{@link #SUBMITTED}			Initial state & result of a {@link Command#UNDEFER}
 * <LI>{@link #PREPARING}			State while configuring devices to run before a scan
 * <LI>{@link #RUNNING}				State while actually performing a scan
 * <LI>{@link #REQUEST_PAUSE}		Transient state after a {@link Command#PAUSE_JOB}
 * <LI>{@link #PAUSED}				State after a {@link #REQUEST_PAUSE}
 * <LI>{@link #REQUEST_RESUME}		Transient state after a {@link Command#RESUME_JOB}
 * <LI>{@link #RESUMED}				State after a {@link #REQUEST_RESUME}
 * <LI>{@link #REQUEST_TERMINATE}	Transient state after a {@link Command#TERMINATE_JOB}
 * <LI>{@link #TERMINATED}			State after an abort, interrupt or a {@link #REQUEST_TERMINATE}
 * <LI>{@link #FAILED}				State after a failed run
 * <LI>{@link #FINISHING}			State while tidying up after a scan
 * <LI>{@link #COMPLETE}			State after a successful run
 * <LI>{@link #UNFINISHED}			Unused
 * <LI>{@link #DEFERRED}			State after a {@link Command#DEFER}
 * <LI>{@link #NONE}				An alternative {@link #FAILED} state, rarely used
 * </UL>
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
