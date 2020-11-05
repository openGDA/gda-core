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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.device.IScannableMotor;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;

/**
 * Tests the {@link ProcessingRequestHandlerService}
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProcessingRequestHandlerServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProcessingRequestHandlerServiceTest {

	@Autowired
	ProcessingRequestHandlerService service;

	/**
	 * Verifies the service can handle {@link IScannableMotor} devices
	 * @throws MalformedURLException
	 */
	@Test
	public void testSavuProcessingRequest() throws  MalformedURLException {
		String file1 = "file://lev1/lev2";
		String file2 = "file://lev3/lev4";
		List<URL> paths = new ArrayList<>();
		paths.add(new URL(file1));
		paths.add(new URL(file2));
		SavuProcessingRequest request = new SavuProcessingRequest.Builder()
			.withValue(paths)
			.build();

		Collection<Object> outCollection = service.translateToCollection(request);
		Assert.assertEquals(2, outCollection.size());
		outCollection.forEach(
				e ->
					Assert.assertTrue(e.equals(file1) || e.equals(file2))
				);
	}

	/**
	 * Verifies the service can handle {@link IScannableMotor} devices
	 */
	@Test
	public void testUnknownRequest() {
		ProcessingRequestPair<Double> customPair = new ProcessingRequestPair<Double>() {

			@Override
			public String getKey() {
				return "WeightedProcess";
			}

			@Override
			public Collection<Double> getValue() {
				Set<Double> weights = new HashSet<>();
				weights.add(1.2);
				weights.add(3.7);
				return weights;
			}
		};

		Collection<Object> outCollection = service.translateToCollection(customPair);
		Assert.assertEquals(0, outCollection.size());
	}
}
