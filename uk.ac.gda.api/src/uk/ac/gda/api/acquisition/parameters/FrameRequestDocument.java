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

package uk.ac.gda.api.acquisition.parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = FrameRequestDocument.Builder.class)
public class FrameRequestDocument {

	/**
	 * The detector identifier. The value may be:
	 * <ul>
	 * <li>a detector bean ID</li>
	 * <li>Malcolm device id</li>
	 * </ul>
	 */
	private final String name;

	/**
	 * The detector exposure time.
	 */
	private final double exposure;

	/**
	 * The detector name, from the Malcolm DetectorTable, which accepts <i>exposure</i> property.
	 * (from the web gui, http://beamline-control:8008/gui/MALCOLM_ID --> Malcolm --> Detectors --> Edit)
	 * <p>
	 * <b>NOTE</b> this is a temporary solution to mitigate the case where any element in the Malcolm DetectorTable
	 * is parsed as IMalcolmDetectorModel however not all the element in the MalcolmDetector table contains the properties
	 * defined by the IMalcolmDetectorModel.
	 * One consequence of this is that using IMalcolmDetectorModel.setExposureTime (on the GDA side)
	 * on an element which does not expect that value (on the Malcolm side), makes GDA generate a message that Malcolm will consider invalid.
	 * (caused by bug K11-1228)
	 * </p>
	 */
	private final String malcolmDetectorName;

	private final String detectorController;

	private FrameRequestDocument(String name, double exposure, String malcolmDetectorName, String detectorController) {
		this.name = name;
		this.exposure = exposure;
		this.malcolmDetectorName = malcolmDetectorName;
		this.detectorController = detectorController;
	}

	public String getName() {
		return name;
	}

	public double getExposure() {
		return exposure;
	}

	public String getMalcolmDetectorName() {
		return malcolmDetectorName;
	}

	public String getDetectorController() {
		return detectorController;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String name;
		private double exposure;
		private String malcolmDetectorName;
		private String detectorController;

	    public Builder withName(String name) {
	        this.name = name;
	        return this;
	    }

	    public Builder withExposure(double exposure) {
	        this.exposure = exposure;
	        return this;
	    }

	    public Builder withMalcolmDetectorName(String malcolmDetectorName) {
	        this.malcolmDetectorName = malcolmDetectorName;
	        return this;
	    }

	    public Builder withDetectorController(String detectorController) {
	        this.detectorController = detectorController;
	        return this;
	    }

	    public FrameRequestDocument build() {
	        return new FrameRequestDocument(name, exposure, malcolmDetectorName, detectorController);
	    }
	}
}
