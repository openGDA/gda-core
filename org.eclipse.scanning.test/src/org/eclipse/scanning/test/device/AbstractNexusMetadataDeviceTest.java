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

package org.eclipse.scanning.test.device;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.Services;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractNexusMetadataDeviceTest<N extends NXobject> {

	protected static final String UNITS_ATTR_VAL_MILLIMETERS = "mm";
	protected static final String UNITS_ATTR_VAL_DEGREES = "deg";
	protected static final String UNITS_ATTR_VAL_GEV = "GeV";
	protected static final String UNITS_ATTR_VAL_FLUX = "1/s/cm^2";
	protected static final String UNITS_ATTR_VAL_KELVIN = "K";
	protected static final String UNITS_ATTR_VAL_AMPS = "A";

	protected IScannableDeviceService scannableDeviceService;

	private INexusDevice<N> nexusDevice;

	@BeforeEach
	public void setUp() throws Exception {
		scannableDeviceService = new MockScannableConnector(null);
		new Services().setScannableDeviceService(scannableDeviceService);
		setupTestFixtures();
		nexusDevice = setupNexusDevice();
	}

	protected void setupTestFixtures() throws Exception {
		// do nothing, subclasses may override
	}

	protected abstract INexusDevice<N> setupNexusDevice() throws Exception;

	protected INexusDevice<N> getNexusDevice() {
		return nexusDevice;
	}

	protected <T> IScannable<T> createMockScannable(String name, T position) throws Exception {
		return createMockScannable(name, position, null);
	}

	protected <T> IScannable<T> createMockScannable(String name, T position, String units) throws Exception {
		@SuppressWarnings("unchecked")
		final IScannable<T> mockScannable = mock(IScannable.class);
		when(mockScannable.getName()).thenReturn(name);
		when(mockScannable.getPosition()).thenReturn(position);
		when(mockScannable.getUnit()).thenReturn(units);
		scannableDeviceService.register(mockScannable);
		return mockScannable;
	}

	protected <T> IScannable<T> createThrowingScannable(String name) throws Exception {
		@SuppressWarnings("unchecked")
		final IScannable<T> mockScannable = mock(IScannable.class);
		when(mockScannable.getName()).thenReturn(name);
		when(mockScannable.getPosition()).thenThrow(ScanningException.class);
		scannableDeviceService.register(mockScannable);
		return mockScannable;
	}

	protected Object getScannableValue(String scannableName) throws Exception {
		return scannableDeviceService.getScannable(scannableName).getPosition();
	}

	@Test
	public void testGetNexusProvider() throws Exception {
		final NexusObjectProvider<N> nexusProvider = nexusDevice.getNexusProvider(null);
		assertThat(nexusProvider, is(notNullValue()));
		checkNexusProvider(nexusProvider);

		final N nexusObject = nexusProvider.getNexusObject();
		assertThat(nexusObject, is(notNullValue()));
		checkNexusObject(nexusObject);
	}

	protected void checkNexusProvider(NexusObjectProvider<N> nexusProvider) {
		assertThat(nexusProvider.getName(), is(equalTo(nexusDevice.getName())));
	}

	protected abstract void checkNexusObject(N nexusObject) throws Exception;

}
