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


	public NXDetectorDataFileLinkAppender(String filename) {
		this.filename = filename;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) {

		if (!StringUtils.hasLength(filename)) {
			throw new IllegalArgumentException("filename is null or zero length");
		}

		data.addScanFileLink(detectorName, "nxfile://" + filename + "#entry/instrument/detector/data");

	}

}
