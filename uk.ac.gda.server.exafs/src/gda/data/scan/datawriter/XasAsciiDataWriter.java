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
import gda.device.Detector;
import gda.device.detector.DarkCurrentDetector;
import gda.device.detector.DarkCurrentResults;
import gda.device.detector.xspress.Xspress2System;
import gda.factory.Finder;
import gda.scan.IScanDataPoint;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.xspress.DetectorElement;

/**
 * Extension to the asciidatawriter which uses xml files if defined which have more options specific to the exafs RCP
 * GUI as used on spectroscopy beamlines
 */
public class XasAsciiDataWriter extends AsciiDataWriter {

	private static Logger logger = LoggerFactory.getLogger(XasAsciiDataWriter.class);

	private String sampleName;
	private List<String> descriptions;
	private Boolean runFromExperimentDefinition = false;
	private String nexusFilePath;
	private String asciiFileNameTemplate;

	public XasAsciiDataWriter() throws InstantiationException {
		super();
		setScanDataPointFormatter(new XasScanDataPointFormatter());
	}

	@Override
	public void createNextFile() throws Exception {

		// if template has not been supplied e.g. we are in a command-line scan
		if (asciiFileNameTemplate == null) {
			if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX)) {
				asciiFileNameTemplate = "%d_" + LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME) + "."
						+ this.fileExtension;
			} else {
				asciiFileNameTemplate = "%d." + this.fileExtension;
			}
			dataDir += "ascii/";
		}

		this.currentFileName = String.format(asciiFileNameTemplate, getFileNumber());
		super.createNextFile();
	}

	@Override
	public void writeHeader() {

		// use configured header first
		super.writeHeader();

		try {
			// only do this when running from the Experiment Definition GUI
			if (!runFromExperimentDefinition)
				return;

			file.write("#\n");
			file.write("# Ascii output file name: '" + fileName + "'\n");
			if (nexusFilePath != null) {
				file.write("# Nexus output file: '" + nexusFilePath + "'\n");
				file.write("# The XML files, ScanParameters, SampleParameters, DetectorParameters, OutputParameters\n");
				file.write("# are stored in the nexus file.\n");
			}

			file.write("#\n");
			// write out sample parameters
			if (!descriptions.isEmpty()) {
				if (!sampleName.isEmpty())
					file.write("# Sample name: " + sampleName + "\n");
				for (int i = 0; i < descriptions.size(); i++) {
					String startMsg = "# ";
					if (i == 0)
						startMsg += "Sample description: ";
					else
						startMsg += "Additional comments: ";
					file.write(startMsg + descriptions.get(i) + "\n");
				}
			}
			file.flush();
		} catch (Exception e) {
			logger.error("Exception while writing out header of ascii file: " + fileUrl);
		}
	}

	/**
	 * The Xas scan should have columns ordered as follows: Fluorescence: Energy I0 It Iref ln(I0/It) ln(I0/Iref) FF
	 * FF/I0 time Transmission: Energy I0 It Iref ln(I0/It) ln(I0/Iref) time
	 */
	@Override
	public void addData(IScanDataPoint dataPoint) throws Exception {

		try {
			if (firstData) {
				this.setupFile();

				// write out the command if its not too long
				if (!dataPoint.getCommand().contains("org.python.core")) {
					file.write("# command: " + dataPoint.getCommand() + "\n");
				} else {
					file.write("#");
				}

				file.write("#\n");
				// Set the detector type
				String xspressName = LocalProperties.get("gda.exafs.xspressName", "xspress2system");// Why is this a
																									// local property?
																									// The name is
																									// defined in spring
				String xmapName = LocalProperties.get("gda.exafs.xmapName", "xmapMca");// Why is this a local property?
																						// The name is defined in spring
				if (dataPoint.isDetector(xspressName)) {
					file.write("# Detector: Ge (XSPRESS)\n");
					StringBuffer buf = new StringBuffer();
					buf.append("Disabled elements: ");
					boolean found = false;
					Xspress2System xspress = Finder.getInstance().find(xspressName);// Finder Arghhhhhhhhh
					if (xspress != null) {
						for (DetectorElement element : xspress.getDetectorList()) {
							if (element.isExcluded()) {
								if (found) {
									buf.append(",");
								}
								found = true;
								buf.append(element.getNumber());
							}
						}
					}
					if (found) {
						file.write("# " + buf);
					}
				} else if (dataPoint.isDetector(xmapName)) {
					file.write("# Detector: Si (XIA)\n");
				} else {
					file.write("#\n");
				}

				// write out dark current if a detector present is a DarkCurrentDetector
				final DarkCurrentResults da = getDarkCurrent(dataPoint);
				if (da != null && da.getCounts().length >= 3) {
					file.write("#\n");
					file.write(String
							.format("# Dark current intensity was collected over %.2fs. Average counts per second: I0 %.2f   It %.2f   Iref %.2f\n",
									da.getTimeInS(), da.getCounts()[0] / da.getTimeInS(),
									da.getCounts()[1] / da.getTimeInS(), da.getCounts()[2] / da.getTimeInS()));
					file.write("# Dark current has been automatically removed from counts in main scan (I0,It,Iref)\n");
					file.write("#\n");
				}

				if (da != null && da.getCounts().length == 1) {
					file.write("#\n");
					file.write(String.format("# Dark current intensity (Hz), collected over %.2fs: I1 %.2f\n",
							da.getTimeInS(), da.getCounts()[0] / da.getTimeInS()));
					file.write("# Dark current has been automatically removed from counts in main scan (I1)\n");
					file.write("#\n");
				}

				columnHeader = dataPoint.getHeaderString(getScanDataPointFormatter());
				writeColumnHeadings();
				firstData = false;
			}
			super.addData(dataPoint);
		} finally {
			firstData = false;
		}
	}

	private DarkCurrentResults getDarkCurrent(IScanDataPoint dataPoint) {
		final List<Detector> d = dataPoint.getDetectors();
		for (Detector detector : d) {
			if (detector instanceof DarkCurrentDetector) {
				return ((DarkCurrentDetector) detector).getDarkCurrentResults();
			}
		}
		return null;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}

	public Boolean getRunFromExperimentDefinition() {
		return runFromExperimentDefinition;
	}

	public void setRunFromExperimentDefinition(Boolean runFromExperimentDefinition) {
		this.runFromExperimentDefinition = runFromExperimentDefinition;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getAsciiFileNameTemplate() {
		return asciiFileNameTemplate;
	}

	/**
	 * This must also include the subdirectory
	 * 
	 * @param asciiFileNameTemplate
	 */
	public void setAsciiFileNameTemplate(String asciiFileNameTemplate) {
		this.asciiFileNameTemplate = asciiFileNameTemplate;
	}

	public String getNexusFilePath() {
		return nexusFilePath;
	}

	public void setNexusFilePath(String nexusFilePath) {
		this.nexusFilePath = nexusFilePath;
	}
}
