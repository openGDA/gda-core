/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.io.File;

import org.nexusformat.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;

/**
 * A nexus data writer that stores the XAS xml files as strings.
 */
public class XasNexusDataWriter extends NexusExtraMetadataDataWriter {

	private static Logger logger = LoggerFactory.getLogger(XasNexusDataWriter.class);

	public XasNexusDataWriter() throws InstantiationException {
		super();
	}

	public XasNexusDataWriter(Long fileNumber) throws InstantiationException {
		super(fileNumber);
	}

	@Override
	protected void createCustomMetaData() throws NexusException {
		super.createCustomMetaData();
		// add extra metadata from xml files to the nexus file if available
		if (XasAsciiDataWriter.group != null) {
			try {
				// Store XML
				file.makegroup("xml", "NXsample");
				file.opengroup("xml", "NXsample");
				try {
					writeXml("ScanParameters", XasAsciiDataWriter.group.getScan());
					writeXml("SampleParameters", XasAsciiDataWriter.group.getSample());
					writeXml("DetectorParameters", XasAsciiDataWriter.group.getDetector());
					writeXml("OutputParameters", XasAsciiDataWriter.group.getOutput());

					addDetectorXML();

				} finally {
					file.closegroup();
				}
			} catch (Exception e) {
				logger.warn("Exception while adding extra metadata: " + e.getMessage(), e);
			}
		}
	}

	protected void addDetectorXML() throws NexusException, Exception {
		IDetectorParameters detParams = XasAsciiDataWriter.group.getDetector();

		if (detParams instanceof DetectorParameters) {
			DetectorParameters dp = (DetectorParameters) detParams;
			if (dp.getExperimentType().equalsIgnoreCase("fluorescence")
					&& dp.getFluorescenceParameters().getDetectorType().equals("Germanium")) {
				if ((new File(LocalProperties.getConfigDir() + "/templates/Xspress_Parameters.xml")).exists())
					writeXspressParameters();
			} else if (dp.getExperimentType().equalsIgnoreCase("fluorescence")) {
				if ((new File(LocalProperties.getConfigDir() + "/templates/Vortex_Parameters.xml")).exists())
					writeVortexParameters();
			} else if (dp.getExperimentType().equalsIgnoreCase("diffraction")) {
				if ((new File(LocalProperties.getConfigDir() + "/templates/GSMD_Parameters.xml")).exists()) {
					writeXml("GsmdParameters", LocalProperties.getConfigDir() + "/templates/GSMD_Parameters.xml");
				}
			}
		}
	}

	private void writeVortexParameters() throws Exception {
		String slurp = BeansFactory.getXMLString(LocalProperties.getConfigDir() + "/templates/Vortex_Parameters.xml");
		int startOfCountsData = slurp.indexOf("<ElementCountsData>");
		int endOfCountsData = slurp.lastIndexOf("</ElementCountsData>");

		String substring = slurp.substring(0, startOfCountsData);
		substring += slurp.substring(endOfCountsData + "</ElementCountsData>".length());
		slurp = null;

		NeXusUtils.writeNexusString(file, "VortexParameters", substring);
	}

	protected void writeXspressParameters() throws Exception {
		String slurp = BeansFactory.getXMLString(LocalProperties.getConfigDir() + "/templates/Xspress_Parameters.xml");
		int startOfCountsData = slurp.indexOf("<ElementCountsData>");
		int endOfCountsData = slurp.lastIndexOf("</ElementCountsData>");

		String substring = slurp.substring(0, startOfCountsData);
		substring += slurp.substring(endOfCountsData + "</ElementCountsData>".length());
		slurp = null;

		NeXusUtils.writeNexusString(file, "XspressParameters", substring);
	}

	private void writeXml(final String name, final Object bean) throws NexusException, Exception {
		if (bean == null)
			return;
		NeXusUtils.writeNexusString(file, name, BeansFactory.getXMLString(bean));
	}

}
