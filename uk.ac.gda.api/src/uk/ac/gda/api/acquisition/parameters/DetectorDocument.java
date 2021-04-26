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

	/**
	 * The detector readout time.
	 * <p>
	 * <b>NOTE</b> this is a temporary solution to mitigate the case where the acquisition engine acquisition time
	 * accounts not only for the detector exposure but also for the readout time. This property should be removed as soon
	 * an improved Malcolm version will be deployed (BC-1349)
	 * </p>
	 */
	@Deprecated
	private final double readout;

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



	private DetectorDocument(String name, double exposure, double readout, String malcolmDetectorName) {
		this.name = name;
		this.exposure = exposure;
		this.readout = readout;
		this.malcolmDetectorName= malcolmDetectorName;
	}

	public DetectorDocument(DetectorDocument detectorDocument) {
		super();
		this.name = detectorDocument.getName();
		this.exposure = detectorDocument.getExposure();
		this.readout = detectorDocument.getReadout();
		this.malcolmDetectorName = detectorDocument.getMalcolmDetectorName();
	}

	public String getName() {
		return name;
	}

	public double getExposure() {
		return exposure;
	}

	/**
	 * @return the readout for this detector
	 * @deprecated this property is used to compensate the exposure in malcolm. However anew development in Malcolm made this unnecessary
	 */
	@Deprecated
	public double getReadout() {
		return readout;
	}

	public String getMalcolmDetectorName() {
		return malcolmDetectorName;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String name;
		private double exposure;
		private double readout;
		private String malcolmDetectorName;

	    public Builder withName(String name) {
	        this.name = name;
	        return this;
	    }

	    public Builder withExposure(double exposure) {
	        this.exposure = exposure;
	        return this;
	    }

	    /**
	     * @param readout
	     * @return the detector readout
	     * @deprecated to be removed
	     */
		@Deprecated
	    public Builder withReadout(double readout) {
	        this.readout = readout;
	        return this;
	    }

	    public Builder withMalcolmDetectorName(String malcolmDetectorName) {
	        this.malcolmDetectorName = malcolmDetectorName;
	        return this;
	    }

	    public DetectorDocument build() {
	        return new DetectorDocument(name, exposure, readout, malcolmDetectorName);
	    }
	}
}
