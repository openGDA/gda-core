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

package uk.ac.diamond.daq.mapping.api.document.handlers.processing;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;

/**
 * Tests the {@link ProcessingRequestHandlerService}
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProcessingRequestHandlerServiceTestConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProcessingRequestHandlerServiceTest {

	@Autowired
	ProcessingRequestHandlerService service;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void testUnknownRequest() {
		ProcessingRequestPair<Double> customPair = new ProcessingRequestPair<Double>() {

			@Override
			public String getKey() {
				return "WeightedProcess";
			}

			@Override
			public List<Double> getValue() {
				List<Double> weights = new ArrayList<>();
				weights.add(1.2);
				weights.add(3.7);
				return weights;
			}
		};


		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setProcessingRequest(new ProcessingRequest());
		scanRequest.getProcessingRequest().setRequest(new HashMap<>());
		service.handle(customPair, scanRequest);

		Assert.assertEquals(0, scanRequest.getProcessingRequest().getRequest().size());
	}
}
