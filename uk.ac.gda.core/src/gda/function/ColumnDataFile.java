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

package gda.function;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;

/**
 * Reads a file which contains columnar data and allows access to the columns. The file format is: # Any line beginning
 * with # is ignored xvalue1 yonevalue1 ytwovalue1 ythreevalue1... xvalue2 yonevalue2 ytwovalue2 ythreevalue2... xvalue3
 * yonevalue3 ytwovalue3 ythreevalue3... ... ...
 */
public class ColumnDataFile extends FindableConfigurableBase {
	public static final String GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR = "gda.function.columnDataFile.lookupDir";

	private static final Logger logger = LoggerFactory.getLogger(ColumnDataFile.class);

	/** Format index and maximum value into a more useful out of bounds error message */
	private static final BiFunction<Integer, Integer, String> INDEX_ERROR_MESSAGE = (index, range) ->
			String.format("Index %d is out of range. Expected value in range (0, %d)", index, range);

	int numberOfXValues;

	private String filename;

	private double[][] columnData;

	private List<String> columnUnits;

	private int[] columnDecimalPlaces;

	private boolean filenameIsFull = false;

	/**
	 * Returns the (data) filename
	 *
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename
	 *
	 * @param filename
	 *            the filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Sets the filename
	 *
	 * @param filename
	 *            the filename
	 * @param filenameIsFull
	 */
	public void setFilename(String filename, boolean filenameIsFull) {
		this.filename = filename;
		this.filenameIsFull = filenameIsFull;
	}

	/**
	 * Reads the file and rearranges the data into the required column based format.
	 */
	private void readTheFile() {
		String nextLine;
		String[] unitStrings = new String[] {};
		ArrayList<String> lines = new ArrayList<>();
		String filePath = filename;
		if (!filenameIsFull) {
			// Find out lookup table folder
			String lookupTableFolder = LocalProperties.get(GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR);
			filePath = lookupTableFolder + File.separator + filename;
		}

		try (FileReader fr = new FileReader(filePath); BufferedReader br = new BufferedReader(fr)) {
			logger.debug("ColumnDataFile loading file: {}", filePath);
			while (((nextLine = br.readLine()) != null) && (nextLine.length() > 0)) {
				if (nextLine.startsWith("Units")) {
					logger.debug("Units are : {}", nextLine.substring(6));
					// NB This regex means one or more comma space or tab
					// This split will include the word "Units" as one of
					// the
					// unitStrings
					unitStrings = nextLine.split("[, \t][, \t]*");
				} else if (!nextLine.startsWith("#")) {
					lines.add(nextLine);
				}
			}

		} catch (IOException ioe) {
			throw new RuntimeException("Could not load '" + filePath + "'", ioe);
		}

		if (lines.isEmpty()) {
			throw new IllegalArgumentException("File " + filePath + " does not contain any data");
		}
		numberOfXValues = lines.size();
		logger.debug("the file contained {} lines", numberOfXValues);
		int nColumns = new StringTokenizer(lines.get(0), ", \t").countTokens();
		logger.debug("each line should contain {} numbers", nColumns);

		columnUnits = Arrays.stream(unitStrings)
				.skip(1) // Skip 'Units' keyword
				.map(units -> "\"\"".equals(units) ? "" : units) // A literal "" should be replaced by an empty string
				.collect(toList());

		if (columnUnits == null || columnUnits.isEmpty()) {
			// If no units are given, columns should be dimensionless
			columnUnits = range(0, nColumns).mapToObj(i -> "").collect(toList());
		}
		// Create array in column (i.e. the opposite to the file) order
		columnData = new double[nColumns][numberOfXValues];

		columnDecimalPlaces = calculateDecimalPlaces(lines.get(0));

		double[] thisLine;
		int i;
		int j;
		for (i = 0; i < numberOfXValues; i++) {
			nextLine = lines.get(i);
			thisLine = stringToDoubleArray(nextLine);
			for (j = 0; j < thisLine.length; j++)
				columnData[j][i] = thisLine[j];
		}
	}

	/**
	 * @param string
	 * @return an array of token positions
	 */
	private int[] calculateDecimalPlaces(String string) {
		return stream(string.split("[, \t]+"))
				.mapToInt(s -> (s.length() - s.indexOf('.') - 1) % s.length()) // characters to right of '.' (or 0 if none present)
				.toArray();
	}

	/**
	 * Returns a particular column of data
	 *
	 * @param which
	 * @return a double array of the values in the column
	 */
	public double[] getColumn(int which) {
		try {
			return columnData[which];
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(INDEX_ERROR_MESSAGE.apply(which, columnData.length -1), e);
		}
	}

	/**
	 * Returns the units a particular column claims to be in
	 *
	 * @param which
	 * @return the Unit
	 */
	public String getColumnUnits(int which) {
		try {
			return columnUnits.get(which);
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(INDEX_ERROR_MESSAGE.apply(which, columnUnits.size()-1), e);
		}
	}

	/**
	 * Returns the number of decimal places originally specified for a column
	 *
	 * @param which
	 * @return the numberOfDecimalPlaces
	 */
	public int getColumnDecimalPlaces(int which) {
		try {
			return columnDecimalPlaces[which];
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException(INDEX_ERROR_MESSAGE.apply(which, columnDecimalPlaces.length -1), e);
		}
	}

	/**
	 * Takes a string of comma, space or tab separated values and parses it into an array of doubles
	 *
	 * @param string
	 *            the input string
	 * @return an array of doubles found in the string
	 */
	private double[] stringToDoubleArray(String string) {
		return stream(string.split("[, \t]+"))
				.mapToDouble(Double::parseDouble)
				.toArray();
	}

	/**
	 * Returns the number of x values. Since this is columnar data this will be the number of ROWS in the file
	 *
	 * @return the number of x values
	 */
	public int getNumberOfXValues() {
		return numberOfXValues;
	}

	@Override
	public void configure() throws FactoryException {
		logger.debug("ColumnDataFile configure called");
		readTheFile();
		setConfigured(true);
	}
}
