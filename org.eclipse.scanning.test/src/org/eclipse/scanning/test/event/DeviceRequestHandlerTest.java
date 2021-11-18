/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.device.DeviceRequestHandler;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DeviceRequestHandlerTest {

	@Mock
	private IScannable<Object> device;

	@Mock
	private IScannableDeviceService service;

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	private static final String SCANNABLE_NAME = "ExampleDevice";


	@Test
	public void testDeviceTypeScannable() throws ScanningException{

		List<String> scannableNames = List.of("Device1", "Device2", "Device3", "Device4");
		Mockito.when(service.getScannableNames()).thenReturn(scannableNames);

		// A request with only the type specified
		final DeviceRequest request = new DeviceRequest(DeviceType.SCANNABLE);
		DeviceRequestHandler handler = new DeviceRequestHandler(null, service, request, null);

		DeviceRequest response = handler.process(request);


		Collection<DeviceInformation<?>> info = response.getDevices();
		// Only the name is set in each DeviceInformation
		assertEquals(scannableNames, info.stream().map(DeviceInformation::getName).collect(Collectors.toList()));
		info.forEach(deviceInfo -> {
			assertNull(deviceInfo.getUnit());
			assertNull(deviceInfo.getUpper());
			assertNull(deviceInfo.getLower());
		});
	}

	@Test
	public void testDeviceName() throws ScanningException {

		Mockito.when(service.getScannable(SCANNABLE_NAME)).thenReturn(device);

		Mockito.when(device.getName()).thenReturn(SCANNABLE_NAME);
		Mockito.when(device.getPosition()).thenReturn(7);
		Mockito.when(device.getLevel()).thenReturn(5);
		Mockito.when(device.getUnit()).thenReturn("mm");
		Mockito.when(device.getMaximum()).thenReturn(10);
		Mockito.when(device.getMinimum()).thenReturn(1);

		// A request with name and device type specified
		final DeviceRequest request = new DeviceRequest(SCANNABLE_NAME, DeviceType.SCANNABLE);
		DeviceRequestHandler handler = new DeviceRequestHandler(null, service, request, null);

		DeviceRequest response = handler.process(request);
		// Response device value equals to device position
		assertEquals(7, response.getDeviceValue());

		// DeviceInformation contains values returned by the device
		DeviceInformation<?> deviceInfo = response.getDeviceInformation();
		assertEquals(SCANNABLE_NAME, deviceInfo.getName());
		assertEquals(5, deviceInfo.getLevel());
		assertEquals("mm", deviceInfo.getUnit());
		assertEquals(10, deviceInfo.getUpper());
		assertEquals(1, deviceInfo.getLower());

	}

	@Test
	public void testGetScannableException() throws ScanningException {
		Mockito.when(service.getScannable(SCANNABLE_NAME)).thenThrow(ScanningException.class);

		// A request with name and device type specified
		final DeviceRequest request = new DeviceRequest(SCANNABLE_NAME, DeviceType.SCANNABLE);
		DeviceRequestHandler handler = new DeviceRequestHandler(null, service, request, null);

		DeviceRequest response = handler.process(request);

		Collection<DeviceInformation<?>> info = response.getDevices();
		// DeviceInformation for device is null when scannable with given name is not found
		assertNull(info);
	}

	@Test
	public void testGetPositionException() throws ScanningException {

		Mockito.when(service.getScannable(SCANNABLE_NAME)).thenReturn(device);
		Mockito.when(device.getPosition()).thenThrow(ScanningException.class);

		// A request with name and device type specified
		final DeviceRequest request = new DeviceRequest(SCANNABLE_NAME, DeviceType.SCANNABLE);
		DeviceRequestHandler handler = new DeviceRequestHandler(null, service, request, null);

		DeviceRequest response = handler.process(request);

		Collection<DeviceInformation<?>> info = response.getDevices();
		// DeviceInformation for device is null when current device position cannot be read
		assertNull(info);
	}

}
