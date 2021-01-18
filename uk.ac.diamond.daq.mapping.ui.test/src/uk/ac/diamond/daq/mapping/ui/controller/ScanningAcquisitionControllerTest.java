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

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.service.IScanningAcquisitionService;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.FinderService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanningAcquisitionControllerConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanningAcquisitionControllerTest {

	private ScanningAcquisitionController controller;

	@Autowired
	FinderService finderService;

	@Autowired
	IScanningAcquisitionService scanningAcquisitionService;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/scanningAcquisitionControllerTest");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
	}

	@Before
	public void before() {
		controller = Optional.ofNullable(SpringApplicationContextFacade.getBean("scanningAcquisitionController",
													AcquisitionsPropertiesHelper.AcquisitionPropertyType.TOMOGRAPHY))
				.map(ScanningAcquisitionController.class::cast)
				.orElseGet(() -> null);
	}


	@Test
	public void testControllerWithoutNewScanningAcquisitionSupplier() {
		controller.createNewAcquisition();
		Assert.assertNotNull(controller.getAcquisition());
		Assert.assertNull(controller.getAcquisition().getAcquisitionConfiguration());
	}

	/**
	 * The new scanning acquisition without configured acquisitions types does not create the ImageCalibration document
	 */
	@Test
	public void testControllerWithNewScanningAcquisitionSupplierNoAcquistionProperties() {
		Supplier<ScanningAcquisition> newScanningAcquisitionSupplier = getScanningAcquisitionSupplier();
		controller.setDefaultNewAcquisitionSupplier(newScanningAcquisitionSupplier);
		controller.createNewAcquisition();
		Assert.assertNull(controller.getAcquisition().getAcquisitionConfiguration().getImageCalibration());
	}

	private Supplier<ScanningAcquisition> getScanningAcquisitionSupplier() {
		return () -> {
			ScanningAcquisition newConfiguration = new ScanningAcquisition();
			newConfiguration.setUuid(UUID.randomUUID());
			ScanningConfiguration configuration = new ScanningConfiguration();
			newConfiguration.setAcquisitionConfiguration(configuration);

			ScanningParameters acquisitionParameters = new ScanningParameters();
			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);
			return newConfiguration;
		};
	}
}
