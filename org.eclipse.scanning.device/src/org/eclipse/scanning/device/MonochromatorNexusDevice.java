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

import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

public class MonochromatorNexusDevice extends AbstractNexusMetadataDevice<NXmonochromator> {

	private String energyScannableName;

	private String energyErrorScannableName;

	public void setEnergyScannableName(String energyScannableName) {
		this.energyScannableName = energyScannableName;
	}

	public void setEnergyErrorScannableName(String energyErrorScannableName) {
		this.energyErrorScannableName = energyErrorScannableName;
	}

	@Override
	public NexusObjectProvider<NXmonochromator> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXmonochromator monochromator = NexusNodeFactory.createNXmonochromator();

		writeScannableValue(monochromator, NXmonochromator.NX_ENERGY, energyScannableName);
		writeScannableValue(monochromator, NXmonochromator.NX_ENERGY_ERROR, energyErrorScannableName);

		return new NexusObjectWrapper<>(getName(), monochromator);
	}


}
