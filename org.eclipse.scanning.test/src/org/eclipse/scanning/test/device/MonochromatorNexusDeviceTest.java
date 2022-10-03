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

import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertUnits;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcrystal;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.scanning.device.GroupMetadataNode;
import org.eclipse.scanning.device.MetadataNode;
import org.eclipse.scanning.device.MonochromatorNexusDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableField;

public class MonochromatorNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXmonochromator> {

	private static final String MONOCHROMATOR_NAME = "mono";
	private static final String ENERGY_SCANNABLE_NAME = "energy";
	private static final String ENERGY_ERROR_SCANNABLE_NAME = "energyErrors";

	private static final String CRYSTAL1_GROUP_NAME = "crystal1";
	private static final String CRYSTAL2_GROUP_NAME = "crystal2";
	private static final String EXPECTED_CRYSTAL_USAGE = "Bragg";
	private static final String EXPECTED_CRYSTAL_TYPE = "Diamond";
	private static final String CRYSTAL1_TEMPERATURE_SCANNABLE_NAME = "c1temp";
	private static final String CRYSTAL2_TEMPERATURE_SCANNABLE_NAME = "c2temp";

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(ENERGY_SCANNABLE_NAME, 358.89, UNITS_ATTR_VAL_GEV);
		createMockScannable(ENERGY_ERROR_SCANNABLE_NAME, 0.1, UNITS_ATTR_VAL_GEV);

		createMockScannable(CRYSTAL1_TEMPERATURE_SCANNABLE_NAME, 52.5, UNITS_ATTR_VAL_KELVIN);
		createMockScannable(CRYSTAL2_TEMPERATURE_SCANNABLE_NAME, 63.2, UNITS_ATTR_VAL_KELVIN);
	}

	@Override
	protected INexusDevice<NXmonochromator> setupNexusDevice() throws Exception {
		final MonochromatorNexusDevice monochromatorDevice = new MonochromatorNexusDevice();
		monochromatorDevice.setName(MONOCHROMATOR_NAME);
		monochromatorDevice.setEnergyScannableName(ENERGY_SCANNABLE_NAME);
		monochromatorDevice.setEnergyErrorScannableName(ENERGY_ERROR_SCANNABLE_NAME);

		// TODO or should there be a custom setter for crystal?
		final GroupMetadataNode<NXcrystal> crystal1 = createCrystalNode(CRYSTAL1_GROUP_NAME, 1, CRYSTAL1_TEMPERATURE_SCANNABLE_NAME);
		final GroupMetadataNode<NXcrystal> crystal2 = createCrystalNode(CRYSTAL2_GROUP_NAME, 2, CRYSTAL2_TEMPERATURE_SCANNABLE_NAME);
		monochromatorDevice.setCustomNodes(Arrays.asList(crystal1, crystal2));

		return monochromatorDevice;
	}

	private GroupMetadataNode<NXcrystal> createCrystalNode(String name, int number,
			String temperatureScannableName) {
		final GroupMetadataNode<NXcrystal> crystal = new GroupMetadataNode<>(name, NexusBaseClass.NX_CRYSTAL);

		final List<MetadataNode> crystalFields = new ArrayList<>();
		crystalFields.add(new ScalarField(NXcrystal.NX_USAGE, EXPECTED_CRYSTAL_USAGE));
		crystalFields.add(new ScalarField(NXcrystal.NX_TYPE, EXPECTED_CRYSTAL_TYPE));
		crystalFields.add(new ScalarField(NXcrystal.NX_ORDER_NO, number));
		crystalFields.add(new ScannableField(NXcrystal.NX_TEMPERATURE, temperatureScannableName, UNITS_ATTR_VAL_KELVIN));
		crystal.addChildNodes(crystalFields);

		return crystal;
	}

	@Override
	protected void checkNexusObject(NXmonochromator monochromator) throws Exception {
		assertThat(monochromator.getEnergyScalar(), is(equalTo(getScannableValue(ENERGY_SCANNABLE_NAME))));
		assertUnits(monochromator, NXmonochromator.NX_ENERGY, UNITS_ATTR_VAL_GEV);
		assertThat(monochromator.getEnergy_errorScalar(), is(equalTo(getScannableValue(ENERGY_ERROR_SCANNABLE_NAME))));
		assertUnits(monochromator, NXmonochromator.NX_ENERGY_ERROR, UNITS_ATTR_VAL_GEV);

		final String[] crystalNames = { CRYSTAL1_GROUP_NAME, CRYSTAL2_GROUP_NAME };
		final String[] crystalTempScannableNames = { CRYSTAL1_TEMPERATURE_SCANNABLE_NAME, CRYSTAL2_TEMPERATURE_SCANNABLE_NAME };
		assertThat(monochromator.getGroupNodeNames(), containsInAnyOrder(crystalNames));
		assertThat(monochromator.getAllCrystal().size(), is(crystalNames.length));
		for (int i = 0; i < crystalNames.length; i++) {
			final NXcrystal crystal = monochromator.getCrystal(crystalNames[i]);
			assertThat(crystal, is(notNullValue()));
			assertThat(crystal.getDataNodeNames(), containsInAnyOrder(
					NXcrystal.NX_USAGE, NXcrystal.NX_TYPE, NXcrystal.NX_ORDER_NO, NXcrystal.NX_TEMPERATURE));
			assertThat(crystal.getTemperatureScalar(), is(equalTo(getScannableValue(crystalTempScannableNames[i]))));
			assertUnits(crystal, NXcrystal.NX_TEMPERATURE, UNITS_ATTR_VAL_KELVIN);
		}
	}
}
