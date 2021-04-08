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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import uk.ac.gda.util.list.PrimitiveArrayEncoder;

public class FluoCompositeDataStore {

	private static final Logger logger = LoggerFactory.getLogger(FluoCompositeDataStore.class);

	private String fileName;

	private String columnFileName;

	private List<String> extraScannables = Collections.emptyList();

	public FluoCompositeDataStore(String fileName) {
		this.fileName = fileName;
		columnFileName = fileName+".dat";
	}

	public double[][] readDataFromFile() {
		double[][] emptyReturn = new double[][] {/* empty */};
		File dataFile = new File(fileName);
		if (!dataFile.exists()) {
			return emptyReturn;
		}
		return readDataFileFirstLine(dataFile, emptyReturn);
	}

	private double[][] readDataFileFirstLine(File dataFile, double[][] defaultValue) {
		try {
			return FileUtils.readLines(dataFile, Charset.defaultCharset()).stream()
				.findFirst()
				.map(this::getDataFromString)
				.orElseGet(() -> defaultValue);
		} catch (IOException e) {
			logger.error("IOException whilst reading stored detector editor data from file {}", fileName, e);
			return defaultValue;
		}
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
	 * @throws IOException
	 */
	public void writeDataToColumnFile(double[][] data) throws IOException {
		int numElements = data.length;
		int numChannels = data[0].length;

		StringBuilder str = new StringBuilder();

		// Header
		str.append("# Filename    : "+columnFileName+"\n"+
				   "# Description : MCA data from file "+fileName+" saved in columns\n"+
				   "# Number of elements : "+numElements+"\n"+
				   "# Number of channels : "+numChannels+"\n");


		// Positions of the scannables
		getExtraScannables().forEach(name ->	getScannablePosition(name).ifPresent(pos -> str.append("# "+pos+"\n")));

		str.append("#\n\n");

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
		writeStringToFile(columnFileName, str.toString());

	}

	private Optional<String> getScannablePosition(String scannableName) {
		return Finder.findOptionalOfType(scannableName, Scannable.class)
				.map(this::getFormattedCurrentPosition)
				.orElseGet(() -> {
					logger.warn("Could not get position of {} - scannable could not be found on server", scannableName);
					return Optional.empty();
				});
	}

	private Optional<String> getFormattedCurrentPosition(Scannable scannable) {
		try {
			return Optional.of(ScannableUtils.getFormattedCurrentPosition(scannable));
		} catch (DeviceException e) {
			logger.warn("Problem getting position of scannable {}", scannable, e);
			return Optional.empty();
		}
	}

	public String getColumnFileName() {
		return columnFileName;
	}

	public void writeDataToFile(double[][] newData) throws IOException {
		writeStringToFile(fileName, getDataString(newData));
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

	/**
	 * Write string to file using Apache {@link FileUtils#writeStringToFile(File, String, Charset)}.
	 * Any IOExceptions produced are rethrown.
	 * @param fileName
	 * @param data
	 * @throws IOException
	 */
	private void writeStringToFile(String fileName, String data) throws IOException {
		try {
			FileUtils.writeStringToFile(new File(fileName), data, Charset.defaultCharset());
		} catch (IOException e) {
			throw new IOException("IOException whilst writing detector editor MCA data to file "+fileName, e);
		}
	}

	public void setExtraScannables(List<String> extraScannables) {
		this.extraScannables = new ArrayList<>(extraScannables);
	}

	private List<String> getExtraScannables() {
		if (extraScannables == null) {
			extraScannables = new ArrayList<>();
		}
		return extraScannables;
	}
}
