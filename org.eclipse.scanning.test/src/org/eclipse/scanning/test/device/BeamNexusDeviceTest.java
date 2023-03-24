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
import static org.eclipse.scanning.device.AbstractMetadataField.ATTRIBUTE_NAME_LOCAL_NAME;
import static org.eclipse.scanning.device.AbstractMetadataField.ATTRIBUTE_NAME_UNITS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.device.BeamNexusDevice;
import org.hamcrest.Matchers;

import gda.TestHelpers;
import gda.factory.Factory;
import gda.factory.Finder;

class BeamNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXbeam> {

	private static final String INCIDENT_ENERGY_SCANNABLE_NAME = "energy";
	private static final String INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME = "incident_beam_divergence";
	private static final String BEAM_EXTENT_SCANNABLE_NAME = "beam_extent";
	private static final String INCIDENT_POLARIZATION_SCANNABLE_NAME = "incident_polarization";
	private static final String FLUX_SCANNABLE_NAME = "flux";
	private static final double BEAM_DISTANCE = 123.456;

	@Override
	protected void setupTestFixtures() throws Exception {
		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(createMockScannable(INCIDENT_ENERGY_SCANNABLE_NAME, 234.88, UNITS_ATTR_VAL_GEV));
		factory.addFindable(createMultiFieldMockScannable(INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME,
				new String[0], new String[] { "horizontal_divergence", "vertical_divergence" },
				new Object[] { 12.34, 56.78 }, UNITS_ATTR_VAL_DEGREES));

		factory.addFindable(createMockScannable(INCIDENT_POLARIZATION_SCANNABLE_NAME, 3.683));
		factory.addFindable(createMockScannable(BEAM_EXTENT_SCANNABLE_NAME, 0.01, UNITS_ATTR_VAL_MILLIMETERS));
		factory.addFindable(createMockScannable(FLUX_SCANNABLE_NAME, 843.23, UNITS_ATTR_VAL_FLUX));
		Finder.addFactory(factory);
	}

	@Override
	protected INexusDevice<NXbeam> setupNexusDevice() throws Exception {
		final BeamNexusDevice beamNexusDevice = new BeamNexusDevice();
		beamNexusDevice.setBeamExtentScannableName(BEAM_EXTENT_SCANNABLE_NAME);
		beamNexusDevice.setIncidentEnergyScannableName(INCIDENT_ENERGY_SCANNABLE_NAME);
		beamNexusDevice.setIncidentBeamDivergenceScannableName(INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME);
		beamNexusDevice.setIncidentPolarizationScannableName(INCIDENT_POLARIZATION_SCANNABLE_NAME);
		beamNexusDevice.setFluxScannableName(FLUX_SCANNABLE_NAME);
		beamNexusDevice.setDistance(BEAM_DISTANCE);
		return beamNexusDevice;
	}

	@Override
	protected void checkNexusProvider(NexusObjectProvider<NXbeam> nexusProvider) {
		assertThat(nexusProvider, is(notNullValue()));
		assertThat(nexusProvider.getCategory(), is(NexusBaseClass.NX_SAMPLE));
	}

	@Override
	protected void checkNexusObject(NXbeam beam) throws Exception {
		assertThat(beam, is(notNullValue()));
		final DataNode extentDataNode = beam.getDataNode(NXbeam.NX_EXTENT);
		assertThat(extentDataNode, is(notNullValue()));
		final IDataset extentDataset = extentDataNode.getDataset().getSlice();
		assertThat(extentDataset.getDouble(), is(equalTo(getScannableValue(BEAM_EXTENT_SCANNABLE_NAME))));

		assertThat(beam.getDistanceScalar(), is(closeTo(BEAM_DISTANCE, 1e-15)));
		assertThat(beam.getDataNode(NXbeam.NX_DISTANCE).getAttributeNames(), Matchers.contains(ATTRIBUTE_NAME_UNITS));
		assertUnits(beam, NXbeam.NX_DISTANCE, UNITS_ATTR_VAL_MILLIMETERS);

		assertThat(beam.getIncident_energyScalar(), is(equalTo(getScannableValue(INCIDENT_ENERGY_SCANNABLE_NAME))));
		assertThat(beam.getDataNode(NXbeam.NX_INCIDENT_ENERGY).getAttributeNames(),
				containsInAnyOrder(ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_UNITS));

		assertUnits(beam, NXbeam.NX_INCIDENT_ENERGY, UNITS_ATTR_VAL_GEV);
		assertThat(beam.getAttrString(NXbeam.NX_INCIDENT_ENERGY, ATTRIBUTE_NAME_LOCAL_NAME),
				is(equalTo(INCIDENT_ENERGY_SCANNABLE_NAME + "." + INCIDENT_ENERGY_SCANNABLE_NAME)));

		assertThat(beam.getDataNode(NXbeam.NX_INCIDENT_BEAM_DIVERGENCE).getDataset(),
				is(equalTo(DatasetFactory.createFromObject(getScannableValue(INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME)))));
		assertThat(beam.getDataNode(NXbeam.NX_INCIDENT_BEAM_DIVERGENCE).getAttributeNames(),
				containsInAnyOrder(ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_UNITS));
		assertUnits(beam, NXbeam.NX_INCIDENT_BEAM_DIVERGENCE, UNITS_ATTR_VAL_DEGREES);
		assertThat(beam.getAttrString(NXbeam.NX_INCIDENT_BEAM_DIVERGENCE, ATTRIBUTE_NAME_LOCAL_NAME), is(equalTo(
				INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME)));

		assertThat(beam.getIncident_polarizationScalar(), is(equalTo(getScannableValue(INCIDENT_POLARIZATION_SCANNABLE_NAME))));
		assertThat(beam.getDataNode(NXbeam.NX_INCIDENT_POLARIZATION).getAttributeNames(), contains(ATTRIBUTE_NAME_LOCAL_NAME));
		assertUnits(beam, NXbeam.NX_INCIDENT_POLARIZATION, null);
		assertThat(beam.getAttrString(NXbeam.NX_INCIDENT_POLARIZATION, ATTRIBUTE_NAME_LOCAL_NAME),
				is(equalTo(INCIDENT_POLARIZATION_SCANNABLE_NAME + "." + INCIDENT_POLARIZATION_SCANNABLE_NAME)));

		assertThat(beam.getFluxScalar(), is(equalTo(getScannableValue(FLUX_SCANNABLE_NAME))));
		assertThat(beam.getDataNode(NXbeam.NX_FLUX).getAttributeNames(), containsInAnyOrder(ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_UNITS));
		assertUnits(beam, NXbeam.NX_FLUX, UNITS_ATTR_VAL_FLUX);
		assertThat(beam.getAttrString(NXbeam.NX_FLUX, ATTRIBUTE_NAME_LOCAL_NAME), is(equalTo(FLUX_SCANNABLE_NAME + "." + FLUX_SCANNABLE_NAME)));
	}

}
