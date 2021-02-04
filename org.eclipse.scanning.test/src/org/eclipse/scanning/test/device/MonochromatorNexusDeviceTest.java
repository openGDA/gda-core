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
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.scanning.device.MonochromatorNexusDevice;

public class MonochromatorNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXmonochromator> {

	private static final String MONOCHROMATOR_NAME = "mono";

	private static final String ENERGY_SCANNABLE_NAME = "energy";

	private static final String ENERGY_ERROR_SCANNABLE_NAME = "energyErrors";

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(ENERGY_SCANNABLE_NAME, 358.89);
		createMockScannable(ENERGY_ERROR_SCANNABLE_NAME, 0.1);
	}

	@Override
	protected INexusDevice<NXmonochromator> setupNexusDevice() throws Exception {
		final MonochromatorNexusDevice monochromatorDevice = new MonochromatorNexusDevice();
		monochromatorDevice.setName(MONOCHROMATOR_NAME);
		monochromatorDevice.setEnergyScannableName(ENERGY_SCANNABLE_NAME);
		monochromatorDevice.setEnergyErrorScannableName(ENERGY_ERROR_SCANNABLE_NAME);
		return monochromatorDevice;
	}

	@Override
	protected void checkNexusObject(NXmonochromator monochromator) throws Exception {
		assertThat(monochromator.getEnergyScalar(), is(equalTo(getScannableValue(ENERGY_SCANNABLE_NAME))));
		assertThat(monochromator.getEnergy_errorScalar(), is(equalTo(getScannableValue(ENERGY_ERROR_SCANNABLE_NAME))));
	}

}
