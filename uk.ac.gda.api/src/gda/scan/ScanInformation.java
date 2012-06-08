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

package gda.scan;

import java.util.List;

/**
 * Object that provides information on a scan that scannables or detectors might want to know.
 * 
 * To be extended. Scan number would be good...
 */
public class ScanInformation {

	private int[] dimensions;
	private final Long scanNumber;
	
	
	public ScanInformation(int[] dimensions, Long scanNumber) {
		this.dimensions = dimensions;
		this.scanNumber = scanNumber;
	}
	
	public ScanInformation(List<Integer> dimensions, Long scanNumber) {
		this.scanNumber = scanNumber;
		int len = dimensions.size();
		this.dimensions = new int[len];
		for (int i = 0; i < this.dimensions.length; i++) {
			this.dimensions[i] = dimensions.get(i);
		}
	}

	public int[] getDimensions() {
		return dimensions;
	}
	
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format("Scan %d : A Scan of rank %d with the dimensions: ", scanNumber, dimensions.length));
		for (int i = 0; i < dimensions.length; i++) {
			sb.append(dimensions[i]);
			if ((i + 1) < dimensions.length) sb.append(" x ");
		}
		return sb.toString();
	}

	public Long getScanNumber() {
		return scanNumber;
	}
}
