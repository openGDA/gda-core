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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Describes a detector essential information to start an acquisition.
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = DetectorDocument.Builder.class)
public class DetectorDocument {
	/**
	 * The detector identifier. The value may be:
	 * <ul>
	 * <li><code>a detector bean ID</code>, see , i.e. elements 1 an 2 in the tree</li>
	 * <li><code>malcom device EPICS id</code>, i.e. element 0 in the tree</li>
	 * </ul>
	 */
	private final String name;

	/**
	 * The detector exposure time.
	 */
	private final double exposure;

	public DetectorDocument(String name, double exposure) {
		super();
		this.name = name;
		this.exposure = exposure;
	}

	public DetectorDocument(DetectorDocument detectorDocument) {
		super();
		this.name = detectorDocument.getName();
		this.exposure = detectorDocument.getExposure();
	}

	public String getName() {
		return name;
	}

	public double getExposure() {
		return exposure;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String name;
		private double exposure;

	    Builder withName(String name) {
	        this.name = name;
	        return this;
	    }

	    Builder withExposure(double exposure) {
	        this.exposure = exposure;
	        return this;
	    }

	    public DetectorDocument build() {
	        return new DetectorDocument(name, exposure);
	    }
	}
}
