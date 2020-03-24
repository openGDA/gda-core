/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document;

import java.net.URL;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;

/**
 * A document describing an acquisition performed by one or more {@code detectors} on a defined {@code scanpath}.
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScanRequestDocument.Builder.class)
public class ScanRequestDocument {

	private final URL filePath;
	private final DetectorDocument[] detectors;
	private final ScanpathDocument scanpath;

	/**
	 * @param filePath the path where the acquisition will be written
	 * @param detectors the elements which perform the acquisition
	 * @param scanpath the acquisition trajectory
	 */
	public ScanRequestDocument(URL filePath, DetectorDocument[] detectors, ScanpathDocument scanpath) {
		super();
		this.filePath = filePath;
		this.detectors = detectors;
		this.scanpath = scanpath;
	}

	public final URL getFilePath() {
		return filePath;
	}

	public DetectorDocument[] getDetectors() {
		return detectors;
	}

	public ScanpathDocument getScanpath() {
		return scanpath;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private URL filePath;
		private DetectorDocument[] detectors;
		private ScanpathDocument scanpath;

	    Builder withFilePath(URL filePath) {
	        this.filePath = filePath;
	        return this;
	    }

	    Builder withDetectors(DetectorDocument[] detectors) {
	        this.detectors = detectors;
	        return this;
	    }

	    Builder withScanpath(ScanpathDocument scanpath) {
	        this.scanpath = scanpath;
	        return this;
	    }

	    public ScanRequestDocument build() {
	        return new ScanRequestDocument(filePath, detectors, scanpath);
	    }
	}
}
