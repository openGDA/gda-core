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
import gda.exafs.scan.BeanGroup;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.io.IOException;
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

	/**
	 * @param group
	 */
	public static void setBeanGroup(BeanGroup group) {
		XasAsciiDataWriter.group = group;
	}

	public static BeanGroup getBeanGroup() {
		return group;
	}

	private String nexusFilePath;

	/**
	 * Constructor
	 * 
	 * @throws InstantiationException
	 */
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
//				if (p.getName() != null)
//					file.write("# Sample name: " + p.getName() + "\n");
				if (p.getDescriptions() != null){
					file.write("# Sample description: " + p.getDescriptions().get(0) + "\n");
					file.write("# Additional comments: " + p.getDescriptions().get(1) + "\n");
				}
					//for (String des : p.getDescriptions()) {
						//file.write("# Sample description");
						//file.write(String.format("%s\n", des));
					//}
			}

			file.flush();

		} catch (IOException e) {
			logger.error("Exception while writing out header of ascii file: " + fileUrl);
		} catch (Exception e) {
			logger.error("Exception while calculating dark current for ascii file: " + fileUrl);
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
				final Double[] da = getDarkCurrent(dataPoint);
				if (da != null && da.length >= 3) {
					file.write("#\n");
					file.write(String.format("# Dark current intensity (Hz): I0 %6.0f   It %6.0f   Iref %6.0f\n",
							da[0], da[1], da[2]));
					file.write("# Dark current has been automatically removed from counts in main scan (I0,It,Iref)\n");
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

	private Double[] getDarkCurrent(IScanDataPoint dataPoint) {
		final List<Detector> d = dataPoint.getDetectors();
		for (Detector detector : d) {
			if (detector instanceof DarkCurrentDetector) {
				return ((DarkCurrentDetector) detector).getDarkCurrent();
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

		String nameFrag = LocalProperties.get("gda.instrument", "I20");

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
				//this.setRepetitionNumber( group.getScanNumber());
				filePrefix = params.getAsciiFileName() + "_" + group.getScanNumber() + "." + this.fileExtension;
			} else {
				dataDir += "ascii/";
				filePrefix = nameFrag + "." + this.fileExtension;
			}
		} catch (RuntimeException ne) {
			if (group != null && group.isIncompleteDataAllowed()) {
				return;
			}
			throw ne;
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

	/**
	 * 
	 Example of Header for Ascii Output file for I20 Below is the example header. #’s distinguish the header and
	 * footer from the data. In the case of transmission detection, the red text is to be included; in the case of
	 * fluorescence detection, the blue text is to be included. A footer shows the ring current at the end of the scan.
	 * # DIAMOND LIGHT SOURCE # Instrument: I20-XAS; Date: Day dd/mm/yyyy hh:mm:ss # Ring energy: 3 GeV # Initial ring
	 * current = 230 mA # Filling mode: uniform filling # Wiggler gap selected: 11 mm # Primary slits: vertical gap=3
	 * mm; horizontal gap=3 mm; vertical offset=1 mm; horizontal offset=1 mm # Secondary slits: vertical gap=3 mm;
	 * horizontal gap=3 mm; vertical offset=1 mm; horizontal offset=1 mm # Optic Hutch Mirrors Coating: silicon or
	 * rhodium # Monochromator Crystal cut: Si(111) # Experimental slits: vertical gap=3 mm; horizontal gap=3 mm;
	 * vertical offset=1 mm; horizontal offset=1 mm # Incident angle for Harmonic Rejection Mirrors: 3.5 mrad # Harmonic
	 * Rejection Mirrors Coating: silicon or rhodium # Transmission measurement # Range of Ion Chambers: I0=10^-8;
	 * It=10^-8; Iref=10^-8 # Fluorescence measurement # Range of Ion Chambers: I0=10^-8; It=10^-8; Iref=10^-8 #
	 * Detector: Ge (XSPRESS) or Si (XIA) # # Ascii output file name: 'XXXXXX.dat' # Nexus output file:
	 * '/dls_sw/dasc/users/data/nexus/XXXXXX.nxs' # Sample name: 'Sample123' # Comment: 'This comment and the sample
	 * name are taken from the corresponding comment and sample name in the sample parameters file.' # # Sample
	 * parameter file: '/dls_sw/dasc/users/scripts/Sample_Parameters.xml' # Scan parameter file:
	 * '/dls_sw/dasc/users/scripts/XAS_Parameters.xml' # Detector parameter file:
	 * '/dls_sw/dasc/users/scripts/Detector_Parameters.xml' # Output parameter file:
	 * '/dls_sw/dasc/users/scripts/Output_Parameters.xml' # # Dark current: # E I0 It If ln(I0/It) ln(It/Ir) FF FF/I0
	 * Integration_time DATA … DATA … DATA … # DIAMOND LIGHT SOURCE # Instrument: I20-XAS; Date: Day dd/mm/yyyy hh:mm:ss
	 * # Final ring current = 225 mA The 9 columns of data to be recorded are described below. When the measurement is
	 * being made in transmission only (without a fluorescence detector), the columns FF and FF/I0 are not to be
	 * included, in this case only 7 columns of data are recorded.
	 **/
}
