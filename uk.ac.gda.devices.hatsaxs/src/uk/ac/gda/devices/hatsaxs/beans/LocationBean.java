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

import uk.ac.gda.util.beans.xml.XMLRichBean;

public class LocationBean implements XMLRichBean {
	
	private static final long serialVersionUID = 4468559432687294813L;

	private static final int OFFSET = 65; //ASCII A char
	
	transient private PlateConfig config = null;
	short plate = 1;
	char row = 'A';
	short column = 1;

	public LocationBean() {
	}

	public LocationBean(PlateConfig config) {
		this.config = config;
	}
	
	public short getPlate() {
		return plate;
	}

	public void setPlate(short plate) {
		if (!validPlate(plate)) {
			throw new IllegalArgumentException("Invalid plate: " + plate); 
		}
		this.plate = plate;
	}

	public char getRow() {
		return row;
	}

	public int getRowAsInt() {
		return Character.getNumericValue(row)-9;
	}
	
	public void setRow(char row) {
		if (!validRow(row)) {
			throw new IllegalArgumentException(row + " is not a valid row");
		}
		this.row = Character.toUpperCase(row);
	}

	public short getColumn() {
		return column;
	}

	public void setColumn(short column) {
		if (!validColumn(column)) {
			throw new IllegalArgumentException(column + " is not a valid column");
		}
		this.column = column;
	}
	
	public PlateConfig getConfig() {
		return config;
	}
	
	public void setConfig(PlateConfig config) {
		this.config = config;
	}
	
	public static String getPlateText(short p) {
		switch (p) {
		case 0:
			return "--";
		case 1:
			return "I";
		case 2:
			return "II";
		case 3:
			return "III";
		default:
			return "Error: " + p;
		}
	}
	
	public void setPlate(String value) {
		short newPlate = plate;
		try {
			newPlate = Short.valueOf(value);
		} catch (NumberFormatException e) {
			String in = value;
			if (in.equalsIgnoreCase("I")) {
				newPlate = 1;
			} else if (in.equalsIgnoreCase("II")) {
				newPlate = 2;
			} else if (in.equalsIgnoreCase("III")) {
				newPlate = 3;
			} else {
				throw new IllegalArgumentException("Invalid plate: " + value, e);
			}
		}
		setPlate(newPlate);
	}

	@Override
	public String toString() {
		String platestr = getPlateText(plate);
		return String.format("%3s %c %2d", platestr, row, column);
	}

	public void clear() {	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocationBean) {
			LocationBean foreignLocation = (LocationBean) obj;
			return foreignLocation.plate == plate && foreignLocation.column == column && foreignLocation.row == row;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return plate + 5 * row + 31 * column;
	}
	
	public boolean isValid() {
		return config != null && validPlate(plate) && validRow(row) && validColumn(column);
	}
	
	private boolean validPlate(int p) {
		if (config == null) { return true; } //we have no idea
		int plates = config.getAvailablePlates().length;
		return (p <= plates && p > 0);
	}
	private boolean validColumn(int c) {
		if (config == null) { return true; } //we have no idea
		try {
			Plate p = config.getPlate(plate);
			return (c <= p.getColumnCount() && c > 0);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new IllegalStateException("Location set to invalid plate", ex);
		}
	}
	private boolean validRow(char r) {
		if (config == null) { return true; } //we have no idea
		try {
			char R = Character.toUpperCase(r);
			Plate p = config.getPlate(plate);
			return (!(R < 'A' || R >= p.getRowCount() + OFFSET));
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new IllegalStateException("Location set to invalid plate", ex);
		}
	}
}
