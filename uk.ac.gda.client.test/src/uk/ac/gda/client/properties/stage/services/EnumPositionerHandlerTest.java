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

package uk.ac.gda.client.properties.stage.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
import uk.ac.gda.client.properties.stage.position.ScannableKeys;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue.PositionType;
import uk.ac.gda.common.exception.GDAException;

public class EnumPositionerHandlerTest {

	private DeviceHandler handler = new EnumPositionerHandler();

	@Mock
	private EnumPositioner device;

	@Mock
	private ScannablesPropertiesHelper scannablesPropertiesHelper;

	private ScannablePropertiesValue config = new ScannablePropertiesValue();



	@Mock
	private ScannableKeys scannableKeys;

	@Rule
	public MockitoRule initMocks = MockitoJUnit.rule();

	@Before
	public void autowirePropertiesService() {
		when(scannablesPropertiesHelper.getScannablePropertiesDocument(any())).thenReturn(mock(ScannableProperties.class));
		ReflectionTestUtils.setField(handler, "scannablesPropertiesHelper", scannablesPropertiesHelper);
	}

	@Test
	public void handlesEnumPositioners() throws Exception {
		var deviceName = "shutter";
		when(device.getName()).thenReturn(deviceName);

		config.setPosition("CLOSED");
		var document = handler.devicePositionAsDocument(device, config);
		assertNotNull(document);

		// will only test name in this test (fairly boring)
		assertThat(document.getDevice(), equalTo(deviceName));
	}

	@Test
	public void doesNotHandleNonEnumPositioners() throws Exception {
		// a scannable other than IScannableMotor
		var document = handler.devicePositionAsDocument(mock(Scannable.class), config);
		// null means we cannot handle it
		assertNull(document);
	}

	@Test
	public void documentForAbsolutePosition() throws Exception {
		config.setPositionType(PositionType.ABSOLUTE);
		var position = "OPEN";
		config.setPosition(position);

		var document = handler.devicePositionAsDocument(device, config);
		assertThat(document.getPosition(), equalTo(position));
	}

	@Test
	public void failsWhenAbsolutePositionNotConfigured() {
		config.setPositionType(PositionType.ABSOLUTE);
		assertThrows(GDAException.class,
				() -> handler.devicePositionAsDocument(device, config));
	}

	/**
	 * It is possible to configure the enumerated positions
	 * in a {@link ScannableProperties} object.
	 * <p>
	 * This is an extra layer of redirection, covered by this test.
	 */
	@Test
	public void documentForAbsolutePositionConfiguredInScannablesProperties() throws Exception {
		config.setPositionType(PositionType.ABSOLUTE);
		// the position as configured in the client
		var position = "open sesame";
		config.setPosition(position);

		// the position the device understands
		var realPosition = "OPEN";

		// mock the configuration
		var properties = new ScannableProperties();
		properties.setEnumsMap(Map.of(position, realPosition));
		when(scannablesPropertiesHelper.getScannablePropertiesDocument(scannableKeys))
			.thenReturn(properties);

		config.setScannableKeys(scannableKeys);

		var document = handler.devicePositionAsDocument(device, config);
		assertThat(document.getPosition(), equalTo(realPosition));
	}

	@Test
	public void documentForCurrentPosition() throws Exception {
		var position = "Red";
		when(device.getPosition()).thenReturn(position);

		config.setPositionType(PositionType.CURRENT);

		var document = handler.devicePositionAsDocument(device, config);
		assertThat(document.getPosition(), equalTo(position));
	}

	@Test
	public void relativePositionNotSupported() {
		config.setPositionType(PositionType.RELATIVE);
		config.setPosition("LEFT");

		assertThrows(GDAException.class, () -> handler.devicePositionAsDocument(device, config));
	}

	@Test
	public void deviceFailureWrappedAsGDAException() throws Exception {
		when(device.getPosition()).thenThrow(DeviceException.class);
		config.setPositionType(PositionType.CURRENT);
		assertThrows(GDAException.class, () -> handler.devicePositionAsDocument(device, config));
	}

}
