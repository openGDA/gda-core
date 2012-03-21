/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import gda.util.exceptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExafsIncrementalFile extends IncrementalFile {

	private static final Logger logger = LoggerFactory.getLogger(ExafsIncrementalFile.class);

	public ExafsIncrementalFile() throws InstantiationException {
		super();
	}

	@Override
	public void setHeader(String header) {
	}

	@Override
	public void writeHeader() {
	}

	@Override
	public void writeColumnHeadings() {
	}

	@Override
	public void writeFooter() {
	}

	/**
	 * Closes current file and opens a new file with an incremental number. For use when many files being created
	 * instead of a single file being appended to.
	 * 
	 * @throws Exception
	 */
	@Override
	public void createNextFile() throws Exception {
		try {
			if (file != null) {
				file.close();
			}
			currentFileName = thisFileNumber + "." + this.fileExtension;

			// See if we want a file prefix...
			if (filePrefix == null) {
				this.filePrefix = LocalProperties.get("gda.data.scan.datawriter.filePrefix");
			}
			if (this.filePrefix != null) {
				currentFileName = thisFileNumber + "_" + filePrefix;
			}

			fileName = currentFileName;

			// don't use File.extension as the print out will not work in windows - and Java will know what to do with a
			// / on Windows.
			if (!dataDir.endsWith("/")) {
				dataDir += "/";
			}

			fileUrl = dataDir + currentFileName;
			try {
				// Check to see if the file(s) already exists!
				File f = new File(fileUrl);
				if (f.exists()) {
					throw new Exception("The file " + fileUrl + " already exists.");
				}
				File fparent = new File(f.getParent());
				if (!fparent.exists()) {
					fparent.mkdirs();
				}
				file = new FileWriter(f);
			} catch (IOException ex1) {
				String error = "Failed to create a new data file: " + fileUrl + " - " + ex1.getMessage();
				exceptionUtils.logException(logger, "Failed to create a new data file: " + fileUrl, ex1);
				currentScanController.haltCurrentScan();
				terminalPrinter.print(error);
			}
			terminalPrinter.print("Writing data to file:" + fileUrl);
		} catch (Exception ex) {
			String error = "Failed to create a new data file: " + fileUrl + " - " + ex.getMessage();
			exceptionUtils.logException(logger, "Failed to create a new data file: " + fileUrl, ex);
			terminalPrinter.print(error);
			terminalPrinter.print(ex.getMessage());
			throw new Exception(error);
		}
	}
}
