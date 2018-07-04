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

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;

public class Services {

	private static IRunnableDeviceService runnableDeviceService;

	private static IMalcolmConnection connectorService;

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		Services.runnableDeviceService = runnableDeviceService;
	}

	public static IMalcolmConnection getConnectorService() {
		return Services.connectorService;
	}

	public void setConnectorService(IMalcolmConnection connectorService) {
		Services.connectorService = connectorService;
	}

}
