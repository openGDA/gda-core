/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

package gda.data.scan.datawriter;

import gda.configuration.properties.LocalProperties;
import gda.jython.IJythonNamespace;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of IncrementalFile class which produces files in the SRS ascii format. It uses the reference of the scan
 * which created this datahandler to determine which columns to have in the output. The header information is passed as
 * an ArrayList<String> with: <LI>element 0 is title element 1 is condition1 element 2 is condition2 element 3 is
 * condition3 element 4 - n are comments</LI>
 *
 * <PRE>
 *
 * &amp;SRS SRSRUN=129283,SRSDAT=040910,SRSTIM=181147,
 * SRSSTN='W164',SRSPRJ='POWDERDF',SRSEXP='12345432', SRSTLE='k733a ',
 * SRSCN1='k733 aft',SRSCN2='room tem',SRSCN3='drot 3 s', &amp;END Calib A with
 * error 0.0000 0.0000 Detector 2 Angle 0 Calib B with error 1.000000 0.000000
 * SPECTRUM 1 Accumulation Time for Data = 120.000 Seconds Current Temp 0.0000
 * Set Point 0.0000 Active segment 0.0000 Active program 0.0000 Program Elapsed
 * time 0.0000 Time parameters read TEMP/TIME DISABLED DEDS time stamp Fri Sep
 * 10 18:13:49 2004 119. 70. 51. 49. 42. 29. 47. 45. etc...
 *
 * </PRE>
 * <p>
 * The columns may be automatically aligned to the headers by setting the java property
 * gda.data.scan.datawriter.dataFormat.SixdSrsDataWriter.aligncolumns to true
 */
public class SixdSrsDataWriter extends IncrementalFile {

	private static final Logger logger = LoggerFactory.getLogger(SixdSrsDataWriter.class);

	/**
	 * When set to true by localproperty then header and column data will be padded with spaces so they are aligned to
	 * each other in the file. There will still be a tab character betwwen each column.
	 */
	private boolean alignColumns = false;

	/**
	 * Constructor
	 *
	 * @throws InstantiationException
	 */
	public SixdSrsDataWriter() throws InstantiationException {
		super();

		alignColumns = LocalProperties.get("gda.data.scan.datawriter.dataFormat.SixdSrsDataWriter.aligncolumns", "false")
				.toLowerCase().equals("true");
	}

	IScanDataPoint latestPoint;

	/**
	 * for incremental addition of data.
	 * <P>
	 * data should be in the form of a double array. This data will be tab separated, terminated with an "\n", and
	 * appended to the open file.
	 *
	 * @param dataPoint
	 *            Object
	 * @throws Exception
	 */
	@Override
	public void addData(IScanDataPoint dataPoint) throws Exception {
		latestPoint = dataPoint;

		// Extracting the line from the dataPoint might cause an exception.
		// Do this before calling prepareForCollection so as not to risk creating
		// an empty file.
		String lineToAdd;

		try {
			if (alignColumns) {
				lineToAdd = dataPoint.toFormattedString() + "\n";
			} else {
				lineToAdd = dataPoint.toDelimitedString() + "\n";
			}
		} catch (Exception e) {
			String msg = "Problem extracting delimited string from point " + dataPoint.getCurrentPointNumber();
			logger.error(msg , e);
			throw new Exception(msg , e);
		}


		// Check that the datapoint returns a header before creating a file,
		// so as not to risk writing a dodgy one.
		getHeaderLine();

		if (firstData) {
			this.prepareForCollection();
			firstData = false;
		}

		dataPoint.setCurrentFilename(getCurrentFileName());
		if (file == null) {
			logger.error("In addData() file field was found to be null. Throwing IllegalStateException.");
			throw new IllegalStateException("Could not add data to file as the file field is null (probably due to it having been closed)");
		}
		file.write(lineToAdd);
		file.flush();

		super.addData(dataPoint); // passes the data on to the extenders

	}

	@Override
	public void setHeader(String header) {
		this.header = new ArrayList<String>();
		for (int i = 0; i < 4; i++) {
			this.header.add("");
		}
		this.header.add(header);
	}

	@Override
	public void writeHeader() {
		String SRSWriteAtFileCreation;
		try {
			// get relevent info and print to 'file'
			file.write(" &SRS\n");
			// get datetime
			Calendar rightNow = Calendar.getInstance();

			int year = rightNow.get(Calendar.YEAR);
			int month = rightNow.get(Calendar.MONTH);
			int day = rightNow.get(Calendar.DAY_OF_MONTH);
			int hour = rightNow.get(Calendar.HOUR);
			int minute = rightNow.get(Calendar.MINUTE);
			int second = rightNow.get(Calendar.SECOND);
			/* For testing we want to ensure files are identical and so we need to override the time line */
			if (LocalProperties.check("gda.data.scan.datawriter.setTime0")) {
				year = 2000;
				month = 1;
				day = 1;
				hour = minute = second = 0;
			}
			long fileNumber=0;
			try {
				fileNumber = getFileNumber();
			} catch (Exception e) {
				logger.error("Error getting fileNumber", e);
			}
			String line = " SRSRUN=" + fileNumber + ",SRSDAT=" + String.valueOf(year) + String.valueOf(month)
					+ String.valueOf(day) + ",SRSTIM=" + String.valueOf(hour) + String.valueOf(minute)
					+ String.valueOf(second) + ",\n";

			file.write(line);

			// get fixed properties
			String srsStation = LocalProperties.get("gda.data.scan.datawriter.srsStation");
			String srsProject = LocalProperties.get("gda.data.scan.datawriter.srsProject");
			String srsExperiment = LocalProperties.get("gda.data.scan.datawriter.srsExperiment");

			// add extra header information if a header has been added
			// through the setHeader() method.

			String srsTitle = "";
			String srsCondition1 = "";
			String srsCondition2 = "";
			String srsCondition3 = "";

			// The following hack checks the root jython namespace for a variable called
			// SRSWriteAtFileCreation. If this exists, the sting it contains is written
			// to the header of the SRS file.

			try {
				IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
				SRSWriteAtFileCreation = (String) jythonNamespace.getFromJythonNamespace("SRSWriteAtFileCreation");
				jythonNamespace.placeInJythonNamespace("SRSWriteAtFileCreation", null);
				// runCommand("SRSWriteAtFileCreation=None");
			} catch (Exception e) {
				// There was no variable SRSwriteToNextNewFile in the jython
				// namespace or something else went wrong
				SRSWriteAtFileCreation = "";
			}

			if (header != null && header.size() >= 4) {
				srsTitle = header.get(0);
				srsCondition1 = header.get(1);
				srsCondition2 = header.get(2);
				srsCondition3 = header.get(3);
			} else {
				header = new ArrayList<String>(0);
			}

			String blanks8 = "        ";
			String blanks60 = "                                                            ";
			srsStation += "    ";
			srsProject += blanks8;
			srsExperiment += blanks8;
			srsTitle += blanks60;
			srsCondition1 += blanks8;
			srsCondition2 += blanks8;
			srsCondition3 += blanks8;

			file.write(" SRSSTN='" + srsStation.substring(0, 4) + "',SRSPRJ='" + srsProject.substring(0, 8)
					+ "',SRSEXP='" + srsExperiment.substring(0, 8) + "',\n");
			file.write(" SRSTLE='" + srsTitle.substring(0, 60) + "',\n");
			file.write(" SRSCN1='" + srsCondition1.substring(0, 8) + "',SRSCN2='" + srsCondition2.substring(0, 8)
					+ "',SRSCN3='" + srsCondition3.substring(0, 8) + "',\n");

			// Write metadata in header data at start of file (KLUDGE)
			file.write("<MetaDataAtStart>\n");
			file.write("command="+ this.getCommand() + "\n");

			if (SRSWriteAtFileCreation != null) {
				file.write(SRSWriteAtFileCreation.trim());
			}

			file.write("\n</MetaDataAtStart>\n");
			file.write(" &END\n");

			// Now write out any comment lines
			for (int i = 4; i < header.size(); i++) {
				file.write(header.get(i) + "\n");
			}


			// now write the column headings
			file.write(getHeaderLine() + "\n");
		} catch (IOException ex) {
			logger.error("Error when writing SixdSrsDataWriter header", ex);
		}
	}

	private String getCommand(){
		// The following hack checks the root jython namespace for a variable called
		// CustomerisedUserCommand. If this exists, the sting it contains is used as
		// the scan command, instead of the command from the latest scanDataPoint.
		String CustomerisedUserCommand;

		try {
			IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
			CustomerisedUserCommand = (String) jythonNamespace.getFromJythonNamespace("CustomerisedUserCommand");
			jythonNamespace.placeInJythonNamespace("CustomerisedUserCommand", null);
		} catch (Exception e) {
			// There was no variable CustomerisedUserCommand in the jython namespace or something else went wrong
			CustomerisedUserCommand = "";
		}

		if (CustomerisedUserCommand != null) {
			logger.info("Get customerised command from Jython namespace: " + CustomerisedUserCommand);
			return CustomerisedUserCommand;
		}

		return this.latestPoint.getCommand();
	}

	private String getHeaderLine() {
		if (alignColumns) {
			return latestPoint.getHeaderString();
		}
		return latestPoint.getDelimitedHeaderString();
	}

	@Override
	public void writeFooter() {
	}

	@Override
	public void writeColumnHeadings() {
	}
}
