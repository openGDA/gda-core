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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice.InsertionDeviceType;

public class InsertionDeviceNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXinsertion_device> {

	private static final String GAP_SCANNABLE_NAME = "gap";
	private static final String TAPER_SCANNABLE_NAME = "taper";
	private static final String HARMONIC_SCANNABLE_NAME = "harmonic";

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(GAP_SCANNABLE_NAME, 2.3);
		createMockScannable(TAPER_SCANNABLE_NAME, 7.24);
		createMockScannable(HARMONIC_SCANNABLE_NAME, 2);
	}

	@Override
	protected INexusDevice<NXinsertion_device> setupNexusDevice() throws Exception {
		final InsertionDeviceNexusDevice insertionDevice = new InsertionDeviceNexusDevice();
		insertionDevice.setName("insertionDevice");
		insertionDevice.setType(InsertionDeviceType.WIGGLER.toString());
		insertionDevice.setGapScannableName(GAP_SCANNABLE_NAME);
		insertionDevice.setTaperScannableName(TAPER_SCANNABLE_NAME);
		insertionDevice.setHarmonicScannableName(HARMONIC_SCANNABLE_NAME);
		return insertionDevice;
	}

	@Override
	protected void checkNexusObject(NXinsertion_device nxInsertionDevice) throws Exception {
		assertThat(nxInsertionDevice.getTypeScalar(), is(InsertionDeviceType.WIGGLER.toString()));
		assertThat(nxInsertionDevice.getGapScalar(), is(equalTo(getScannableValue(GAP_SCANNABLE_NAME))));
		assertThat(nxInsertionDevice.getTaperScalar(), is(equalTo(getScannableValue(TAPER_SCANNABLE_NAME))));
	}

}
