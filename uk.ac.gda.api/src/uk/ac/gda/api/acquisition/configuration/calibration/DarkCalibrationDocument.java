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

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * Describes a dark calibration acquisition.
 * If {@link #getDetectorDocument()} returns {@code null} should be assumed that the acquisition is done with an already
 * defined detector
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = DarkCalibrationDocument.Builder.class)
public class DarkCalibrationDocument {

	private final Set<DevicePositionDocument> position;

	private final DetectorDocument detectorDocument;

	/**
	 * The number of dark exposures to take
	 */
	private int numberExposures;
	/**
	 * Defines if the dark exposure(s) are made before the acquisition
	 */
	private boolean beforeAcquisition;
	/**
	 * Defines if the dark exposure(s) are made after the acquisition
	 */
	private boolean afterAcquisition;

	private DarkCalibrationDocument(DetectorDocument detectorDocument, int numberExposures, boolean beforeAcquisition,
			boolean afterAcquisition, Set<DevicePositionDocument> position) {
		this.detectorDocument = detectorDocument;
		this.numberExposures = numberExposures;
		this.beforeAcquisition = beforeAcquisition;
		this.afterAcquisition = afterAcquisition;
		this.position = position;
	}

	public DetectorDocument getDetectorDocument() {
		return detectorDocument;
	}

	public int getNumberExposures() {
		return numberExposures;
	}

	public void setNumberExposures(int numberExposures) {
		this.numberExposures = numberExposures;
	}

	public boolean isBeforeAcquisition() {
		return beforeAcquisition;
	}

	public void setBeforeAcquisition(boolean beforeAcquisition) {
		this.beforeAcquisition = beforeAcquisition;
	}

	public boolean isAfterAcquisition() {
		return afterAcquisition;
	}

	public void setAfterAcquisition(boolean afterAcquisition) {
		this.afterAcquisition = afterAcquisition;
	}

	public Set<DevicePositionDocument> getPosition() {
		return position;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private Set<DevicePositionDocument> position;
		private DetectorDocument detectorDocument;

		private int numberExposures;
		private boolean beforeAcquisition;
		private boolean afterAcquisition;

		public Builder() {
		}

		public Builder(DarkCalibrationDocument document) {
			this.detectorDocument = document.getDetectorDocument();
			this.numberExposures = document.getNumberExposures();
			this.beforeAcquisition = document.isBeforeAcquisition();
			this.afterAcquisition = document.isAfterAcquisition();
			this.position = document.getPosition();
		}

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

	    public Builder withPosition(Set<DevicePositionDocument> position) {
	        this.position = position;
	        return this;
	    }

	    public DarkCalibrationDocument build() {
	        return new DarkCalibrationDocument(detectorDocument, numberExposures, beforeAcquisition,
	        		afterAcquisition, position);
	    }
	}

	@Override
	public int hashCode() {
		return Objects.hash(afterAcquisition, beforeAcquisition, detectorDocument, numberExposures, position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DarkCalibrationDocument other = (DarkCalibrationDocument) obj;
		return afterAcquisition == other.afterAcquisition && beforeAcquisition == other.beforeAcquisition
				&& Objects.equals(detectorDocument, other.detectorDocument) && numberExposures == other.numberExposures
				&& Objects.equals(position, other.position);
	}

}
