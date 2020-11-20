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

import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

public class SourceNexusDevice extends AbstractNexusMetadataDevice<NXsource> {

	private String currentScannableName;

	private String longName;

	public void setCurrentScannableName(String currentScannableName) {
		this.currentScannableName = currentScannableName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	@Override
	public NexusObjectProvider<NXsource> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXsource source = NexusNodeFactory.createNXsource();

		// use the long name as the value of the name field
		source.setNameScalar(longName);
		// use the (short) name of the device as the short name
		source.setAttribute(NXsource.NX_NAME, NXsource.NX_NAME_ATTRIBUTE_SHORT_NAME, getName());
		writeScannableValue(source, NXsource.NX_CURRENT, currentScannableName);

		return new NexusObjectWrapper<>(getName(), source);
	}

}
