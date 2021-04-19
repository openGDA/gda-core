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

import org.springframework.util.StringUtils;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.NXDetectorData;

/**
 * Adds a link to an external HDF5 File. Use only at start of scan.
 */
public class NXDetectorDataHDF5FileLinkAppender implements NXDetectorDataAppender {

	private final String writerName;
	private final String filename;
	private final Double xPixelSize;
	private final Double yPixelSize;
	private final String xPixelSizeUnit;
	private final String yPixelSizeUnit;

	public NXDetectorDataHDF5FileLinkAppender(String filename, String writerName) {
		this.writerName=writerName;
		this.filename = filename;
		this.xPixelSize = null;
		this.yPixelSize = null;
		this.xPixelSizeUnit = null;
		this.yPixelSizeUnit = null;
	}

	public NXDetectorDataHDF5FileLinkAppender(String expectedFullFileName, Double xPixelSize, Double yPixelSize, String writerName) {
		this.writerName=writerName;
		this.filename = expectedFullFileName;
		this.xPixelSize = xPixelSize;
		this.yPixelSize = yPixelSize;
		this.xPixelSizeUnit = null;
		this.yPixelSizeUnit = null;
	}

	public NXDetectorDataHDF5FileLinkAppender(String expectedFullFileName, Double xPixelSize, Double yPixelSize,
			String xPixelSizeUnit, String yPixelSizeUnit, String writerName) {
		this.writerName=writerName;
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

		if (writerName == null || writerName.isEmpty() || writerName.equals("hdfwriter")) { //the default name coded in the HDF5 file writer, using String instead CONSTANT string to avoid dependency on epics plugin
			data.addScanFileLink(detectorName, "nxfile://" + filename + "#entry/instrument/detector/data");
		} else {
			data.addExternalFileLink(detectorName, writerName, "nxfile://" + filename + "#entry/instrument/detector/data", false, false);
		}

		if (xPixelSize!=null) {
			data.addData(detectorName, "x_pixel_size", new NexusGroupData(xPixelSize), xPixelSizeUnit);
		}
		if (yPixelSize!=null) {
			data.addData(detectorName, "y_pixel_size", new NexusGroupData(yPixelSize), yPixelSizeUnit);
		}
	}
}
