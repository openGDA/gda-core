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

package uk.ac.gda.devices.bssc.beans;

import uk.ac.gda.beans.IRichBean;

public class LocationBean implements IRichBean{
	short plate;
	char row;
	short column;
	
	public short getPlate() {
		return plate;
	}

	public void setPlate(short plate) {
		this.plate = plate;
	}

	public char getRow() {
		return row;
	}

	public int getRowAsInt() {
		return Character.getNumericValue(row)-9;
	}
	
	public void setRow(char row) {
		this.row = Character.toUpperCase(row);
	}

	public short getColumn() {
		return column;
	}

	public void setColumn(short column) {
		this.column = column;
	}

	@Override
	public String toString() {
		String platestr = "X";
		switch (plate) {
			case 1: platestr = "I";
				break;
			case 2: platestr = "II";
				break;
			case 3: platestr = "III";
		}
		return String.format("%3s %c %2d", platestr, row, column);
	}

	@Override
	public void clear() {
		
	}
}