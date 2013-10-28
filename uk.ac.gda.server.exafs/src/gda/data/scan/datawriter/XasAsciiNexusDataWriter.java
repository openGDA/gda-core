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

	public XasAsciiNexusDataWriter() {
		super();
	}

	private void storeFilenamesWithRegistar() {
		FileRegistrarHelper.registerFiles(new String[] { ascii.fileUrl });
	}

	@Override
	public String getCurrentFileName() {
		if (nexus == null){
			return "";
		}
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
			ascii = new XasAsciiDataWriter();
			nexus = new XasNexusDataWriter(ascii.getFileNumber());
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
		if (nexusFileNameTemplate != null){
			nexus.setNexusFileNameTemplate(nexusFileNameTemplate);
		}
		if (asciiFileNameTemplate != null) {
			ascii.setAsciiFileNameTemplate(asciiFileNameTemplate);
		}
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
		return ascii.getConfiguration();
	}

	@Override
	public void setConfiguration(AsciiDataWriterConfiguration configuration) {
		ascii.setConfiguration(configuration);
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

	// from both datawriter components

	public Boolean getRunFromExperimentDefinition() {
		return nexus.getRunFromExperimentDefinition() && ascii.getRunFromExperimentDefinition();
	}

	public void setRunFromExperimentDefinition(Boolean runFromExperimentDefinition) {
		nexus.setRunFromExperimentDefinition(runFromExperimentDefinition);
		ascii.setRunFromExperimentDefinition(runFromExperimentDefinition);
	}

	// from the XasNexusDataWriter component

	public IScanParameters getScanBean() {
		return nexus.getScanBean();
	}

	public void setScanBean(IScanParameters scanBean) {
		this.nexus.setScanBean(scanBean);
	}

	public IDetectorParameters getDetectorBean() {
		return nexus.getDetectorBean();
	}

	public void setDetectorBean(IDetectorParameters detectorBean) {
		this.nexus.setDetectorBean(detectorBean);
	}

	public ISampleParameters getSampleBean() {
		return nexus.getSampleBean();
	}

	public void setSampleBean(ISampleParameters sampleBean) {
		nexus.setSampleBean(sampleBean);
	}

	public IOutputParameters getOutputBean() {
		return nexus.getOutputBean();
	}

	public void setOutputBean(IOutputParameters outputBean) {
		this.nexus.setOutputBean(outputBean);
	}

	public String getXmlFolderName() {
		return nexus.getXmlFolderName();
	}

	public void setXmlFolderName(String xmlFolderName) {
		this.nexus.setXmlFolderName(xmlFolderName);
	}

	public String getXmlFileName() {
		return nexus.getXmlFileName();
	}

	public void setXmlFileName(String xmlFileName) {
		nexus.setXmlFileName(xmlFileName);
	}

	// from the XasAsciiDataWriter component

	public List<String> getDescriptions() {
		return ascii.getDescriptions();
	}

	public void setDescriptions(List<String> descriptions) {
		ascii.setDescriptions(descriptions);
	}

	public String getSampleName() {
		return ascii.getSampleName();
	}

	public void setSampleName(String sampleName) {
		ascii.setSampleName(sampleName);
	}
}
