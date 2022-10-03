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

/**
 * This Epics Device fails to start with the error {@code WARNING: Channel provider
 * with name 'evil' not available.}
 * <p>
 * This is because the Pva provider name is invalid.
 *
 * @author Matt Taylor
 *
 */
public class EPICSv4EvilDevice extends AbstractEPICSv4Device {

	public EPICSv4EvilDevice(String deviceName) {
		super(deviceName);
	}

	@Override
	protected String getPvaProviderName() {
		return "evil";
	}
}