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
package org.eclipse.scanning.malcolm.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;

public class MalcolmEventDelegate {

	// listeners
	private final Set<IMalcolmEventListener> listeners = new CopyOnWriteArraySet<>();

	// Bean to contain all the settings for a given
	// scan and to hold data for scan events
	private MalcolmEvent templateBean;

	private final IMalcolmDevice<?> malcolmDevice;

	public MalcolmEventDelegate(IMalcolmDevice<?> malcolmDevice) {
		this.malcolmDevice = malcolmDevice;
	}

	/**
	 * Call to publish an event. If the topic is not opened, this call prompts the delegate to open a connection. After
	 * this the close method *must* be called.
	 *
	 * @param event
	 * @throws Exception
	 */
	public void sendEvent(MalcolmEvent event)  throws Exception {

		if (templateBean!=null) BeanMerge.merge(templateBean, event);
		fireMalcolmListeners(event);
	}


	public void addMalcolmListener(IMalcolmEventListener listener) {
		listeners.add(listener);
	}

	public void removeMalcolmListener(IMalcolmEventListener listener) {
		listeners.remove(listener);
	}

	private void fireMalcolmListeners(MalcolmEvent event) {
		for (IMalcolmEventListener listener : listeners) {
			listener.eventPerformed(event);
		}
	}

	public void sendStateChanged(DeviceState state, DeviceState old, String message) throws Exception {
		final MalcolmEvent evt = new MalcolmEvent(malcolmDevice);
		evt.setPreviousState(old);
		evt.setDeviceState(state);
		evt.setMessage(message);
		sendEvent(evt);
	}

	public void setTemplateBean(MalcolmEvent bean) {
		templateBean = bean;
	}

	public void close() {
		listeners.clear();
	}

}
