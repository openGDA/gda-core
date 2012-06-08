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

package gda.function.lookup;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.factory.Localizable;
import gda.function.Lookup;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.QuantityFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.collections.map.MultiValueMap;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a generic lookup function for looking up the value for a scannable (scannable name as column
 * heading)that corresponding to a given key (such as row name). It reads a text file which contains columnar data in
 * the following format:
 * <ul>
 * <li>Comments: Any line beginning with # is ignored</li>
 * <li>Column Heading: the name of scannable objects, both Java Scannable and Jython PseudoDevice objects are supported,
 * starting with a Marker string constant "ScannableNames" (the default) or Spring configured property "columnHead" used
 * as key</li>
 * <li>Unit String: the physical units used by each of the scannables, starting with a Marker string constant
 * "ScannableUnits" (the default) or Spring configure property "columnUnit"</li>
 * <li>Lookup Values: Multiple rows and columns of data with NO Marker, instead, the first column of data (i.e. the
 * left-most scannable's values) are used as key by default for the lookup on specific row.</li>
 * </ul>
 * it uses a MultiValuedMap object to store the lookup table.
 */
public class LookupTable implements Findable, Configurable, Lookup, Localizable {
	/**
	 * the logger instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(LookupTable.class);
	/**
	 * the name of this object
	 */
	private String name;
	/**
	 * total number of rows in the lookup table
	 */
	private int numberOfRows;
	/**
	 * the number of decimal places used for the scannable objects
	 */
	private ArrayList<Object> keys;
	/**
	 * the filename of the lookup table - ASCII file
	 */
	private String filename;
	private String dirLUT = null;
	/**
	 * Column head holds the name of the column header - previously this was hard coded to ScannableNames - so
	 * defaulting to to 'ScannableNames' for backward compatability
	 */
	private String columnHead = "ScannableNames";
	/**
	 * Column head holds the name of the column units - previously this was hard coded to ScannableUnits - so defaulting
	 * to to 'ScannableUnits' for backward compatability
	 */
	private String columnUnit = "ScannableUnits";

	/**
	 * @return Returns the columnHead.
	 */
	public String getColumnHead() {
		return columnHead;
	}

	/**
	 * @param columnHead
	 *            The columnHead to set.
	 */
	public void setColumnHead(String columnHead) {
		this.columnHead = columnHead;
	}

	/**
	 * @return Returns the columnUnit.
	 */
	public String getColumnUnit() {
		return columnUnit;
	}

	/**
	 * @param columnUnit
	 *            The columnUnit to set.
	 */
	public void setColumnUnit(String columnUnit) {
		this.columnUnit = columnUnit;
	}

	public String getDirLUT() {
		return dirLUT;
	}

	public void setDirLUT(String dirLUT) {
		this.dirLUT = dirLUT;
	}

	private MultiValueMap lookupMap = new MultiValueMap();
	private boolean configured = false;
	private ObservableComponent observableComponent = new ObservableComponent();
	private boolean local = false;

	/**
	 * default constructor
	 */
	public LookupTable() {
	}

	@Override
	public void configure() {
		logger.debug("LookupTable configure called");
		if (!configured) {
			String filePath;
			if (dirLUT == null) {
				String gda_config = LocalProperties.get(LocalProperties.GDA_CONFIG);
				String lookupTableFolder = LocalProperties.get("gda.function.lookupTable.dir", gda_config
						+ File.separator + "lookupTables");
				filePath = lookupTableFolder + File.separator + filename;
			} else {
				filePath = dirLUT + File.separator + filename;
			}
			readTheFile(filePath);
			configured = true;
		}
	}

	private void checkConfigured() throws DeviceException{
		if (!configured)
			throw new DeviceException("LookupTable '" + getName() +"' is not configured");
	}
	@Override
	public void reload() {
		configured = false;
		configure();
	}

	/**
	 * Reads the lookup table file and put them into a multi-Valued Map for looking up value for the specified energy
	 * and scannable name.
	 * 
	 * @param filePath
	 */
	private void readTheFile(String filePath) {
		lookupMap.clear();

		BufferedReader br;
		String nextLine;
		String[] names = null;
		String[] unitStrings = null;
		int[] decimalPlaces = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			// Find out lookup table folder

			logger.debug("LookupTable loading file: " + filePath);

			br = new BufferedReader(new FileReader(filePath));
			while (((nextLine = br.readLine()) != null) && (nextLine.length() > 0)) {
				if (nextLine.startsWith(getColumnHead())) {
					logger.debug("Names are :" + nextLine.substring(6));
					// NB This regex means one or more comma space or tab
					// This split will include the word "ScannableUnits" as one of the names
					names = nextLine.split("[, \t][, \t]*");
				} else if (nextLine.startsWith(getColumnUnit())) {
					logger.debug("Units are :" + nextLine.substring(6));
					// NB This regex means one or more comma space or tab
					// This split will include the word "Units" as one of the unitStrings
					unitStrings = nextLine.split("[, \t][, \t]*");
				} else if (!nextLine.startsWith("#"))
					lines.add(nextLine);
			}
		} catch (FileNotFoundException fnfe) {
			throw new IllegalArgumentException("LookupTable could not open file " + filePath, fnfe);
		} catch (IOException ioe) {
			throw new RuntimeException("LookupTable IOException ", ioe);
		}

		numberOfRows = lines.size();
		logger.debug("the file contained " + numberOfRows + " lines");
		int nColumns = new StringTokenizer(lines.get(0), ", \t").countTokens();
		logger.debug("each line should contain " + nColumns + " numbers");
		keys = new ArrayList<Object>();
		if (names != null) {
			for (int i = 0; i < nColumns; i++) {
				lookupMap.put(names[0], names[i + 1]);
			}
			keys.add(names[0]);
		}

		if (unitStrings != null) {
			for (int i = 0; i < nColumns; i++) {
				lookupMap.put(unitStrings[0], QuantityFactory.createUnitFromString(unitStrings[i + 1]));
			}
			keys.add(unitStrings[0]);
		}

		decimalPlaces = calculateDecimalPlaces(lines.get(0));
		if (decimalPlaces != null) {
			for (int i = 0; i < nColumns; i++) {
				lookupMap.put("DecimalPlaces", decimalPlaces[i]);
			}
			keys.add("DecimalPlaces");
		}

		double[] thisLine;
		int i;
		int j;
		for (i = 0; i < numberOfRows; i++) {
			nextLine = lines.get(i);
			thisLine = stringToDoubleArray(nextLine);
			for (j = 0; j < thisLine.length; j++)
				lookupMap.put(String.format("%.3f", thisLine[0]), thisLine[j]);
			keys.add(String.format("%.3f", thisLine[0]));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<String> getScannableNames() throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			return (ArrayList<String>) lookupMap.getCollection(getColumnHead());
		}
	}

	/**
	 * the energy value needs to be Double parseable, otherwise the method
	 * @throws DeviceException 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public double lookupValue(Object energy, String scannableName) throws DeviceException {
		checkConfigured();
		double result;
		synchronized (lookupMap) {
			int index = indexOfScannable(getScannableNames(), scannableName);
			double energyDouble = Double.parseDouble(energy.toString());
			String value = String.format("%.3f", energyDouble);
			logger.debug("energy is : {}", value);

			ArrayList<Double> list = (ArrayList<Double>) lookupMap.getCollection(value);
			if (list == null) {
				logger.error("{}: list is null", getName());
				throw new IllegalStateException("Cannot find value for " + scannableName + " at energy " + energy);
			}
			result = list.get(index);
			return result;
		}

	}

	/**
	 * @param scannableName
	 * @return Unit
	 * @throws DeviceException 
	 */
	@SuppressWarnings("unchecked")
	public Unit<? extends Quantity> lookupUnit(String scannableName) throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			return ((ArrayList<Unit<? extends Quantity>>) lookupMap.getCollection(getColumnUnit()))
					.get(indexOfScannable(getScannableNames(), scannableName));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized String lookupUnitString(String scannableName) throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			Unit<? extends Quantity> unit = ((ArrayList<Unit<? extends Quantity>>) lookupMap
					.getCollection(getColumnUnit())).get(indexOfScannable(getScannableNames(), scannableName));
			return unit.toString();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized int lookupDecimalPlaces(String scannableName) throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			return ((ArrayList<Integer>) lookupMap.getCollection("DecimalPlaces")).get(indexOfScannable(
					getScannableNames(), scannableName));
		}
	}

	/**
	 * returns or find the index of a scannable name in an Arraylist of Scannable names
	 * 
	 * @param scannableNames
	 * @param scannableName
	 * @return index
	 */
	private int indexOfScannable(ArrayList<String> scannableNames, String scannableName) {
		return scannableNames.indexOf(scannableName);
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

	@Override
	public int getNumberOfRows() {
		return numberOfRows;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	private MultiValueMap getLookupMap() {
		return this.lookupMap;
	}

	/**
	 * @return array of the values in the first column, considering that the first three rows are ScannableNames,
	 *         ScannableUnits, and DecimalPlaces
	 * @throws DeviceException 
	 */
	@Override
	public double[] getLookupKeys() throws DeviceException {
		checkConfigured();
		ArrayList<Object> actualKeys = getKeys();
		final int firstRows = 3;// ScannableNames, ScannableUnits, DecimalPlaces
		int totalKeys = actualKeys.size();
		if (totalKeys > firstRows) {
			double[] keys = new double[totalKeys - firstRows];
			for (int count = firstRows; count < actualKeys.size(); count++) {
				// While reading the file there is a mandate that the first column must be a double value - so
				// additional checks aren't required here
				keys[count - firstRows] = Double.parseDouble((String) actualKeys.get(count));
			}
			return keys;
		}

		return null;
	}

	// below is testing code used in this class internally only
	/**
	 * configures the object, used by this class's main test method only
	 * 
	 * @param filename
	 */
	private void configure(String filename) {
		logger.debug("LookupTable configure called");
		if (!configured) {

			readTheFile(filename);
			configured = true;
		}
	}

	/**
	 * returns the keys of map in its original order in the map
	 * 
	 * @return array list of keys
	 */
	private ArrayList<Object> getKeys() {
		return keys;
	}

	/**
	 * Tests this class
	 * 
	 * @param args
	 * @throws DeviceException 
	 */
	public static void main(String[] args) throws DeviceException {
		System.out.println("testing LookupTable file reading ....");
		LookupTable lut = new LookupTable();
		// lut.configure("C:\\workspace\\config\\i11-EpicsSimulation\\lookupTables\\Automated_energy_setup.txt");
		lut.configure("/scratch/i12workspace/i12-config/lookupTables/tomo/module_lookup_table.txt");
		MultiValueMap lum = lut.getLookupMap();
		for (Object key : lut.getKeys()) {
			Collection<?> list = lum.getCollection(key);
			for (Object o : list) {
				System.out.print(o + "\t");
			}
			System.out.println();
		}

		lut.getLookupKeys();

	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 * 
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	@Override
	public boolean isLocal() {
		return this.local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}
}
