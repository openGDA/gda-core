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

public class XesScanDataPointFormatter implements ScanDataPointFormatter {

	private static final int DEFAULT_COLUMN_WIDTH = 16;
	
	private int columnWidth;
	
	/**
	 * Something of a bodge, we figure out that any parameters
	 * not in this this are signal. That might not be the case
	 * but it should be.
	 */
	private static final List<String> XAS_SCAN_VARIABLES;
	static {
		XAS_SCAN_VARIABLES = new ArrayList<String>(7);
		XAS_SCAN_VARIABLES.add("Energy");
		XAS_SCAN_VARIABLES.add("energy");
		XAS_SCAN_VARIABLES.add("XESEnergy");
		XAS_SCAN_VARIABLES.add("I1");
		XAS_SCAN_VARIABLES.add("FF");
		XAS_SCAN_VARIABLES.add("FFI1");
		XAS_SCAN_VARIABLES.add("Time");
	}
	
	public XesScanDataPointFormatter() {
		setColumnWidth(DEFAULT_COLUMN_WIDTH);
	}

	/**
	 * NOTE: The map is assumed to be ordered properly using a 
	 * LinkedHashMap.
	 */
	@Override
	public String getHeader(IScanDataPoint currentPoint, Map<String,String> data) {
		
		if (!(data instanceof LinkedHashMap<?, ?>)) throw new RuntimeException("Cannot deal with hashtables which do not have a well defined iteration order.");
		
		// Header
		final StringBuilder headerBuf = new StringBuilder();
		if (data.get("Energy")!=null) {
			addColumnEntry(headerBuf, "E0");		
		} else if (data.get("energy")!=null) {
			addColumnEntry(headerBuf, "E0");		
		}
		
		if (data.get("XESEnergy") != null) {
			addColumnEntry(headerBuf, "Ef");	
		}
		if (data.get("I1") != null) {
			addColumnEntry(headerBuf, "I1");	// xes ion chmaber
		}
		if (data.get("FF") != null) {
			addColumnEntry(headerBuf, "If");	// the FF from the vortex but labelled here as If
		}
		if (data.get("FFI1")!=null) {
			addColumnEntry(headerBuf, "If/I1"); //  vortex FF over xes ion chmaber
		}

		if (data.get("Time")!=null) {
			addColumnEntry(headerBuf, "Integration Time");
		}
		
		final Map<String,String> signalData = getSignalData(data);
		for (String name : signalData.keySet()) {
			addColumnEntry(headerBuf, name);
		}
		
		return headerBuf.toString();
	}

	@Override
	public String getData(IScanDataPoint currentPoint, Map<String,String> data) {

		if (!(data instanceof LinkedHashMap<?, ?>)) throw new RuntimeException("Cannot deal with hashtables which do not have a well defined iteration order.");

		// Data
		final StringBuilder dataBuf = new StringBuilder();
		if (data.get("Energy")!=null) {
			addColumnEntry(dataBuf, data.get("Energy"));		
		} else if (data.get("energy")!=null){
			addColumnEntry(dataBuf, data.get("energy"));	
		}
		
		if (data.get("XESEnergy") != null) {
			addColumnEntry(dataBuf, data.get("XESEnergy"));	
		}
		if (data.get("I1") != null) {
			addColumnEntry(dataBuf, data.get("I1"));	
		}
		if (data.get("FF") != null) {
			addColumnEntry(dataBuf, data.get("FF"));	
		}
		if (data.get("FFI1")!=null) {
			addColumnEntry(dataBuf, data.get("FFI1"));
		}
		
		if (data.get("Time")!=null) {
			addColumnEntry(dataBuf, data.get("Time"));		
		}
		
		final Map<String,String> signalData = getSignalData(data);
		for (String name : signalData.keySet()) addColumnEntry(dataBuf, signalData.get(name));

		return dataBuf.toString();
	}

	/**
	 * Gets a column entry containing the value and terminated with a \t
	 */
	private void addColumnEntry(final StringBuilder buf, final String valString) {
		
		if (valString == null || valString.isEmpty()){
			return;
		}
		
		if (valString.length()>getColumnWidth()) {
			buf.append(valString);
			buf.append("\t");
			return;
		}
		for (int i = 0; i < getColumnWidth(); i++) {
			if (i<valString.length()) {
				buf.append(valString.charAt(i));
			} else {
				buf.append(" ");
			}
		}
		buf.append("\t");
	}

	/**
	 * NOTE assumes data = LinkedHashMap
	 * @param data
	 */
	private Map<String,String> getSignalData(Map<String, String> data) {
		// NOTE: Important that is LinkedHashMap.
		final Map<String,String> signalData = new LinkedHashMap<String, String>();
		final List<String> used = new ArrayList<String>(3);
		for (String name : data.keySet()) {
			if (XAS_SCAN_VARIABLES.contains(name.trim())) continue;
			if (used.contains(name.trim())) continue;
			signalData.put(name, data.get(name));
			used.add(name.trim());
		}
		return signalData;
	}

	/**
	 * 
	 * @param dataPoint
	 * @return true if this formatter is to be used for the point.
	 */
	@Override
	public boolean isValid(IScanDataPoint dataPoint) {
		return true; // for testing
//	    return dataPoint.isScannable("xas_scannable") || dataPoint.isScannable("energy") || dataPoint.isScannable("Energy");
	}

	/**
	 * @return Returns the columnWidth.
	 */
	public int getColumnWidth() {
		return columnWidth;
	}

	/**
	 * @param columnWidth The columnWidth to set.
	 */
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}
}
