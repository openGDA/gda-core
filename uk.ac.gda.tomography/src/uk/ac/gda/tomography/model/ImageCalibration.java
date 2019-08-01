package uk.ac.gda.tomography.model;

/**
 * Defines how many and how the calibration images will be acquired
 */
public class ImageCalibration {

	private int numberDark;
	private int numberFlat;

	private boolean beforeAcquisition;
	private boolean afterAcquisition;

	public ImageCalibration() {
	}

	public ImageCalibration(ImageCalibration imageCalibration) {
		this.numberDark = imageCalibration.getNumberDark();
		this.numberFlat = imageCalibration.getNumberFlat();
		this.beforeAcquisition = imageCalibration.isBeforeAcquisition();
		this.afterAcquisition = imageCalibration.isAfterAcquisition();
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
