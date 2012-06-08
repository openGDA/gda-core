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

package gda.device.detector.mar345;

import gda.device.Detector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * MAR345 Status Reader class.
 */
public class Mar345StatusReader {
	/**
	 * 
	 */
	public final String statusFileName;

	private int lastReadStatus = 0;
	private boolean lastStatusIsValid = false;
	private long lastValidModTime = 0;

	/**
	 * Constructor.
	 * 
	 * @param fileName
	 *            The full path of the mar status file
	 */
	public Mar345StatusReader(String fileName) {
		statusFileName = fileName;
	}

	/**
	 * Obtains detector status from the mar.message file. If the text contains line 'mar345: Task SCAN...' (or '...Task
	 * CHANGE' or '...Task CHANGE') and is not followed by the word 'ENDED', then the status is taken to be busy, else
	 * idle.
	 * 
	 * @return Returns the detector status (as per detector interface)
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public int getDetectorStatus() throws FileNotFoundException, IOException, Exception {

		BufferedReader statusFile = null;
		try {

			File f = new File(statusFileName);
			long thisModTime = f.lastModified();
			if (lastStatusIsValid && lastValidModTime == thisModTime)
				return lastReadStatus;

			// Open status file
			statusFile = new BufferedReader(new FileReader(statusFileName));

			// Read file line by line: if 'mar345: Task SCAN...' found anywhere, then detector is busy, else idle
			String line = null;
			int thisStatus = Detector.IDLE;
			while (statusFile.ready()) {
				line = statusFile.readLine();
				if (line.matches(".*345:[ \\t]+Task[ \\t]+(SCAN|CHANGE|ERASE).*")) {
					
					thisStatus = Detector.BUSY;
					if (line.contains("ENDED")) {
						thisStatus = Detector.IDLE;
						break;
					}
				}
			}
			lastReadStatus = thisStatus;
			lastStatusIsValid = true;
			lastValidModTime = thisModTime;
			return thisStatus;
		} catch (Exception e) {
			lastStatusIsValid = false;
			throw e;
		} finally {
			if (statusFile != null)
				statusFile.close();
		}
	}
}
