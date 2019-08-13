/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.tomography.scan.tests;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gda.device.Device;
import gda.device.motor.DummyMotor;
import uk.ac.gda.tomography.service.TomographyServiceException;
import uk.ac.gda.tomography.ui.controller.TomographyPerspectiveController;

public class TomographyPerspectiveControllerTest {

	private File tempFile;
	private ObjectMapper mapper;

	@Before
	public void before() throws TomographyServiceException {
		mapper = new ObjectMapper();
	}

	@After
	public void after() throws TomographyServiceException {

	}

	/**
	 * Sets the acquisition devices as per the default stage.
	 */
	@Test
	public void defaultStage() {
		TomographyPerspectiveController controller = new TomographyPerspectiveController();
		controller.getTomographyAcquisitionController();
		Assert.assertTrue(controller.getTomographyAcquisitionController().getAcquisition().getAcquisitionConfiguration().getDevices().isEmpty());
	}

	/**
	 * Updates the acquisition devices
	 */
	@Test
	public void changeStages() {
		TomographyPerspectiveController controller = new TomographyPerspectiveController();
		Set<Device> oldDevices = new HashSet<>();
		Device oldDev = new DummyMotor();
		oldDevices.add(oldDev);
		controller.getTomographyAcquisitionController().getAcquisition().getAcquisitionConfiguration().setDevices(oldDevices);

		Set<Device> newDevices = new HashSet<>();
		Device newDev = new DummyMotor();
		newDevices.add(newDev);
		controller.getTomographyAcquisitionController().updateDevices(oldDevices, newDevices);
		Assert.assertFalse(controller.getTomographyAcquisitionController().getAcquisition().getAcquisitionConfiguration().getDevices().isEmpty());
		Assert.assertTrue(controller.getTomographyAcquisitionController().getAcquisition().getAcquisitionConfiguration().getDevices().contains(newDev));
		Assert.assertFalse(controller.getTomographyAcquisitionController().getAcquisition().getAcquisitionConfiguration().getDevices().contains(oldDev));
	}

}
