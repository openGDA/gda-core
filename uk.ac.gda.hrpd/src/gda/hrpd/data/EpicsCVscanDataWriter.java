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

import gda.beamline.BeamlineInfo;
import gda.beamline.beam.Beam;
import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.metadata.Metadata;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Robot;
import gda.device.Scannable;
import gda.device.Temperature;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.device.detector.multichannelscaler.Mca;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.hrpd.SampleInfo;
import gda.jython.JythonServerFacade;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class writes files of tabulated data from Multiple Analyser Crystal Detectors, along with a header and footer if
 * needed, in ASCII format. The files created use names which are an increment from the last name.
 */
public class EpicsCVscanDataWriter implements DataWriter, Findable, Configurable {
	/**
	 * logging instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(EpicsCVscanDataWriter.class);
	private String name;
	/**
	 * the current file number being written to, which is managed by the NumTracker Class
	 */
	protected long thisFileNumber = 0;
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
	 * the full file name of the current data file
	 */
	protected String currentFileName = null;
	/**
	 * sample information instance
	 */
	private SampleInfo sampaleInfo;
	/**
	 * Beam information instance
	 */
	private Beam beamInfo;
	public SampleInfo getSampleInfo() {
		return sampaleInfo;
	}

	public void setSampleInfo(SampleInfo saminfo) {
		this.sampaleInfo = saminfo;
	}

	public Beam getBeamInfo() {
		return beamInfo;
	}

	public void setBeamInfo(Beam beam) {
		this.beamInfo = beam;
	}

	public BeamlineInfo getBeamlineInfo() {
		return beamlineInfo;
	}

	public void setBeamlineInfo(BeamlineInfo bli) {
		this.beamlineInfo = bli;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * beamline parameters
	 */
	private BeamlineInfo beamlineInfo;
	/**
	 * Metadata instance
	 */
	private Metadata metadata;

	private double sampleNo = Double.MIN_VALUE;

	private ArrayList<Scannable> parentScannables = new ArrayList<Scannable>();

	private String motorName;
	private ArrayList<Detector> allDetectors = new ArrayList<Detector>();
	private double temp = Double.NaN;
	private int numberOfDetectors;
	private double scantime;
	private double samplePosition=Double.NaN;
	private boolean rawData = false;
	private ArrayList<String> header;
	private String delimiter = "\t";
	private double monitorAverage;

	/**
	 * Constructor which determines the name of the next file and creates it.
	 */
	public EpicsCVscanDataWriter() {
	}

	@Override
	public void configure() {
		if (metadata == null) {
			logger.warn("{}: Can not find 'GDAMetadata' object.", getName());
			//throw new FactoryException(getName()+": Can not find 'GDAMetadata' object.");
		}

		if (beamInfo == null) {
			logger.warn("{}: Can not find beam information object 'beam'", getName());
		}

		if (beamlineInfo == null) {
			logger.warn("{}: Can not find beamline information object 'beamline'", getName());
		}
		if (sampaleInfo == null) {
			logger.warn("{}: Cannot find sample information data object 'SampleInfo'", getName());
		}

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
	 * @param motorName
	 * @param allDetectors
	 * @param path1
	 * @param data
	 * @param scantime
	 * @return filename
	 */
	public String addRawData(File file, int rows, ArrayList<Scannable> parentScannables, String motorName,
			ArrayList<Detector> allDetectors, double[] path1, int[][] data, double scantime, double monitorValue) {

		String datapoint = "";
		this.parentScannables = parentScannables;
		this.motorName = motorName;
		this.allDetectors = allDetectors;
		this.scantime = scantime;
		this.monitorAverage = monitorValue;
		this.rawData = true;
		int i = 0;

		getParentScannablesPositions(parentScannables);
		JythonServerFacade.getInstance().print("Number of data points : " + rows);
		JythonServerFacade.getInstance().print(
				"Writing raw data to file: " + file.getAbsolutePath() + ", Please wait ...");
		try {

			FileWriter filewriter = new FileWriter(file);
			writeHeader(filewriter);

			for (i = 0; i < rows; i++) {
				if (path1 != null) {
					datapoint += path1[i] + delimiter;
				}
				for (int j = 0; j < numberOfDetectors; j++) {
					datapoint += data[j][i] + delimiter;
				}
				datapoint = datapoint.trim() + "\n";
				filewriter.write(datapoint);
				datapoint = "";
			}
			writeFooter();
			filewriter.close();
			datapoint = null;
			FileRegistrarHelper.registerFile(file.getAbsolutePath());
		} catch (IOException e) {
			String error = "Cannot write to the data file: " + file.getAbsolutePath();
			logger.error(error, e);
			JythonServerFacade.getInstance().haltCurrentScan();
			JythonServerFacade.getInstance().print(error);
		} finally {
			if (i < rows) {
				logger.info("Number of data points saved in file {} is {}.", file.getAbsoluteFile(), i);
			}
			// Print informational message to console.
			JythonServerFacade.getInstance().print("Writing data to file: " + file.getAbsolutePath() + " completed");

		}
		return file.getAbsolutePath();
	}

	public String addRebinnedData(File file, int rows, ArrayList<Scannable> parentScannables, double[] tth, double[] count,
			double[] counterror, double scantime, double monitorValue) {

		String datapoint = "";
		this.parentScannables = parentScannables;
		this.scantime = scantime;
		this.monitorAverage = monitorValue;
		this.rawData = false;

		int i = 0;
		getParentScannablesPositions(parentScannables);

		JythonServerFacade.getInstance().print("Number of data points : " + rows);
		JythonServerFacade.getInstance().print(
				"Starting write rebinned data to file: " + file.getAbsolutePath());
		try {

			FileWriter filewriter = new FileWriter(file);
			writeHeader(filewriter);
			for (i = 0; i < rows; i++) {
				if (tth != null) {
					datapoint += String.format("%7.3f", tth[i]) + delimiter;
				}
				if (count != null) {
					datapoint += String.format("%10.3f",count[i]) + delimiter;
				}
				if (counterror != null) {
					datapoint += String.format("%10.3f",counterror[i]) + delimiter;
				}
				datapoint = datapoint.trim() + "\n";
				filewriter.write(datapoint);
				datapoint = "";
			}
			writeFooter();
			filewriter.close();
			datapoint = null;
			FileRegistrarHelper.registerFile(file.getAbsolutePath());
		} catch (IOException e) {
			String error = "Cannot write to the data file: " + file.getAbsolutePath();
			logger.error(error, e);
			JythonServerFacade.getInstance().haltCurrentScan();
			JythonServerFacade.getInstance().print(error);
		} finally {
			if (i < rows) {
				logger.info("Number of data points saved in file {} is {}.", file.getAbsoluteFile(), i);
			}
			// Print informational message to console.
			JythonServerFacade.getInstance().print("Writing data to file: " + file.getAbsolutePath() + " completed");
		}
		return file.getAbsolutePath();
	}

	private void getParentScannablesPositions(ArrayList<Scannable> parentScannables) {
		if (!parentScannables.isEmpty()) {
			for (Scannable s : this.parentScannables) {
				if (s instanceof Robot) { // for sample changer
					try {
						this.sampleNo = ((Robot) s).getSamplePosition();
					} catch (DeviceException e) {
						logger.error("Cannot get the sample number.", e);
					}
				} else if (s instanceof Temperature) { // for temperature controller
					try {
						this.temp=Double.parseDouble(s.getPosition().toString());
					} catch (DeviceException e) {
						logger.error("Cannot get the sample temperature from {}.",s.getName(), e);
					}
				} else if (s.getName().equalsIgnoreCase("spos")) { // for sample position
					try {
						this.samplePosition= Double.parseDouble(s.getPosition().toString());
					} catch (DeviceException e) {
						logger.error("Cannot get the sample position from {}.",s.getName(), e);
					}
				} else {
					try {
						setHeader(s.getName() + "=" + s.getPosition());
					} catch (DeviceException e) {
						logger.error("Failed to get position from {}",s.getName(),e);
					}
				}

			}
		}
	}

	/**
	 * Writes any file footers and closes file.
	 */
	@Override
	public void completeCollection() {
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
	 * SamplePosition=20.0
	 * &lt;/dt&gt;
	 * &amp;END
	 * </pre>
	 */
	public void writeHeader(FileWriter filewriter) {
		String SRSWriteAtFileCreation;
		if (sampaleInfo != null && sampaleInfo.isConfigured() && this.sampleNo != Integer.MIN_VALUE) {
			// load sample information from Excel file for robotscan
			sampaleInfo.open();
			sampaleInfo.loadSampleInfo((int)this.sampleNo);
			sampaleInfo.close();
		}
		try {
			// get relevant info and print to 'file'
			filewriter.write("&DLS\n");
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
			if (sampaleInfo != null) { // use robotscan and SampleInfo.xls
				header1 += "CarouselNo=" + sampaleInfo.getCarouselNo() + "\n";
				header1 += "SampleID=" + sampaleInfo.getSampleID() + "\n";
				header1 += "SampleName=" + sampaleInfo.getSampleName() + "\n";
				header1 += "Description=" + sampaleInfo.getDescription() + "\n";
				header1 += "Title=" + sampaleInfo.getTitle() + "\n";
				header1 += "Comment=" + sampaleInfo.getComment() + "\n";
			} else { //
				header1 += "CarouselNo=" + "Not Set" + "\n";
				header1 += "SampleID=" + "Not Set" + "\n";
				header1 += "SampleName=" + "Not Set" + "\n";
				header1 += "Description=" + "Not Set" + "\n";
				header1 += "Title=" + "Not Set" + "\n";
				header1 += "Comment=" + "Not Set" + "\n";
			}
			if (beamlineInfo != null) {
				header1 += "RunNumber=" + beamlineInfo.getFileNumber() + "\n";
			} else {
				header1 += "RunNumber=" + "" + "\n";
			}
			header1 += "ScanTime=" + scantime + "\n";
			header1 += "MonitorAverageCount=" + monitorAverage + "\n";
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
			if (beamInfo != null && !((Double) beamInfo.getWavelength()).isNaN()) {
				header1 += "Wavelength=" + beamInfo.getWavelength() + "\n";
			} else {
				header1 += "Wavelength=" + "Not Set" + "\n";
			}
			if (!((Double) this.temp).isNaN()) {
				header1 += "Temperature=" + this.temp + "\n";
			} else {
				header1 += "Temperature=" + "Room Temperature" + " K\n";
			}
			if (!((Double) this.samplePosition).isNaN()) {
				header1 += "SamplePosition=" + this.samplePosition + "\n";
			} else {
				header1 += "SamplePosition=" + "Not Recorded" + "\n";
			}

			filewriter.write(header1);

			if (beamlineInfo != null) {
				if (beamlineInfo.getHeader() != null) {
					header.add(beamlineInfo.getHeader());
				}
				if (beamlineInfo.getSubHeader() != null) {
					header.add(beamlineInfo.getSubHeader());
				}
			}

			// The following hack checks the root jython namespace for a
			// variable called SRSWriteAtFileCreation. If this exists, the sting
			// it contains is written to the header of the SRS file.

			try {
				SRSWriteAtFileCreation = (String) JythonServerFacade.getInstance().getFromJythonNamespace(
						"SRSWriteAtFileCreation");
				JythonServerFacade.getInstance().placeInJythonNamespace("SRSWriteAtFileCreation", null);
				// runCommand("SRSWriteAtFileCreation=None");
			} catch (Exception e) {
				// There was no variable SRSwriteToNextNewFile in the jython namespace or something else went wrong
				SRSWriteAtFileCreation = "";
			}
			// any other comment input with setHeader(String message) methods
			if (header != null && !header.isEmpty()) {
				filewriter.write("\nAdditional custom metadata start here:\n");
				for (String s : header) {
					filewriter.write(s + "\n");
				}
				filewriter.write("\nAdditional custom metadata end here:\n");
			} else {
				header = new ArrayList<String>(0);
			}

			// Write metadata in header data at start of file (KLUDGE)
			if (SRSWriteAtFileCreation != null) {
				filewriter.write("<MetaDataAtStart>" + SRSWriteAtFileCreation + "</MetaDataAtStart>\n");
			}

			filewriter.write("&END\n");

			// now write the column headings
			writeColumnHeadings(filewriter);

			// filewriter.write("\n");

			if (sampaleInfo != null && sampaleInfo.isSaveExperimentSummary()) {
				sampaleInfo.setRunNumber(String.valueOf(beamlineInfo.getFileNumber()));
				sampaleInfo.setDate(date);
				sampaleInfo.setTime(time);
				try {
					sampaleInfo.setBeamline(metadata.getMetadataValue("instrument", "gda.instrument", "i11"));
					sampaleInfo.setProject(metadata.getMetadataValue("proposal", "gda.data.project", "HRPD"));
					sampaleInfo.setExperiment(metadata.getMetadataValue("investigation", "gda.data.experiment", "MAC"));
				} catch (DeviceException e) {
					logger.warn("failed to get metatdata from Metadata repository.");
				}
				sampaleInfo.setWavelength(String.valueOf(beamInfo.getWavelength()));
				sampaleInfo.setTemperature(String.valueOf(this.temp));

				sampaleInfo.saveExperimentInfo((int)sampleNo);
			}
		} catch (IOException ex) {
			logger.error("Error when writing MAC Data File header: " + ex.getMessage(), ex);
		}
	}

	/**
	 * This should be extended by inheriting classes.
	 */
	public void writeColumnHeadings(FileWriter filewriter) {
		String header = "";
		if (rawData) {
			header +=motorName.trim() + delimiter;
			numberOfDetectors = 0;
			for (Detector det : this.allDetectors) {
				EpicsMultiChannelScaler mcs = (EpicsMultiChannelScaler) det;
				Set<Map.Entry<Integer, Mca>> entrySet = mcs.getMcaList().entrySet();
				for (Map.Entry<Integer, Mca> e : entrySet) {
					header += e.getValue().getDetectorName() + delimiter;
					numberOfDetectors++;
				}
			}
		} else {
			header = "tth" + delimiter + "counts" + delimiter + "error";
		}
		header = header.trim() + "\n";
		try {
			filewriter.write(header);
		} catch (IOException e) {
			logger.error("Failed when write header to file {}", filewriter, e);
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
		return delimiter ;
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
	 * @return long
	 */
	public long getFileNumber() {
		return thisFileNumber=beamlineInfo.getFileNumber();
	}
	public long incrementFileNumber() {
		return thisFileNumber=beamlineInfo.getNextFileNumber();
	}
	/**
	 * return the scan file name with prefix and/or suffix if specified, without extension specifier.
	 * @return root part of the MAC file name.
	 */
	@Override
	public String getCurrentFileName() {
		thisFileNumber = beamlineInfo.getFileNumber();
		this.filePrefix = LocalProperties.get("gda.data.file.prefix", "");
		this.fileSuffix = LocalProperties.get("gda.data.file.suffix", "-mac");
		currentFileName = this.filePrefix + thisFileNumber + this.fileSuffix;
		return currentFileName;
	}

//	public String getCurrentFileNameWithExtension(String ext) {
//		return getCurrentFileName() + "." + ext;
//	}

	@Override
	public void setHeader(String header) {
		this.header.add(header);
	}

	public void setComment(String comment) {
		this.header.add(comment);
	}
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getCurrentScanIdentifier() {
		return String.valueOf(thisFileNumber);
	}

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<String> getHeader() {
		return this.header;
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setHeader(ArrayList<String> header) {
		this.header=header;
		
	}

	@Override
	public void configureScanNumber(Long scanNumber) throws Exception {
		//do nothing as this class does not use the scanNumber but gets the fileNumber from beamlineInfo
	}

}
