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

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;

import gda.configuration.properties.LocalProperties;


/**
 * A nexus data writer that stores the XAS xml files contents.
 */
public class XasNexusDataWriter extends NexusDataWriter {

	public XasNexusDataWriter() throws InstantiationException {
		super();
		setupProperties();
	}

	public XasNexusDataWriter(int fileNumber) throws InstantiationException {
		super(fileNumber);
		setupProperties();
	}

	@Override
	public void createNextFile() throws Exception {
		if (getNexusFileNameTemplate() == null) {
			if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX))
				setNexusFileNameTemplate("nexus/%d_" + LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".nxs");
			else
				setNexusFileNameTemplate("nexus/%d.nxs");
		}
		super.createNextFile();
	}
	@Override
	public void completeCollection() throws Exception {
		//TODO Check if following will work when used as nested scan, the old file.opengroup method did not.
		IDataset data = DatasetFactory.createFromObject(new int[] {scanPointNumber+1});
		file.createData(this.entryName, "scan_dimensions", data, true);
		super.completeCollection();
	}
}
