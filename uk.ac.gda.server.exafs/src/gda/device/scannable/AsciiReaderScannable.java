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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class AsciiReaderScannable extends SimpleScannable {

	private String[] columnHeadings;
	private int[] columnToExtraNameMap;
	private String previousLine = "";
	private String filename = "/dls/b18/data/2010/cm1901-3/Experiment_1/ascii/Ptfoil3_1_532.dat";
	private BufferedReader reader;
	private static ArrayList<String> dataLines;
	private static int delay = 200;
	
	public AsciiReaderScannable() {
		this.inputNames = new String[] { "Energy" };
		this.extraNames = new String[] { "Integration Time" };
		this.outputFormat = new String[] { "%8.2f", "%8.2f" };
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gda.device.Scannable#atScanEnd()
	 */
	@Override
	public void atScanEnd() throws DeviceException {
		dataLines = new ArrayList<String>();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see gda.device.Scannable#atScanStart()
	 */
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


	private void mapExtraNamesToColumns() throws DeviceException {
		columnToExtraNameMap = new int[2];
		String extraName = "Energy";
		columnToExtraNameMap[0] = ArrayUtils.indexOf(columnHeadings, extraName);
		if (columnToExtraNameMap[0] == -1)
			throw new DeviceException("Column " + extraName + " not found in file " + filename);
		extraName = "Integration Time";
		columnToExtraNameMap[1] = ArrayUtils.indexOf(columnHeadings, extraName);
		if (columnToExtraNameMap[1] == -1)
			throw new DeviceException("Column " + extraName + " not found in file " + filename);
	}

	private void extractColumnHeadings(String previousLine) {
		previousLine = previousLine.trim().substring(1);
		columnHeadings = previousLine.split("\t");
		for (int i = 0; i < columnHeadings.length; i++)
			columnHeadings[i] = columnHeadings[i].trim();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		try {
			String line = dataLines.remove(0);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
			
			if (line == null)
				line = previousLine;

			String[] parts = line.split("\t");

			double[] output = new double[2];
			for (int i = 0; i < 2; i++)
				output[i] = Double.parseDouble(parts[columnToExtraNameMap[i]]);

			previousLine = line;
			return new double[] {output[0], output[1]};
		} catch (IndexOutOfBoundsException e) {
			throw new DeviceException(e.getMessage(), e);
		}
		
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
