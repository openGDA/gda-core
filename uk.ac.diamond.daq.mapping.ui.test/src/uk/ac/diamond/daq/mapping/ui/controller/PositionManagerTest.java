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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplateConfiguration;
import uk.ac.gda.client.properties.mode.Modes;
import uk.ac.gda.client.properties.mode.TestMode;
import uk.ac.gda.client.properties.mode.TestModeElement;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.client.properties.stage.position.ScannableKeys;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.ui.tool.document.ClientPropertiesHelper;

public class PositionManagerTest {

	private static final String X_AXIS = "stagex";
	private static final String Y_AXIS = "stagey";
	private static final String SHUTTER = "shutter";
	private static final String SHUTTER_OPEN = "Open";
	private static final String SHUTTER_CLOSE = "Close";
	private static final String BASE_POSITIONER = "basex";
	private static final String POSITIONER_IN = "In";
	private static final String POSITIONER_OUT = "Out";

	/** Object under test */
	private PositionManager manager = new PositionManager();

	@Mock
	private StageController stageController;

	@Mock
	private ClientPropertiesHelper clientPropertiesHelper;

	@Mock
	private TestMode testMode;

	@Mock
	private ScanningAcquisition acquisition;

	@Mock
	private ScanningParameters scanningParameters;

	private AcquisitionKeys key = new AcquisitionKeys(AcquisitionPropertyType.DEFAULT, AcquisitionSubType.STANDARD, AcquisitionTemplateType.STATIC_POINT);

	@Rule
	public MockitoRule initMocks = MockitoJUnit.rule();

	/**
	 * Injects the (mock) autowired services via reflection
	 */
	@Before
	public void injectServices() {
		ReflectionTestUtils.setField(manager, "stageController", stageController);
		ReflectionTestUtils.setField(manager, "clientPropertiesHelper", clientPropertiesHelper);
	}

	/**
	 * Enables the {@code acquisition.getAcquisitionConfiguration().getAcquisitionParameters()} call
	 */
	@Before
	public void mockAcquisition() {
		var scanningConfiguration = mock(ScanningConfiguration.class);
		when(acquisition.getAcquisitionConfiguration()).thenReturn(scanningConfiguration);
		when(scanningConfiguration.getAcquisitionParameters()).thenReturn(scanningParameters);
	}

	/**
	 * Enabled the {@code clientPropertiesHelper.getModes().getTest()} call
	 */
	@Before
	public void mockClientModes() {
		var modes = mock(Modes.class);
		when(clientPropertiesHelper.getModes()).thenReturn(modes);
		when(modes.getTest()).thenReturn(testMode);
	}

	/**
	 * First source of positions is the current position of scannables
	 * configured as contributing to {@code Position.START}.
	 * <p>
	 * e.g.
	 * <pre>
	 *  client.positions[0].position = START
	 *  client.positions[0].keys = beam_selector:selector\, shutter:shutter
	 * </pre>
	 *
	 */
	@Test
	public void globalPositionFromStageController() {
		mockGlobalPosition(Map.of(X_AXIS, 50.0, Y_AXIS, 21.4, SHUTTER, SHUTTER_OPEN));
		var expected = Set.of(
				createPosition(SHUTTER, "Open"),
				createPosition(X_AXIS, 50.0),
				createPosition(Y_AXIS, 21.4)
				);

		var startPosition = manager.getStartPosition(acquisition, key);

		assertThat(startPosition, is(equalTo(expected)));
	}

	/**
	 * Second source of positions is scannable/position pairs
	 * configured against acquisition type
	 * <p>
	 * e.g.
	 * <pre>
	 *  client.acquisitions[0].type = DIFFRACTION
	 *  client.acquisitions[0].startPosition[0].scannableKeys = shutter:shutter
	 *  client.acquisitions[0].startPosition[0].labelledPosition = OPEN
	 * </pre>
	 *
	 * Overwrites global position if referencing same device.
	 */
	@Test
	public void acquisitionPropertyTypePositionsTrumpGlobalPositions() {

		key = new AcquisitionKeys(AcquisitionPropertyType.DIFFRACTION, AcquisitionSubType.STANDARD, AcquisitionTemplateType.STATIC_POINT);

		mockGlobalPosition(Map.of(X_AXIS, 50.0, Y_AXIS, 21.4));
		mockAcquisitionPropertyType(key.getPropertyType(), Map.of(SHUTTER, SHUTTER_OPEN, X_AXIS, 100.0));

		var expected = Set.of(
				createPosition(SHUTTER, "Open"),
				createPosition(X_AXIS, 100.0), // overwrites global position
				createPosition(Y_AXIS, 21.4)
				);

		var startPosition = manager.getStartPosition(acquisition, key);

		assertThat(startPosition, is(equalTo(expected)));
	}

	/**
	 * Third source of positions is scannable/position pairs
	 * configured against acquisition 'template' type.
	 * <p>
	 * e.g.
	 * <pre>
	 *  client.acquisitions[1].templates[0].startPosition[1].scannableKeys = base_x:selector
	 *  client.acquisitions[1].templates[0].startPosition[1].labelledPosition = GTS
	 * </pre>
	 *
	 * Replaces previously collected documents if relating to same device.
	 */
	@Test
	public void acquisitionTemplateTypePositionsTrumpAcquisitionPropertyTypePositions() {

		key = new AcquisitionKeys(AcquisitionPropertyType.CALIBRATION, AcquisitionSubType.STANDARD, AcquisitionTemplateType.FLAT);

		mockAcquisitionPropertyType(key.getPropertyType(), Map.of(BASE_POSITIONER, POSITIONER_IN));
		mockAcquisitionTemplateType(key, Map.of(BASE_POSITIONER, POSITIONER_OUT));

		var expectedPosition = Set.of(createPosition(BASE_POSITIONER, POSITIONER_OUT));
		var startPosition = manager.getStartPosition(acquisition, key);

		assertThat(startPosition, is(equalTo(expectedPosition)));
	}

	/**
	 * The final source of positions come from the acquisition instance,
	 * which may be set via some acquisition GUI
	 */
	@Test
	public void acquisitionInstanceTrumpsAll() {
		mockAcquisitionTemplateType(key, Map.of(SHUTTER, SHUTTER_OPEN));
		mockInstancePosition(Map.of(SHUTTER, SHUTTER_CLOSE));

		var expectedPosition = Set.of(createPosition(SHUTTER, SHUTTER_CLOSE));
		var startPosition = manager.getStartPosition(acquisition, key);

		assertThat(startPosition, is(equalTo(expectedPosition)));
	}

	/**
	 * Additionally, devices which are not always safe or possible to move
	 * can be filtered out when configured as excluded in {@link TestMode}.
	 * <p>
	 * e.g.
	 * <pre>
	 *  client.modes.test.active = true
	 *  client.modes.test.elements[0].device = shutter:shutter
	 *  client.modes.test.elements[0].exclude = true
	 * </pre>
	 */
	@Test
	public void excludesDevicesConfiguredInTestModeWhenInTestMode() {
		mockGlobalPosition(Map.of(SHUTTER, SHUTTER_OPEN));
		mockAcquisitionPropertyType(key.getPropertyType(), Map.of(BASE_POSITIONER, POSITIONER_IN));
		mockAcquisitionTemplateType(key, Map.of(SHUTTER, SHUTTER_CLOSE));

		var xpos = 42.3;
		var ypos = 0.45;
		mockInstancePosition(Map.of(X_AXIS, xpos, Y_AXIS, ypos));

		excludeInTestMode(SHUTTER, BASE_POSITIONER);
		setTestModeActive(true);

		var expectedPosition = Set.of(createPosition(X_AXIS, xpos), createPosition(Y_AXIS, ypos));
		var startPosition = manager.getStartPosition(acquisition, key);

		assertThat(startPosition, is(equalTo(expectedPosition)));
	}

	@Test
	public void doesNotExcludeDevicesWhenNotInTestMode() {
		mockGlobalPosition(Map.of(SHUTTER, SHUTTER_OPEN));
		mockAcquisitionPropertyType(key.getPropertyType(), Map.of(BASE_POSITIONER, POSITIONER_IN));
		mockAcquisitionTemplateType(key, Map.of(SHUTTER, SHUTTER_CLOSE));

		var xpos = 42.3;
		var ypos = 0.45;
		mockInstancePosition(Map.of(X_AXIS, xpos, Y_AXIS, ypos));

		excludeInTestMode(SHUTTER, BASE_POSITIONER);
		setTestModeActive(false);

		var expectedPosition = Set.of(
				createPosition(SHUTTER, SHUTTER_CLOSE),
				createPosition(BASE_POSITIONER, POSITIONER_IN),
				createPosition(X_AXIS, xpos),
				createPosition(Y_AXIS, ypos));
		var startPosition = manager.getStartPosition(acquisition, key);

		assertThat(startPosition, is(equalTo(expectedPosition)));
	}

	private void mockGlobalPosition(Map<String, Object> positions) {
		when(stageController.reportPositions(Position.START)).thenReturn(toDevicePositionDocuments(positions));
	}

	private void mockAcquisitionPropertyType(AcquisitionPropertyType type, Map<String, Object> positions) {

		var position = positions.entrySet().stream()
			.map(entry -> createScannablePropertiesValue(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		var configuration = new AcquisitionConfigurationProperties();
		configuration.setStartPosition(position);

		position.forEach(this::dummyCreateDevicePositionDocument);

		when(clientPropertiesHelper.getAcquisitionConfigurationProperties(type)).thenReturn(Optional.of(configuration));
	}

	private void mockAcquisitionTemplateType(AcquisitionKeys key, Map<String, Object> positions) {
		var position = positions.entrySet().stream()
				.map(entry -> createScannablePropertiesValue(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		var configuration = new AcquisitionTemplateConfiguration();
		configuration.setStartPosition(position);

		position.forEach(this::dummyCreateDevicePositionDocument);
		when(clientPropertiesHelper.getAcquisitionTemplateConfiguration(key)).thenReturn(Optional.of(configuration));
	}

	private void mockInstancePosition(Map<String, Object> positions) {
		when(scanningParameters.getStartPosition()).thenReturn(toDevicePositionDocuments(positions));
	}

	private void excludeInTestMode(String... devices) {
		var elements = Stream.of(devices).map(this::excludedDevice).collect(Collectors.toList());
		elements.forEach(element -> dummyGetScannablePropertiesDocument(element.getDevice()));
		when(testMode.getElements()).thenReturn(elements);
	}

	private void setTestModeActive(boolean active) {
		when(testMode.isActive()).thenReturn(active);
	}

	private DevicePositionDocument createPosition(String deviceName, Object position) {
		if (position instanceof String) {
			return createPosition(deviceName, (String) position);
		} else {
			return createPosition(deviceName, (double) position);
		}
	}

	private DevicePositionDocument createPosition(String deviceName, String position) {
		return new DevicePositionDocument.Builder()
				.withValueType(ValueType.LABELLED)
				.withDevice(deviceName)
				.withLabelledPosition(position).build();
	}

	private DevicePositionDocument createPosition(String deviceName, double position) {
		return new DevicePositionDocument.Builder()
				.withValueType(ValueType.NUMERIC)
				.withDevice(deviceName)
				.withPosition(position).build();
	}

	private void dummyCreateDevicePositionDocument(ScannablePropertiesValue value) {
		var position = value.getLabelledPosition() == null ?
				value.getPosition() : value.getLabelledPosition();
		var document = createPosition(value.getScannableKeys().getScannableId(), position);
		when(stageController.createDevicePositionDocument(value)).thenReturn(document);
	}

	private void dummyGetScannablePropertiesDocument(ScannableKeys scannableKeys) {
		var properties = new ScannableProperties();
		properties.setScannable(scannableKeys.getScannableId());
		when(stageController.getScannablePropertiesDocument(scannableKeys)).thenReturn(properties);
	}

	private ScannableKeys dummyScannableKey(String name) {
		var key = new ScannableKeys();
		key.setGroupId(name);
		key.setScannableId(name);
		return key;
	}

	private Set<DevicePositionDocument> toDevicePositionDocuments(Map<String, Object> positions) {
		return positions.entrySet().stream()
				.map(entry -> createPosition(entry.getKey(), entry.getValue()))
				.collect(Collectors.toSet());
	}

	private ScannablePropertiesValue createScannablePropertiesValue(String name, Object position) {
		var spv = new ScannablePropertiesValue();
		spv.setScannableKeys(dummyScannableKey(name));
		if (position instanceof String) {
			spv.setLabelledPosition((String) position);
		} else {
			spv.setPosition((double) position);
		}
		return spv;
	}

	private TestModeElement excludedDevice(String device) {
		var element = new TestModeElement();
		element.setDevice(dummyScannableKey(device));
		element.setExclude(true);
		return element;
	}

}
