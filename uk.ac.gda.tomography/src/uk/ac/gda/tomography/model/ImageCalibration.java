package uk.ac.gda.tomography.model;

/**
 * Defines how many and how the calibration images will be acquired
 */
public class ImageCalibration {

	private int numberDark;
	private double darkExposure;
	private int numberFlat;
	private double flatExposure;

	private boolean beforeAcquisition;
	private boolean afterAcquisition;

	public ImageCalibration() {
	}

	public ImageCalibration(ImageCalibration imageCalibration) {
		this.numberDark = imageCalibration.getNumberDark();
		this.afterAcquisition = imageCalibration.isAfterAcquisition();
		this.numberFlat = imageCalibration.getNumberFlat();
		this.beforeAcquisition = imageCalibration.isBeforeAcquisition();
		this.afterAcquisition = imageCalibration.isAfterAcquisition();
		this.darkExposure = imageCalibration.getDarkExposure();
		this.flatExposure = imageCalibration.getFlatExposure();
	}

	public int getNumberDark() {
		return numberDark;
	}

	public void setNumberDark(int numberDark) {
		this.numberDark = numberDark;
	}

	public int getNumberFlat() {
		return numberFlat;
	}

	public void setNumberFlat(int numberFlat) {
		this.numberFlat = numberFlat;
	}

	public double getDarkExposure() {
		return darkExposure;
	}

	public void setDarkExposure(double darkExposure) {
		this.darkExposure = darkExposure;
	}

	public double getFlatExposure() {
		return flatExposure;
	}

	public void setFlatExposure(double flatExposure) {
		this.flatExposure = flatExposure;
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
}
