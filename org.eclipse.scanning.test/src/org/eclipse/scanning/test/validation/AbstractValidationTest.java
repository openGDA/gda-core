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
package org.eclipse.scanning.test.validation;

import java.io.File;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.server.application.PseudoSpringParser;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;

public abstract class AbstractValidationTest {

    protected ValidatorService validator;

	@Before
	public void before() throws Exception {
		ServiceTestHelper.setupServices();

		validator = ServiceTestHelper.getValidatorService();

		// Make a few detectors and models...
		PseudoSpringParser parser = new PseudoSpringParser();
		parser.parse(getClass().getResourceAsStream("test_detectors.xml"));

		IRunnableDevice<DummyMalcolmModel> device = ServiceTestHelper.getRunnableDeviceService().getRunnableDevice("malcolm");

		// Just for testing we give it a dir.
		File dir = File.createTempFile("fred", ".nxs").getParentFile();
		((IMalcolmDevice<?>)device).setFileDir(dir.getAbsolutePath());

		// Just for testing, we make the detector legal.
		IMalcolmDevice<?> mdevice = (IMalcolmDevice<?>)device;
		GridModel gmodel = new GridModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		// Cannot set the generator from @PreConfigure in this unit test.
		mdevice.setPointGenerator(ServiceTestHelper.getPointGeneratorService().createGenerator(gmodel));
	}

}
