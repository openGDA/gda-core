/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.list.PrimitiveArrayEncoder;

public class FluoCompositeDataStore {

	private static final Logger logger = LoggerFactory.getLogger(FluoCompositeDataStore.class);

	private String fileName;

	private String columnFileName;

	public FluoCompositeDataStore(String fileName) {
		this.fileName = fileName;
	}

	public double[][] readDataFromFile() {
		double[][] result = new double[][] {/* empty */};
		try {
			File dataFile = new File(fileName);
			if (!dataFile.exists()) {
				return result;
			}

			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			String strLine = in.readLine();
			in.close();

			result = getDataFromString(strLine);

		} catch (IOException e) {
			logger.error("IOException whilst reading stored detector editor data from file " + fileName);
		}
		return result;
	}

	private double[][] getDataFromString(String compressedData) {
		final String[] split = compressedData.split(";");
		final double[][] data = new double[split.length][];
		for (int i = 0; i < split.length; i++) {
			data[i] = PrimitiveArrayEncoder.getDoubleArray(split[i]);
		}
		return data;
	}

	/**
	 * Write MCA data to text file, counts in columns (i.e. column 1 has counts for each channel in element 1 etc).
	 * Output filename is mca filename with .dat appended.
	 * @param data
	 */
	public void writeDataToColumnFile(double[][] data) {
		int numElements = data.length;
		int numChannels = data[0].length;

		columnFileName = fileName+".dat";
		String header = "# Filename    : "+columnFileName+"\n"+
					    "# Description : MCA data from file "+fileName+" saved in columns\n"+
					    "# Number of elements : "+numElements+"\n"+
					    "# Number of channels : "+numChannels+"\n#\n\n";

		StringBuilder str = new StringBuilder();
		str.append(header);

		// Column labels
		str.append("# Channel");
		for (int i = 0; i < numElements; i++) {
			str.append(",\tElement_" + i);
		}
		str.append("\n");

		// Add the data in columns (one detector element per column)
		DecimalFormat formatter = new DecimalFormat("0.#####E0");
		for (int i = 0; i < numChannels; i++) {
			str.append(i);
			for (int j = 0; j < numElements; j++) {
				str.append("\t\t"); // 2 tabs to align with column names (test alignment with non-zero data)
				str.append(formatter.format(data[j][i]));
			}
			str.append("\n");
		}

		//Write the file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(columnFileName));
			out.write(str.toString());
			out.close();
		} catch (IOException e) {
			logger.error("IOException whilst writing stored detector editor data to file " + columnFileName);
		}
	}

	public String getColumnFileName() {
		return columnFileName;
	}

	public void writeDataToFile(double[][] newData) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(getDataString(newData));
			out.write("\n");
			out.close();
		} catch (IOException e) {
			logger.error("IOException whilst writing stored detector editor data from file " + fileName);
		}
	}

	private String getDataString(double[][] data) {
		if (data == null) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			stringBuilder.append(PrimitiveArrayEncoder.getString(data[i]));
			if (i != data.length - 1) {
				stringBuilder.append(";");
			}
		}
		return stringBuilder.toString();
	}
}
