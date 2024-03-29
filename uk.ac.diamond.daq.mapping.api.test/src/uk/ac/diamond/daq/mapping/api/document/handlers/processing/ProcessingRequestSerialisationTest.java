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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;

/**
 * Where we ensure all processing request implementations can be consistently de/serialised
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProcessingRequestHandlerServiceTestConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProcessingRequestSerialisationTest {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
	}

	@AfterClass
	public static void afterClass() {
		System.clearProperty(GDA_CONFIG);
	}

	@Autowired
	private DocumentMapper documentMapper;

	@Test
	public void testSavuProcessingRequest() throws Exception {

		String file1 = "file://processing/file1";
		String file2 = "file://processing/file2";

		List<URL> files = Arrays.asList(new URL(file1), new URL(file2));

		SavuProcessingRequest originalRequest = new SavuProcessingRequest.Builder()
												.withValue(files).build();

		String serialisedRequest = documentMapper.convertToJSON(originalRequest);

		SavuProcessingRequest deserialisedRequest = documentMapper.convertFromJSON(serialisedRequest, SavuProcessingRequest.class);

		assertEquals(files, deserialisedRequest.getValue());
		assertTrue(serialisedRequest.contains("\"key\" : \"" + originalRequest.getKey() + "\""));
	}

}
