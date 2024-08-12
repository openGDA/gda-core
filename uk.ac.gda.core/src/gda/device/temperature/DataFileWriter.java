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

package gda.device.temperature;

import gda.data.NumTracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles data file opening, closing and writing.
 */
public class DataFileWriter {
	private static final Logger logger = LoggerFactory.getLogger(DataFileWriter.class);

	private String debugName = "DataFileWriter";

	private String dataFileName = null;

	private BufferedWriter writer = null;

	private String filePrefix = null;

	private String fileExtension = null;

	private NumTracker numTracker = null;

	/**
	 * Create a data file writer.
	 *
	 * @param filePrefix
	 *            the file prefix, to be added before the run number.
	 * @param fileExtension
	 *            the file extension, to be added after the run number.
	 */
	public DataFileWriter(String filePrefix, String fileExtension) {
		this.filePrefix = filePrefix;
		this.fileExtension = fileExtension;
		try {
			numTracker = new NumTracker(fileExtension);
		} catch (IOException e) {
			logger.error(debugName + "Failed to find runnumber file");
		}
	}

	/**
	 * Constructs next file name
	 *
	 * @return the next datafile name
	 */
	private String nextDataFileName() {
		return filePrefix + numTracker.incrementNumber() + "." + fileExtension;
	}

	/**
	 * Opens data file.
	 */
	public void open() {
		try {
			dataFileName = nextDataFileName();
			writer = new BufferedWriter(new FileWriter(new File(dataFileName)));
		} catch (IOException ioe) {
			logger.error("Error in {}.open()", debugName, ioe);
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
				logger.error("Error in {}.write({})", debugName, toWrite, ioe);
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
				dataFileName = null;
			}
		} catch (IOException ioe) {
			logger.error("Error in {}.close()", debugName, ioe);
		}
	}

	/**
	 * Get the current data filename. This may be null if not open.
	 *
	 * @return the current data file name.
	 */
	public String getDataFileName() {
		return dataFileName;
	}
}
