/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdata;

import gda.device.detector.NXDetectorData;

import org.springframework.util.StringUtils;

/**
 * Adds a link to an external hdfFile. Use only at start of scan.
 */
public class NXDetectorDataFileLinkAppender implements NXDetectorDataAppender {

	private final String filename;
	private final Double xPixelSize;
	private final Double yPixelSize;
	private final String xPixelSizeUnit;
	private final String yPixelSizeUnit;

	public NXDetectorDataFileLinkAppender(String filename) {
		this.filename = filename;
		this.xPixelSize = null;
		this.yPixelSize = null;
		this.xPixelSizeUnit = null;
		this.yPixelSizeUnit = null;
	}

	public NXDetectorDataFileLinkAppender(String expectedFullFileName, Double xPixelSize, Double yPixelSize, String xPixelSizeUnit, String yPixelSizeUnit) {
		this.filename = expectedFullFileName;
		this.xPixelSize = xPixelSize;
		this.yPixelSize = yPixelSize;
		this.xPixelSizeUnit = xPixelSizeUnit;
		this.yPixelSizeUnit = yPixelSizeUnit;		
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) {

		if (!StringUtils.hasLength(filename)) {
			throw new IllegalArgumentException("filename is null or zero length");
		}

		data.addScanFileLink(detectorName, "nxfile://" + filename + "#entry/instrument/detector/data");
		if (xPixelSize!=null) {
			data.addData(detectorName, "x_pixel_size", xPixelSize, xPixelSizeUnit);
		}
		if (yPixelSize!=null) {
			data.addData(detectorName, "y_pixel_size", yPixelSize, yPixelSizeUnit);
		}

	}

}
