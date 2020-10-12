package uk.ac.gda.api.acquisition.configuration;

import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;

/**
 * Defines how many and how the calibration images will be acquired
 */
public class ImageCalibration {

	private FlatCalibrationDocument flatCalibration;
	private DarkCalibrationDocument darkCalibration;

	public ImageCalibration() {
	}

	public ImageCalibration(ImageCalibration imageCalibration) {
		this.flatCalibration = imageCalibration.getFlatCalibration();
		this.darkCalibration = imageCalibration.getDarkCalibration();
	}

	public FlatCalibrationDocument getFlatCalibration() {
		return flatCalibration;
	}

	public void setFlatCalibration(FlatCalibrationDocument flatCalibration) {
		this.flatCalibration = flatCalibration;
	}

	public DarkCalibrationDocument getDarkCalibration() {
		return darkCalibration;
	}

	public void setDarkCalibration(DarkCalibrationDocument darkCalibration) {
		this.darkCalibration = darkCalibration;
	}
}
