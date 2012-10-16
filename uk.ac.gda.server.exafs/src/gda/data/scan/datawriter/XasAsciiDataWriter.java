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
import gda.data.PathConstructor;
import gda.device.Detector;
import gda.device.detector.DarkCurrentDetector;
import gda.device.detector.DarkCurrentResults;
import gda.exafs.scan.BeanGroup;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.util.io.FileUtils;

/**
 * Extension to the asciidatawriter which uses xml files if defined which have more options specific to the exafs RCP
 * GUI as used on spectroscopy beamlines
 */
public class XasAsciiDataWriter extends AsciiDataWriter {

	private static Logger logger = LoggerFactory.getLogger(XasAsciiDataWriter.class);

	protected static BeanGroup group;

	public static void setBeanGroup(BeanGroup group) {
		XasAsciiDataWriter.group = group;
	}

	public static BeanGroup getBeanGroup() {
		return group;
	}

	private String nexusFilePath;

	public XasAsciiDataWriter() throws InstantiationException {
		super();
		determineFilename();
		setScanDataPointFormatter(new XasScanDataPointFormatter());
	}

	@Override
	public void writeHeader() {
		
		// use configured header first
		super.writeHeader();

		try {
			// only do this when running from the GUI
			if (group == null) {
				return;
			}

			file.write("#\n");
			file.write("# Ascii output file name: '" + fileName + "'\n");
			if (nexusFilePath != null) {
				file.write("# Nexus output file: '" + nexusFilePath + "'\n");
				file.write("# The XML files, ScanParameters, SampleParameters, DetectorParameters, OutputParameters\n");
				file.write("# are stored in the nexus file.\n");
			}

			file.write("#\n");
			final ISampleParameters p = (ISampleParameters) getBean(group.getSample());
			// write out sample parameters
			if (p != null) {				
				for (int i =0; i < p.getDescriptions().size(); i++){
					String startMsg = "# ";
					if (i==0){
						startMsg += "Sample description: ";
					} else {
						startMsg += "Additional comments: ";
					}
					file.write(startMsg + p.getDescriptions().get(i) + "\n");
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
				if (!dataPoint.getCommand().contains("org.python.core")){
					file.write("# command: " + dataPoint.getCommand() + "\n");
				}

				file.write("#\n");
				// Set the detector type
				String xspressName = LocalProperties.get("gda.exafs.xspressName","xspress2system");
				String xmapName = LocalProperties.get("gda.exafs.xmapName","xmapMca");
				if (dataPoint.isDetector(xspressName)) {
					file.write("# Detector: Ge (XSPRESS)\n");
				} else if (dataPoint.isDetector(xmapName)) {
					file.write("# Detector: Si (XIA)\n");
				}

				// write out dark current if a detector present is a DarkCurrentDetector
				final DarkCurrentResults da = getDarkCurrent(dataPoint);
				if (da != null && da.getCounts().length >= 3) {
					file.write("#\n");
					file.write(String.format(
							"# Dark current intensity (Hz), collected over %.2fs: I0 %.2f   It %.2f   Iref %.2f\n",
							da.getTimeInS(), da.getCounts()[0] / da.getTimeInS(), da.getCounts()[1] / da.getTimeInS(),
							da.getCounts()[2] / da.getTimeInS()));
					file.write("# Dark current has been automatically removed from counts in main scan (I0,It,Iref)\n");
					file.write("#\n");
				}
				try {
					if (da != null && da.getCounts().length == 1) {
						file.write("#\n");
						file.write(String.format(
								"# Dark current intensity (Hz), collected over %.2fs: I1 %.2f\n",
								da.getTimeInS(), da.getCounts()[0] / da.getTimeInS()));
						file.write("# Dark current has been automatically removed from counts in main scan (I1)\n");
						file.write("#\n"); 
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e);
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

	@Override
	public void writeFooter() {
		super.writeFooter();
		group = null;
	}

	protected void determineFilename() {

		String nameFrag = LocalProperties.get("gda.instrument");

		dataDir = getDataDirectory();

		final File file = new File(dataDir);
		if (!file.exists())
			file.mkdirs();
		// NOTE: The dir was not tested for existing before being tested for being writable.
		// This means that when it was not existing at the start of a new visit for instance,
		// it was failing the canWrite test here and consequently writing to the wrong folder.
		if (!file.canWrite()) {
			final File newFile = FileUtils.createNewUniqueDir(file.getParentFile(), file.getName());
			logger.info("'" + dataDir + "' is not writable. Using '" + newFile.getName() + "' instead.");
			dataDir = newFile.getName() + "/";
		}
		try {
			if (group != null && group.getScanNumber() >= 0) {
				final OutputParameters params = (OutputParameters) XasAsciiDataWriter.group.getOutput();
				dataDir += params.getAsciiDirectory() + "/";
				final ISampleParameters sampleParams = XasAsciiDataWriter.group.getSample();
				String sampleName = sampleParams.getName().trim().replaceAll(" ", "_");
				filePrefix = sampleName + "_";
				if (nameFrag != null && nameFrag.equals("i20")){
					currentFileName = filePrefix + getFileNumber()+ "_" + group.getScanNumber() + "." + this.fileExtension;
				} else {
					currentFileName = getFileNumber()+ "_" + group.getScanNumber() +"_" + sampleName + "." + this.fileExtension;
				}
			} else {
				dataDir += "ascii/";
				if (nameFrag != null && nameFrag.equals("i20")){
					filePrefix = Long.toString(getFileNumber());
					currentFileName = filePrefix +"." + this.fileExtension;
				} else {
					filePrefix = nameFrag;
					currentFileName = filePrefix + "_" + getFileNumber()+ "." + this.fileExtension;
				}
			}
		} catch (RuntimeException ne) {
			if (group != null && group.isIncompleteDataAllowed()) {
				return;
			}
			throw ne;
		} catch (Exception e) {
			logger.error("Exception getting next file number", e);
			currentFileName = filePrefix + "." + this.fileExtension;
		}
	}

	public static String getDataDirectory() {
		if (group != null && group.getExperimentFolderName() != null) {
			return PathConstructor.createFromDefaultProperty() + group.getExperimentFolderName() + "/";
		}
		return PathConstructor.createFromDefaultProperty();
	}

	protected Object getBean(final Object var) {
		try {
			return BeansFactory.getBeanObject(group.getScriptFolder(), var);
		} catch (Exception ne) {
			if (group.isIncompleteDataAllowed()) {
				return null;
			}
			throw new RuntimeException(ne);
		}
	}

	public void setNexusFilePath(String nexusFileTemplate) {
		this.nexusFilePath = nexusFileTemplate;
	}
}
