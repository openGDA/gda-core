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

import java.util.List;
import java.util.Map;
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
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.AcquisitionManager;
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
	private AcquisitionManager acquisitionManager;

	@Mock
	private TestMode testMode;

	@Mock
	private ScanningAcquisition acquisition;

	@Mock
	private ScanningParameters scanningParameters;

	private AcquisitionKeys key = new AcquisitionKeys(AcquisitionPropertyType.DEFAULT, AcquisitionSubType.STANDARD, TrajectoryShape.STATIC_POINT);

	@Rule
	public MockitoRule initMocks = MockitoJUnit.rule();

	/**
	 * Injects the (mock) autowired services via reflection
	 */
	@Before
	public void injectServices() {
		ReflectionTestUtils.setField(manager, "stageController", stageController);
		ReflectionTestUtils.setField(manager, "clientPropertiesHelper", clientPropertiesHelper);
		ReflectionTestUtils.setField(manager, "acquisitionManager", acquisitionManager);
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
	 */
	@Test
	public void globalPositionFromStageController() {
		mockGlobalPosition(Map.of(X_AXIS, 50.0, Y_AXIS, 21.4, SHUTTER, SHUTTER_OPEN));
		var expected = Set.of(
				createPosition(SHUTTER, "Open"),
				createPosition(X_AXIS, 50.0),
				createPosition(Y_AXIS, 21.4));

		var startPosition = manager.getStartPosition(key);

		assertThat(startPosition, is(equalTo(expected)));
	}

	/**
	 * Second source of positions is the start positions
	 * configured against the AcquisitionTemplate corresponding to
	 * the given acquisition key.
	 *
	 * These will override global positions if referencing same device.
	 */
	@Test
	public void startPositionFromAcquisitionTemplate() {
		key = new AcquisitionKeys(AcquisitionPropertyType.CALIBRATION, AcquisitionSubType.DARK, TrajectoryShape.STATIC_POINT);

		mockAcquisitionTemplatePosition(key, List.of(createScannablePropertiesValue(SHUTTER, SHUTTER_CLOSE)));

		var expected = Set.of(createPosition(SHUTTER, SHUTTER_CLOSE));
		var startPosition = manager.getStartPosition(key);

		assertThat(startPosition, is(equalTo(expected)));
	}

	/**
	 * Third source of positions is {@link ScannablePropertiesValue}
	 * configured against acquisition type on the PositionManager.
	 *
	 * Overrides earlier computed position if referencing same device.
	 */
	@Test
	public void propertyTypePositionsConfiguredInPositionManager() {

		key = new AcquisitionKeys(AcquisitionPropertyType.DIFFRACTION, AcquisitionSubType.STANDARD, TrajectoryShape.TWO_DIMENSION_POINT);

		var diffractionPositions = List.of(
				createScannablePropertiesValue(SHUTTER, SHUTTER_OPEN),
				createScannablePropertiesValue(X_AXIS, 100.0));

		mockStageController(diffractionPositions);

		manager.configurePosition(key.getPropertyType(), diffractionPositions);

		var expected = Set.of(
				createPosition(SHUTTER, "Open"),
				createPosition(X_AXIS, 100.0));

		var startPosition = manager.getStartPosition(key);

		assertThat(startPosition, is(equalTo(expected)));
	}

	/**
	 * Fourth and final source of positions is {@link ScannablePropertiesValue}
	 * configured against acquisition subtype on the PositionManager.
	 *
	 * Overrides earlier computed position if referencing same device.
	 */
	@Test
	public void subTypePositionsConfiguredInPositionManager() {
		key = new AcquisitionKeys(AcquisitionPropertyType.DIFFRACTION, AcquisitionSubType.BEAM_SELECTOR, TrajectoryShape.STATIC_POINT);

		var position = List.of(createScannablePropertiesValue(X_AXIS, 0.0), createScannablePropertiesValue(Y_AXIS, 12.0));
		mockStageController(position);

		manager.configurePosition(key.getSubType(), position);

		var expected = Set.of(createPosition(X_AXIS, 0.0), createPosition(Y_AXIS, 12.0));
		var startPosition = manager.getStartPosition(key);

		assertThat(startPosition, is(equalTo(expected)));
	}

	/**
	 * The position sources in order are:
	 *  1) global start position
	 *  2) position on acquisition template
	 *  3) position configured in PositionManager against AcquisitionPropertyType
	 *  4) position configured in PositionManager against AcquisitionSubType
	 *
	 * Earlier positions get overwritten if referencing same device
	 */
	@Test
	public void positionSourcesOverrideOrder() {
		mockGlobalPosition(Map.of(SHUTTER, SHUTTER_CLOSE));

		mockAcquisitionTemplatePosition(key, List.of(
				createScannablePropertiesValue(SHUTTER, SHUTTER_OPEN), // overrides global
				createScannablePropertiesValue(BASE_POSITIONER, POSITIONER_OUT)));

		var acquisitionTypePosition = List.of(
				createScannablePropertiesValue(BASE_POSITIONER, POSITIONER_IN), // overrides template
				createScannablePropertiesValue(X_AXIS, 15.5),
				createScannablePropertiesValue(Y_AXIS, 24.2));

		mockStageController(acquisitionTypePosition);
		manager.configurePosition(key.getPropertyType(), acquisitionTypePosition);

		var subTypePosition = List.of(
				createScannablePropertiesValue(Y_AXIS, 100.0)); // overrides acquisition type

		mockStageController(subTypePosition);
		manager.configurePosition(key.getSubType(), subTypePosition);

		var expected = Set.of(
				/* from template */
				createPosition(SHUTTER, SHUTTER_OPEN),

				/* from acquisition type */
				createPosition(BASE_POSITIONER, POSITIONER_IN),
				createPosition(X_AXIS, 15.5),

				/* from acquisition subtype */
				createPosition(Y_AXIS, 100.0));

		var startPosition = manager.getStartPosition(key);
		assertThat(startPosition, is(equalTo(expected)));
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
		mockAcquisitionTemplatePosition(key, List.of(createScannablePropertiesValue(BASE_POSITIONER, POSITIONER_IN)));

		var xpos = 42.3;
		var ypos = 0.45;
		var subtypePositions = List.of(
				createScannablePropertiesValue(X_AXIS, xpos),
				createScannablePropertiesValue(Y_AXIS, ypos));

		mockStageController(subtypePositions);
		manager.configurePosition(key.getSubType(), subtypePositions);

		excludeInTestMode(SHUTTER, BASE_POSITIONER);
		setTestModeActive(true);

		var expectedPosition = Set.of(createPosition(X_AXIS, xpos), createPosition(Y_AXIS, ypos));
		var startPosition = manager.getStartPosition(key);

		assertThat(startPosition, is(equalTo(expectedPosition)));
	}

	@Test
	public void doesNotExcludeDevicesWhenNotInTestMode() {
		mockGlobalPosition(Map.of(SHUTTER, SHUTTER_OPEN));
		mockAcquisitionTemplatePosition(key, List.of(createScannablePropertiesValue(BASE_POSITIONER, POSITIONER_IN)));

		var xpos = 42.3;
		var ypos = 0.45;
		var subtypePositions = List.of(
				createScannablePropertiesValue(X_AXIS, xpos),
				createScannablePropertiesValue(Y_AXIS, ypos));

		mockStageController(subtypePositions);
		manager.configurePosition(key.getSubType(), subtypePositions);

		excludeInTestMode(SHUTTER, BASE_POSITIONER);
		setTestModeActive(false);

		var expectedPosition = Set.of(
				createPosition(SHUTTER, SHUTTER_OPEN),
				createPosition(BASE_POSITIONER, POSITIONER_IN),
				createPosition(X_AXIS, xpos),
				createPosition(Y_AXIS, ypos));
		var startPosition = manager.getStartPosition(key);

		assertThat(startPosition, is(equalTo(expectedPosition)));
	}

	private void mockGlobalPosition(Map<String, Object> positions) {
		when(stageController.reportPositions(Position.START)).thenReturn(toDevicePositionDocuments(positions));
	}

	private void mockAcquisitionTemplatePosition(AcquisitionKeys key, List<ScannablePropertiesValue> positions) {
		positions.forEach(this::dummyCreateDevicePositionDocument);
		when(acquisitionManager.getStartPosition(key)).thenReturn(positions);
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
		return new DevicePositionDocument.Builder()
				.withDevice(deviceName)
				.withPosition(position).build();
	}

	private void mockStageController(List<ScannablePropertiesValue> values) {
		values.forEach(this::dummyCreateDevicePositionDocument);
	}

	private void dummyCreateDevicePositionDocument(ScannablePropertiesValue value) {
		var position = value.getPosition();
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
		spv.setPosition(position);
		return spv;
	}

	private TestModeElement excludedDevice(String device) {
		var element = new TestModeElement();
		element.setDevice(dummyScannableKey(device));
		element.setExclude(true);
		return element;
	}

}
