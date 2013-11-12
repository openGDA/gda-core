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

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

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
	private IScanParameters scanBean;
	private IDetectorParameters detectorBean;
	private ISampleParameters sampleBean;
	private IOutputParameters outputBean;
	private String xmlFolderName;
	private String xmlFileName;
	private List<String> descriptions;
	private String sampleName;
	private AsciiDataWriterConfiguration configuration;

	public XasAsciiNexusDataWriter() {
		super();
	}

	private void storeFilenamesWithRegistar() {
		FileRegistrarHelper.registerFiles(new String[] { ascii.fileUrl });
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
	public String getCurrentScanIdentifier() {
		return nexus.getCurrentScanIdentifier();
	}

	@Override
	public void setHeader(String header) {
		// ignore as they both do their own thing
	}

	@Override
	public void addData(IScanDataPoint newData) throws Exception {
		if (firstData) {
			ascii = new XasAsciiDataWriter(Long.parseLong(newData.getScanIdentifier()));
			ascii.setRunFromExperimentDefinition(runFromExperimentDefinition);
			ascii.setDescriptions(descriptions);
			ascii.setSampleName(sampleName);
			ascii.setConfiguration(configuration);

			nexus = new XasNexusDataWriter(Long.parseLong(newData.getScanIdentifier()));
			nexus.setRunFromExperimentDefinition(runFromExperimentDefinition);
			nexus.setScanBean(scanBean);
			nexus.setDetectorBean(detectorBean);
			nexus.setSampleBean(sampleBean);
			nexus.setOutputBean(outputBean);
			nexus.setXmlFolderName(xmlFolderName);
			nexus.setXmlFileName(xmlFileName);
			
			setFileNameTemplates();
			firstData = false;
		}

		try {
			nexus.addData(newData);
		} catch (Exception e) {
			// ignore so we don't prevent the xas file from being written
			// we should at least log this!
		}
		try {
			ascii.addData(newData);
		} catch (Exception e) {
			// ignore so we don't prevent the drop file from being written
			// we should at least log this!
		} finally {
			try {
				super.addData(this, newData);
			} catch (Exception e) {
				logger.error("exception received from DataWriterBase.addData(...)", e);
			}
		}
	}

	private synchronized void setFileNameTemplates() throws Exception {
		if (nexusFileNameTemplate != null)
			nexus.setNexusFileNameTemplate(nexusFileNameTemplate);
		if (asciiFileNameTemplate != null)
			ascii.setAsciiFileNameTemplate(asciiFileNameTemplate);
		ascii.setNexusFilePath(nexus.getCurrentFileName()); // to cross reference in its header
	}

	@Override
	public void completeCollection() throws Exception {
		storeFilenamesWithRegistar();
		try {
			nexus.completeCollection();
		} catch (Exception e) {
			// ignore so we don't prevent the xas file from being written
			logger.error("ignored nexus error: ", e);
		}
		ascii.completeCollection();
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

	public Boolean getRunFromExperimentDefinition() {
		return runFromExperimentDefinition;
	}

	public void setRunFromExperimentDefinition(Boolean runFromExperimentDefinition) {
		this.runFromExperimentDefinition = runFromExperimentDefinition;
	}

	public void setScanBean(IScanParameters scanBean) {
		this.scanBean = scanBean;
	}


	public void setDetectorBean(IDetectorParameters detectorBean) {
		this.detectorBean = detectorBean;
	}

	public void setSampleBean(ISampleParameters sampleBean) {
		this.sampleBean = sampleBean;
	}

	public void setOutputBean(IOutputParameters outputBean) {
		this.outputBean = outputBean;
	}

	public void setXmlFolderName(String xmlFolderName) {
		this.xmlFolderName = xmlFolderName;
	}

	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
}
