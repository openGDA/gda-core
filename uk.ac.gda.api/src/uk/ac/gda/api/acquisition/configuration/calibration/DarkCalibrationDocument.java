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

package uk.ac.gda.api.acquisition.configuration.calibration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.api.acquisition.parameters.DetectorDocument;

/**
 * Describes a dark calibration acquisition.
 * If {@link #getDetectorDocument()} returns {@code null} should be assumed that the acquisition is done with an already
 * defined detector
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = DarkCalibrationDocument.Builder.class)
public class DarkCalibrationDocument {
	private final DetectorDocument detectorDocument;

	/**
	 * The number of dark exposures to take
	 */
	private final int numberExposures;
	/**
	 * Defines if the dark exposure(s) are made before the acquisition
	 */
	private final boolean beforeAcquisition;
	/**
	 * Defines if the dark exposure(s) are made after the acquisition
	 */
	private final boolean afterAcquisition;

	private DarkCalibrationDocument(DetectorDocument detectorDocument, int numberExposures, boolean beforeAcquisition,
			boolean afterAcquisition) {
		this.detectorDocument = detectorDocument;
		this.numberExposures = numberExposures;
		this.beforeAcquisition = beforeAcquisition;
		this.afterAcquisition = afterAcquisition;
	}

	public DarkCalibrationDocument(DarkCalibrationDocument darkCalibrationDocument) {
		this.detectorDocument = darkCalibrationDocument.getDetectorDocument();
		this.numberExposures = darkCalibrationDocument.getNumberExposures();
		this.beforeAcquisition = darkCalibrationDocument.isBeforeAcquisition();
		this.afterAcquisition = darkCalibrationDocument.isAfterAcquisition();
	}

	public DetectorDocument getDetectorDocument() {
		return detectorDocument;
	}

	public int getNumberExposures() {
		return numberExposures;
	}

	public boolean isBeforeAcquisition() {
		return beforeAcquisition;
	}

	public boolean isAfterAcquisition() {
		return afterAcquisition;
	}


	@JsonPOJOBuilder
	public static class Builder {
		private DetectorDocument detectorDocument;

		private int numberExposures;
		private boolean beforeAcquisition;
		private boolean afterAcquisition;

	    public Builder withDetectorDocument(DetectorDocument detectorDocument) {
	        this.detectorDocument = detectorDocument;
	        return this;
	    }

	    public Builder withNumberExposures(int numberExposures) {
	        this.numberExposures = numberExposures;
	        return this;
	    }

	    public Builder withBeforeAcquisition(boolean beforeAcquisition) {
	        this.beforeAcquisition = beforeAcquisition;
	        return this;
	    }

	    public Builder withAfterAcquisition(boolean afterAcquisition) {
	        this.afterAcquisition = afterAcquisition;
	        return this;
	    }

	    public DarkCalibrationDocument build() {
	        return new DarkCalibrationDocument(detectorDocument, numberExposures, beforeAcquisition, afterAcquisition);
	    }
	}
}