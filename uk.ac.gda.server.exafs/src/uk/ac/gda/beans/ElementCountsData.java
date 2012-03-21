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

package uk.ac.gda.beans;

import java.io.Serializable;
import java.util.Arrays;

import uk.ac.gda.util.list.PrimitiveArrayEncoder;

/**
 * Element counts. Holds a two dimensional array [grade][mca] Often there will only be one grade.
 */
public class ElementCountsData implements Serializable {

	public static ElementCountsData[] getDataFor(final int[][][] data) {
		final ElementCountsData[] e = new ElementCountsData[data.length];
		for (int i = 0; i < data.length; i++) {
			e[i] = new ElementCountsData(data[i]);
		}
		return e;
	}

	public static int[][][] getDataFrom(Object value) {

		if (!(value instanceof ElementCountsData[])) {
			throw new RuntimeException("value must be ElementCountsData[]");
		}

		final ElementCountsData[] e = (ElementCountsData[]) value;
		final int[][][] ret = new int[e.length][][];
		for (int i = 0; i < e.length; i++) {
			ret[i] = e[i].getData();
		}
		return ret;
	}

	private int[][] theData;

	public ElementCountsData() {
	}

	private ElementCountsData(final int[][] data) {
		setData(data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ElementCountsData other = (ElementCountsData) obj;
		if (!Arrays.equals(theData, other.theData)) {
			return false;
		}
		return true;
	}

	/**
	 * Convenience method
	 * 
	 * @return i
	 */
	public int[][] getData() {
		return theData;
	}

	/**
	 * @return Returns the compressedData.
	 */
	public String getDataString() {
		if (theData == null) {
			return null;
		}

		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < theData.length; i++) {
			buf.append(PrimitiveArrayEncoder.getString(theData[i]));
			if (i != theData.length - 1) {
				buf.append(";");
			}
		}
		return buf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(theData);
		return result;
	}

	/**
	 * Convenience method
	 * 
	 * @param data
	 */
	public void setData(final int[][] data) {
		theData = data;
	}

	/**
	 * @param compressedData
	 *            The compressedData to set.
	 */
	public void setDataString(String compressedData) {
		// this.dataString = compressedData;
		// if (compressedData==null) return null;
		final String[] split = compressedData.split(";");
		final int[][] ret = new int[split.length][];
		for (int i = 0; i < split.length; i++) {
			ret[i] = PrimitiveArrayEncoder.getIntArray(split[i]);
		}
		theData = ret;
	}

}
