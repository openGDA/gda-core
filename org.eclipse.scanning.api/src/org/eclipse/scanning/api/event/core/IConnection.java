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

import org.eclipse.scanning.api.event.EventException;

/**
 * An instance of a class that implements this interfaces represents a connection of some kind that can be
 * disconnected by calling the {@link #disconnect()} method.
 * Note that there is no {@code connect()} method, the connection is assumed to be either connected when
 * created or automatically connects when necessary.
 * It is undefined whether it is possible to reconnect to a connection. Subclasses may override this.
 *
 * @author Matthew Gerring
 */
public interface IConnection {

	/**
	 * Call to disconnect any resources which we no longer need.
	 * The resource may have timed out so it might not be connected,
	 * in that case it silently returns.
	 *
	 * @throws EventException if resource could not be disconnected.
	 */
	public void disconnect() throws EventException ;

	/**
	 * Returns whether this connection is connected. This method returns <code>true</code> until
	 * disconnect is called, even if the connection is created lazily.
	 *
	 * @return <code>true</code> if connected, <code>false</code> otherwise
	 */
	public boolean isConnected();

}
