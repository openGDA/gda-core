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
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

/**
 * An {@link INexusDevice} implementation that adds an {@link NXmonochromator} to the nexus tree.
 */
public class MonochromatorNexusDevice extends AbstractNexusMetadataDevice<NXmonochromator> {

	public void setEnergyScannableName(String energyScannableName) {
		addScannableField(NXmonochromator.NX_ENERGY, energyScannableName);
	}

	public void setEnergyErrorScannableName(String energyErrorScannableName) {
		addScannableField(NXmonochromator.NX_ENERGY_ERROR, energyErrorScannableName);
	}

	@Override
	public NexusObjectProvider<NXmonochromator> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXmonochromator monochromator = NexusNodeFactory.createNXmonochromator();
		writeFields(monochromator);

		return new NexusObjectWrapper<>(getName(), monochromator);
	}


}
