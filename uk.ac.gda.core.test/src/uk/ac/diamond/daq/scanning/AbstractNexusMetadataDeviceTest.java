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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.junit.Before;
import org.junit.Test;

import gda.data.ServiceHolder;

public abstract class AbstractNexusMetadataDeviceTest<N extends NXobject> {

	protected IScannableDeviceService scannableDeviceService;

	private INexusDevice<N> nexusDevice;

	@Before
	public void setUp() throws Exception {
		scannableDeviceService = new ScannableDeviceConnectorService();
		new ServiceHolder().setScannableDeviceService(scannableDeviceService);
		setupMockScannables();
		nexusDevice = setupNexusDevice();
	}

	protected abstract void setupMockScannables() throws Exception;

	protected abstract INexusDevice<N> setupNexusDevice() throws Exception ;

	protected <T> IScannable<T> createMockScannable(String name, T position) throws Exception {
		@SuppressWarnings("unchecked")
		final IScannable<T> mockScannable = mock(IScannable.class);
		when(mockScannable.getName()).thenReturn(name);
		when(mockScannable.getPosition()).thenReturn(position);
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
