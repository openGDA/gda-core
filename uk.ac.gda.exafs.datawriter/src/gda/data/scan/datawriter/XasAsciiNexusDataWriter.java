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

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write to an Xas and a Nexus file simultaneously
 */
public class XasAsciiNexusDataWriter extends DataWriterBase implements ConfigurableAsciiFormat {
	private static Logger logger = LoggerFactory.getLogger(XasAsciiNexusDataWriter.class);
	private XasAsciiDataWriter ascii;
	private XasNexusDataWriter nexus;
	private String nexusFileNameTemplate;
	private String asciiFileNameTemplate;
	private Boolean firstData = true;
	private Boolean runFromExperimentDefinition = false;
	private List<String> descriptions;
	private String sampleName;
	private AsciiDataWriterConfiguration configuration;
	private String scanParametersName;
	private String sampleParametersName;
	private String detectorParametersName;
	private String outputParametersName;
	private String folderName;

	public XasAsciiNexusDataWriter() {
		super();
	}

	private void storeFilenamesWithRegistar() {
		if (ascii != null)
			FileRegistrarHelper.registerFiles(new String[] { ascii.getCurrentFileName() });
		if (nexus != null)
			FileRegistrarHelper.registerFiles(new String[] { nexus.getDataDir() + nexus.getNexusFileName() });
	}

	@Override
	public String getCurrentFileName() {
		if (nexus == null)
			return "";
		return nexus.getCurrentFileName();
	}

	public String getAsciiFileName() {
		return ascii.getCurrentFileName();
	}

	@Override
	public int getCurrentScanIdentifier() {
		return nexus.getCurrentScanIdentifier();
	}

	@Override
	public void setHeader(String header) {
		// ignore as they both do their own thing
	}

	@Override
	public void addData(IScanDataPoint newData) throws Exception {
		if (firstData) {
			ascii = new XasAsciiDataWriter(newData.getScanIdentifier());
			ascii.setRunFromExperimentDefinition(runFromExperimentDefinition);
			ascii.setDescriptions(descriptions);
			ascii.setSampleName(sampleName);
			if (configuration != null)
				ascii.setConfiguration(configuration);

			ascii.setScanParametersName(scanParametersName);
			ascii.setDetectorParametersName(detectorParametersName);
			ascii.setSampleParametersName(sampleParametersName);
			ascii.setOutputParametersName(outputParametersName);
			ascii.setFolderName(folderName);

			nexus = new XasNexusDataWriter(newData.getScanIdentifier());
			setFileNameTemplates();
			firstData = false;
		}


		// Nested try..catch..finallys so we at least attempt to write data to
		// other file writers if there is an error, but still ensure that the
		// exception is propagated.
		// This is vital in ContinuousScans to ensure that the scan is stopped
		// if it is aborted / interrupted during data writing.
		try {
			nexus.addData(newData);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				ascii.addData(newData);
			} catch (Exception e) {
				throw e;
			} finally {
				try {
					super.addData(this, newData);
				} catch (Exception e) {
					logger.error("exception received from DataWriterBase.addData(...)", e);
					throw e;
				}
			}
		}
	}

	private synchronized void setFileNameTemplates() throws Exception {
		if (nexusFileNameTemplate != null)
			nexus.setNexusFileNameTemplate(nexusFileNameTemplate);
		if (asciiFileNameTemplate != null)
			ascii.setAsciiFileNameTemplate(asciiFileNameTemplate);
		ascii.setNexusFilePath(nexus.getCurrentFileName()); // to cross
															// reference in its
															// header
	}

	@Override
	public void completeCollection() throws Exception {
		if (ascii != null) {
			storeFilenamesWithRegistar();
			try {
				nexus.completeCollection();
			} catch (Exception e) {
				// ignore so we don't prevent the xas file from being written
				logger.error("ignored nexus error: ", e);
			}
			ascii.completeCollection();
		}
	}

	@Override
	public ArrayList<String> getHeader() {
		return nexus.getHeader();
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		super.addDataWriterExtender(dataWriterExtender);
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		super.removeDataWriterExtender(dataWriterExtender);
	}

	@Override
	public AsciiDataWriterConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(AsciiDataWriterConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getNexusFileNameTemplate() {
		return nexusFileNameTemplate;
	}

	public void setNexusFileNameTemplate(String nexusFileNameTemplate) {
		this.nexusFileNameTemplate = nexusFileNameTemplate;
	}

	public String getAsciiFileNameTemplate() {
		return asciiFileNameTemplate;
	}

	public void setAsciiFileNameTemplate(String asciiFileNameTemplate) {
		this.asciiFileNameTemplate = asciiFileNameTemplate;
	}

	public List<String> getDescriptions() {
		return ascii.getDescriptions();
	}

	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public void setScanParametersName(String scanParametersName) {
		this.scanParametersName = scanParametersName;
	}

	public void setSampleParametersName(String sampleParametersName) {
		this.sampleParametersName = sampleParametersName;
	}

	public void setDetectorParametersName(String detectorParametersName) {
		this.detectorParametersName = detectorParametersName;
	}

	public void setOutputParametersName(String outputParametersName) {
		this.outputParametersName = outputParametersName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public Boolean getRunFromExperimentDefinition() {
		return runFromExperimentDefinition;
	}

	public void setRunFromExperimentDefinition(Boolean runFromExperimentDefinition) {
		this.runFromExperimentDefinition = runFromExperimentDefinition;
	}

}
