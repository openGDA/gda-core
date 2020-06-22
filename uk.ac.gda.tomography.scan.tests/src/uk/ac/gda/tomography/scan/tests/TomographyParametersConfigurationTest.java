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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import gda.device.IScannableMotor;
import gda.device.scannable.DummyScannableMotor;
import gda.rcp.views.TabCompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.configuration.ImageCalibration;
import uk.ac.diamond.daq.mapping.api.document.base.configuration.MultipleScans;
import uk.ac.diamond.daq.mapping.api.document.base.configuration.MultipleScansType;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.tomography.base.TomographyConfiguration;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.stage.CommonStage;
import uk.ac.gda.tomography.stage.StageDescription;
import uk.ac.gda.tomography.stage.enumeration.Stage;
import uk.ac.gda.tomography.stage.enumeration.StageDevice;

public class TomographyParametersConfigurationTest {

	private File tempFile;
	private ObjectMapper mapper;

	@Before
	public void before() {
		mapper = new ObjectMapper();
	}

	@After
	public void after() {

	}

	@Test
	public void basicDeserialization() throws JsonParseException, JsonMappingException, IOException {
		String jsonData = getResourceAsString("/resources/simpleTomographyParametersConfiguration.json");
		AcquisitionConfigurationBase<TomographyParameters> configuration = mapper.readValue(jsonData, TomographyConfiguration.class);
		TomographyParameters tp = configuration.getAcquisitionParameters();
		ScannableTrackDocument std = tp.getScanpathDocument().getScannableTrackDocuments().get(0);
		Assert.assertEquals(2.0, std.getStart(), 0.0);
		Assert.assertEquals(5.0, std.getStop(), 0.0);
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
		ScannableTrackDocument std = tp.getScanpathDocument().getScannableTrackDocuments().get(0);
		Assert.assertEquals(2.0, std.getStart(), 0.0);
		Assert.assertEquals(5.0, std.getStop(), 0.0);
		Assert.assertEquals("uno", configuration.getMetadata().get("one"));
		Assert.assertEquals("due", configuration.getMetadata().get("two"));
	}

	/**
	 * Tests a serialisation with Devices
	 *
	 * @throws JsonProcessingException
	 */
	@Ignore //Too many problem importing dependencies. Have to think a different approach
	@Test
	public void devicesSerialization() throws JsonProcessingException {
		StageDescription mode = createBasicTomographyMode();

		String confAsString = mapper.writeValueAsString(mode);
		assertThat(confAsString, containsString("MotorOne"));
		assertThat(confAsString, containsString("MotorTwo"));

		System.out.println(confAsString);
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
		TomographyParameterAcquisition newConfiguration = new TomographyParameterAcquisition();
		TomographyConfiguration configuration = new TomographyConfiguration();
		newConfiguration.setAcquisitionConfiguration(configuration);
		newConfiguration.setName("Default name");
		TomographyParameters acquisitionParameters = new TomographyParameters();

		acquisitionParameters.setScanType(ScanType.FLY);
		configuration.setImageCalibration(new ImageCalibration());

		ScanpathDocument.Builder scanpathBuilder = new ScanpathDocument.Builder();
		scanpathBuilder.withModelDocument(AcquisitionTemplateType.ONE_DIMENSION_LINE);
		ScannableTrackDocument.Builder scannableTrackBuilder = new ScannableTrackDocument.Builder();
		scannableTrackBuilder.withStart(0.0);
		scannableTrackBuilder.withStop(180.0);
		scannableTrackBuilder.withPoints(1);
		List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();
		scannableTrackDocuments.add(scannableTrackBuilder.build());
		scanpathBuilder.withScannableTrackDocuments(scannableTrackDocuments);
		acquisitionParameters.setScanpathDocument(scanpathBuilder.build());

		MultipleScans multipleScan = new MultipleScans();
		multipleScan.setMultipleScansType(MultipleScansType.REPEAT_SCAN);
		multipleScan.setNumberRepetitions(1);
		multipleScan.setWaitingTime(0);
		configuration.setMultipleScans(multipleScan);
		newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);
		return newConfiguration.getAcquisitionConfiguration();
	}

	private StageDescription createBasicTomographyMode() {
		return new CommonStage(Stage.GTS) {

			private Map<StageDevice, IScannableMotor> intMotors() {
				Map<StageDevice, IScannableMotor> motors = new EnumMap<>(StageDevice.class);

				IScannableMotor device = new DummyScannableMotor();
				device.setName("MotorOne");
				motors.put(StageDevice.MOTOR_STAGE_X, device);

				device = new DummyScannableMotor();
				device.setName("MotorTwo");
				motors.put(StageDevice.MOTOR_STAGE_Y, device);

				return motors;
			}

			@Override
			public Map<StageDevice, IScannableMotor> getMotors() {
				return intMotors();
			}

			@Override
			protected void populateDevicesMap() {
			}

			@Override
			protected TabCompositeFactory[] getTabsFactories() {
				return null;
			}
		};
	}
}