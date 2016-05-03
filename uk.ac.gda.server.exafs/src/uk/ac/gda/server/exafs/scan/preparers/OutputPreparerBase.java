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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nfunk.jep.ParseException;
import org.springframework.beans.factory.InitializingBean;

import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.JEPScannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.MetadataParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.server.exafs.scan.OutputPreparer;

public abstract class OutputPreparerBase implements OutputPreparer, InitializingBean {

	private AsciiDataWriterConfiguration datawriterconfig;
	private NXMetaDataProvider metashop;
	private Set<Scannable> scannablesAddedToMetadata = new HashSet<Scannable>();
	// Metadata set provided by OutputParameter bean and written into the ASCII header
	private Set<AsciiMetadataConfig> asciiMetadataList = new HashSet<AsciiMetadataConfig>();
	private Set<Scannable> scannablesToBeAddedAsColumnInDataFile = new HashSet<Scannable>();

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
	public void configure(IOutputParameters outputParameters, IScanParameters scanBean, IDetectorParameters detectorBean, ISampleParameters sampleParameters)
			throws DeviceException {

		List<MetadataParameters> metadata = outputParameters.getMetadataList();
		addMetadata(metadata);

		List<SignalParameters> signalList = outputParameters.getSignalList();

		try {
			setScannablesToBeAddedAsColumnInDataFile(signalList);
		} catch (ParseException e) {
			throw new DeviceException("Problem getting the scannables To be added as column in Nexus and Ascii file.");
		}

	}

	private void addMetadata(List<MetadataParameters> metadata) {
		ArrayList<AsciiMetadataConfig> header = datawriterconfig.getHeader();
		for (MetadataParameters parameter : metadata) {
			String scannableName = parameter.getScannableName();
			Scannable scannable = retriveScannable(scannableName);
			if (scannable != null) {
				// add metadata to the Nexus file
				addOneMetadataElementToNexus(scannable);
				// add metadata to the ASCII file
				addOneMetadataElementToAscii(scannable, header);
				scannablesAddedToMetadata.add(scannable);
			}
		}
		datawriterconfig.setHeader(header);
	}

	private void addOneMetadataElementToNexus(Scannable scannable) {
		if (!metashop.containsKey(scannable.getName()))
			metashop.add(scannable);
	}

	private List<AsciiMetadataConfig> addOneMetadataElementToAscii(Scannable scannable, List<AsciiMetadataConfig> header) {
		AsciiMetadataConfig asciiConfig = new AsciiMetadataConfig();
		asciiConfig.setLabel(scannable.getName() + ": %4.1f");
		Scannable[] labels = { scannable };
		asciiConfig.setLabelValues(labels);
		header.add(asciiConfig);
		asciiMetadataList.add(asciiConfig);
		return header;
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

	// need to reset at the end of tne scan instead of the beginning of the next scan otherwise if we run a scan
	// through the jython console it will take the metadata from the previous scan (as the datawriter configuration
	// will stay in memory.

	@Override
	public void resetStaticMetadataList() {
		resetNexusStaticMetadataList();
		resetAsciiStaticMetadataList();
		scannablesAddedToMetadata.clear();
	}

	private void resetNexusStaticMetadataList() {
		for (Scannable scannable : scannablesAddedToMetadata) {
			if (scannable != null) {
				metashop.remove(scannable);
			}
		}
	}

	private void resetAsciiStaticMetadataList() {
		ArrayList<AsciiMetadataConfig> header = datawriterconfig.getHeader();
		if (!asciiMetadataList.isEmpty())
			header.removeAll(asciiMetadataList);
		datawriterconfig.setHeader(header);
		asciiMetadataList.clear();
	}

	@Override
	public ScanPlotSettings getPlotSettings() {
		return null;
	}

	private void setScannablesToBeAddedAsColumnInDataFile(List<SignalParameters> signalList) throws ParseException {
		//make sure that the previous list has been cleared
		String dataFormat;
		scannablesToBeAddedAsColumnInDataFile.clear();
		for (SignalParameters signal:signalList){
			dataFormat = "%6." + signal.getDecimalPlaces() + "g";
			signalParametersValidator(signal);
			Scannable scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(), dataFormat, signal.getName(),
					signal.getExpression());
			scannablesToBeAddedAsColumnInDataFile.add(scannable);
		}
	}

	@Override
	public Set<Scannable> getScannablesToBeAddedAsColumnInDataFile() {
		return scannablesToBeAddedAsColumnInDataFile;
	}

	private void signalParametersValidator(SignalParameters signal) {
		if (signal.getExpression().equals(""))
			signal.setExpression(null);
	}

}