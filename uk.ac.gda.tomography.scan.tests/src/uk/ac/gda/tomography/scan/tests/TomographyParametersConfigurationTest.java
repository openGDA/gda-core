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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.motor.DummyMotor;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.base.TomographyConfiguration;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;
import uk.ac.gda.tomography.service.TomographyServiceException;

public class TomographyParametersConfigurationTest {

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
	public void basicDeserialization() throws JsonParseException, JsonMappingException, IOException {
		String jsonData = getResourceAsString("/resources/simpleTomographyParametersConfiguration.json");
		TomographyConfiguration configuration = mapper.readValue(jsonData, TomographyConfiguration.class);
		TomographyParameters tp = configuration.getAcquisitionParameters();

		Assert.assertEquals(false, tp.getStart().isUseCurrentAngle());
		Assert.assertEquals(RangeType.RANGE_360, tp.getEnd().getRangeType());
		Assert.assertEquals(2, tp.getImageCalibration().getNumberDark());
		Assert.assertEquals(3, tp.getMultipleScans().getNumberRepetitions());
	}

	/**
	 * Tests a basic serialisation
	 *
	 * @throws JsonProcessingException
	 */
	@Test
	public void basicSerialization() throws JsonProcessingException {
		TomographyConfiguration configuration = createBasicTomographyParametersConfiguration();

		String confAsString = mapper.writeValueAsString(configuration);

		assertThat(confAsString, containsString("devices"));
		assertThat(confAsString, containsString("acquisitionParameters"));
		assertThat(confAsString, containsString("metadata"));
	}

	/**
	 * Tests a serialisation with Metadata
	 *
	 * @throws JsonProcessingException
	 */
	@Test
	public void basicWithMetadataSerialization() throws JsonProcessingException {
		TomographyConfiguration configuration = createBasicTomographyParametersConfiguration();

		Map<String, String> metadata = new HashMap<>();
		metadata.put("one", "uno");
		metadata.put("two", "due");
		configuration.setMetadata(metadata);

		String confAsString = mapper.writeValueAsString(configuration);

		assertThat(confAsString, containsString("one"));
		assertThat(confAsString, containsString("uno"));
		assertThat(confAsString, containsString("two"));
		assertThat(confAsString, containsString("due"));
	}

	@Test
	public void deserializationWithMetadata() throws JsonParseException, JsonMappingException, IOException {
		String jsonData = getResourceAsString("/resources/tomographyParametersConfigurationWithMetadata.json");
		TomographyConfiguration configuration = mapper.readValue(jsonData, TomographyConfiguration.class);
		TomographyParameters tp = configuration.getAcquisitionParameters();

		Assert.assertEquals(false, tp.getStart().isUseCurrentAngle());
		Assert.assertEquals(RangeType.RANGE_360, tp.getEnd().getRangeType());
		Assert.assertEquals(2, tp.getImageCalibration().getNumberDark());
		Assert.assertEquals(3, tp.getMultipleScans().getNumberRepetitions());
		Assert.assertEquals("uno", configuration.getMetadata().get("one"));
		Assert.assertEquals("due", configuration.getMetadata().get("two"));
	}

	/**
	 * Tests a serialisation with Devices
	 *
	 * @throws JsonProcessingException
	 * @throws DeviceException
	 */
	@Test
	public void devicesSerialization() throws JsonProcessingException, DeviceException {
		TomographyConfiguration configuration = createBasicTomographyParametersConfiguration();

		Set<Device> devices = new HashSet<>();
		Device device = new DummyMotor();
		device.setName("MotorOne");
		// I cannot set attributes because Device.getAttributes() does not exists consequently Jackson cannot serialise something it cannot read
		// device.setAttribute("greeting", "Hello");
		devices.add(device);

		device = new DummyMotor();
		device.setName("MotorTwo");
		// device.setAttribute("greeting", "Ciao");
		devices.add(device);

		configuration.setDevices(devices);

		String confAsString = mapper.writeValueAsString(configuration);
		assertThat(confAsString, containsString("MotorOne"));
		assertThat(confAsString, containsString("MotorTwo"));

		System.out.println(confAsString);
	}

	/**
	 * This test is ignored at the moment as it is not possible to deserialise a Device. To do this would be necessary to add
	 * Jackson annotations like @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "class").
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void deserializationWithDevices() throws JsonParseException, JsonMappingException, IOException {
		String jsonData = getResourceAsString("/resources/tomographyParametersConfigurationWithDevices.json");
		TomographyConfiguration configuration = mapper.readValue(jsonData, TomographyConfiguration.class);

		long devicesNum = configuration.getDevices().stream().filter(d -> {
			return d.getName().equals("MotorOne") || d.getName().equals("MotorTwo");
		}).count();
		Assert.assertEquals(2, devicesNum);
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

	private TomographyConfiguration createBasicTomographyParametersConfiguration() {
		TomographyParameters tp = new TomographyParameters();
		tp.setScanType(ScanType.FLY);

		StartAngle startAngle = new StartAngle();
		startAngle.setStart(2.3);
		startAngle.setUseCurrentAngle(false);
		tp.setStart(startAngle);

		EndAngle endAngle = new EndAngle();
		endAngle.setRangeType(RangeType.RANGE_360);
		endAngle.setNumberRotation(3);
		endAngle.setCustomAngle(25.2);
		tp.setEnd(endAngle);

		ImageCalibration imageCalibration = new ImageCalibration();
		imageCalibration.setAfterAcquisition(true);
		imageCalibration.setBeforeAcquisition(false);
		imageCalibration.setNumberDark(2);
		imageCalibration.setNumberFlat(2);
		tp.setImageCalibration(imageCalibration);

		MultipleScans multipleScans = new MultipleScans();
		multipleScans.setMultipleScansType(MultipleScansType.SWITCHBACK_SCAN);
		multipleScans.setNumberRepetitions(3);
		multipleScans.setWaitingTime(1000);
		tp.setMultipleScans(multipleScans);

		TomographyConfiguration configuration = new TomographyConfiguration();
		configuration.setAcquisitionParameters(tp);
		return configuration;
	}
}
