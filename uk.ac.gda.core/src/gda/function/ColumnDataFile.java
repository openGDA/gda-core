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

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FactoryException;
import gda.function.lookup.AbstractColumnFile;

/**
 * Reads a file which contains columnar data and allows access to the columns. The file format is: # Any line beginning
 * with # is ignored xvalue1 yonevalue1 ytwovalue1 ythreevalue1... xvalue2 yonevalue2 ytwovalue2 ythreevalue2... xvalue3
 * yonevalue3 ytwovalue3 ythreevalue3... ... ...
 */
public class ColumnDataFile extends AbstractColumnFile {

	private static final Logger logger = LoggerFactory.getLogger(ColumnDataFile.class);

	/** Format index and maximum value into a more useful out of bounds error message */
	private static final BiFunction<Integer, Integer, String> INDEX_ERROR_MESSAGE = (index, range) ->
			String.format("Index %d is out of range. Expected value in range (0, %d)", index, range);

	int numberOfXValues;

	private double[][] columnData;

	private List<String> columnUnits;

	private int[] columnDecimalPlaces;

	/**
	 * Sets the filename
	 *
	 * @param filename the filename
	 * @param filenameIsFull
	 */
	public void setFilename(String filename, boolean filenameIsFull) {
		setFilename(filename);
		setDirectory(filenameIsFull ? "/" : getDefaultLookup());
	}

	private void readTheFile() throws IOException {
		List<String> units = new ArrayList<>();
		List<String[]> data = new ArrayList<>();
		try (Stream<String[]> lines = readLines()) {
			lines.filter(Objects::nonNull).forEach(line -> {
				if (line[0].equals("Units")) {
					units.addAll(asList(copyOfRange(line, 1, line.length)));
				} else {
					data.add(line);
				}
			});
		}
		if (data.isEmpty()) {
			throw new IllegalArgumentException("File " + getPath() + " does not contain any data");
		}
		numberOfXValues = data.size();
		logger.debug("The file contained {} lines", numberOfXValues);
		int nColumns = data.get(0).length;
		logger.debug("each line should contain {} numbers", nColumns);

		columnUnits = units.stream()
				.map(unit -> "\"\"".equals(unit) ? "" : unit) // A literal "" should be replaced by an empty string
				.collect(toList());
		if (columnUnits == null || columnUnits.isEmpty()) {
			// If no units are given, columns should be dimensionless
			columnUnits = range(0, nColumns).mapToObj(i -> "").collect(toList());
		}

		// Create array in column (i.e. the opposite to the file) order
		columnData = new double[nColumns][numberOfXValues];

		columnDecimalPlaces = calculateDecimalPlaces(data.get(0));

		String[] stringLine;
		double[] doubleLine;
		int i;
		int j;
		for (i = 0; i < numberOfXValues; i++) {
			stringLine = data.get(i);
			doubleLine = stream(stringLine).mapToDouble(Double::parseDouble).toArray();
			// TODO check doubleLine.length matches nColumns
			for (j = 0; j < doubleLine.length; j++)
				columnData[j][i] = doubleLine[j];
		}
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
		try {
			readTheFile();
		} catch (IOException e) {
			throw new FactoryException("Error reading file", e);
		}
		setConfigured(true);
	}
}
