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
package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.junit.jupiter.api.BeforeAll;

import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractWatchdogTest extends AbstractAcquisitionTest {

	protected static IScannableDeviceService scannableDeviceService;

	@BeforeAll
	public static void createServices() throws Exception {
		setupServices();
		scannableDeviceService = ServiceProvider.getService(IScannableDeviceService.class);
	}

}
