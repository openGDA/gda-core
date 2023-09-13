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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.DummyMultiFieldUnitsScannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractNexusMetadataDeviceTest<N extends NXobject> {

	protected static final String UNITS_ATTR_VAL_MILLIMETERS = "mm";
	protected static final String UNITS_ATTR_VAL_DEGREES = "deg";
	protected static final String UNITS_ATTR_VAL_GEV = "GeV";
	protected static final String UNITS_ATTR_VAL_FLUX = "1/s/cm^2";
	protected static final String UNITS_ATTR_VAL_KELVIN = "K";
	protected static final String UNITS_ATTR_VAL_AMPS = "A";

	private static final String[] NO_NAMES = new String[0];

	protected IScannableDeviceService scannableDeviceService;

	private INexusDevice<N> nexusDevice;

	@BeforeEach
	public void setUp() throws Exception {
		scannableDeviceService = new ScannableDeviceConnectorService();
		ServiceProvider.setService(IScannableDeviceService.class, scannableDeviceService);
		setupTestFixtures();
		nexusDevice = setupNexusDevice();
	}

	@AfterEach
	public void tearDown() {
		Finder.removeAllFactories();
		ServiceProvider.reset();
	}

	@SuppressWarnings("unused") // suppresses exception not thrown warning - overridden method in subclasses may throw exception
	protected void setupTestFixtures() throws Exception {
		// do nothing, subclasses may override
	}

	protected abstract INexusDevice<N> setupNexusDevice() throws Exception;

	protected INexusDevice<N> getNexusDevice() {
		return nexusDevice;
	}

	protected Scannable createMockScannable(String name, Object position) throws Exception {
		return createMockScannable(name, position, null);
	}

	protected Scannable createMockScannable(String name, Object position, String units) throws Exception {
		final ScannableMotionUnits mockScannable = mock(ScannableMotionUnits.class);
		when(mockScannable.getName()).thenReturn(name);
		when(mockScannable.getPosition()).thenReturn(position);
		when(mockScannable.getUserUnits()).thenReturn(units);
		when(mockScannable.getInputNames()).thenReturn(new String[] { name });
		when(mockScannable.getExtraNames()).thenReturn(NO_NAMES);

		return mockScannable;
	}

	protected Scannable createMultiFieldMockScannable(String name, String[] inputNames,
			String[] extraNames, Object[] position, String units) throws DeviceException {
		final DummyMultiFieldUnitsScannable<?> scannable = new DummyMultiFieldUnitsScannable<>(name, units);
		scannable.setInputNames(inputNames);
		scannable.setExtraNames(extraNames);
		scannable.setCurrentPosition(position);

		return scannable;
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
