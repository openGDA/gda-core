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

import gda.configuration.properties.LocalProperties;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.util.QuantityFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Reads a file which contains columnar data and allows access to the columns. The file format is: # Any line beginning
 * with # is ignored xvalue1 yonevalue1 ytwovalue1 ythreevalue1... xvalue2 yonevalue2 ytwovalue2 ythreevalue2... xvalue3
 * yonevalue3 ytwovalue3 ythreevalue3... ... ...
 */
public class ColumnDataFile implements Findable, Configurable {
	public static final String GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR = "gda.function.columnDataFile.lookupDir";

	private static final Logger logger = LoggerFactory.getLogger(ColumnDataFile.class);

	String name;

	int numberOfXValues;

	private String filename;

	private double[][] columnData;

	private ArrayList<Unit<? extends Quantity>> columnUnits;

	private int[] columnDecimalPlaces;

	private boolean filenameIsFull = false;

	/**
	 * FIXME remove empty constructor
	 */
	public ColumnDataFile() {
	}

	/**
	 * Returns the (data) filename
	 * 
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the name of this instance
	 * 
	 * @return the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
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
		BufferedReader br;
		String nextLine;
		String[] unitStrings = null;
		ArrayList<String> lines = new ArrayList<String>();
		String filePath = filename;
		if (!filenameIsFull) {
			String lookupTableFolder = LocalProperties.get(GDA_FUNCTION_COLUMN_DATA_FILE_LOOKUP_DIR);

			filePath = lookupTableFolder + File.separator + filename;
		}
		try {
			// Find out lookup table folder

			logger.debug("ColumnDataFile loading file: " + filePath);

			br = new BufferedReader(new FileReader(filePath));
			while (((nextLine = br.readLine()) != null) && (nextLine.length() > 0)) {
				// Message.out("ColumnDataFile.readThFile read line " +
				// nextLine,
				// Message.Level.THREE);
				if (nextLine.startsWith("Units")) {
					logger.debug("Units are :" + nextLine.substring(6));
					// NB This regex means one or more comma space or tab
					// This split will include the word "Units" as one of
					// the
					// unitStrings
					unitStrings = nextLine.split("[, \t][, \t]*");
				} else if (!nextLine.startsWith("#"))
					lines.add(nextLine);
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Could not load " + StringUtils.quote(filePath), ioe);
		}

		numberOfXValues = lines.size();
		logger.debug("the file contained " + numberOfXValues + " lines");
		int nColumns = new StringTokenizer(lines.get(0), ", \t").countTokens();
		logger.debug("each line should contain " + nColumns + " numbers");

		if (unitStrings != null) {
			columnUnits = new ArrayList<Unit<? extends Quantity>>();
			// NB unitStrings contains as its first element "Units" hence
			// the i+1
			for (int i = 0; i < nColumns; i++){
				Unit<? extends Quantity> unit = QuantityFactory.createUnitFromString(unitStrings[i + 1]);
				if(unit == null){
					throw new RuntimeException("unit is null for string " + unitStrings[i + 1]);
				}
				columnUnits.add(unit);
			}
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

		/*
		 * for (i = 0; i < nColumns; i++) { Message.out("column " + i + " is " + doubleArrayToString(columnData[i])); }
		 */
	}

	/**
	 * @param string
	 * @return an array of token positions
	 */
	private int[] calculateDecimalPlaces(String string) {
		int[] values;
		String nextToken;
		int index;

		StringTokenizer strtok = new StringTokenizer(string, ", \t");

		values = new int[strtok.countTokens()];
		int i = 0;
		while (strtok.hasMoreTokens()) {
			nextToken = strtok.nextToken();
			index = nextToken.indexOf('.');
			if (index == -1)
				values[i] = 0;
			else
				values[i] = nextToken.length() - index - 1;
			i++;
		}

		return values;
	}

	/**
	 * Converts an array of doubles into a string. Exists mainly for debugging.
	 * 
	 * @param values
	 *            the array of doubles
	 * @return a string containing the doubles separated by commas.
	 */
	public String doubleArrayToString(double[] values) {
		String string = "";
		for (int j = 0; j < values.length; j++)
			string = string + values[j] + " ";

		return string;
	}

	/**
	 * Returns a particular column of data
	 * 
	 * @param which
	 * @return a double array of the values in the column
	 */
	public double[] getColumn(int which) {
		return columnData[which];
	}

	/**
	 * Returns the units a particular column claims to be in
	 * 
	 * @param which
	 * @return the Unit
	 */
	public Unit<? extends Quantity> getColumnUnits(int which) {
		return columnUnits.get(which);
	}

	/**
	 * Returns the number of decimal places originally specified for a column
	 * 
	 * @param which
	 * @return the numberOfDecimalPlaces
	 */
	public int getColumnDecimalPlaces(int which) {
		return columnDecimalPlaces[which];
	}

	/**
	 * Takes a string of comma, space or tab separated values and parses it into an array of doubles
	 * 
	 * @param string
	 *            the input string
	 * @return an array of doubles found in the string
	 */
	private double[] stringToDoubleArray(String string) {
		double[] values;

		StringTokenizer strtok = new StringTokenizer(string, ", \t");

		values = new double[strtok.countTokens()];
		int i = 0;
		while (strtok.hasMoreTokens()) {
			values[i] = Double.valueOf(strtok.nextToken()).doubleValue();
			i++;
		}

		return values;
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
	}
}
