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
			if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX))
				setNexusFileNameTemplate("nexus/%d_" + LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + ".nxs");
			else
				setNexusFileNameTemplate("nexus/%d.nxs");
		}
		super.createNextFile();
	}

<<<<<<< HEAD
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
					NeXusUtils.writeNexusString(file, "xmlFolderName", xmlFolderName);
					writeBean("ScanParameters", scanBean);
					writeBean("DetectorParameters", detectorBean);
					writeBean("SampleParameters", sampleBean);
					writeBean("OutputParameters", outputBean);
					// if fluoresence then get the xml detector config from FluorescenceParameters else if diffraction then from  SoftXRaysParameters
					if (xmlFolderName != null && !xmlFolderName.isEmpty() && xmlFileName != null && !xmlFileName.isEmpty())
						writeBean("DetectorConfigurationParameters", xmlFolderName + IPath.SEPARATOR + xmlFileName);
				} finally {
					file.closegroup();
				}
			} catch (Exception e) {
				logger.warn("Exception while adding extra metadata: " + e.getMessage(), e);
			}
		}
	}

	private void writeBean(final String name, final Object bean) throws NexusException, Exception {
		if (bean == null)
			return;
		NeXusUtils.writeNexusString(file, name, BeansFactory.getXMLString(bean));
	}
=======
>>>>>>> refs/heads/local_master
	
}
