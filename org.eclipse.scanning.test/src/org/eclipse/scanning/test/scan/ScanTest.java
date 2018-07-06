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

import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;

public class ScanTest extends AbstractScanTest {

	@Before
	public void setup() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();
		eservice = ServiceTestHelper.getEventService();
		connector = ServiceTestHelper.getScannableDeviceService();
		dservice = ServiceTestHelper.getRunnableDeviceService();
		gservice = ServiceTestHelper.getPointGeneratorService();
	}

}
