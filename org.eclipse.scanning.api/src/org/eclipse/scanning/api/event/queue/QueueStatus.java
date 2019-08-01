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
package org.eclipse.scanning.api.event.queue;

import org.eclipse.scanning.api.event.core.IJobQueue;

/**
 * An enumeration of the possible states of an {@link IJobQueue}.
 */
public enum QueueStatus {

	/**
	 * A constant to indicate that the {@link IJobQueue}'s consumer thread is running.
	 */
	RUNNING,

	/**
	 * A constant to indicate that the {@link IJobQueue}'s consumer thread is paused.
	 */
	PAUSED,

	/**
	 * A constant to indicate that the consumer is either stopped, or has not yet been started.
	 */
	STOPPED

}
