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

package gda.data.scan.datawriter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gda.scan.ScanDataPointFormatter;

public class XasScanDataPointFormatter implements ScanDataPointFormatter {
	private static final int DEFAULT_COLUMN_WIDTH = 16;
	private int columnWidth;
	private static final String TIME = "Time";

	/** Data name - column name map controlling the order in which data columns are added, and the name of the header
	 * (key = data name (name of scannable), value = header name)
	 */
	private Map<String, String> extraScanVariables = Collections.emptyMap();

	/** If set to true, the include {@link #XAS_SCAN_VARIABLES} as well as any {@link #extraScanVariables}
	 * when  generating header and data rows from input data
	 */
	private boolean includeDefaultVariables = true;

	/**
	 * Default data name - column name map used for XAS scans.
	 * (key = data name, value = header name).
	 */
	private static final Map<String, String> XAS_SCAN_VARIABLES;
	static {
		XAS_SCAN_VARIABLES = new LinkedHashMap<>();
		XAS_SCAN_VARIABLES.put("bragg1", "Energy");
		XAS_SCAN_VARIABLES.put("bragg1WithOffset", "Energy");
		XAS_SCAN_VARIABLES.put("XESEnergy", "Ef");
		XAS_SCAN_VARIABLES.put("Energy", "Energy");
		XAS_SCAN_VARIABLES.put("energy", "energy");
		XAS_SCAN_VARIABLES.put("XES", "XESBragg");
		XAS_SCAN_VARIABLES.put("XESBragg", "XESBragg");
		XAS_SCAN_VARIABLES.put("I0", "I0");
		XAS_SCAN_VARIABLES.put("It", "It");
		XAS_SCAN_VARIABLES.put("Iref", "Iref");
		XAS_SCAN_VARIABLES.put("I1", "I1");
		XAS_SCAN_VARIABLES.put("Iother", "Iother");
		XAS_SCAN_VARIABLES.put("lnI0It", "lnI0It");
		XAS_SCAN_VARIABLES.put("lnItIref", "lnItIref");
		XAS_SCAN_VARIABLES.put("FF", "FF");
		XAS_SCAN_VARIABLES.put("FFI0", "FF/I0");
		XAS_SCAN_VARIABLES.put("FFI1", "FF/I1");
	}


	public XasScanDataPointFormatter() {
		setColumnWidth(DEFAULT_COLUMN_WIDTH);
	}

	/**
	 * NOTE: The map is assumed to be ordered properly using a LinkedHashMap.
	 */
	@Override
	public String getHeader(Map<String, String> data) {

		if (!(data instanceof LinkedHashMap<?, ?>))
			throw new RuntimeException("Cannot deal with hashtables which do not have a well defined iteration order.");

		// Header
		final StringBuilder headerBuf = new StringBuilder();
		for(var ent : getAllScanVariables().entrySet()) {
			if (data.containsKey(ent.getKey())) {
				addColumnEntry(headerBuf, ent.getValue());
			}
		}

		// Add headers for the 'signal' data
		getSignalData(data).keySet()
			.stream()
			.filter(name -> !name.equals(TIME))
			.forEach(name -> addColumnEntry(headerBuf, name));

		// Put the time column last
		if (hasTime(data)) {
			addColumnEntry(headerBuf, TIME);
		}

		return headerBuf.toString();
	}

	private boolean hasTime(Map<String, String> data) {
		return data.containsKey(TIME);
	}

	@Override
	public String getData(Map<String, String> data) {
		if (!(data instanceof LinkedHashMap<?, ?>))
			throw new RuntimeException("Cannot deal with hashtables which do not have a well defined iteration order.");

		// Data
		final StringBuilder dataBuf = new StringBuilder();

		for(var varName : getAllScanVariables().keySet()) {
			if (data.containsKey(varName)) {
				addColumnEntry(dataBuf, data.get(varName));
			}
		}

		// Add the 'signal' data
		getSignalData(data).entrySet()
			.stream()
			.filter(ent -> !ent.getKey().equals(TIME))
			.forEach(ent -> addColumnEntry(dataBuf, ent.getValue()));

		// Add time data last
		if (hasTime(data)) {
			addColumnEntry(dataBuf, data.get(TIME));
		}

		return dataBuf.toString();
	}

	/**
	 * Gets a column entry containing the value and terminated with a \t
	 */
	private void addColumnEntry(final StringBuilder buf, final String valString) {
		if (valString == null || valString.isEmpty())
			return;

		if (valString.length() > getColumnWidth()) {
			buf.append(valString);
			buf.append("\t");
			return;
		}
		buf.append(String.format("%-"+getColumnWidth()+"s\t", valString));
	}

	/**
	 * Generate set of data not already present in the 'default' or 'extra' scan variables.
	 *
	 * @param data
	 */
	private Map<String, String> getSignalData(Map<String, String> data) {
		// NOTE: Important that is LinkedHashMap.
		final Map<String, String> signalData = new LinkedHashMap<>();
		// Generate new map of data signals that are not 'xas scan variables'.
		Set<String> scanVariables = getAllScanVariables().keySet();
		data.entrySet()
			.stream()
			.filter(ent -> !scanVariables.contains(ent.getKey().trim()))
			.forEach(ent -> signalData.putIfAbsent(ent.getKey().trim(), ent.getValue()));
		return signalData;
	}

	/**
	 * @return Returns the columnWidth.
	 */
	public int getColumnWidth() {
		return columnWidth;
	}

	/**
	 * @param columnWidth
	 *            The columnWidth to set.
	 */
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	/**
	 * Generate a combined set of 'scan variables' - by appending the default XAS data name-column name map
	 * in {@link #XAS_SCAN_VARIABLES} to the custom map in {@link #extraScanVariables}.
	 * (The defaults are included only if {@link #includeDefaultVariables} = true)
	 * @return combined map
	 */
	private Map<String, String> getAllScanVariables() {
		Map<String, String> allVariables = new LinkedHashMap<>();
		if (includeDefaultVariables) {
			allVariables.putAll(XAS_SCAN_VARIABLES);
		}
		allVariables.putAll(extraScanVariables);
		return allVariables;
	}

	public boolean isIncludeDefaultVariables() {
		return includeDefaultVariables;
	}

	public void setIncludeDefaultVariables(boolean includeDefaultVariables) {
		this.includeDefaultVariables = includeDefaultVariables;
	}

	public Map<String, String> getExtraScanVariables() {
		return extraScanVariables;
	}

	/**
	 * Set a custom mapping between data name and header names.
	 * {This is appended to the defaukt XAS values, unless {@link #setIncludeDefaultVariables(boolean)} has been set to false)
	 *
	 * @param extraScanVariables Linked hash map with : key=scannable/data name, value = column name
	 */
	public void setExtraScanVariables(Map<String, String> extraScanVariables) {
		this.extraScanVariables = extraScanVariables;
	}
}
