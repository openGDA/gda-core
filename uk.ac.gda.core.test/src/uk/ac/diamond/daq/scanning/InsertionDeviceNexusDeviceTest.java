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

package uk.ac.diamond.daq.scanning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.junit.Before;
import org.junit.Test;

import gda.data.ServiceHolder;
import uk.ac.diamond.daq.scanning.InsertionDeviceNexusDevice.InsertionDeviceType;

public class InsertionDeviceNexusDeviceTest {

	private static final String INSERTION_DEVICE_NAME = "insertionDevice";

	private static final String GAP_SCANNABLE_NAME = "gap";
	private static final String TAPER_SCANNABLE_NAME = "taper";
	private static final String HARMONIC_SCANNABLE_NAME = "harmonic";

	private IScannableDeviceService scannableDeviceService;

	private InsertionDeviceNexusDevice insertionDevice;

	@Before
	public void setUp() throws Exception {
		scannableDeviceService = new ScannableDeviceConnectorService();
		new ServiceHolder().setScannableDeviceService(scannableDeviceService);
		scannableDeviceService.register(createMockScannable(GAP_SCANNABLE_NAME, 2.3));
		scannableDeviceService.register(createMockScannable(TAPER_SCANNABLE_NAME, 7.24));
		scannableDeviceService.register(createMockScannable(HARMONIC_SCANNABLE_NAME, 2));

		insertionDevice = new InsertionDeviceNexusDevice();
		insertionDevice.setName(INSERTION_DEVICE_NAME);
		insertionDevice.setType(InsertionDeviceType.WIGGLER.toString());
		insertionDevice.setGapScannableName(GAP_SCANNABLE_NAME);
		insertionDevice.setTaperScannableName(TAPER_SCANNABLE_NAME);
		insertionDevice.setHarmonicScannableName(HARMONIC_SCANNABLE_NAME);
	}

	private <T> IScannable<T> createMockScannable(String name, T position) throws Exception {
		@SuppressWarnings("unchecked")
		final IScannable<T> mockScannable = mock(IScannable.class);
		when(mockScannable.getName()).thenReturn(name);
		when(mockScannable.getPosition()).thenReturn(position);
		return mockScannable;
	}

	@Test
	public void testGetNexusProvider() throws Exception {
		final NexusObjectProvider<NXinsertion_device> nexusObjectProvider = insertionDevice.getNexusProvider(null);
		assertThat(nexusObjectProvider, is(notNullValue()));
		assertThat(nexusObjectProvider.getName(), is(equalTo(INSERTION_DEVICE_NAME)));

		final NXinsertion_device nxInsertionDevice = nexusObjectProvider.getNexusObject();
		assertThat(nxInsertionDevice, is(notNullValue()));
		assertThat(nxInsertionDevice.getTypeScalar(), is(InsertionDeviceType.WIGGLER.toString()));
		assertThat(nxInsertionDevice.getGapScalar(),
				is(equalTo(scannableDeviceService.getScannable(GAP_SCANNABLE_NAME).getPosition())));
		assertThat(nxInsertionDevice.getTaperScalar(),
				is(equalTo(scannableDeviceService.getScannable(TAPER_SCANNABLE_NAME).getPosition())));
	}

}
