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

package gda.device.detector.uviewnew.corba.impl;

import gda.configuration.properties.LocalProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles UView Image data file opening, closing and writing.
 */
public class ImageDataFileWriter {
	private static final Logger logger = LoggerFactory.getLogger(ImageDataFileWriter.class);

	private String debugName = "DataFileWriter";

	private String dataFileName = null;

	private BufferedWriter writer = null;

	private String filePrefix = null;

	private String fileSuffix = null;

	/**
	 * Constructor.
	 * 
	 * @param filePrefix
	 * @param fileSuffix
	 */
	public ImageDataFileWriter(String filePrefix, String fileSuffix) {
		this.filePrefix = filePrefix;
		this.fileSuffix = fileSuffix;
	}

	/**
	 * Gets the next run number.
	 * 
	 * @return int value of the next run number
	 */
	private int getNextRunNumber() {
		String runNumberFile = LocalProperties.get("gda.runNumberFile");
		BufferedReader in = null;
		BufferedWriter out = null;
		int runNumber = 0;

		// FIXME: it should only be possible for one DataFileWriter at a time
		// to access the runNumberFile

		try {
			in = new BufferedReader(new FileReader(runNumberFile));
			String line = in.readLine();
			runNumber = Integer.parseInt(line);
			in.close();
			out = new BufferedWriter(new FileWriter(runNumberFile));
			out.write("" + (runNumber + 1));
			out.close();
		} catch (FileNotFoundException fnfe) {
			logger.error("Failed to find runnumber file: " + runNumberFile);
		} catch (IOException ioe) {
			logger.error("IOException reading or writing file: " + runNumberFile);
		}

		return runNumber;
	}

	/**
	 * Constructs next file name
	 * 
	 * @return String value of the next datafile name
	 */
	private String nextDataFileName() {
		return filePrefix + getNextRunNumber() + "." + fileSuffix;
	}

	/**
	 * Opens data file.
	 */
	public void open() {
		try {
			dataFileName = nextDataFileName();
			writer = new BufferedWriter(new FileWriter(new File(dataFileName)));
		} catch (IOException ioe) {
			logger.error(debugName + ".open() caught IOException" + ioe.getMessage());
		}
	}

	/**
	 * Writes a string to the data file.
	 * 
	 * @param toWrite
	 *            the string to write
	 */
	public void write(String toWrite) {
		if (writer != null) {
			try {
				writer.write(toWrite + "\n");
			} catch (IOException ioe) {
				logger.error(debugName + ".write() caught IOException" + ioe.getMessage());
			}
		}
	}

	/**
	 * Closes data file
	 */
	public void close() {
		try {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		} catch (IOException ioe) {
			logger.error(debugName + ".close() caught IOException" + ioe.getMessage());
		}
	}

	/**
	 * Get the data file name
	 * 
	 * @return String dataFileName
	 */
	public String getDataFileName() {
		return dataFileName;
	}
}
