/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import gda.configuration.properties.LocalProperties;


/**
 * A nexus data writer that stores the XAS xml files contents.
 */
public class XasNexusDataWriter extends NexusDataWriter {

	public XasNexusDataWriter() {
		super();
		setDefaultNexusFileNameTemplate();
		// set the default templates - may be changed later on by call to {@link #setNexusFileNameTemplate}
	}

	public XasNexusDataWriter(int fileNumber) {
		super(fileNumber);
		setDefaultNexusFileNameTemplate();

		// run configureScanNumber again to set the filename using the updated filename template
		configureScanNumber(fileNumber);
	}

	public void setDefaultNexusFileNameTemplate() {
		if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX))
			super.setNexusFileNameTemplate("nexus/%d_" + LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".nxs");
		else
			super.setNexusFileNameTemplate("nexus/%d.nxs");
	}
}
