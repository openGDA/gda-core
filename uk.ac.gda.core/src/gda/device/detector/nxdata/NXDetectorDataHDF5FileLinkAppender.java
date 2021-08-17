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

import org.eclipse.dawnsci.nexus.NXdetector;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.NXDetectorData;

/**
 * Adds a link to an external HDF5 Files created by multiple File Writers. Use only at start of scan.
 */
public class NXDetectorDataHDF5FileLinkAppender implements NXDetectorDataAppender {

	private static final String ENTRY_INSTRUMENT_DETECTOR_DATA = "#entry/instrument/detector/data";
	private static final String NXFILE_SCHEME = "nxfile://";
	private final String writerName;
	private final String filename;
	private final Double xPixelSize;
	private final Double yPixelSize;
	private final String xPixelSizeUnit;
	private final String yPixelSizeUnit;
	private final int dataRank;

	public NXDetectorDataHDF5FileLinkAppender(String filename, String writerName) {
		this(filename,null,null,null,null,writerName,-1);
	}

	public NXDetectorDataHDF5FileLinkAppender(String expectedFullFileName, Double xPixelSize, Double yPixelSize, String writerName) {
		this(expectedFullFileName,xPixelSize,yPixelSize,null,null,writerName,-1);
	}

	public NXDetectorDataHDF5FileLinkAppender(String expectedFullFileName, Double xPixelSize, Double yPixelSize,
			String xPixelSizeUnit, String yPixelSizeUnit, String writerName) {
		this(expectedFullFileName,xPixelSize,yPixelSize,xPixelSizeUnit,yPixelSizeUnit,writerName,-1);
	}

	public NXDetectorDataHDF5FileLinkAppender(String expectedFullFileName, Double xPixelSize, Double yPixelSize,
			String xPixelSizeUnit, String yPixelSizeUnit, String writerName, int dataRank) {
		this.writerName=writerName;
		this.filename = expectedFullFileName;
		this.xPixelSize = xPixelSize;
		this.yPixelSize = yPixelSize;
		this.xPixelSizeUnit = xPixelSizeUnit;
		this.yPixelSizeUnit = yPixelSizeUnit;
		this.dataRank = dataRank;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) {

		if (!StringUtils.hasLength(filename)) {
			throw new IllegalArgumentException("filename is null or zero length");
		}

		if (writerName == null || writerName.isEmpty() || writerName.equals("hdfwriter")) {
			if (LocalProperties.get(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter").equals("NexusScanDataWriter")) {
				data.addExternalFileLink(detectorName, NXdetector.NX_DATA, NXFILE_SCHEME + filename + ENTRY_INSTRUMENT_DETECTOR_DATA, dataRank);
			} else {
				//keep the original way of handling filename for backward compatibility for now.
				data.addExternalFileLink(detectorName, NXFILE_SCHEME + filename + ENTRY_INSTRUMENT_DETECTOR_DATA, -1);
			}
		} else {
			data.addExternalFileLink(detectorName, writerName, NXFILE_SCHEME + filename + ENTRY_INSTRUMENT_DETECTOR_DATA, dataRank);
		}

		if (xPixelSize!=null) {
			data.addData(detectorName, "x_pixel_size", new NexusGroupData(xPixelSize), xPixelSizeUnit);
		}
		if (yPixelSize!=null) {
			data.addData(detectorName, "y_pixel_size", new NexusGroupData(yPixelSize), yPixelSizeUnit);
		}
	}
}
