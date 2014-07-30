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

import java.io.Serializable;
import java.util.List;

/**
 * Object that provides information about a scan, but not its data.
 */
public class ScanInformation implements Serializable{

	private int[] dimensions = new int[]{};
	private int scanNumber = -1;
	private String[] scannableNames = new String[]{};
	private String[] detectorNames = new String[]{};
	private String filename = "";
	private String instrument = "";
	private int numberOfPoints = -1;
	
	
	public ScanInformation(){
	}
	
	public ScanInformation(int[] dimensions, int scanNumber, String[] ScannableNames, String[] DetectorNames, String filename, String instrument, int NumberOfPoints) {
		this.dimensions = dimensions;
		this.scanNumber = scanNumber;
		scannableNames = ScannableNames;
		detectorNames = DetectorNames;
		this.filename = filename;
		this.instrument = instrument;
		numberOfPoints = NumberOfPoints;
	}
	
	public ScanInformation(List<Integer> dimensions, int scanNumber, String[] ScannableNames, String[] DetectorNames, String filename, String instrument, int NumberOfPoints) {
		this.scanNumber = scanNumber;
		int len = dimensions.size();
		this.dimensions = new int[len];
		for (int i = 0; i < this.dimensions.length; i++) {
			this.dimensions[i] = dimensions.get(i);
		}
		scannableNames = ScannableNames;
		detectorNames = DetectorNames;
		this.filename = filename;
		this.instrument = instrument;
		numberOfPoints = NumberOfPoints;
	}

	public int[] getDimensions() {
		return dimensions;
	}

	public void setDimensions(int[] dimensions) {
		this.dimensions = dimensions;
	}

	public String[] getScannableNames() {
		return scannableNames;
	}

	public void setScannableNames(String[] scannableNames) {
		this.scannableNames = scannableNames;
	}

	public String[] getDetectorNames() {
		return detectorNames;
	}

	public void setDetectorNames(String[] detectorNames) {
		this.detectorNames = detectorNames;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public void setNumberOfPoints(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
	}

	public int getScanNumber() {
		return scanNumber;
	}

	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
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
