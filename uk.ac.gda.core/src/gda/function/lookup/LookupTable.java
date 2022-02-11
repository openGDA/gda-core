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

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.function.Lookup;
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
public class LookupTable extends AbstractColumnFile implements Lookup {
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

	/** @deprecated use {@link #getDirectory} instead */
	@Deprecated(forRemoval = true, since = "GDA 9.18")
	public String getDirLUT() {
		logger.warn("getDirLUT is deprecated, use getDirectory instead. This will be removed in GDA 9.26");
		return getDirectory();
	}

	/** @deprecated use {@link #setDirectory} instead */
	@Deprecated(forRemoval = true, since = "GDA 9.18")
	public void setDirLUT(String dirLUT) {
		logger.warn("setDirLUT is deprecated, use setDirectory instead. This will be removed in GDA 9.26");
		setDirectory(dirLUT);
	}

	private MultiValueMap lookupMap = new MultiValueMap();

	@Override
	public void configure() throws FactoryException {
		logger.info("{} configuring lookup table. This will take a while, please wait ......", getName());
		if (!isConfigured()) {
			try {
				readTheFile();
			} catch (IOException e) {
				throw new FactoryException("Could not read file", e);
			}
			setConfigured(true);
		}
	}

	private void checkConfigured() throws DeviceException{
		if (!isConfigured()) {
			throw new DeviceException("LookupTable '" + getName() +"' is not configured");
		}
	}

	@Override
	public void reload() throws FactoryException {
		setConfigured(false);
		configure();
	}

	/**
	 * Reads the lookup table file and put them into a multi-Valued Map for looking up value for the specified energy
	 * and scannable name.
	 *
	 * @param filePath
	 * @throws IOException
	 * @throws FactoryException
	 */
	private void readTheFile() throws IOException, FactoryException {
		lookupMap.clear();
		List<String> names = new ArrayList<>();
		List<String> units = new ArrayList<>();
		List<String[]> data = new ArrayList<>();

		try (Stream<String[]> lines = readLines()) {
			lines.forEach(line -> {
				if (line[0].startsWith(columnHead)) {
					names.addAll(asList(copyOfRange(line, 1, line.length)));
				} else if (line[0].startsWith(columnUnit)) {
					units.addAll(asList(copyOfRange(line, 1, line.length)));
				} else {
					data.add(line);
				}
			});
		}
		if (data.isEmpty()) {
			throw new FactoryException("File " + getPath() + " does not contain any data");
		}

		numberOfRows = data.size();
		logger.debug("the file containes {} lines", numberOfRows);
		int nColumns = data.get(0).length;
		logger.debug("each line contains {} numbers", nColumns);
		keys = new ArrayList<>();

		lookupMap.putAll(columnHead, names);
		keys.add(columnHead);
		if (units.isEmpty()) {
			// If no units are given, columns should be dimensionless
			units.addAll(range(0, nColumns).mapToObj(i -> "").collect(toList()));
		}
		lookupMap.putAll(columnUnit, units.stream()
				.map(unit -> "\"\"".equals(unit) ? "" : unit) // A literal "" should be replaced by an empty string
				.map(QuantityFactory::createUnitFromString)
				.collect(toList()));
		keys.add(columnUnit);

		stream(calculateDecimalPlaces(data.get(0))).forEach(dp -> lookupMap.put("DecimalPlaces", dp));
		keys.add("DecimalPlaces");

		DoubleFunction<String> format = d -> String.format("%.3f", d);
		for (int i = 0; i < numberOfRows; i++) {
			String[] stringLine = data.get(i);
			List<Double> values = stream(stringLine)
					.map(Double::parseDouble)
					.collect(toList());
			lookupMap.putAll(format.apply(values.get(0)), values);
			keys.add(format.apply(values.get(0)));
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
			if (index < 0) {
				throw new IllegalArgumentException("Scannable '" + scannableName + "' not found in table");
			}
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

	@Override
	public int getNumberOfRows() {
		return numberOfRows;
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
}
