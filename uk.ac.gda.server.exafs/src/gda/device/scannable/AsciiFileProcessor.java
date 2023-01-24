/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.jython.InterfaceProvider;

/**
 * Read an ascii file produced by a scan, create a new one containing only data from columns specified.
 */
public class AsciiFileProcessor extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(AsciiFileProcessor.class);
	private String ext = "_test";

	/** Names of the columns containing data to be extracted from original file */
	private List<String> columnNames = Collections.emptyList();
	public static final String COMMENT_CHAR = "#";
	private static final String WHITESPACE = "\\s+";
	private String processedFileName = "";

	public AsciiFileProcessor() {
		// Set to empty lists to avoid exceptions when formatting the position
		setOutputFormat(new String[]{});
		setInputNames(new String[]{});
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	@Override
	public void asynchronousMoveTo(Object position) {
		return; // do nothing
	}

	@Override
	public void atScanEnd() {
		String dataWriterFileName = getLastFileName();
		String asciiInputFile = dataWriterFileName;
		if (dataWriterFileName.endsWith(".nxs")) {
			asciiInputFile = tryToGetAsciiName(dataWriterFileName);
		}
		try {
			processFile(asciiInputFile);
		} catch(IOException e) {
			logger.warn("Problem processing ascii file from scan {}", asciiInputFile, e);
		}
	}

	/**
	 *
	 * Make ascii file from nexus filename - assume it is in 'ascii' filder and has same name as nexus,
	 * except ending with .dat. <br> e.g. If Nexus = Experiment_1/nexus/1234.nxs then Ascii = Experiment_1/ascii/1234.dat
	 *
	 * @param nexusFilename
	 * @return path to ascii file
	 */
	private String tryToGetAsciiName(String nexusFilename) {
		Path asciiDir = Paths.get(nexusFilename).getParent().getParent().resolve("ascii");

		String name = FilenameUtils.getBaseName(nexusFilename)+".dat";
		return asciiDir.resolve(name).toString();
	}

	public String processFile(String file) throws IOException {
		String newFile = file.replace(".dat", ext+".dat");
		return processFile(file, newFile);
	}

	public String processFile(String file, String newFile) throws IOException {
		logger.info("Trying to extract ascii data from file {}", file);
		processedFileName = newFile;
		logger.info("Saving processed data into file : {}", newFile);
		try (BufferedReader inputFile = new BufferedReader( new FileReader(file) )) {
			List<String> headerLines = readHeader(inputFile);
			String columnNamesFromFile = headerLines.get(headerLines.size()-2);
			List<Map.Entry<String,Integer>> columnInfo = getColumnIndices(columnNamesFromFile);
			try(BufferedWriter bufWriter = new BufferedWriter( new FileWriter(newFile) )){
				bufWriter.write(COMMENT_CHAR+" ascii data extracted from "+file);
				bufWriter.newLine();
				for(int i=0; i<headerLines.size()-2; i++) {
					bufWriter.write(headerLines.get(i));
					bufWriter.newLine();
				}

				// Names of the output columns
				List<String> names = columnInfo.stream()
						.map(Map.Entry::getKey)
						.collect(Collectors.toList());

				String columnNameString = String.join("\t", names);
				bufWriter.write(COMMENT_CHAR + " " + columnNameString);
				bufWriter.newLine();

				// Indices of the source columns to be put in the processed file
				List<Integer> indices = columnInfo.stream()
						.map(Map.Entry::getValue)
						.collect(Collectors.toList());

				// 1st line of real data is last line of header string list
				String inputLine = headerLines.get(headerLines.size()-1);
				while (inputLine != null) {
					if (inputLine.startsWith(COMMENT_CHAR)) {
						bufWriter.write(inputLine);
					} else {
						bufWriter.write(getColumnValues(inputLine, indices));
					}
					bufWriter.newLine();
					inputLine = inputFile.readLine();
				}
			}
		}
		logger.info("Finished extracting data");
		InterfaceProvider.getTerminalPrinter().print("Processed Ascii data written to file : "+processedFileName);

		return newFile;
	}

	/**
	 * Read the header from ascii file - i.e. first lines in the file that start with a comment character
	 * @param fileReader
	 * @return list of header lines, including 1st line of data (last element)
	 * @throws IOException
	 */
	private List<String> readHeader(BufferedReader fileReader) throws IOException {
		List<String> headerLines = new ArrayList<>();
		String line = fileReader.readLine();
		while(line.startsWith(COMMENT_CHAR)) {
			headerLines.add(line);
			line = fileReader.readLine();
		}
		headerLines.add(line); // 1st line of data
		return headerLines;
	}

	/**
	 *
	 * @param allColumnNames - names of all column names in source ascii file
	 * @return map of indices of required column in the source file.
	 */
	public List<Map.Entry<String, Integer>> getColumnIndices(String allColumnNames) {
		// List of all column names by splitting string on whitespace (ignore leading #)
		List<String> allColumns = Arrays.asList(allColumnNames.replaceFirst(COMMENT_CHAR, "").trim().split(WHITESPACE));
		List<Map.Entry<String, Integer>> list = new ArrayList<>();
		columnNames.forEach(name -> {
			if (allColumns.contains(name)) {
				list.add(new SimpleEntry<>(name, allColumns.indexOf(name)));
			} else {
				logger.info("Not including {} in processed output - column not found in source file", name);
			}
		});
		return list;
	}

	/**
	 * Extract values from a string of data. Each value in the string is separated by 1 or more characters of whitespace
	 * @param line string of data
	 * @param indices indices of data values to extract from string
	 * @return string of required values
	 */
	public String getColumnValues(String line, Collection<Integer> indices) {
		String[] values = line.trim().split("\\s+");
		StringBuilder builder = new StringBuilder();
		for(Integer index : indices) {
			if (index != null && index >= 0 && values.length > index) {
				builder.append(values[index] + "\t");
			}
		}
		return builder.toString();
	}

	private String getLastFileName() {
		return InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getFilename();
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> requiredColumnNames) {
		this.columnNames = requiredColumnNames;
	}

	public String getNewFileExtension() {
		return ext;
	}

	public void setNewFileExtension(String ext) {
		this.ext = ext;
	}

	public String getProcessedFileName() {
		return processedFileName;
	}
}
