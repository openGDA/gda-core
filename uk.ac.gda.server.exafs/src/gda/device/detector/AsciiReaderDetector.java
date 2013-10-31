/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector;

import gda.device.DeviceException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

/**
 * Reads an ascii data file written by the AsciiDataWriter class and returns the data there as its own. I hope this is
 * only used for simulations and GUI testing ;)
 */
public class AsciiReaderDetector extends DetectorBase {
	private BufferedReader reader;
	private String filename = "/dls/b18/data/2010/cm1901-3/Experiment_1/ascii/Ptfoil3_1_532.dat";
	private ArrayList<String> dataLines;
	private int delay = 200;
	private String[] columnHeadings;
	private int[] columnToExtraNameMap;
	private String previousLine = "";

	public AsciiReaderDetector() {
		inputNames = new String[] {};
		extraNames = new String[] { "I0", "It", "ln(I0/It)" };
		outputFormat = new String[] { "%8.2f", "%8.2f", "%8.2f", "%8.2f" };
	}

	//TODO I just found this method to be a copy of AsciiReaderScannable.atScanStart() or vice versa. We should know better than to fork code.
	@Override
	public void atScanStart() throws DeviceException {
		try {
			reader = new BufferedReader(new FileReader(filename));
			dataLines = new ArrayList<String>();
			String tmpLine = reader.readLine(); 
			while (tmpLine != null) {
				dataLines.add(tmpLine);
				tmpLine = reader.readLine();
			}
			reader.close();
			String previousLine = "";
			while (dataLines.get(0).startsWith("#"))
				previousLine = dataLines.remove(0);
			// the last line will be the column headings
			extractColumnHeadings(previousLine);
			mapExtraNamesToColumns();
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e);
		}
	}
	
	@Override
	public void atScanEnd() throws DeviceException {
		dataLines = new ArrayList<String>();
	}

	private void mapExtraNamesToColumns() throws DeviceException {
		columnToExtraNameMap = new int[extraNames.length];
		for (int i = 0; i < extraNames.length; i++) {
			String extraName = extraNames[i];
			columnToExtraNameMap[i] = ArrayUtils.indexOf(columnHeadings, extraName);
			if (columnToExtraNameMap[i] == -1)
				throw new DeviceException("Column " + extraName + " not found in file " + filename);
		}
	}

	private void extractColumnHeadings(String previousLine) {
		previousLine = previousLine.trim().substring(1);
		columnHeadings = previousLine.split("\t");
		for (int i = 0; i < columnHeadings.length; i++)
			columnHeadings[i] = columnHeadings[i].trim();
	}

	@Override
	public void collectData() throws DeviceException {

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "reads old files to find data";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "SRS File Reading detector";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "SrsReaderDetector";
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		try {
			String line = dataLines.remove(0);
			Thread.sleep(delay);
			if (line == null)
				line = previousLine;
			String[] parts = line.split("\t");
			Double[] output = new Double[extraNames.length];
			for (int i = 0; i < extraNames.length; i++)
				output[i] = Double.parseDouble(parts[columnToExtraNameMap[i]]);
			previousLine = line;
			return output;
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e);
		}
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

}
