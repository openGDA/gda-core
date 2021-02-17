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

package org.eclipse.scanning.device;

import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

public final class BeamNexusDevice extends AbstractNexusMetadataDevice<NXbeam> {

	// use NXbeam.NX_EXTENT once nexus base classes have been regenerated (DAQ-2948)
	public static final String FIELD_NAME_EXTENT = "extent";

	private String incidentEnergyScannableName;
	private String incidentBeamDivergenceScannableName;
	private String incidentPolarizationScannableName;
	private String beamExtentScannableName;
	private String fluxScannableName;

	@Override
	public NexusObjectProvider<NXbeam> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXbeam beam = NexusNodeFactory.createNXbeam();
		beam.setDistanceScalar(0.0); // since this is the beam at the sample, the distance is 0
		writeScannableValue(beam, NXbeam.NX_INCIDENT_ENERGY, incidentEnergyScannableName);
		writeScannableValue(beam, NXbeam.NX_INCIDENT_BEAM_DIVERGENCE, incidentBeamDivergenceScannableName);
		writeScannableValue(beam, NXbeam.NX_INCIDENT_POLARIZATION, incidentPolarizationScannableName);
		writeScannableValue(beam, NXbeam.NX_FLUX, fluxScannableName);
		writeScannableValue(beam, FIELD_NAME_EXTENT, beamExtentScannableName);

		final NexusObjectWrapper<NXbeam> nexusWrapper = new NexusObjectWrapper<>(getName(), beam);
		nexusWrapper.setCategory(NexusBaseClass.NX_SAMPLE);
		return nexusWrapper;
	}

	public void setIncidentEnergyScannableName(String incidentEnergyScannableName) {
		this.incidentEnergyScannableName = incidentEnergyScannableName;
	}

	public void setIncidentBeamDivergenceScannableName(String incidentBeamDivergenceScannableName) {
		this.incidentBeamDivergenceScannableName = incidentBeamDivergenceScannableName;
	}

	public void setIncidentPolarizationScannableName(String incidentPolarizationScannableName) {
		this.incidentPolarizationScannableName = incidentPolarizationScannableName;
	}

	public void setBeamExtentScannableName(String beamExtentScannableName) {
		this.beamExtentScannableName = beamExtentScannableName;
	}

	public void setFluxScannableName(String fluxScannableName) {
		this.fluxScannableName = fluxScannableName;
	}

}