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

package uk.ac.gda.api.acquisition.configuration.calibration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.exception.GDAException;

public class ImageCalibrationDocumentTest {

	@Test
	public void deserialiseImageCalibrations() throws GDAException  {
		ImageCalibration document = deserialiseDocument("test/resources/ImageCalibrationDeserialise.json",
				ImageCalibration.class);

	    assertEquals(2, document.getFlatCalibration().getNumberExposures(), 0);
	    assertTrue(document.getDarkCalibration().isBeforeAcquisition());
	    assertFalse(document.getFlatCalibration().isBeforeAcquisition());
	    assertNull(document.getFlatCalibration().getDetectorDocument());
	}

	@Test
	public void serialiseImageCalibrations()
	  throws IOException {

		ImageCalibration imageCalibration = new ImageCalibration();

		DarkCalibrationDocument.Builder builderDark = new DarkCalibrationDocument.Builder();
		builderDark.withBeforeAcquisition(true);
		builderDark.withNumberExposures(1);
		imageCalibration.setDarkCalibration(builderDark.build());

		FlatCalibrationDocument.Builder builderFlat = new FlatCalibrationDocument.Builder();
		builderFlat.withBeforeAcquisition(false);
		builderFlat.withNumberExposures(2);
		imageCalibration.setFlatCalibration(builderFlat.build());

	    String json = new ObjectMapper().writeValueAsString(imageCalibration);
	    System.out.println(json);
	    assertThat(json, containsString("\"darkCalibration\":{\"detectorDocument\":null,\"numberExposures\":1,\"beforeAcquisition\":true,\"afterAcquisition\":false}"));
	    assertThat(json, containsString("\"flatCalibration\":{\"detectorDocument\":null,\"numberExposures\":2,\"beforeAcquisition\":false,\"afterAcquisition\":false}"));
	}

	protected <T> T deserialiseDocument(String resourcePath, Class<T> clazz) throws GDAException {
		File resource = new File(resourcePath);
		try {
			return  new ObjectMapper().readValue(resource.toURI().toURL(), clazz);
		} catch (IOException e) {
			throw new GDAException(e);
		}
	}
}
