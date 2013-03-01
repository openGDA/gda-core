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
	private String[] scannableNames;
	private String[] detectorNames;
	
	
	public ScanInformation(int[] dimensions, Long scanNumber, String[] ScannableNames, String[] DetectorNames) {
		this.dimensions = dimensions;
		this.scanNumber = scanNumber;
		scannableNames = ScannableNames;
		detectorNames = DetectorNames;
	}
	
	public ScanInformation(List<Integer> dimensions, Long scanNumber, String[] ScannableNames, String[] DetectorNames) {
		this.scanNumber = scanNumber;
		int len = dimensions.size();
		this.dimensions = new int[len];
		for (int i = 0; i < this.dimensions.length; i++) {
			this.dimensions[i] = dimensions.get(i);
		}
		scannableNames = ScannableNames;
		detectorNames = DetectorNames;
	}

	public int[] getDimensions() {
		return dimensions;
	}
	
	public Long getScanNumber() {
		return scanNumber;
	}
	
	public String[] getScannableNames() {
		return scannableNames;
	}

	public String[] getDetectorNames() {
		return detectorNames;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format("Scan %d : A Scan of rank %d with the dimensions: ", scanNumber, dimensions.length));
		for (int i = 0; i < dimensions.length; i++) {
			sb.append(dimensions[i]);
			if ((i + 1) < dimensions.length) sb.append(" x ");
		}
		sb.append(" over scannables: ");
		for (int i = 0; i < scannableNames.length; i++) {
			sb.append(scannableNames[i]);
			if ((i + 1) < scannableNames.length) sb.append(", ");
		}
		sb.append(" using detectors: ");
		for (int i = 0; i < detectorNames.length; i++) {
			sb.append(detectorNames[i]);
			if ((i + 1) < detectorNames.length) sb.append(", ");
		}
		return sb.toString();
	}

}
