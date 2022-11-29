/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument.Axis;
import uk.ac.diamond.daq.mapping.ui.experiment.controller.ExperimentScanningAcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.client.AcquisitionManager;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplate;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.rest.ExperimentControllerServiceClient;
import uk.ac.gda.ui.tool.rest.ScanningAcquisitionRestServiceClient;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Base class for ScanningAcquisitionController integration tests.
 *
 * Child test classes must inject the relevant GDA configuration file e.g.
 * <pre>
{@code	@BeforeClass}
public static void beforeClass() {
	System.setProperty(GDA_CONFIG, "test/resources/scanningAcquisitionControllerTest");
}
 * </pre>
 * They may also wish to call {@link #injectAcquisitionManager(AcquisitionController, AcquisitionKeys)}
 * before invoking any ScanningAcquisitionController methods.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanningAcquisitionControllerConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class ScanningAcquisitionControllerIntegrationTest {

	static final String BASE_X = "base_x";
	static final String EH_SHUTTER = "eh_shutter";

	@Autowired
	ScanningAcquisitionRestServiceClient scanningAcquisitionServer;

	@Autowired
	FinderService finderService;

	@Autowired
	ClientRemoteServices clientRemoteService;

	@Autowired
	ClientSpringContext context;

	@Autowired
	ExperimentControllerServiceClient experimentClient;

	@Autowired
	PositionManager positionManager;

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
	}

	@Before
	public void before() {
		LocalProperties.reloadAllProperties();
	}

	/**
	 * Hack to inject OSGi service AcquisitionManager into Spring component in Spring integration test
	 */
	void injectAcquisitionManager(AcquisitionController<ScanningAcquisition> controller, AcquisitionKeys keys) {
		ScanningAcquisitionController scanningController = null;
		if (controller instanceof ScanningAcquisitionController) {
			scanningController = (ScanningAcquisitionController) controller;
		} else if (controller instanceof ExperimentScanningAcquisitionController) {
			var experimentController = (ExperimentScanningAcquisitionController) controller;
			scanningController = (ScanningAcquisitionController) experimentController.getAcquisitionController();
		} else {
			throw new RuntimeException("I don't know how to inject AcquisitionManager in ScanningAcquisitionController");
		}

		var template = new AcquisitionTemplate();
		template.setType(keys.getPropertyType());
		template.setSubType(keys.getSubType());
		var path = new ScannableTrackDocument.Builder()
				.withAxis(Axis.THETA)
				.withScannable("gts_theta")
				.withStart(0)
				.withStop(180)
				.withPoints(200).build();
		template.setDefaultPaths(List.of(path));

		var engine = new AcquisitionEngineDocument();
		engine.setType(AcquisitionEngineType.MALCOLM);
		template.setEngine(engine);

		template.setDetectors(Set.of("PCO_CAMERA"));

		template.setStartPosition(Collections.emptyList());

		var manager = new AcquisitionManager(List.of(template));
		scanningController.setAcquisitionManager(manager);

		ReflectionTestUtils.setField(positionManager, "acquisitionManager", manager);
	}

}
