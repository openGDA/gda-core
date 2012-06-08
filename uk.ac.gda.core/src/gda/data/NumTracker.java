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

package gda.data;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to get the next number to use for an experiment using incrementing numbers
 * stored on disk.
 * 
 * <p>The numbers are stored in a filename, with an experiment-specific file extension. Filenames have the format
 * {@code "<number>.<extension>"}. Examples include {@code "123.tmp"} and {@code "765.cd"}.
 * 
 * <p>The files are stored in the directory specified by the <code>gda.data.numtracker</code> property if set
 * otherwise in the directory specified by the <code>gda.var</code> property.
 */
public class NumTracker {
	private static final Logger logger = LoggerFactory.getLogger(NumTracker.class);

	private static final String FILE_PATTERN_TEMPLATE = "(\\d+)(\\.%s)$";
	
	private File dir;

	private String extension;

	private FilenameFilter filter;


	/**
	 * Creates a {@code NumTracker} with extension set to tmp or value of java property 
	 * LocalProperties.GDA_DATA_NUMTRACKER_EXTENSION,
	 * 
	 * @throws IOException if the property is not set or the directory cannot be created
	 */
	public NumTracker() throws IOException {
		this("tmp");
	}

	/**
	 * Creates a {@code NumTracker} with the specified extension.
	 * 
	 * @param extension the file extension to use if not set in java property LocalProperties.GDA_DATA_NUMTRACKER_EXTENSION, e.g. {@code "tmp"}
	 * 
	 * @throws IOException if the property is not set or the directory cannot be created
	 */
	public NumTracker(String extension) throws IOException {
		setFileExtension(LocalProperties.get(LocalProperties.GDA_DATA_NUMTRACKER_EXTENSION,extension));

		String fallbackDirname = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		String dirname = LocalProperties.get(LocalProperties.GDA_DATA_NUMTRACKER, fallbackDirname);
		// Try to read the directory from the java.properties
		if (dirname == null) {
			final String msg = "You have not set the " + LocalProperties.GDA_VAR_DIR + " or " + LocalProperties.GDA_DATA_NUMTRACKER + " property";
			logger.error(msg);
			throw new IOException(msg);
		}
		
		// If the directory doesn't exist then create it.
		dir = new File(dirname);
		if (!dir.exists()) {
			logger.debug("Creating " + dir);
			if (!dir.mkdirs()) {
				final String msg = "Could not create num tracker directory " + dir;
				logger.error(msg);
				throw new IOException(msg);
			}
		}
	}

	/**
	 * Returns the current file number.
	 * 
	 * @return the current file number
	 */
	public long getCurrentFileNumber() {
		return findBiggestNumber();
	}

	/**
	 * Increments the number, replaces the file on disk, and returns the new number.
	 * 
	 * @return the new number (or zero if a problem occurs)
	 */
	public long incrementNumber() {
		long nextNum = findBiggestNumber() + 1;
		if (writeNewFile(nextNum)) {
			deleteNumberedFile(nextNum - 1);
			return nextNum;
		}
		return 0;
	}

	/**
	 * Set the file number to a new value.
	 * 
	 * @param number the new file number
	 */
	public void setFileNumber(long number) {
		long oldNum = findBiggestNumber();
		if (writeNewFile(number)) {
			deleteNumberedFile(oldNum);
		}
	}

	/**
	 * Reset the file number to zero. Removes all other numbered files (with the same extension).
	 */
	public void resetFileNumber() {
		if (dir.exists()) {
			for (File f : dir.listFiles(filter)) {
				if (!f.delete()) {
					logger.error("Could not delete file " + f);
				}
			}
		}
		writeNewFile(0);
	}

	/**
	 * Set the file extension.
	 * 
	 * @param extension the file extension
	 */
	private void setFileExtension(String extension) {

		// Remove leading dots
		while (extension.startsWith(".")) {
			extension = extension.substring(1);
		}
		this.extension = extension;
		
		// Set the pattern matching to use, based on the file extension.
		String filePattern = String.format(FILE_PATTERN_TEMPLATE, extension);
		filter = new NumTrackerFilenameFilter(filePattern);
	}

	/**
	 * Delete the file for the specified number.
	 * 
	 * @param number the number of the file to delete
	 * 
	 * @return true if deletion worked; false otherwise
	 */
	private boolean deleteNumberedFile(long number) {
		if (dir.exists()) {
			File theFile = makeFile(number);
			if (theFile.exists()) {
				if (!theFile.delete()) {
					logger.error("Could not delete file " + theFile);
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Finds the largest number for which a file exists.
	 * 
	 * @return the largest number found, or zero if the directory doesn't exist, or no files are found
	 */
	private long findBiggestNumber() {
		if (!dir.exists()) {
			return 0;
		}

		long largestNumber = 0;

		for (String filename : dir.list(filter)) {
			long number = Long.parseLong(filename.substring(0, filename.indexOf('.')));
			largestNumber = Math.max(largestNumber, number);
		}

		return largestNumber;
	}

	/**
	 * Write a new file (in {number}.extension format) in the directory. This method will create the directory
	 * if it does not exist.
	 * 
	 * @param number the number of the file to create
	 * 
	 * @return true if the file was successfully created; false otherwise
	 */
	private boolean writeNewFile(long number) {
		if (dir.exists()) {
			File theFile = makeFile(number);
			try {
				theFile.createNewFile();
				logger.debug("Created temporary run number file " + theFile);
				return true;
			} catch (IOException e) {
				logger.error("Could not create temporary run number file " + theFile,e);
			}
		}
		return false;
	}
	
	protected File makeFile(long number) {
		return new File(dir, Long.toString(number) + "." + extension);
	}

	/**
	 * {@link FilenameFilter} that accepts files matching the current file pattern.
	 */
	private class NumTrackerFilenameFilter implements FilenameFilter {
		
		private String pattern;
		
		public NumTrackerFilenameFilter(String pattern) {
			this.pattern = pattern;
		}
		
		@Override
		public boolean accept(File dir, String name) {
			return name.matches(pattern);
		}
	}
}
