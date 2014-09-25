/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan.preparers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.server.exafs.scan.OutputPreparer;

public class OutputPreparerBase implements OutputPreparer {

	private AsciiDataWriterConfiguration datawriterconfig;
	private NXMetaDataProvider metashop;
	private Set<String> scannablesAddedToMetadata = new HashSet<String>();
	
	public OutputPreparerBase(AsciiDataWriterConfiguration datawriterconfig, NXMetaDataProvider metashop) {
		this.datawriterconfig = datawriterconfig;
		this.metashop =  metashop;
	}

	@Override
	public void prepare(IOutputParameters outputParameters, IScanParameters scanBean) throws DeviceException {
		List<MetadataParameters> metadata = outputParameters.getMetadataList();
		for(MetadataParameters parameter : metadata) {
			if (!metashop.containsKey(parameter.getScannableName())) {
				metashop.add(Finder.getInstance().find(parameter.getScannableName()));
				scannablesAddedToMetadata.add(parameter.getScannableName());
			}
		}
	}

	// # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
	// # If this returns None, then let the Ascii Data Writer class find the config for itself.
	@Override
	public AsciiDataWriterConfiguration getAsciiDataWriterConfig(IScanParameters scanBean) {
		return datawriterconfig;
	}

	@Override
	public void _resetNexusStaticMetadataList() {
		for (String scannable : scannablesAddedToMetadata) {
			metashop.remove(scannable);
		}
		scannablesAddedToMetadata.clear();
	}

	@Override
	public ScanPlotSettings getPlotSettings(IDetectorParameters detectorBean, IOutputParameters outputBean) {
		return null;
	}


}
