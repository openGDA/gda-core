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

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanPlotSettings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.server.exafs.scan.OutputPreparer;

public abstract class OutputPreparerBase implements OutputPreparer, InitializingBean {

	private AsciiDataWriterConfiguration datawriterconfig;
	private NXMetaDataProvider metashop;
	private Set<String> scannablesAddedToMetadata = new HashSet<String>();

	public OutputPreparerBase() {
	}

	public OutputPreparerBase(AsciiDataWriterConfiguration datawriterconfig, NXMetaDataProvider metashop) {
		this.datawriterconfig = datawriterconfig;
		this.metashop = metashop;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (datawriterconfig == null) {
			throw new IllegalArgumentException("Missing datawriterconfig configuration");
		}
		if (metashop == null) {
			throw new IllegalArgumentException("Missing metashop");
		}
	}

	public AsciiDataWriterConfiguration getDatawriterconfig() {
		return datawriterconfig;
	}

	public void setDatawriterconfig(AsciiDataWriterConfiguration datawriterconfig) {
		this.datawriterconfig = datawriterconfig;
	}

	public NXMetaDataProvider getMetashop() {
		return metashop;
	}

	public void setMetashop(NXMetaDataProvider metashop) {
		this.metashop = metashop;
	}

	@Override
	public void configure(IOutputParameters outputParameters, IScanParameters scanBean, IDetectorParameters detectorBean)
			throws DeviceException {
		List<MetadataParameters> metadata = outputParameters.getMetadataList();
		for (MetadataParameters parameter : metadata) {
			String scannableName = parameter.getScannableName();
			if (!metashop.containsKey(scannableName)) {
				Scannable scannable= retriveScannable(scannableName);
				if (scannable!= null){
					metashop.add(scannable);
					scannablesAddedToMetadata.add(scannableName);
				}
			}
		}
	}
	
	private Scannable retriveScannable(String scannableName){
		Scannable scannableFromFinder = Finder.getInstance().find(scannableName);
		if (scannableFromFinder!= null){
			return scannableFromFinder;
		}
		Scannable scannableFromNamespace = (Scannable) InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		return scannableFromNamespace;
	}
	
	@Override
	public void beforeEachRepetition() throws Exception {
		//
	}

	// # Determines the AsciiDataWriterConfiguration to use to format the header/footer of the ascii data files
	// # If this returns None, then let the Ascii Data Writer class find the config for itself.
	@Override
	public AsciiDataWriterConfiguration getAsciiDataWriterConfig(IScanParameters scanBean) {
		return datawriterconfig;
	}

	@Override
	public void resetNexusStaticMetadataList() {
		for (String scannableName : scannablesAddedToMetadata) {
			Scannable scannable = retriveScannable(scannableName);
			if (scannable != null) {
				metashop.remove(scannable);
			}
		}
		scannablesAddedToMetadata.clear();
	}

	@Override
	public ScanPlotSettings getPlotSettings() {
		return null;
	}
}
