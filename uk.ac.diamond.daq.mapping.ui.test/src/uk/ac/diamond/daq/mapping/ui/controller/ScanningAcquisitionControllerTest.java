/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.controller;

import static java.util.Objects.requireNonNull;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;

import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanningAcquisitionControllerConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanningAcquisitionControllerTest {

	private ScanningAcquisitionController controller;

	@Before
	public void before() throws Exception {
		controller = requireNonNull((ScanningAcquisitionController) getBean("scanningAcquisitionController",
				AcquisitionsPropertiesHelper.AcquisitionPropertyType.TOMOGRAPHY));
	}

	@Test
	public void testControllerWithoutNewScanningAcquisitionSupplier() {
		controller.createNewAcquisition();
		Assert.assertNotNull(controller.getAcquisition());
		Assert.assertNull(controller.getAcquisition().getAcquisitionConfiguration());
	}

	@Test
	public void testControllerWithNewScanningAcquisitionSupplier() {
		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier =
			() -> {
				ScanningAcquisition newConfiguration = new ScanningAcquisition();
				newConfiguration.setUuid(UUID.randomUUID());
				ScanningConfiguration configuration = new ScanningConfiguration();
				newConfiguration.setAcquisitionConfiguration(configuration);
				return newConfiguration;
			};
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();
		Assert.assertNotNull(controller.getAcquisition());
		Assert.assertNotNull(controller.getAcquisition().getAcquisitionConfiguration());
	}

}
