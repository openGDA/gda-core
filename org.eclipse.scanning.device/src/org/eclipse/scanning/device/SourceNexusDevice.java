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
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;

/**
 * An {@link INexusDevice} implementation that adds an {@link NXsource} to the nexus tree.
 */
public class SourceNexusDevice extends AbstractNexusMetadataDevice<NXsource> {

	public static final String DEFAULT_SOURCE_NAME = "Diamond Light Source";
	public static final String DEFAULT_TYPE = "Synchrotron X-ray Source";
	public static final String DEFAULT_PROBE = "x-ray";

	private String sourceName = DEFAULT_SOURCE_NAME;

	private String type = DEFAULT_TYPE;

	private String probe = DEFAULT_PROBE;

	public void setCurrentScannableName(String currentScannableName) {
		addScannableField(NXsource.NX_CURRENT, currentScannableName);
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setProbe(String probe) {
		this.probe = probe;
	}

	@Override
	protected void writeFields(NXsource source) throws NexusException {
		super.writeFields(source);

		// use the long name as the value of the name field
		source.setNameScalar(sourceName);
		source.setTypeScalar(type);
		source.setProbeScalar(probe);
	}

	@Override
	public NexusObjectProvider<NXsource> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXsource source = NexusNodeFactory.createNXsource();
		writeFields(source);

		return new NexusObjectWrapper<>(getName(), source);
	}

}
