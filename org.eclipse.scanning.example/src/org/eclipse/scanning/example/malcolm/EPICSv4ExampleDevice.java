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
package org.eclipse.scanning.example.malcolm;

import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvdatabase.pva.ChannelProviderLocalFactory;

/**
 * A functioning example Epics V4 device.
 *
 * @author Matt Taylor
 *
 */
public class EPICSv4ExampleDevice extends AbstractEPICSv4Device {

	public EPICSv4ExampleDevice(String deviceName) {
		super(deviceName);
	}

	@Override
	protected String getPvaProviderName() {
		ChannelProvider channelProvider = ChannelProviderLocalFactory.getChannelProviderLocal();
		return channelProvider.getProviderName();
	}
}
