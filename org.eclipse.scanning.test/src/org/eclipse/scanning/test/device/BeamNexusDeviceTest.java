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
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.device.BeamNexusDevice;

public class BeamNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXbeam> {

	private static final String INCIDENT_ENERGY_SCANNABLE_NAME = "energy";
	private static final String INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME = "incident_beam_divergence";
    private static final String BEAM_EXTENT_SCANNABLE_NAME = "beam_extent";
	private static final String INCIDENT_POLARIZATION_SCANNABLE_NAME = "incident_polarization";
	private static final String FLUX_SCANNABLE_NAME = "flux";

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(INCIDENT_ENERGY_SCANNABLE_NAME, 234.88);
		createMockScannable(INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME, 1.234);
		createMockScannable(INCIDENT_POLARIZATION_SCANNABLE_NAME, 3.683);
		createMockScannable(BEAM_EXTENT_SCANNABLE_NAME, 0.01);
		createMockScannable(FLUX_SCANNABLE_NAME, 843.23);
	}

	@Override
	protected INexusDevice<NXbeam> setupNexusDevice() throws Exception {
		final BeamNexusDevice beamScannable = new BeamNexusDevice();
		beamScannable.setBeamExtentScannableName(BEAM_EXTENT_SCANNABLE_NAME);
		beamScannable.setIncidentEnergyScannableName(INCIDENT_ENERGY_SCANNABLE_NAME);
		beamScannable.setIncidentBeamDivergenceScannableName(INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME);
		beamScannable.setIncidentPolarizationScannableName(INCIDENT_POLARIZATION_SCANNABLE_NAME);
		beamScannable.setFluxScannableName(FLUX_SCANNABLE_NAME);
		return beamScannable;
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

		assertThat(beam.getDistanceScalar(), is(closeTo(0.0, 1e-15)));
		assertThat(beam.getIncident_energyScalar(), is(equalTo(getScannableValue(INCIDENT_ENERGY_SCANNABLE_NAME))));
		assertThat(beam.getIncident_beam_divergenceScalar(), is(equalTo(getScannableValue(INCIDENT_BEAM_DIVERGENCE_SCANNABLE_NAME))));
		assertThat(beam.getIncident_polarizationScalar(), is(equalTo(getScannableValue(INCIDENT_POLARIZATION_SCANNABLE_NAME))));
		assertThat(beam.getFluxScalar(), is(equalTo(getScannableValue(FLUX_SCANNABLE_NAME))));
	}

}
