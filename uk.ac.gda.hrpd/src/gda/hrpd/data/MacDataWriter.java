/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.hrpd.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.beamline.BeamlineInfo;
import gda.beamline.beam.Beam;
import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.scan.datawriter.DataWriterBase;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Robot;
import gda.device.Scannable;
import gda.device.Temperature;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.device.detector.multichannelscaler.Mca;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.hrpd.SampleInfo;
import gda.jython.JythonServerFacade;
import gda.scan.IScanDataPoint;

/**
 * This class writes files of tabulated data from Multiple Analyser Crystal Detectors, along with a header and footer if
 * needed, in ASCII format. The files created use names which are an increment from the last name.
 */
public class MacDataWriter extends DataWriterBase implements Findable, Configurable {
	/**
	 * logging instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(MacDataWriter.class);
	@SuppressWarnings("unused")
	private static final String name = "MACDataWriter";
	/**
	 * the current file number being written to, which is managed by the NumTracker Class
	 */
	protected int thisFileNumber = 0;
	/**
	 * file extension to use
	 */
	protected String fileExtension = null;
	/**
	 * file prefix to use (if any)
	 */
	protected String filePrefix = null;
	/**
	 * file suffix to use (if any)
	 */
	protected String fileSuffix = null;
	/**
	 * the data directory where all data files will be placed
	 */
	// protected String dataDir = null;
	/**
	 * file handle of currently open file
	 */
	protected FileWriter file = null;
	/**
	 * the full path name of the data file
	 */
	protected String fileUrl = null;

	/**
	 * first data marker used to determine whether to write out column headers
	 */
	protected boolean firstData = true;
	/**
	 * the full file name of the current data file
	 */
	protected String currentFileName = null;
	/**
	 * sample information instance
	 */
	private SampleInfo saminfo;
	/**
	 * Beam information instance
	 */
	private Beam beam;
	/**
	 * beamline parameters
	 */
	private BeamlineInfo bli;
	/**
	 * Metadata instance
	 */
	private Metadata metadata;

	private double sampleNo = Double.MIN_VALUE;

	private Vector<Scannable> parentScannables = new Vector<Scannable>();

	private Vector<Scannable> allScannables = new Vector<Scannable>();
	private Vector<Detector> allDetectors = new Vector<Detector>();
	private Finder finder = Finder.getInstance();
	private double temp = Double.NaN;
	private int numberOfDetectors;
	private double scantime;
	private boolean configured = false;

	/**
	 * Constructor which determines the name of the next file and creates it.
	 */
	public MacDataWriter() {
	}


	@Override
	public void configure() {
		if (configured) {
			return;
		}
		if ((metadata = GDAMetadataProvider.getInstance(false)) == null) {
			logger.warn("Can not find 'GDAMetadata' object.");
		}

		if ((beam = (Beam) finder.find("beam")) == null) {
			logger.warn("Can not find beam information object 'beam'");
		}

		if ((bli = (BeamlineInfo) finder.find("beamline")) == null) {
			logger.warn("Can not find beamline information object 'beamline'");
		}
		// if (bli != null) {
		// dataDir = bli.getDataDir();
		// } else {
		// dataDir = PathConstructor.createFromDefaultProperty();
		// }
		if ((saminfo = (SampleInfo) finder.find("SampleInfo")) == null) {
			logger.warn("Cannot find sample information data object 'SampleInfo'");
		}
		configured = true;

	}

	@Override
	public void reconfigure() throws FactoryException {
		logger.debug("Empty reconfigure() called");
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	@Override
	public void addData(IScanDataPoint dataPoint) {
		logger.info("This method is not applicable to MAC data.");
		throw new IllegalStateException("This method is not applicable to MAC data.");
	}

	/**
	 * MAC specific data writer
	 *
	 * @param rows
	 * @param parentScannables
	 * @param allScannables
	 * @param allDetectors
	 * @param path1
	 * @param path2
	 * @param path3
	 * @param path4
	 * @param path5
	 * @param path6
	 * @param path7
	 * @param path8
	 * @param data
	 * @param error2
	 * @param scantime
	 * @return filename
	 */
	public String addData(int rows, Vector<Scannable> parentScannables, Vector<Scannable> allScannables,
			Vector<Detector> allDetectors, double[] path1, double[] path2, double[] path3, double[] path4,
			double[] path5, double[] path6, double[] path7, double[] path8, int[][] data, double[] error2, double scantime) {

		String datapoint = "";
		this.parentScannables = parentScannables;
		this.allScannables = allScannables;
		this.allDetectors = allDetectors;
		this.scantime = scantime;

		int i=0;
		if (!parentScannables.isEmpty()) {
			for (Scannable s : this.parentScannables) {
				if (s instanceof Robot) { // for sample changer
					try {
						this.sampleNo = ((Robot) s).getSamplePosition();
					} catch (DeviceException e) {
						logger.error("Cannot get the sample number.", e);
					}
				}
				if (s instanceof Temperature) { // for temperature controller
					try {
						this.temp = Double.parseDouble(((Temperature) s).getPosition().toString());
					} catch (DeviceException e) {
						logger.error("Cannot get the sample temperature.",e);
					}
				}
			}
		}
		if (path1 != null) {
			this.prepareForCollection();
		}
		JythonServerFacade.getInstance().print("Number of data points : " + rows);
		try {
			for (i = 0; i < rows; i++) {
				if (path1 != null) {
					datapoint += path1[i] + delimiter;
				}
				if (path2 != null) {
					datapoint += path2[i] + delimiter;
				}
				if (path3 != null) {
					datapoint += path3[i] + delimiter;
				}
				if (path4 != null) {
					datapoint += path4[i] + delimiter;
				}
				if (path5 != null) {
					datapoint += path5[i] + delimiter;
				}
				if (path6 != null) {
					datapoint += path6[i] + delimiter;
				}
				if (path7 != null) {
					datapoint += path7[i] + delimiter;
				}
				if (path8 != null) {
					datapoint += path8[i] + delimiter;
				}
				for (int j = 0; j < numberOfDetectors; j++) {
					datapoint += data[j][i] + delimiter;
				}
				if (error2 != null) {
					datapoint += error2[i] + delimiter;
				}
				datapoint = datapoint.trim() + "\n";
				file.write(datapoint);
				datapoint = "";
			}
			datapoint = null;

		} catch (IOException e) {
			logger.error("Cannot write to the data file: " + fileUrl, e);
		}
		finally {
			try {
				completeCollection();
			} catch (Exception e) {
				logger.error("Exception caught in MacDataWriter.completeCollection().", e);

			}
			file = null;
			if (i<rows) {
				logger.info("Number of data points saved in file {} is {} < 60000 expected", fileUrl, i);
			}
			// Print informational message to console.
			JythonServerFacade.getInstance().print("Writing data to file: " + fileUrl + " completed");

		}
		return fileUrl;
	}

	/**
	 * Open files and writes out headers.
	 */
	public void prepareForCollection() {
		createNextFile();
		if (file != null) {
			writeHeader();
		} else {
			logger.error("failed to create file {}", fileUrl);
		}
	}

	/**
	 * Closes current file and opens a new file with an incremental number. For use when many files being created
	 * instead of a single file being appended to.
	 */
	public void createNextFile() {

		if (file != null) {
			try {
				file.flush();
				file.close();
			} catch (Throwable et) {
				String error = "Error closing MAC data file. ";
				logger.error(error + et.getMessage());
				JythonServerFacade.getInstance().print(error);
				JythonServerFacade.getInstance().print(et.getMessage());
			}
		}
		// Get the next run number
		thisFileNumber = bli.getNextFileNumber();
		// this is to save data with header along side of existing file created by cvscan - which will eventually be
		// replaced by this
		// thisFileNumber = bli.getFileNumber();

		// Set the file extension to be used.
		this.fileExtension = LocalProperties.get("gda.data.file.extension", "dat");
		this.filePrefix = LocalProperties.get("gda.data.file.prefix", "");
		this.fileSuffix = LocalProperties.get("gda.data.file.suffix", "");
		currentFileName = this.filePrefix + thisFileNumber + this.fileSuffix + "." + this.fileExtension;

		fileUrl = PathConstructor.createFromDefaultProperty() + File.separator + currentFileName;
		try {
			file = new FileWriter(fileUrl);
		} catch (IOException ex1) {
			String error = "Failed to create a new data file: " + fileUrl;
			logger.error(error);
			JythonServerFacade.getInstance().requestFinishEarly();
			JythonServerFacade.getInstance().print(error);
		}
		// Print informational message to console.
		JythonServerFacade.getInstance().print("Writing data to file: " + fileUrl + " Please wait ...");
	}

	/**
	 * Returns the file reference to the data file
	 *
	 * @return FileWriter
	 */
	public FileWriter getData() {
		return file;
	}

	/**
	 * Writes any file footers and closes file.
	 * @throws Exception
	 */
	@Override
	public void completeCollection() throws Exception {
		if (file != null) {
			// System.err.println("Writing footer.");
			writeFooter();
			FileRegistrarHelper.registerFile(fileUrl);
			// System.err.println("Releasing file.");
			releaseFile();
		}
		super.completeCollection();
	}

	/**
	 * Releases the file handle.
	 */
	public void releaseFile() {
		try {
			// System.err.println("Closing file.");
			file.close();
		} catch (IOException ex) {
		}
	}

	/**
	 * Standard header for synchrotron experiments at beamline I11 at DLS written in the format of name/key-value pair,
	 * placed in between the markers of &DLS and &END
	 *
	 * <pre>
	 * &amp;DLS
	 * &lt;dt&gt;
	 * CarouselNo=1
	 * SampleID=S537
	 * SampleName=Silicon
	 * Description='Silicon Standard'
	 * Title='Calibration SampleInfo Scan'
	 * Comment='any comments added by the users'
	 * RunNumber=245
	 * Date=dd-mmm-yy
	 * Time=hr:min:sec
	 * BeamlineInfo=I11
	 * Project=HRPD
	 * Experiment=MAC
	 * Wavelength=1.2
	 * Temperature=295
	 * &lt;/dt&gt;
	 * &amp;END
	 * </pre>
	 */
	public void writeHeader() {
		String SRSWriteAtFileCreation;
		if (saminfo != null && saminfo.isConfigured() && this.sampleNo != Integer.MIN_VALUE) {
			// load sample information from Excel file for robotscan
			saminfo.open();
			saminfo.loadSampleInfo((int)this.sampleNo);
			saminfo.close();
		}
		try {
			// get relevant info and print to 'file'
			file.write("&DLS\n");
			// get date and time

			String date;
			String time;
			Locale currentLocale = Locale.getDefault();
			DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, currentLocale);
			DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, currentLocale);
			Date today = new Date();

			date = dateFormatter.format(today);
			time = timeFormatter.format(today);
			String header1 = "";
			if (saminfo != null) { // use robotscan and SampleInfo.xls
				header1 += "CarouselNo=" + saminfo.getCarouselNo() + "\n";
				header1 += "SampleID=" + saminfo.getSampleID() + "\n";
				header1 += "SampleName=" + saminfo.getSampleName() + "\n";
				header1 += "Description=" + saminfo.getDescription() + "\n";
				header1 += "Title=" + saminfo.getTitle() + "\n";
				header1 += "Comment=" + saminfo.getComment() + "\n";
			} else { //
				header1 += "CarouselNo=" + "Not Set" + "\n";
				header1 += "SampleID=" + "Not Set" + "\n";
				header1 += "SampleName=" + "Not Set" + "\n";
				header1 += "Description=" + "Not Set" + "\n";
				header1 += "Title=" + "Not Set" + "\n";
				header1 += "Comment=" + "Not Set" + "\n";
			}
			if (bli != null) {
				header1 += "RunNumber=" + bli.getFileNumber() + "\n";
			} else {
				header1 += "RunNumber=" + "" + "\n";
			}
			header1 += "ScanTime="+scantime+"\n";
			header1 += "Date=" + date + "\n";
			header1 += "Time=" + time + "\n";
			if (metadata != null) {
				try {
					header1 += "Beamline=" + metadata.getMetadataValue("instrument", "gda.instrument", "i11") + "\n";
					header1 += "Project=" + metadata.getMetadataValue("proposal", "gda.data.project", "HRPD") + "\n";
					header1 += "Experiment=" + metadata.getMetadataValue("investigation", "gda.data.experiment", "MAC")
							+ "\n";
				} catch (DeviceException e1) {
					logger.warn("failed to get metatdata from Metadata repository.");
				}
			}
			if (beam != null && !((Double) beam.getWavelength()).isNaN()) {
				header1 += "Wavelength=" + beam.getWavelength() + "\n";
			} else {
				header1 += "Wavelength=" + "Not Set" + "\n";
			}
			if (!((Double) this.temp).isNaN()) {
				header1 += "Temperature=" + this.temp + "\n";
			} else {
				header1 += "Temperature=" + 295 + "K\n";
			}

			file.write(header1);

			if (bli != null) {
				if (bli.getHeader() != null) {
					header.add(bli.getHeader());
				}
				if (bli.getSubHeader() != null) {
					header.add(bli.getSubHeader());
				}
			}

			// The following hack checks the root jython namespace for a
			// variable called SRSWriteAtFileCreation. If this exists, the sting
			// it contains is written to the header of the SRS file.

			try {
				SRSWriteAtFileCreation = (String) JythonServerFacade.getInstance().getFromJythonNamespace(
						"SRSWriteAtFileCreation");
				JythonServerFacade.getInstance().placeInJythonNamespace("SRSWriteAtFileCreation", null);// runCommand("SRSWriteAtFileCreation=None");
			} catch (Exception e) {
				// There was no variable SRSwriteToNextNewFile in the jython namespace or something else went wrong
				SRSWriteAtFileCreation = "";
			}
			// any other comment input with setHeader(String message) methods
			if (header != null && !header.isEmpty()) {
				file.write("\nCustom beamline header start here:\n");
				for (String s : header) {
					file.write(s + "\n");
				}
				file.write("\nCustom beamline header end here:\n");
			} else {
				header = new ArrayList<String>(0);
			}

			// Write metadata in header data at start of file (KLUDGE)
			if (SRSWriteAtFileCreation != null) {
				file.write("<MetaDataAtStart>" + SRSWriteAtFileCreation + "</MetaDataAtStart>\n");
			}

			file.write("&END\n");

			// now write the column headings
			writeColumnHeadings();

			// file.write("\n");

			if (saminfo != null && saminfo.isSaveExperimentSummary()) {
				saminfo.setRunNumber(String.valueOf(bli.getFileNumber()));
				saminfo.setDate(date);
				saminfo.setTime(time);
				try {
					saminfo.setBeamline(metadata.getMetadataValue("instrument", "gda.instrument", "i11"));
					saminfo.setProject(metadata.getMetadataValue("proposal", "gda.data.project", "HRPD"));
					saminfo.setExperiment(metadata.getMetadataValue("investigation", "gda.data.experiment", "MAC"));
				} catch (DeviceException e) {
					logger.warn("failed to get metatdata from Metadata repository.");
				}
				saminfo.setWavelength(String.valueOf(beam.getWavelength()));
				saminfo.setTemperature(String.valueOf(this.temp));

				saminfo.saveExperimentInfo((int)sampleNo);
			}
		} catch (IOException ex) {
			logger.error("Error when writing MAC Data File header: " + ex.getMessage(), ex);
		}
	}

	/**
	 * This should be extended by inheriting classes.
	 */
	public void writeColumnHeadings() {
		String header = "";

		for (Scannable scannable : this.allScannables) {
			header += scannable.getName() + delimiter;
		}
		numberOfDetectors = 0;
		for (Detector det : this.allDetectors) {
			EpicsMultiChannelScaler mcs = (EpicsMultiChannelScaler) det;
			Set<Map.Entry<Integer, Mca>> entrySet = mcs.getMcaList().entrySet();
			for (Map.Entry<Integer, Mca> e : entrySet) {
				header += e.getValue().getDetectorName() + delimiter;
				numberOfDetectors++;
			}
		}
		header += "ttherror" + delimiter;

		header = header.trim() + "\n";
		try {
			file.write(header);
		} catch (IOException e) {
			logger.error("Failed when write header to file {}", fileUrl, e);
			releaseFile();
			e.printStackTrace();
		}
		header = null;
	}

	/**
	 * This should be extended by inheriting classes.
	 */
	public void writeFooter() {
	}

	/**
	 * Returns the full path of the folder which data files are written to.
	 *
	 * @return the full path of the folder which data files are written
	 */
	public String getDataDir() {
		return PathConstructor.createFromDefaultProperty();
	}

	/**
	 * Get the delimiter used between columns
	 *
	 * @return the delimiter used between columns
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Set the delimiter used between columns (default is a tab '\t')
	 *
	 * @param delimiter
	 *            String
	 */
	public void setDelimiter(String delimiter) {
		DataWriterBase.delimiter = delimiter;
	}

	/**
	 * Returns the number of the last file written to.
	 *
	 * @return int
	 */
	public int getFileNumber() {
		return thisFileNumber;
	}

	@Override
	public String getCurrentFileName() {
		return currentFileName;
	}

	@Override
	public void setHeader(String header) {
		this.header.add(header);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCurrentScanIdentifier() {
		return thisFileNumber;
	}

}
