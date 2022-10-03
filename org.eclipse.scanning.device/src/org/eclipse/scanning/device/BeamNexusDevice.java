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

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NexusBaseClass;

/**
 * An {@link INexusDevice} implementation that adds an {@link NXbeam} to the nexus tree.
 */
public final class BeamNexusDevice extends AbstractNexusMetadataDevice<NXbeam> {

	private static final String UNITS_MILLIS = "mm";

	public BeamNexusDevice() {
		super(NexusBaseClass.NX_BEAM);
		setCategory(NexusBaseClass.NX_SAMPLE);
		addField(new ScalarField(NXbeam.NX_DISTANCE, 0.0, UNITS_MILLIS, true));
	}

	public void setIncidentEnergyScannableName(String incidentEnergyScannableName) {
		addScannableField(NXbeam.NX_INCIDENT_ENERGY, incidentEnergyScannableName);
	}

	/**
	 * Set the distance from the sample to be written to the {@link NXbeam#setDistanceScalar(Double)}
	 * in the generated {@link NXbeam}. The default is {@code 0.0mm} as the {@link NXbeam}
	 * is normally added to the nexus tree as a child group of the {@link NXsample}.
	 * @param distance the distance from the sample
	 */
	public void setDistance(double distance) {
		final ScalarField distanceField = (ScalarField) getNode(NXbeam.NX_DISTANCE);
		distanceField.setValue(distance);
	}

	public void setIncidentEnergyLinkPath(String energyLinkPath) {
		addLinkedField(NXbeam.NX_INCIDENT_ENERGY, energyLinkPath);
	}

	public void setIncidentBeamDivergenceScannableName(String incidentBeamDivergenceScannableName) {
		addScannableField(NXbeam.NX_INCIDENT_BEAM_DIVERGENCE, incidentBeamDivergenceScannableName);
	}

	public void setIncidentPolarizationScannableName(String incidentPolarizationScannableName) {
		addScannableField(NXbeam.NX_INCIDENT_POLARIZATION, incidentPolarizationScannableName);
	}

	public void setBeamExtentScannableName(String beamExtentScannableName) {
		addScannableField(NXbeam.NX_EXTENT, beamExtentScannableName);
	}

	public void setFluxScannableName(String fluxScannableName) {
		addScannableField(NXbeam.NX_FLUX, fluxScannableName);
	}

}