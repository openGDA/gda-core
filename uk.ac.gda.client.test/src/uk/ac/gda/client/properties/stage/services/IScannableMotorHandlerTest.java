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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.Scannable;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue.PositionType;
import uk.ac.gda.common.exception.GDAException;

public class IScannableMotorHandlerTest {

	private DeviceHandler handler = new IScannableMotorHandler();

	@Mock
	private IScannableMotor device;

	private ScannablePropertiesValue config = new ScannablePropertiesValue();

	@Rule
	public MockitoRule initDevice = MockitoJUnit.rule();

	@Test
	public void handlesScannableMotors() throws Exception {
		var deviceName = "motor";
		when(device.getName()).thenReturn(deviceName);

		config.setPosition(0.0);
		var document = handler.devicePositionAsDocument(device, config);
		assertNotNull(document);

		// will only test name in this test (fairly boring)
		assertThat(document.getDevice(), equalTo(deviceName));
	}

	@Test
	public void doesNotHandleNonScannableMotors() throws Exception {
		// a scannable other than IScannableMotor
		var document = handler.devicePositionAsDocument(mock(Scannable.class), config);
		// null means we cannot handle it
		assertNull(document);
	}

	@Test
	public void documentForAbsolutePosition() throws Exception {
		config.setPositionType(PositionType.ABSOLUTE);
		var position = 12.5;
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

	@Test
	public void documentForCurrentPosition() throws Exception {
		var position = 0.5;
		when(device.getPosition()).thenReturn(position);

		config.setPositionType(PositionType.CURRENT);

		var document = handler.devicePositionAsDocument(device, config);
		assertThat(document.getPosition(), equalTo(position));
	}

	@Test
	public void deviceFailureWrappedAsGDAException() throws Exception {
		when(device.getPosition()).thenThrow(DeviceException.class);
		config.setPositionType(PositionType.CURRENT);
		assertThrows(GDAException.class, () -> handler.devicePositionAsDocument(device, config));
	}

	@Test
	public void documentForRelativePosition() throws Exception {
		var devicePosition = 3.59;
		when(device.getPosition()).thenReturn(devicePosition);

		var relativePosition = -0.13;
		config.setPositionType(PositionType.RELATIVE);
		config.setPosition(relativePosition);

		var document = handler.devicePositionAsDocument(device, config);
		assertThat(document.getPosition(), equalTo(devicePosition + relativePosition));
	}

	@Test
	public void failsWhenRelativePositionNotConfigured() {
		config.setPositionType(PositionType.RELATIVE);
		assertThrows(GDAException.class,
				() -> handler.devicePositionAsDocument(device, config));
	}

}
