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
import org.eclipse.dawnsci.nexus.NexusBaseClass;

/**
 * An {@link INexusDevice} implementation that adds an {@link NXsource} to the nexus tree.
 */
public class SourceNexusDevice extends AbstractNexusMetadataDevice<NXsource> {

	public static final String DEFAULT_SOURCE_NAME = "Diamond Light Source";
	public static final String DEFAULT_TYPE = "Synchrotron X-ray Source";
	public static final String DEFAULT_PROBE = "x-ray";

	public SourceNexusDevice() {
		super(NexusBaseClass.NX_SOURCE);
		addScalarField(NXsource.NX_NAME, DEFAULT_SOURCE_NAME);
		addScalarField(NXsource.NX_TYPE, DEFAULT_TYPE);
		addScalarField(NXsource.NX_PROBE, DEFAULT_PROBE);
	}

	public void setCurrentScannableName(String currentScannableName) {
		addScannableField(NXsource.NX_CURRENT, currentScannableName);
	}

	public void setSourceName(String sourceName) {
		addScalarField(NXsource.NX_NAME, sourceName);
	}

	public void setType(String type) {
		addScalarField(NXsource.NX_TYPE, type);
	}

	public void setProbe(String probe) {
		addScalarField(NXsource.NX_PROBE, probe);
	}

}
