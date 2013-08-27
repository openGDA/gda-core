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
import gda.data.nexus.NeXusUtils;

import org.eclipse.core.runtime.IPath;
import org.nexusformat.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

/**
 * A nexus data writer that stores the XAS xml files contents.
 */
public class XasNexusDataWriter extends NexusExtraMetadataDataWriter {

	private static Logger logger = LoggerFactory.getLogger(XasNexusDataWriter.class);

	private String xmlFolderName;
	private String xmlFileName;
	
	private Boolean runFromExperimentDefinition;
	private IScanParameters scanBean;
	private IDetectorParameters detectorBean;
	private ISampleParameters sampleBean;
	private IOutputParameters outputBean;
	
	public XasNexusDataWriter() throws InstantiationException {
		super();
		setupProperties();
	}

	public XasNexusDataWriter(Long fileNumber) throws InstantiationException {
		super(fileNumber);
		setupProperties();
	}
	
	@Override
	public void createNextFile() throws Exception {
		if (getNexusFileNameTemplate() == null) {
			if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX)) {
				setNexusFileNameTemplate("nexus/%d_" + LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".nxs");
			} else {
				setNexusFileNameTemplate("nexus/%d.nxs");
			}
		}
		super.createNextFile();
	}

	@Override
	protected void createCustomMetaData() throws NexusException {
		super.createCustomMetaData();
		// add extra metadata from xml files to the nexus file if available
		if (runFromExperimentDefinition) {
			try {
				// Store XML
				file.makegroup("xml", "NXsample");
				file.opengroup("xml", "NXsample");
				try {
					writeXml("ScanParameters", scanBean);
					writeXml("DetectorParameters", detectorBean);
					writeXml("SampleParameters", sampleBean);
					writeXml("OutputParameters", outputBean);
					// if fluoresence then get the xml detector config from FluorescenceParameters else if diffraction then from  SoftXRaysParameters
					writeXml("DetectorConfigurationParameters", xmlFolderName + IPath.SEPARATOR + xmlFileName);
				} finally {
					file.closegroup();
				}
			} catch (Exception e) {
				logger.warn("Exception while adding extra metadata: " + e.getMessage(), e);
			}
		}
	}

	private void writeXml(final String name, final Object bean) throws NexusException, Exception {
		if (bean == null)
			return;
		NeXusUtils.writeNexusString(file, name, BeansFactory.getXMLString(bean));
	}
	
	public Boolean getRunFromExperimentDefinition() {
		return runFromExperimentDefinition;
	}

	public void setRunFromExperimentDefinition(Boolean runFromExperimentDefinition) {
		this.runFromExperimentDefinition = runFromExperimentDefinition;
	}

	public IScanParameters getScanBean() {
		return scanBean;
	}

	public void setScanBean(IScanParameters scanBean) {
		this.scanBean = scanBean;
	}

	public IDetectorParameters getDetectorBean() {
		return detectorBean;
	}

	public void setDetectorBean(IDetectorParameters detectorBean) {
		this.detectorBean = detectorBean;
	}

	public ISampleParameters getSampleBean() {
		return sampleBean;
	}

	public void setSampleBean(ISampleParameters sampleBean) {
		this.sampleBean = sampleBean;
	}

	public IOutputParameters getOutputBean() {
		return outputBean;
	}

	public void setOutputBean(IOutputParameters outputBean) {
		this.outputBean = outputBean;
	}

	public String getXmlFolderName() {
		return xmlFolderName;
	}

	public void setXmlFolderName(String xmlFolderName) {
		this.xmlFolderName = xmlFolderName;
	}

	public String getXmlFileName() {
		return xmlFileName;
	}

	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}
}
