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

import static java.util.Arrays.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.FindableConfigurableBase;
import gda.function.Lookup;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.QuantityFactory;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This class provides a generic lookup function for looking up the value for a scannable (scannable name as column
 * heading) that corresponds to a given key (such as row name). It reads a text file which contains columnar data in
 * the following format:
 * <dl>
 * <dt>Comments</dt>
 * <dd>Any line beginning with # is ignored</dd>
 *
 * <dt>Column Heading</dt>
 * <dd>The name of scannable objects, starting with a Marker string constant "ScannableNames" (the default)
 * or Spring configured property "columnHead" used as key</dd>
 *
 * <dt>Unit String</dt>
 * <dd>The physical units used by each of the scannables, starting with a Marker string constant
 * "ScannableUnits" (the default) or Spring configure property "columnUnit"</dd>
 *
 * <dt>Lookup Values</dt>
 * <dd>Multiple rows and columns of data with NO Marker, instead, the first column of data (i.e. the
 * left-most scannable's values) are used as key by default for the lookup on specific row.</dd>
 * </dl>
 * it uses a MultiValuedMap object to store the lookup table.
 */
@ServiceInterface(Lookup.class)
public class LookupTable extends FindableConfigurableBase implements Lookup {
	/**
	 * the logger instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(LookupTable.class);
	/**
	 * total number of rows in the lookup table
	 */
	private int numberOfRows;
	/**
	 * the values available in the value map used for the scannable objects
	 */
	private ArrayList<String> keys;
	/**
	 * the filename of the lookup table - ASCII file
	 */
	private String filename;
	private String dirLUT = null;
	/**
	 * Column head holds the name of the column header - previously this was hard coded to ScannableNames - so
	 * defaulting to 'ScannableNames' for backward compatibility
	 */
	private String columnHead = "ScannableNames";
	/**
	 * Column head holds the name of the column units - previously this was hard coded to ScannableUnits - so
	 * defaulting to 'ScannableUnits' for backward compatibility
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
	private ObservableComponent observableComponent = new ObservableComponent();

	@Override
	public void configure() {
		logger.info("{} configuring lookup table. This will take a while, please wait ......", getName());
		if (!isConfigured()) {
			String filePath;
			if (dirLUT == null) {
				String gdaConfig = LocalProperties.get(LocalProperties.GDA_CONFIG);
				String lookupTableFolder = LocalProperties.get("gda.function.lookupTable.dir", gdaConfig
						+ File.separator + "lookupTables");
				filePath = lookupTableFolder + File.separator + filename;
			} else {
				filePath = dirLUT + File.separator + filename;
			}
			readTheFile(filePath);
			setConfigured(true);
		}
	}

	private void checkConfigured() throws DeviceException{
		if (!isConfigured()) {
			throw new DeviceException("LookupTable '" + getName() +"' is not configured");
		}
	}
	@Override
	public void reload() {
		setConfigured(false);
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

		String nextLine;
		String[] names = null;
		String[] unitStrings = null;
		int[] decimalPlaces = null;
		ArrayList<String> lines = new ArrayList<>();
		try (FileReader fr = new FileReader(filePath); BufferedReader br = new BufferedReader(fr)) {
			// Find out lookup table folder

			logger.info("loading the look up table file {} ", filePath);

			while (((nextLine = br.readLine()) != null) && !nextLine.isEmpty()) {
				if (nextLine.startsWith(getColumnHead())) {
					// NB This regex means one or more comma space or tab
					// This split will include the word "ScannableNames" as one of the names
					names = nextLine.split("[, \t][, \t]*");
				} else if (nextLine.startsWith(getColumnUnit())) {
					// NB This regex means one or more comma space or tab
					// This split will include the word "ScannableUnits" as one of the unitStrings
					unitStrings = nextLine.split("[, \t][, \t]*");
				} else if (!nextLine.startsWith("#")) {
					lines.add(nextLine);
				}
			}
		} catch (FileNotFoundException fnfe) {
			throw new IllegalArgumentException("LookupTable could not open file " + filePath, fnfe);
		} catch (IOException ioe) {
			throw new RuntimeException("LookupTable IOException ", ioe);
		}

		numberOfRows = lines.size();
		logger.debug("the file containes {} lines", numberOfRows);
		int nColumns = new StringTokenizer(lines.get(0), ", \t").countTokens();
		logger.debug("each line contains {} numbers", nColumns);
		keys = new ArrayList<>();
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
	public Unit<? extends Quantity<?>> lookupUnit(String scannableName) throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			return ((List<Unit<? extends Quantity<?>>>) lookupMap.getCollection(getColumnUnit()))
					.get(indexOfScannable(getScannableNames(), scannableName));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized String lookupUnitString(String scannableName) throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			Unit<? extends Quantity<?>> unit = ((List<Unit<? extends Quantity<?>>>) lookupMap
					.getCollection(getColumnUnit())).get(indexOfScannable(getScannableNames(), scannableName));
			return unit.toString();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized int lookupDecimalPlaces(String scannableName) throws DeviceException {
		checkConfigured();
		synchronized (lookupMap) {
			return ((List<Integer>) lookupMap.getCollection("DecimalPlaces")).get(indexOfScannable(
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
		return stream(string.split("[, \t]+"))
				.mapToDouble(Double::parseDouble)
				.toArray();
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

	@Override
	public int getNumberOfRows() {
		return numberOfRows;
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	/**
	 * @return array of the values in the first column, considering that the first three rows are ScannableNames,
	 *         ScannableUnits, and DecimalPlaces
	 * @throws DeviceException
	 */
	@Override
	public double[] getLookupKeys() throws DeviceException {
		checkConfigured();
		final int firstRows = 3;// ScannableNames, ScannableUnits, DecimalPlaces
		return keys.stream()
				.skip(firstRows)
				.mapToDouble(Double::parseDouble).toArray();
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
}
