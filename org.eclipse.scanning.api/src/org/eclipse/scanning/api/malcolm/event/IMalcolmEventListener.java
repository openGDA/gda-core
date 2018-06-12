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
package org.eclipse.scanning.api.malcolm.event;

import java.util.EventListener;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;

/**
 * Listeners of this type can be added to an {@link IMalcolmDevice} to listen
 * to events such as state changes and scan progress.
 */
public interface IMalcolmEventListener extends EventListener {

	/**
	 * Called when Malcolm notifies the service that something happened.
	 * @param event
	 */
	public void eventPerformed(MalcolmEvent event);

}
