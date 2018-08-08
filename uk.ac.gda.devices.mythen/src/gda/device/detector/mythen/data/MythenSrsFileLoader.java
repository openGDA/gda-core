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

package gda.device.detector.mythen.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.springframework.util.StringUtils;

/**
 * Loader for Mythen SRS files, where the filename of the Mythen data file is
 * recorded as a value.
 */
public class MythenSrsFileLoader {
	
	/**
	 * Loads the specified SRS file, and returns the Mythen data files from it.
	 * 
	 * @param filename the SRS file
	 * 
	 * @return filenames of Mythen data files from the SRS file
	 * 
	 * @throws IOException
	 */
	public String[] load(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		return read(br);
	}
	
	protected String[] read(BufferedReader br) throws IOException {
		
		// skip through header
		String line = br.readLine();
		while (line != null && !line.contains("&END")) {
			line = br.readLine();
		}
		
		if (line == null) {
			throw new IOException("Reached end of file without seeing &END line");
		}
		
		// read data header; find column containing Mythen filenames
		String headerLine = br.readLine();
		if (!StringUtils.hasText(headerLine)) {
			throw new IOException("Didn't find column list");
		}
		List<String> headers = Arrays.asList(headerLine.split("\t"));
		int mythenColumn = headers.indexOf("mythen");
		if (mythenColumn == -1) {
			throw new IOException("Couldn't find 'mythen' column");
		}
		
		// read rest of data file; extract Mythen filename from each line
		List<String> filenames = new Vector<String>();
		while ((line = br.readLine()) != null) {
			String[] values = line.split("\t");
			String filenameValue = values[mythenColumn];
			filenames.add(filenameValue);
		}
		
		br.close();
		
		return filenames.toArray(new String[filenames.size()]);
	}
	
}
