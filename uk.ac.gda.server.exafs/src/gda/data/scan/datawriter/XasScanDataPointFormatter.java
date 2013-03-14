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

import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPointFormatter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XasScanDataPointFormatter implements ScanDataPointFormatter {

	private static final int DEFAULT_COLUMN_WIDTH = 16;

	private int columnWidth;

	/**
	 * Something of a bodge, we figure out that any parameters not in this this are signal. That might not be the case
	 * but it should be.
	 */
	private static final List<String> XAS_SCAN_VARIABLES;
	static {
		XAS_SCAN_VARIABLES = new ArrayList<String>(7);
		XAS_SCAN_VARIABLES.add("bragg1");
		XAS_SCAN_VARIABLES.add("XESEnergy");
		XAS_SCAN_VARIABLES.add("Energy");
		XAS_SCAN_VARIABLES.add("energy");
		XAS_SCAN_VARIABLES.add("XES");
		XAS_SCAN_VARIABLES.add("I0");
		XAS_SCAN_VARIABLES.add("It");
		XAS_SCAN_VARIABLES.add("Iref");
		XAS_SCAN_VARIABLES.add("I1");
		XAS_SCAN_VARIABLES.add("Iother");
		XAS_SCAN_VARIABLES.add("lnI0It");
		XAS_SCAN_VARIABLES.add("lnItIref");
		XAS_SCAN_VARIABLES.add("FF");
		XAS_SCAN_VARIABLES.add("FFI0");
		XAS_SCAN_VARIABLES.add("FFI1");
		XAS_SCAN_VARIABLES.add("Time");
	}

	public XasScanDataPointFormatter() {
		setColumnWidth(DEFAULT_COLUMN_WIDTH);
	}

	/**
	 * NOTE: The map is assumed to be ordered properly using a LinkedHashMap.
	 */
	@Override
	public String getHeader(IScanDataPoint currentPoint, Map<String, String> data) {

		if (!(data instanceof LinkedHashMap<?, ?>))
			throw new RuntimeException("Cannot deal with hashtables which do not have a well defined iteration order.");

		// Header
		final StringBuilder headerBuf = new StringBuilder();
		if (data.get("bragg1") != null) {
			addColumnEntry(headerBuf, "bragg1");
		}
		if (data.get("Energy") != null) {
			addColumnEntry(headerBuf, "Energy");
		} else if (data.get("energy") != null) {
			addColumnEntry(headerBuf, "energy");
		}
		if (data.get("XESEnergy") != null) {
			addColumnEntry(headerBuf, "Ef");	
		} 
		
		if (data.get("XES") != null) {
			addColumnEntry(headerBuf, "XESBragg");
		}

		if (data.get("I0") != null) {
			addColumnEntry(headerBuf, "I0");
		}
		if (data.get("It") != null) {
			addColumnEntry(headerBuf, "It");
		}
		if (data.get("Iref") != null) {
			addColumnEntry(headerBuf, "Iref");
		}
		if (data.get("Iother") != null) {
			addColumnEntry(headerBuf, "Iother");
		}
		if (data.get("I1") != null) {
			addColumnEntry(headerBuf, "I1");	// xes ion chmaber
		}
		if (data.get("lnI0It") != null) {
			addColumnEntry(headerBuf, "lnI0It");
		}
		if (data.get("lnItIref") != null) {
			addColumnEntry(headerBuf, "lnItIref");
		}

		if (data.get("FF") != null) {
			addColumnEntry(headerBuf, "FF");
		}
		if (data.get("FFI0") != null) {
			addColumnEntry(headerBuf, "FF/I0");
		}
		if (data.get("FFI1")!=null) {
			addColumnEntry(headerBuf, "FF/I1"); //  vortex FF over xes ion chmaber
		}

		if (data.get("XES") == null && data.get("Time") != null) {
			addColumnEntry(headerBuf, "Time");
		}

		final Map<String, String> signalData = getSignalData(data);
		for (String name : signalData.keySet()) {
			if (!(data.get("XES") == null && name.compareTo("Time") == 0)) {
				addColumnEntry(headerBuf, name);
			}
		}
		
		if (data.get("XES") != null && data.get("Time") != null) {
			addColumnEntry(headerBuf, "Time");
		}


		return headerBuf.toString();
	}

	@Override
	public String getData(IScanDataPoint currentPoint, Map<String, String> data) {

		if (!(data instanceof LinkedHashMap<?, ?>))
			throw new RuntimeException("Cannot deal with hashtables which do not have a well defined iteration order.");

		// Data
		final StringBuilder dataBuf = new StringBuilder();
		if (data.get("bragg1") != null) {
			addColumnEntry(dataBuf, data.get("bragg1"));
		}
		
		if (data.get("Energy") != null) {
			addColumnEntry(dataBuf, data.get("Energy"));
		} else if (data.get("energy") != null) {
			addColumnEntry(dataBuf, data.get("energy"));
		} 
		if (data.get("XESEnergy") != null) {
			addColumnEntry(dataBuf, data.get("XESEnergy"));	
		} 
		
		if (data.get("XES") != null) {
			addColumnEntry(dataBuf, data.get("XES"));
		}

		
		if (data.get("I0") != null) {
			addColumnEntry(dataBuf, data.get("I0"));
		}
		if (data.get("It") != null) {
			addColumnEntry(dataBuf, data.get("It"));
		}
		if (data.get("Iref") != null) {
			addColumnEntry(dataBuf, data.get("Iref"));
		}
		if (data.get("I1") != null) {
			addColumnEntry(dataBuf, data.get("I1"));	
		}
		if (data.get("Iother") != null) {
			addColumnEntry(dataBuf, data.get("Iother"));
		}
		if (data.get("lnI0It") != null) {
			addColumnEntry(dataBuf, data.get("lnI0It"));
		}
		if (data.get("lnItIref") != null) {
			addColumnEntry(dataBuf, data.get("lnItIref"));
		}

		if (data.get("FF") != null) {
			addColumnEntry(dataBuf, data.get("FF"));
		}
		if (data.get("FFI0") != null) {
			addColumnEntry(dataBuf, data.get("FFI0"));
		}
		if (data.get("FFI1")!=null) {
			addColumnEntry(dataBuf, data.get("FFI1"));
		}

		if (data.get("XES") == null && data.get("Time") != null) {
			addColumnEntry(dataBuf, data.get("Time"));
		}

		final Map<String, String> signalData = getSignalData(data);
		for (String name : signalData.keySet()) {
			if (!(data.get("XES") == null && name.compareTo("Time") == 0)) {
				addColumnEntry(dataBuf, signalData.get(name));
			}
		}
		
		if (data.get("XES") != null && data.get("Time") != null) {
			addColumnEntry(dataBuf, data.get("Time"));
		}


		return dataBuf.toString();
	}

	/**
	 * Gets a column entry containing the value and terminated with a \t
	 */
	private void addColumnEntry(final StringBuilder buf, final String valString) {

		if (valString == null || valString.isEmpty()) {
			return;
		}

		if (valString.length() > getColumnWidth()) {
			buf.append(valString);
			buf.append("\t");
			return;
		}
		for (int i = 0; i < getColumnWidth(); i++) {
			if (i < valString.length()) {
				buf.append(valString.charAt(i));
			} else {
				buf.append(" ");
			}
		}
		buf.append("\t");
	}

	/**
	 * NOTE assumes data = LinkedHashMap
	 * 
	 * @param data
	 */
	private Map<String, String> getSignalData(Map<String, String> data) {
		// NOTE: Important that is LinkedHashMap.
		final Map<String, String> signalData = new LinkedHashMap<String, String>();
		final List<String> used = new ArrayList<String>(3);
		for (String name : data.keySet()) {
			if (XAS_SCAN_VARIABLES.contains(name.trim()))
				continue;
			if (used.contains(name.trim()))
				continue;
			signalData.put(name, data.get(name));
			used.add(name.trim());
		}
		return signalData;
	}

	@Override
	public boolean isValid(IScanDataPoint dataPoint) {
		return dataPoint.isScannable("xas_scannable") || dataPoint.isScannable("energy")
				|| dataPoint.isScannable("Energy") || dataPoint.isScannable("XESEnergy");
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
}
