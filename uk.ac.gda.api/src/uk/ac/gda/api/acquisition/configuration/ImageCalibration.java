package uk.ac.gda.api.acquisition.configuration;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;

/**
 * Defines how many and how the calibration images will be acquired
 */
@JsonDeserialize(builder = ImageCalibration.Builder.class)
public class ImageCalibration {

	private final FlatCalibrationDocument flatCalibration;
	private final DarkCalibrationDocument darkCalibration;

	private ImageCalibration(FlatCalibrationDocument flatCalibration, DarkCalibrationDocument darkCalibration) {
		this.flatCalibration = flatCalibration;
		this.darkCalibration = darkCalibration;
	}

	public FlatCalibrationDocument getFlatCalibration() {
		return flatCalibration;
	}

	public DarkCalibrationDocument getDarkCalibration() {
		return darkCalibration;
	}

	@JsonPOJOBuilder
	public static class Builder {

		private FlatCalibrationDocument flatCalibration;
		private DarkCalibrationDocument darkCalibration;

		public Builder() {
		}

		public Builder(ImageCalibration document) {
			this.flatCalibration = document.getFlatCalibration();
			this.darkCalibration = document.getDarkCalibration();
		}

	    public Builder withFlatCalibration(FlatCalibrationDocument flatCalibration) {
	        this.flatCalibration = flatCalibration;
	        return this;
	    }

	    public Builder withDarkCalibration(DarkCalibrationDocument darkCalibration) {
	        this.darkCalibration = darkCalibration;
	        return this;
	    }

	    public ImageCalibration build() {
	        return new ImageCalibration(flatCalibration, darkCalibration);
	    }
	}

	@Override
	public int hashCode() {
		return Objects.hash(darkCalibration, flatCalibration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageCalibration other = (ImageCalibration) obj;
		return Objects.equals(darkCalibration, other.darkCalibration)
				&& Objects.equals(flatCalibration, other.flatCalibration);
	}

}
