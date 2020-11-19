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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.diamond.daq.mapping.api.document.DocumentMapper.fromJSON;
import static uk.ac.diamond.daq.mapping.api.document.DocumentMapper.toJSON;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeRequest;
import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;

/**
 * Where we ensure all processing request implementations can be consistently de/serialised
 */
public class ProcessingRequestSerialisationTest {

	@Test
	public void testSavuProcessingRequest() throws Exception {

		String file1 = "file://processing/file1";
		String file2 = "file://processing/file2";

		List<URL> files = Arrays.asList(new URL(file1), new URL(file2));

		SavuProcessingRequest originalRequest = new SavuProcessingRequest.Builder()
												.withValue(files).build();

		String serialisedRequest = toJSON(originalRequest);

		SavuProcessingRequest deserialisedRequest = fromJSON(serialisedRequest, SavuProcessingRequest.class);

		assertEquals(files, deserialisedRequest.getValue());
		assertTrue(serialisedRequest.contains("\"key\" : \"" + originalRequest.getKey() + "\""));
	}

	@Test
	public void testDiffractionCalibrationMergeRequest() throws Exception {

		String calibration = "file://processing/calibration.nxs";
		String datasetName = "pilatus";

		List<URL> file = Arrays.asList(new URL(calibration));

		DiffractionCalibrationMergeRequest originalRequest = new DiffractionCalibrationMergeRequest.Builder()
														.withValue(file)
														.withDeviceName(datasetName).build();

		String serialisedRequest = toJSON(originalRequest);

		DiffractionCalibrationMergeRequest deserialisedRequest = fromJSON(serialisedRequest, DiffractionCalibrationMergeRequest.class);

		assertEquals(datasetName, deserialisedRequest.getDeviceName());
		assertEquals(file, deserialisedRequest.getValue());
		assertTrue(serialisedRequest.contains("\"key\" : \"" + originalRequest.getKey() + "\""));
	}

}
