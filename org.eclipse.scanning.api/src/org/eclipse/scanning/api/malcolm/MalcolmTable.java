/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.malcolm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class representing a table in Malcolm Format (List of columns with data).
 * Instances of this class are NOT thread safe.
 */
public class MalcolmTable implements Iterable<Map<String, Object>> {

	private LinkedHashMap<String, List<?>> tableData;
	private LinkedHashMap<String, Class<?>> tableDataTypes;
	private List<String> headings;
	private int numRows;

	public MalcolmTable() {
		// no-arg constructor required for epics deserialization
	}

	/**
	 * Create a new Malcolm table with the given columns data and data types. The
	 * size of each column must be equal, and the keys of both maps must be the same, these
	 * are the column headings.
	 * <p>
	 * The passed maps are required to be {@link LinkedHashMap}s as the order of the columns must be maintained
	 * when the table is serialized
	 * @param tableAsMap a list of values for each column, of equal lengths
	 * @param dataTypes a map of the data types for each column, this map must have the
	 *    same keys as the tableAsMap
	 */
	public MalcolmTable(LinkedHashMap<String, List<?>> tableAsMap, LinkedHashMap<String, Class<?>> dataTypes) {
		if (tableAsMap == null || dataTypes == null) throw new NullPointerException();
		if (tableAsMap.size() != dataTypes.size()) {
			throw new IllegalArgumentException("The given arguments are not of the same size");
		}

		tableData = tableAsMap;

		numRows = tableData.isEmpty() ? 0 : tableData.values().iterator().next().size();
		tableDataTypes = dataTypes;
		headings = new LinkedList<>(tableAsMap.keySet());

		for (String heading : headings) {
			if (!dataTypes.containsKey(heading)) {
				throw new IllegalArgumentException("The types map has no entry for column " + heading);
			}

			if (tableAsMap.get(heading).size() != numRows) {
				throw new IllegalArgumentException(String.format("The column '%s' has size %d, should be %d",
						heading, tableAsMap.get(heading).size(), numRows));
			}
		}
	}

	/**
	 * Creates a new empty table with columns of the given types.
	 * @param dataTypes map from column name to data type for that column
	 */
	public MalcolmTable(LinkedHashMap<String, Class<?>> dataTypes) {
		tableDataTypes = dataTypes;
		headings = new LinkedList<>(tableDataTypes.keySet());

		tableData = new LinkedHashMap<>(headings.size());
		for (String heading : headings) {
			tableData.put(heading, new ArrayList<>());
		}
		numRows = 0;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getColumn(String columnName) {
		List<?> column = tableData.get(columnName);
		if (column != null) {
			return (List<T>) tableData.get(columnName);
		}
		throw new IllegalArgumentException("Unknown column: " + columnName);
	}

	public Class<?> getColumnClass(String columnName) {
		List<?> column = tableData.get(columnName);
		if (column != null) {
			return tableDataTypes.get(columnName);
		}
		throw new IllegalArgumentException("Unknown column: " + columnName);
	}

	public Map<String, Object> getRow(int rowIndex) {
		if (rowIndex >= numRows) {
			throw new IndexOutOfBoundsException("No such row " + rowIndex + ", number of rows = " + numRows);
		}

		Map<String, Object> row = new LinkedHashMap<>(headings.size());
		for (String heading : headings) {
			row.put(heading, getCellValue(heading, rowIndex));
		}

		return row;
	}

	public Object getCellValue(String columnName, int rowIndex) {
		return getColumn(columnName).get(rowIndex);
	}

	public List<String> getHeadings() {
		return headings;
	}

	public <T> void addRow(Map<String, T> newRow) {
		if (newRow.size() != headings.size()) {
			throw new IllegalArgumentException("The size of the map for the new row must match the number of columns in the table.");
		}
		for (String heading : headings) {
			if (!newRow.containsKey(heading)) {
				throw new IllegalArgumentException("This row map does not have an entry for the column with the heading " + heading);
			}
			@SuppressWarnings("unchecked")
			List<T> columnValues = (List<T>) tableData.get(heading);
			columnValues.add(newRow.get(heading));
		}
		numRows++;
	}

	/**
	 * Returns an iterator over the rows of the table, where each row is represented as a {@link Map}
	 * from a column name (a String) to a value.
	 * @return iterator over table rows
	 */
	@Override
	public Iterator<Map<String, Object>> iterator() {
		return new MalcolmTableRowIterator();
	}

	/**
	 * Returns a stream of the rows of the table, where each row is represented as a {@link Map}
	 * from a column name (a String) to a value.
	 * @return stream of table rows
	 */
	public Stream<Map<String, Object>> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	public class MalcolmTableRowIterator implements Iterator<Map<String, Object>> {

		private int rowNum = 0;

		@Override
		public boolean hasNext() {
			return rowNum < numRows;
		}

		@Override
		public Map<String, Object> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return getRow(rowNum++);
		}

	}

	public Map<String, List<?>> getTableData() {
		return tableData;
	}

	public void setTableData(LinkedHashMap<String, List<?>> tableData) {
		this.tableData = tableData;
		// also sets the number of rows.
		numRows = tableData.values().iterator().next().size();
		if (tableData.values().stream().anyMatch(column -> column.size() != numRows)) {
			throw new IllegalArgumentException("All columns must have the same size");
		}
	}

	public LinkedHashMap<String, Class<?>> getTableDataTypes() {
		return tableDataTypes;
	}

	public void setTableDataTypes(LinkedHashMap<String, Class<?>> tableDataTypes) {
		this.tableDataTypes = tableDataTypes;
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public void setHeadings(List<String> headings) {
		this.headings = headings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headings == null) ? 0 : headings.hashCode());
		result = prime * result + numRows;
		result = prime * result + ((tableData == null) ? 0 : tableData.hashCode());
		result = prime * result + ((tableDataTypes == null) ? 0 : tableDataTypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MalcolmTable other = (MalcolmTable) obj;
		if (headings == null) {
			if (other.headings != null)
				return false;
		} else if (!headings.equals(other.headings))
			return false;
		if (numRows != other.numRows)
			return false;
		if (tableData == null) {
			if (other.tableData != null)
				return false;
		} else if (!tableData.equals(other.tableData))
			return false;
		if (tableDataTypes == null) {
			if (other.tableDataTypes != null)
				return false;
		} else if (!tableDataTypes.equals(other.tableDataTypes))
			return false;
		return true;
	}
}
