/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hatsaxs.beans;

import java.io.Serializable;

public class Plate implements Serializable {
	private int rowCount;
	private int columnCount;
	private String name;

	public Plate() {
		rowCount = columnCount = 1;
		name = "unnamed";
	}
	public Plate(String name, int rows, int columns) {
		rowCount = rows;
		columnCount = columns;
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRowCount() {
		return rowCount;
	}
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	public int getColumnCount() {
		return columnCount;
	}
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
	@Override
	public String toString() {
		return name;//String.format("%d x %d plate", rowCount, columnCount);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columnCount;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + rowCount;
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
		Plate other = (Plate) obj;
		if (columnCount != other.columnCount)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rowCount != other.rowCount)
			return false;
		return true;
	}
	public Character[] getRows() {
		Character[] rows = new Character[rowCount];
		for (int i = 0; i < rowCount; i++) {
			rows[i] = (char)(i+65);
		}
		return rows;
	}
	
	public Integer[] getColumns() {
		Integer[] columns = new Integer[columnCount];
		for (int i = 0; i < columnCount; i++) {
			columns[i] = i+1;
		}
		return columns;
	}
}
