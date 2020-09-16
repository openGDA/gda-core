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

import static gda.TestHelpers.deserialiseDocument;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.acquisition.AcquisitionTestUtils;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.exception.GDAException;

public class ImageCalibrationDocumentTest {

	@Test
	public void deserialiseImageCalibrations() throws GDAException  {
		ImageCalibration document = AcquisitionTestUtils.deserialiseDocument("test/resources/ImageCalibrationDeserialise.json",
				ImageCalibration.class);

	    assertEquals(2, document.getFlatCalibration().getNumberExposures(), 0);
	    assertTrue(document.getDarkCalibration().isBeforeAcquisition());
	    assertFalse(document.getFlatCalibration().isBeforeAcquisition());
	    assertEquals(2.4, document.getFlatCalibration().getDetectorDocument().getExposure(), 0);
	    assertEquals("motor_x", document.getFlatCalibration().getDetectorDocument().getName());
	    Set<DevicePositionDocument> position = document.getFlatCalibration().getPosition();
	    assertEquals(2, position.size(), 0);
	}

	@Test
	public void serialiseImageCalibrations()
	  throws IOException {

		ImageCalibration.Builder imageCalibrationBuilder = new ImageCalibration.Builder();

		DarkCalibrationDocument.Builder builderDark = new DarkCalibrationDocument.Builder();
		builderDark.withBeforeAcquisition(true);
		builderDark.withNumberExposures(1);
		imageCalibrationBuilder.withDarkCalibration(builderDark.build());

		FlatCalibrationDocument.Builder builderFlat = new FlatCalibrationDocument.Builder();
		builderFlat.withBeforeAcquisition(false);
		builderFlat.withNumberExposures(2);
		imageCalibrationBuilder.withFlatCalibration(builderFlat.build());

	    String json = new ObjectMapper().writeValueAsString(imageCalibrationBuilder.build());
	    assertThat(json, containsString("\"darkCalibration\":{\"position\":null,\"detectorDocument\":null,\"numberExposures\":1,\"beforeAcquisition\":true,\"afterAcquisition\":false}"));
	    assertThat(json, containsString("\"flatCalibration\":{\"position\":null,\"detectorDocument\":null,\"numberExposures\":2,\"beforeAcquisition\":false,\"afterAcquisition\":false}"));
	}
}
