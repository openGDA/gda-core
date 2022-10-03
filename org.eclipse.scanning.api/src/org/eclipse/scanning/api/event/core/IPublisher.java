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
package org.eclipse.scanning.api.event.core;

import java.io.PrintStream;

import org.eclipse.scanning.api.event.EventException;


public interface IPublisher<T> extends ITopicConnection {


	/**
	 * Sends information about a specific progress using the topic to
	 * broadcast the information which is the bean (JSON encoded but if
	 * an ISubscriber is used the API user is not exposed to how the encoding
	 * works)
	 *
	 * @param bean
	 */
	public void broadcast(T bean) throws EventException;

	/**
	 * You may optionally set a logging stream on a publisher so that
	 * publications can be recorded to file for debugging.
	 *
	 * @param stream
	 */
	public void setLoggingStream(PrintStream stream);

}
