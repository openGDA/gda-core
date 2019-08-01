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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;
import uk.ac.gda.tomography.model.TomographyConfiguration;
import uk.ac.gda.tomography.service.TomographyServiceException;

public class TomographyScanParametersTest {

	private File tempFile;
	private ObjectMapper mapper;

	@Before
	public void before() throws TomographyServiceException {
		mapper = new ObjectMapper();
	}

	@After
	public void after() throws TomographyServiceException {

	}

	@Test
	public void basicTomographyConfigurationDeserialization() throws JsonParseException, JsonMappingException, IOException {
		String jsonData = getResourceAsString("/resources/simpleTomographyConfiguration.json");
		TomographyConfiguration configuration = mapper.readValue(jsonData, TomographyConfiguration.class);

		Assert.assertEquals(false, configuration.getStart().isUseCurrentAngle());
		Assert.assertEquals(RangeType.RANGE_360, configuration.getEnd().getRangeType());
		Assert.assertEquals(2, configuration.getImageCalibration().getNumberDark());
		Assert.assertEquals(3, configuration.getMultipleScans().getNumberRepetitions());
	}

	/**
	 * Tests a basic serialisation
	 * @throws AcquisitionControllerException
	 * @throws JsonProcessingException
	 */
	@Test
	public void basicTomographyConfigurationSerialization() throws AcquisitionControllerException, JsonProcessingException {
		TomographyConfiguration conf = new TomographyConfiguration();
		conf.setScanType(ScanType.FLY);

		StartAngle startAngle = new StartAngle();
		startAngle.setStart(2.3);
		startAngle.setUseCurrentAngle(false);
		conf.setStart(startAngle);

		EndAngle endAngle = new EndAngle();
		endAngle.setRangeType(RangeType.RANGE_360);
		endAngle.setNumberRotation(3);
		endAngle.setCustomAngle(25.2);
		conf.setEnd(endAngle);

		ImageCalibration imageCalibration = new ImageCalibration();
		imageCalibration.setAfterAcquisition(true);
		imageCalibration.setBeforeAcquisition(false);
		imageCalibration.setNumberDark(2);
		imageCalibration.setNumberFlat(2);
		conf.setImageCalibration(imageCalibration);

		MultipleScans multipleScans = new MultipleScans();
		multipleScans.setMultipleScansType(MultipleScansType.SWITCHBACK_SCAN);
		multipleScans.setNumberRepetitions(3);
		multipleScans.setWaitingTime(1000);
		conf.setMultipleScans(multipleScans);

		String confAsString = mapper.writeValueAsString(conf);

	    assertThat(confAsString, containsString("imageCalibration"));
	}

	private String getResourceAsString(String resource) {
		tempFile = getResourceAsFile(resource);
		try {
			return new String(Files.readAllBytes(Paths.get(tempFile.toURI())));
		} catch (IOException e) {
			Assert.fail("Cannot load the resource");
		}
		return null;
	}

	private File getResourceAsFile(String resource) {
		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), "tmp");
			URL url = this.getClass().getResource(resource);
			Files.copy(Paths.get(url.getPath()), new FileOutputStream(tempFile));
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		return tempFile;
	}
}
