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
package org.eclipse.scanning.connector.epics;

import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionEventListener;
import org.epics.pvaClient.PvaClientMonitor;

public class EpicsV4MonitorListener {

	private IMalcolmConnectionEventListener malcolmListener;
	private PvaClientMonitor monitor;

	public EpicsV4MonitorListener(IMalcolmConnectionEventListener malcolmListener, PvaClientMonitor monitor) {
		this.malcolmListener = malcolmListener;
		this.monitor = monitor;
	}

	public IMalcolmConnectionEventListener getMalcolmListener() {
		return malcolmListener;
	}

	public void setMalcolmListener(IMalcolmConnectionEventListener malcolmListener) {
		this.malcolmListener = malcolmListener;
	}

	public PvaClientMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(PvaClientMonitor monitor) {
		this.monitor = monitor;
	}

}