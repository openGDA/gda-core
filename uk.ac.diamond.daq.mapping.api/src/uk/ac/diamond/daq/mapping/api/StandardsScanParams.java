/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

/**
 * Class to hold the data to be passed to a scripted standards scan
 */
public class StandardsScanParams {

	private String scanPath;
	private double exposureTime;
	private boolean reverseScan;

	public String getScanPath() {
		return scanPath;
	}

	public void setScanPath(String scanPath) {
		this.scanPath = scanPath;
	}

	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	public boolean isReverseScan() {
		return reverseScan;
	}

	public void setReverseScan(boolean reverseScan) {
		this.reverseScan = reverseScan;
	}

	@Override
	public String toString() {
		return "StandardsScanParams [scanPath=" + scanPath + ", exposureTime=" + exposureTime + ", reverseScan="
				+ reverseScan + "]";
	}
}
